package vn.system.app.modules.salarygrade.service;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.orgjobtitle.domain.OrgJobTitle;
import vn.system.app.modules.orgjobtitle.service.OrgJobTitleService;
import vn.system.app.modules.salarygrade.domain.SalaryGrade;
import vn.system.app.modules.salarygrade.domain.request.ReqCreateSalaryGradeDTO;
import vn.system.app.modules.salarygrade.domain.response.ResSalaryGradeDTO;
import vn.system.app.modules.salarygrade.repository.SalaryGradeRepository;

@Service
public class SalaryGradeService {

    private final SalaryGradeRepository salaryGradeRepo;
    private final OrgJobTitleService orgJobTitleService;

    public SalaryGradeService(
            SalaryGradeRepository salaryGradeRepo,
            OrgJobTitleService orgJobTitleService) {

        this.salaryGradeRepo = salaryGradeRepo;
        this.orgJobTitleService = orgJobTitleService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public ResSalaryGradeDTO handleCreate(ReqCreateSalaryGradeDTO req) {

        if (req.getOrgJobTitleId() == null) {
            throw new IdInvalidException("OrgJobTitleId không được để trống");
        }

        if (req.getGradeLevel() == null) {
            throw new IdInvalidException("GradeLevel không được để trống");
        }

        OrgJobTitle ojt = orgJobTitleService.fetchEntityById(req.getOrgJobTitleId());

        boolean existed = salaryGradeRepo
                .existsByOrgJobTitle_IdAndGradeLevel(ojt.getId(), req.getGradeLevel());

        if (existed) {
            throw new IdInvalidException("Bậc lương đã tồn tại trong ngữ cảnh chức danh");
        }

        SalaryGrade sg = new SalaryGrade();
        sg.setOrgJobTitle(ojt);
        sg.setGradeLevel(req.getGradeLevel());

        sg = salaryGradeRepo.save(sg);
        return convertToResDTO(sg);
    }

    /* ================= DELETE (SOFT) ================= */

    @Transactional
    public void handleDelete(Long id) {
        SalaryGrade sg = fetchEntityById(id);
        sg.setStatus(0);
        salaryGradeRepo.save(sg);
    }

    /* ================= FETCH ENTITY ================= */

    public SalaryGrade fetchEntityById(Long id) {
        return salaryGradeRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bậc lương không tồn tại"));
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAll(
            Specification<SalaryGrade> spec,
            Pageable pageable) {

        Page<SalaryGrade> page = salaryGradeRepo.findAll(spec, pageable);

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
                        .map(this::convertToResDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    /* ================= CONVERT ================= */

    private ResSalaryGradeDTO convertToResDTO(SalaryGrade sg) {

        ResSalaryGradeDTO res = new ResSalaryGradeDTO();
        res.setId(sg.getId());
        res.setGradeLevel(sg.getGradeLevel());
        res.setStatus(sg.getStatus());
        res.setCreatedAt(sg.getCreatedAt());
        res.setUpdatedAt(sg.getUpdatedAt());
        res.setCreatedBy(sg.getCreatedBy());
        res.setUpdatedBy(sg.getUpdatedBy());

        // ===== OrgJobTitle =====
        ResSalaryGradeDTO.OrgJobTitleInfo ojt = new ResSalaryGradeDTO.OrgJobTitleInfo();
        ojt.setId(sg.getOrgJobTitle().getId());

        // ===== JobTitle =====
        ResSalaryGradeDTO.JobTitleInfo jt = new ResSalaryGradeDTO.JobTitleInfo();
        jt.setId(sg.getOrgJobTitle().getJobTitle().getId());
        jt.setNameVi(sg.getOrgJobTitle().getJobTitle().getNameVi());

        ojt.setJobTitle(jt);
        res.setOrgJobTitle(ojt);

        return res;
    }
}
