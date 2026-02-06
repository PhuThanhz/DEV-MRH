package vn.system.app.modules.jobtitleperformancecontent.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;

import vn.system.app.modules.companysalarygrade.repository.CompanySalaryGradeRepository;
import vn.system.app.modules.departmentsalarygrade.repository.DepartmentSalaryGradeRepository;
import vn.system.app.modules.sectionsalarygrade.repository.SectionSalaryGradeRepository;

import vn.system.app.modules.jobtitleperformancecontent.domain.JobTitlePerformanceContent;
import vn.system.app.modules.jobtitleperformancecontent.domain.OwnerLevel;
import vn.system.app.modules.jobtitleperformancecontent.domain.request.ReqCreateOrUpdateJobTitlePerformanceContentDTO;
import vn.system.app.modules.jobtitleperformancecontent.domain.response.ResJobTitlePerformanceContentDTO;
import vn.system.app.modules.jobtitleperformancecontent.repository.JobTitlePerformanceContentRepository;

@Service
public class JobTitlePerformanceContentService {

    private final JobTitlePerformanceContentRepository repository;

    private final CompanySalaryGradeRepository companySalaryGradeRepo;
    private final DepartmentSalaryGradeRepository departmentSalaryGradeRepo;
    private final SectionSalaryGradeRepository sectionSalaryGradeRepo;

    private final CompanyJobTitleRepository companyJobTitleRepo;
    private final DepartmentJobTitleRepository departmentJobTitleRepo;
    private final SectionJobTitleRepository sectionJobTitleRepo;

    public JobTitlePerformanceContentService(
            JobTitlePerformanceContentRepository repository,
            CompanySalaryGradeRepository companySalaryGradeRepo,
            DepartmentSalaryGradeRepository departmentSalaryGradeRepo,
            SectionSalaryGradeRepository sectionSalaryGradeRepo,
            CompanyJobTitleRepository companyJobTitleRepo,
            DepartmentJobTitleRepository departmentJobTitleRepo,
            SectionJobTitleRepository sectionJobTitleRepo) {

        this.repository = repository;
        this.companySalaryGradeRepo = companySalaryGradeRepo;
        this.departmentSalaryGradeRepo = departmentSalaryGradeRepo;
        this.sectionSalaryGradeRepo = sectionSalaryGradeRepo;

        this.companyJobTitleRepo = companyJobTitleRepo;
        this.departmentJobTitleRepo = departmentJobTitleRepo;
        this.sectionJobTitleRepo = sectionJobTitleRepo;
    }

    /*
     * =====================================================
     * VALIDATE REQUEST
     * =====================================================
     */
    private void validate(ReqCreateOrUpdateJobTitlePerformanceContentDTO req) {

        if (req.getOwnerJobTitleId() == null || req.getOwnerJobTitleId() <= 0)
            throw new IdInvalidException("ownerJobTitleId không hợp lệ");

        if (req.getSalaryGradeId() == null || req.getSalaryGradeId() <= 0)
            throw new IdInvalidException("salaryGradeId không hợp lệ");

        if ((isBlank(req.getContentA()) && isBlank(req.getContentB())
                && isBlank(req.getContentC()) && isBlank(req.getContentD())))
            throw new IdInvalidException("Phải nhập ít nhất 1 nội dung đánh giá");

        if (req.getOwnerLevel() == null)
            throw new IdInvalidException("ownerLevel không được để trống");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /*
     * =====================================================
     * CHECK ACTIVE BẬC LƯƠNG
     * =====================================================
     */
    private void validateSalaryGradeActive(OwnerLevel level, Long gradeId) {

        boolean active = switch (level) {
            case COMPANY -> companySalaryGradeRepo.findById(gradeId)
                    .orElseThrow(() -> new IdInvalidException("Bậc lương không tồn tại"))
                    .isActive();

            case DEPARTMENT -> departmentSalaryGradeRepo.findById(gradeId)
                    .orElseThrow(() -> new IdInvalidException("Bậc lương không tồn tại"))
                    .isActive();

            case SECTION -> sectionSalaryGradeRepo.findById(gradeId)
                    .orElseThrow(() -> new IdInvalidException("Bậc lương không tồn tại"))
                    .isActive();
        };

        if (!active)
            throw new IdInvalidException("Bậc lương đã bị vô hiệu, không thể thêm tiêu chí");
    }

    /*
     * =====================================================
     * GET GRADE NUMBER
     * =====================================================
     */
    private int getGradeNumber(OwnerLevel level, Long gradeId) {
        return switch (level) {
            case COMPANY -> companySalaryGradeRepo.findById(gradeId).orElseThrow().getGradeLevel();
            case DEPARTMENT -> departmentSalaryGradeRepo.findById(gradeId).orElseThrow().getGradeLevel();
            case SECTION -> sectionSalaryGradeRepo.findById(gradeId).orElseThrow().getGradeLevel();
        };
    }

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @Transactional
    public JobTitlePerformanceContent create(ReqCreateOrUpdateJobTitlePerformanceContentDTO req) {

        validate(req);
        validateSalaryGradeActive(req.getOwnerLevel(), req.getSalaryGradeId());

        int gradeNumber = getGradeNumber(req.getOwnerLevel(), req.getSalaryGradeId());

        boolean exists = repository
                .existsByOwnerJobTitleIdAndSalaryGradeNumberAndActiveTrue(
                        req.getOwnerJobTitleId(), gradeNumber);

        if (exists)
            throw new IdInvalidException("Bậc lương này đã có tiêu chí!");

        JobTitlePerformanceContent entity = new JobTitlePerformanceContent();
        apply(entity, req);
        entity.setSalaryGradeNumber(gradeNumber);

        return repository.save(entity);
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @Transactional
    public JobTitlePerformanceContent update(Long id, ReqCreateOrUpdateJobTitlePerformanceContentDTO req) {

        validate(req);
        validateSalaryGradeActive(req.getOwnerLevel(), req.getSalaryGradeId());

        JobTitlePerformanceContent entity = repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy nội dung"));

        if (!entity.getActive())
            throw new IdInvalidException("Nội dung đã vô hiệu, không thể sửa");

        int gradeNumber = getGradeNumber(req.getOwnerLevel(), req.getSalaryGradeId());

        boolean exists = repository
                .existsByOwnerJobTitleIdAndSalaryGradeNumberAndActiveTrue(
                        req.getOwnerJobTitleId(), gradeNumber);

        if (exists && gradeNumber != entity.getSalaryGradeNumber())
            throw new IdInvalidException("Bậc lương này đã có tiêu chí!");

        apply(entity, req);
        entity.setSalaryGradeNumber(gradeNumber);

        return repository.save(entity);
    }

    /*
     * =====================================================
     * DISABLE
     * =====================================================
     */
    @Transactional
    public void disable(Long id) {

        if (!repository.existsById(id))
            throw new IdInvalidException("Không tìm thấy ID");

        repository.updateActive(id, false);
    }

    /*
     * =====================================================
     * RESTORE
     * =====================================================
     */
    @Transactional
    public void restore(Long id) {

        if (!repository.existsById(id))
            throw new IdInvalidException("Không tìm thấy ID");

        repository.updateActive(id, true);
    }

    /*
     * =====================================================
     * PAGINATION
     * =====================================================
     */
    public ResultPaginationDTO fetchAll(Specification<JobTitlePerformanceContent> spec, Pageable pageable) {

        Page<JobTitlePerformanceContent> page = repository.findAll(spec, pageable);

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
                        .map(this::toDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * =====================================================
     * GET BY OWNER (ACTIVE ONLY)
     * =====================================================
     */
    public List<ResJobTitlePerformanceContentDTO> findByOwner(OwnerLevel level, Long jobTitleId) {

        return repository
                .findByOwnerLevelAndOwnerJobTitleIdAndActiveTrue(level, jobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * APPLY DTO → ENTITY
     * =====================================================
     */
    private void apply(JobTitlePerformanceContent e, ReqCreateOrUpdateJobTitlePerformanceContentDTO r) {
        e.setOwnerLevel(r.getOwnerLevel());
        e.setOwnerJobTitleId(r.getOwnerJobTitleId());
        e.setSalaryGradeId(r.getSalaryGradeId());
        e.setContentA(r.getContentA());
        e.setContentB(r.getContentB());
        e.setContentC(r.getContentC());
        e.setContentD(r.getContentD());
    }

    /*
     * =====================================================
     * ENTITY → DTO
     * =====================================================
     */
    private ResJobTitlePerformanceContentDTO toDTO(JobTitlePerformanceContent e) {

        ResJobTitlePerformanceContentDTO r = new ResJobTitlePerformanceContentDTO();

        r.setId(e.getId());
        r.setOwnerLevel(e.getOwnerLevel());
        r.setOwnerJobTitleId(e.getOwnerJobTitleId());
        r.setSalaryGradeId(e.getSalaryGradeId());
        r.setSalaryGradeNumber(e.getSalaryGradeNumber());
        r.setContentA(e.getContentA());
        r.setContentB(e.getContentB());
        r.setContentC(e.getContentC());
        r.setContentD(e.getContentD());
        r.setActive(e.getActive());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());

        return r;
    }
}
