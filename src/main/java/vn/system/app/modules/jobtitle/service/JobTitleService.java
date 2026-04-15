package vn.system.app.modules.jobtitle.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.domain.request.ReqCreateJobTitleDTO;
import vn.system.app.modules.jobtitle.domain.request.ReqUpdateJobTitleDTO;
import vn.system.app.modules.jobtitle.domain.response.ResJobTitleDTO;
import vn.system.app.modules.jobtitle.repository.JobTitleRepository;
import vn.system.app.modules.positionlevel.domain.PositionLevel;
import vn.system.app.modules.positionlevel.repository.PositionLevelRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class JobTitleService {

    private final JobTitleRepository jobTitleRepo;
    private final PositionLevelRepository positionLevelRepo;
    private final UserPositionRepository userPositionRepo;

    public JobTitleService(
            JobTitleRepository jobTitleRepo,
            PositionLevelRepository positionLevelRepo,
            UserPositionRepository userPositionRepo) {

        this.jobTitleRepo = jobTitleRepo;
        this.positionLevelRepo = positionLevelRepo;
        this.userPositionRepo = userPositionRepo;
    }

    /*
     * ==========================================
     * CREATE
     * ==========================================
     */
    @Transactional
    public ResJobTitleDTO handleCreate(ReqCreateJobTitleDTO req) {

        if (req.getNameVi() == null || req.getNameVi().trim().isEmpty()) {
            throw new IdInvalidException("Tên chức danh (tiếng Việt) không được để trống");
        }

        PositionLevel pl = positionLevelRepo.findById(req.getPositionLevelId())
                .orElseThrow(() -> new IdInvalidException("Bậc chức danh không tồn tại"));

        if (jobTitleRepo.existsByNameViAndPositionLevel_Id(
                req.getNameVi().trim(), req.getPositionLevelId())) {
            throw new IdInvalidException(
                    "Tên chức danh đã tồn tại trong bậc này của công ty");
        }

        JobTitle jt = new JobTitle();
        jt.setNameVi(req.getNameVi().trim());
        jt.setNameEn(req.getNameEn());
        jt.setPositionLevel(pl);
        jt.setActive(req.getActive() != null ? req.getActive() : true);

        jt = jobTitleRepo.save(jt);
        return convertToDTO(jt);
    }

    /*
     * ==========================================
     * UPDATE
     * ==========================================
     */
    @Transactional
    public ResJobTitleDTO handleUpdate(ReqUpdateJobTitleDTO req) {

        JobTitle jt = fetchEntityById(req.getId());

        Long targetPositionLevelId = req.getPositionLevelId() != null
                ? req.getPositionLevelId()
                : jt.getPositionLevel().getId();

        String targetNameVi = req.getNameVi() != null
                ? req.getNameVi().trim()
                : jt.getNameVi();

        if (jobTitleRepo.existsByNameViAndPositionLevel_IdAndIdNot(
                targetNameVi, targetPositionLevelId, req.getId())) {
            throw new IdInvalidException(
                    "Tên chức danh đã tồn tại trong bậc này của công ty");
        }

        if (req.getNameVi() != null) {
            jt.setNameVi(req.getNameVi().trim());
        }

        if (req.getNameEn() != null) {
            jt.setNameEn(req.getNameEn());
        }

        if (req.getActive() != null) {
            if (!req.getActive()) {
                if (userPositionRepo.existsByJobTitleIdAndActiveTrue(jt.getId())) {
                    throw new IdInvalidException(
                            "Chức danh đang có nhân viên sử dụng, không thể vô hiệu hóa");
                }
            }
            jt.setActive(req.getActive());
        }

        if (req.getPositionLevelId() != null &&
                !req.getPositionLevelId().equals(jt.getPositionLevel().getId())) {
            throw new IdInvalidException(
                    "Không được phép đổi bậc/chức danh. Hãy tạo chức danh mới.");
        }

        jt = jobTitleRepo.save(jt);
        return convertToDTO(jt);
    }

    /*
     * ==========================================
     * DELETE (SOFT DELETE)
     * ==========================================
     */
    @Transactional
    public void handleDelete(Long id) {
        JobTitle jt = fetchEntityById(id);

        if (userPositionRepo.existsByJobTitleIdAndActiveTrue(id)) {
            throw new IdInvalidException(
                    "Chức danh đang có nhân viên sử dụng, không thể vô hiệu hóa");
        }

        jt.setActive(false);
        jobTitleRepo.save(jt);
    }

    /*
     * ==========================================
     * GET ONE
     * ==========================================
     */
    public ResJobTitleDTO getJobTitle(Long id) {
        return convertToDTO(fetchEntityById(id));
    }

    /*
     * ==========================================
     * GET ENTITY
     * ==========================================
     */
    public JobTitle fetchEntityById(Long id) {
        return jobTitleRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chức danh"));
    }

    /*
     * ==========================================
     * GET ALL (PAGINATION)
     * ==========================================
     */
    public ResultPaginationDTO fetchAll(
            Specification<JobTitle> spec,
            Pageable pageable) {

        spec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("active"), true));

        Page<JobTitle> page = jobTitleRepo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        rs.setResult(
                page.getContent()
                        .stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * ==========================================
     * FIND ALL ACTIVE
     * ==========================================
     */
    @Transactional(readOnly = true)
    public List<JobTitle> findAllActive() {
        return jobTitleRepo.findByActiveTrue();
    }

    /*
     * ==========================================
     * FIND ALL ACTIVE BY COMPANY
     * ==========================================
     */
    @Transactional(readOnly = true)
    public List<JobTitle> findAllActiveByCompany(Long companyId) {
        return jobTitleRepo.findByPositionLevel_Company_IdAndActiveTrue(companyId);
    }

    /*
     * ==========================================
     * CONVERTER
     * ==========================================
     */
    private ResJobTitleDTO convertToDTO(JobTitle jt) {

        ResJobTitleDTO res = new ResJobTitleDTO();

        res.setId(jt.getId());
        res.setNameVi(jt.getNameVi());
        res.setNameEn(jt.getNameEn());
        res.setActive(jt.isActive());
        res.setCreatedAt(jt.getCreatedAt());
        res.setUpdatedAt(jt.getUpdatedAt());
        res.setCreatedBy(jt.getCreatedBy());
        res.setUpdatedBy(jt.getUpdatedBy());

        if (jt.getPositionLevel() != null) {

            ResJobTitleDTO.PositionLevelInfo pl = new ResJobTitleDTO.PositionLevelInfo();

            pl.setId(jt.getPositionLevel().getId());
            pl.setCode(jt.getPositionLevel().getCode());

            if (jt.getPositionLevel().getCompany() != null) {
                pl.setCompanyId(jt.getPositionLevel().getCompany().getId());
                pl.setCompanyName(jt.getPositionLevel().getCompany().getName());
            }

            res.setPositionLevel(pl);
        }

        return res;
    }
}