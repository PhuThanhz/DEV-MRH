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

@Service
public class JobTitleService {

    private final JobTitleRepository jobTitleRepo;
    private final PositionLevelRepository positionLevelRepo;

    public JobTitleService(
            JobTitleRepository jobTitleRepo,
            PositionLevelRepository positionLevelRepo) {
        this.jobTitleRepo = jobTitleRepo;
        this.positionLevelRepo = positionLevelRepo;
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public ResJobTitleDTO handleCreate(ReqCreateJobTitleDTO req) {

        if (req.getNameVi() == null || req.getNameVi().trim().isEmpty()) {
            throw new IdInvalidException("Tên chức danh (tiếng Việt) không được để trống");
        }

        if (jobTitleRepo.existsByNameVi(req.getNameVi())) {
            throw new IdInvalidException("Tên chức danh đã tồn tại");
        }

        PositionLevel pl = positionLevelRepo.findById(req.getPositionLevelId())
                .orElseThrow(() -> new IdInvalidException("Bậc chức danh không tồn tại"));

        JobTitle jt = new JobTitle();
        jt.setNameVi(req.getNameVi());
        jt.setNameEn(req.getNameEn());
        jt.setPositionLevel(pl);

        // ⭐ Đổi từ status → active
        jt.setActive(req.getActive() != null ? req.getActive() : true);

        jt = jobTitleRepo.save(jt);
        return convertToDTO(jt);
    }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    public ResJobTitleDTO handleUpdate(ReqUpdateJobTitleDTO req) {

        JobTitle jt = fetchEntityById(req.getId());

        if (req.getNameVi() != null) {
            jt.setNameVi(req.getNameVi());
        }

        if (req.getNameEn() != null) {
            jt.setNameEn(req.getNameEn());
        }

        // ⭐ status → active
        if (req.getActive() != null) {
            jt.setActive(req.getActive());
        }

        if (req.getPositionLevelId() != null) {
            PositionLevel pl = positionLevelRepo.findById(req.getPositionLevelId())
                    .orElseThrow(() -> new IdInvalidException("Bậc chức danh không tồn tại"));
            jt.setPositionLevel(pl);
        }

        jt = jobTitleRepo.save(jt);
        return convertToDTO(jt);
    }

    // =========================
    // DELETE (SOFT DELETE)
    // =========================
    @Transactional
    public void handleDelete(Long id) {
        JobTitle jt = fetchEntityById(id);

        // ⭐ soft delete dùng active = false
        jt.setActive(false);

        jobTitleRepo.save(jt);
    }

    // =========================
    // GET ONE
    // =========================
    public ResJobTitleDTO getJobTitle(Long id) {
        JobTitle jt = fetchEntityById(id);
        return convertToDTO(jt);
    }

    // =========================
    // GET ENTITY
    // =========================
    public JobTitle fetchEntityById(Long id) {
        return jobTitleRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chức danh"));
    }

    // =========================
    // GET ALL
    // =========================
    public ResultPaginationDTO fetchAll(
            Specification<JobTitle> spec,
            Pageable pageable) {

        Page<JobTitle> page = jobTitleRepo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        List<ResJobTitleDTO> list = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    // =========================
    // ★ THÊM MỚI: LẤY TẤT CẢ CHỨC DANH ĐANG ACTIVE (dùng cho
    // CompanyJobTitleService)
    // =========================
    @Transactional(readOnly = true)
    public List<JobTitle> findAllActive() {
        return jobTitleRepo.findByActiveTrue();
    }

    // =========================
    // CONVERTER
    // =========================
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
            res.setPositionLevel(pl);
        }

        return res;
    }
}