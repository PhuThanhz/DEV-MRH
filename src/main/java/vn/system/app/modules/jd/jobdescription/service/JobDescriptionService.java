package vn.system.app.modules.jd.jobdescription.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqCreateJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.response.ResJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.repository.JobDescriptionRepository;

import vn.system.app.modules.company.service.CompanyService;
import vn.system.app.modules.department.service.DepartmentService;

import vn.system.app.modules.companyjobtitle.service.CompanyJobTitleService;
import vn.system.app.modules.departmentjobtitle.service.DepartmentJobTitleService;
import vn.system.app.modules.sectionjobtitle.service.SectionJobTitleService;

import vn.system.app.modules.jd.jobdescriptionrequirement.service.JobDescriptionRequirementService;
import vn.system.app.modules.jd.jobdescriptiontask.service.JobDescriptionTaskService;
import vn.system.app.modules.jd.jobdescriptionposition.service.JobDescriptionPositionService;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository repository;

    private final CompanyService companyService;
    private final DepartmentService departmentService;

    private final CompanyJobTitleService companyJobTitleService;
    private final DepartmentJobTitleService departmentJobTitleService;
    private final SectionJobTitleService sectionJobTitleService;

    private final JobDescriptionRequirementService requirementService;
    private final JobDescriptionTaskService taskService;
    private final JobDescriptionPositionService positionService;

    /*
     * ==========================================
     * CREATE
     * ==========================================
     */
    @Transactional
    public JobDescription handleCreate(ReqCreateJobDescriptionDTO req) {

        JobDescription jd = new JobDescription();

        if (req.getCompanyId() != null) {
            jd.setCompany(companyService.fetchEntityById(req.getCompanyId()));
        }

        if (req.getDepartmentId() != null) {
            jd.setDepartment(departmentService.fetchEntityById(req.getDepartmentId()));
        }

        jd.setCode(req.getCode());
        jd.setReportTo(req.getReportTo());
        jd.setBelongsTo(req.getBelongsTo());
        jd.setCollaborateWith(req.getCollaborateWith());
        jd.setStatus("DRAFT");
        jd.setEffectiveDate(req.getEffectiveDate());

        if (req.getDepartmentJobTitleId() != null) {
            jd.setDepartmentJobTitle(
                    departmentJobTitleService.fetchEntityById(req.getDepartmentJobTitleId()));
        }

        if (req.getCompanyJobTitleId() != null) {
            jd.setCompanyJobTitle(
                    companyJobTitleService.fetchEntityById(req.getCompanyJobTitleId()));
        }

        if (req.getSectionJobTitleId() != null) {
            jd.setSectionJobTitle(
                    sectionJobTitleService.fetchEntityById(req.getSectionJobTitleId()));
        }

        validateScope(jd);

        jd = repository.save(jd);

        requirementService.createFromDTO(jd, req.getRequirements());
        taskService.createFromDTO(jd, req.getTasks());
        positionService.createFromDTO(jd, req.getPositions());

        return jd;
    }

    /*
     * ==========================================
     * UPDATE
     * ==========================================
     */
    @Transactional
    public JobDescription handleUpdate(Long id, ReqCreateJobDescriptionDTO req) {

        JobDescription current = fetchById(id);

        if ("PUBLISHED".equals(current.getStatus())) {
            throw new RuntimeException("JD đã ban hành, không thể chỉnh sửa");
        }

        if ("IN_REVIEW".equals(current.getStatus())) {
            throw new RuntimeException("JD đang duyệt, không thể chỉnh sửa");
        }

        current.setCode(req.getCode());
        current.setReportTo(req.getReportTo());
        current.setBelongsTo(req.getBelongsTo());
        current.setCollaborateWith(req.getCollaborateWith());
        current.setEffectiveDate(req.getEffectiveDate());

        return repository.save(current);
    }

    /*
     * ==========================================
     * DELETE
     * ==========================================
     */
    public void handleDelete(Long id) {

        JobDescription jd = fetchById(id);

        if ("PUBLISHED".equals(jd.getStatus())) {
            throw new RuntimeException("Không thể xóa JD đã ban hành");
        }

        if ("IN_REVIEW".equals(jd.getStatus())) {
            throw new RuntimeException("JD đang duyệt không thể xóa");
        }

        repository.delete(jd);
    }

    /*
     * ==========================================
     * FETCH BY ID
     * ==========================================
     */
    public JobDescription fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "JobDescription không tồn tại với id = " + id));
    }

    /*
     * ==========================================
     * FETCH ALL (có filter)
     * ==========================================
     */
    public ResultPaginationDTO fetchAll(
            Specification<JobDescription> spec,
            Pageable pageable) {
        return buildPaginationDTO(repository.findAll(spec, pageable));
    }

    /*
     * ==========================================
     * JD TÔI TẠO
     * ==========================================
     */
    public ResultPaginationDTO fetchMyJobDescriptions(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        return buildPaginationDTO(repository.findByCreatedBy(email, pageable));
    }

    /*
     * ==========================================
     * JD ĐÃ BAN HÀNH
     * ==========================================
     */
    public ResultPaginationDTO fetchPublished(Pageable pageable) {
        return buildPaginationDTO(repository.findByStatus("PUBLISHED", pageable));
    }

    /*
     * ==========================================
     * JD ĐÃ TỪ CHỐI
     * ==========================================
     */
    public ResultPaginationDTO fetchRejected(Pageable pageable) {
        return buildPaginationDTO(repository.findByStatus("REJECTED", pageable));
    }

    /*
     * ==========================================
     * TẤT CẢ JD (admin)
     * ==========================================
     */
    public ResultPaginationDTO fetchAllJd(Pageable pageable) {
        return buildPaginationDTO(repository.findAll(pageable));
    }

    /*
     * ==========================================
     * VALIDATE
     * ==========================================
     */
    private void validateScope(JobDescription jd) {
        if (jd.getCompanyJobTitle() == null
                && jd.getDepartmentJobTitle() == null
                && jd.getSectionJobTitle() == null) {
            throw new IdInvalidException(
                    "JobDescription phải gắn ít nhất 1 chức danh");
        }
    }

    /*
     * ==========================================
     * HELPER — build pagination DTO
     * ==========================================
     */
    private ResultPaginationDTO buildPaginationDTO(Page<JobDescription> page) {
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
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
     * DTO
     * ==========================================
     */
    public ResJobDescriptionDTO convertToDTO(JobDescription jd) {

        ResJobDescriptionDTO res = new ResJobDescriptionDTO();

        res.setId(jd.getId());
        res.setCode(jd.getCode());
        res.setReportTo(jd.getReportTo());
        res.setBelongsTo(jd.getBelongsTo());
        res.setCollaborateWith(jd.getCollaborateWith());
        res.setStatus(jd.getStatus());
        res.setVersion(jd.getVersion());
        res.setEffectiveDate(jd.getEffectiveDate());
        res.setCreatedAt(jd.getCreatedAt());
        res.setUpdatedAt(jd.getUpdatedAt());
        res.setCreatedBy(jd.getCreatedBy());
        res.setUpdatedBy(jd.getUpdatedBy());

        if (jd.getCompany() != null) {
            res.setCompanyId(jd.getCompany().getId());
            res.setCompanyName(jd.getCompany().getName());
        }

        if (jd.getDepartment() != null) {
            res.setDepartmentId(jd.getDepartment().getId());
            res.setDepartmentName(jd.getDepartment().getName());
        }

        if (jd.getCompanyJobTitle() != null) {
            res.setCompanyJobTitleId(jd.getCompanyJobTitle().getId());
        }

        if (jd.getDepartmentJobTitle() != null) {
            res.setDepartmentJobTitleId(jd.getDepartmentJobTitle().getId());
            if (jd.getDepartmentJobTitle().getJobTitle() != null) {
                res.setJobTitleName(
                        jd.getDepartmentJobTitle().getJobTitle().getNameVi());
            }
        }

        if (jd.getSectionJobTitle() != null) {
            res.setSectionJobTitleId(jd.getSectionJobTitle().getId());
        }

        res.setRequirements(requirementService.getByJobDescription(jd.getId()));
        res.setTasks(taskService.getByJobDescription(jd.getId()));
        res.setPositions(positionService.getByJobDescription(jd.getId()));

        return res;
    }
}