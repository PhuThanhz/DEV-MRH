package vn.system.app.modules.jd.jobdescription.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.duty.domain.JDDuty;
import vn.system.app.modules.jd.duty.repository.JDDutyRepository;
import vn.system.app.modules.jd.jobdescription.domain.JDStatus;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqCreateFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqUpdateFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.response.ResFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.response.ResJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.repository.JobDescriptionRepository;
import vn.system.app.modules.jd.positionchart.domain.JDPositionChart;
import vn.system.app.modules.jd.positionchart.repository.JDPositionChartRepository;
import vn.system.app.modules.jd.requirement.domain.JDRequirement;
import vn.system.app.modules.jd.requirement.repository.JDRequirementRepository;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository repository;
    private final JDDutyRepository dutyRepository;
    private final JDRequirementRepository requirementRepository;
    private final JDPositionChartRepository chartRepository;

    public JobDescriptionService(
            JobDescriptionRepository repository,
            JDDutyRepository dutyRepository,
            JDRequirementRepository requirementRepository,
            JDPositionChartRepository chartRepository) {

        this.repository = repository;
        this.dutyRepository = dutyRepository;
        this.requirementRepository = requirementRepository;
        this.chartRepository = chartRepository;
    }

    // =====================================================
    // CREATE FULL JD
    // =====================================================
    @Transactional
    public ResFullJobDescriptionDTO handleCreate(ReqCreateFullJobDescriptionDTO req) {

        JobDescription jd = new JobDescription();

        jd.setCompanyName(req.getCompanyName());
        jd.setIssueNumber(req.getIssueNumber());
        jd.setIssueDate(req.getIssueDate());
        jd.setPageTotal(req.getPageTotal());

        jd.setCode(req.getCode());
        jd.setRevision(req.getRevision());

        jd.setJobTitleName(req.getJobTitleName());
        jd.setDepartmentName(req.getDepartmentName());
        jd.setBelongsTo(req.getBelongsTo());
        jd.setDirectManager(req.getDirectManager());
        jd.setWorkWith(req.getWorkWith());
        jd.setAssignerTitle(req.getAssignerTitle());
        jd.setAssignerName(req.getAssignerName());

        jd.setStatus(JDStatus.DRAFT);

        jd = repository.save(jd);
        Long jdId = jd.getId();

        // ===== DUTY =====
        JDDuty duty = new JDDuty();
        duty.setJobDescriptionId(jdId);
        duty.setContent(req.getDutyContent());
        duty.setActive(true);
        dutyRepository.save(duty);

        // ===== REQUIREMENT =====
        JDRequirement requirement = new JDRequirement();
        requirement.setJobDescriptionId(jdId);
        requirement.setKnowledge(req.getKnowledge());
        requirement.setExperience(req.getExperience());
        requirement.setSkills(req.getSkills());
        requirement.setQualities(req.getQualities());
        requirement.setOtherRequirements(req.getOtherRequirements());
        requirement.setActive(true);
        requirementRepository.save(requirement);

        // ===== POSITION CHART =====
        if (req.getPositionNodes() != null) {
            req.getPositionNodes().forEach(node -> {
                JDPositionChart chart = new JDPositionChart();
                chart.setJobDescriptionId(jdId);
                chart.setNodeKey(node.getNodeKey());
                chart.setTitle(node.getTitle());
                chart.setParentId(node.getParentId());
                chart.setSortOrder(node.getSortOrder());
                chart.setActive(true);
                chartRepository.save(chart);
            });
        }

        return fetchFullById(jdId);
    }

    // =====================================================
    // UPDATE FULL JD
    // =====================================================
    @Transactional
    public ResFullJobDescriptionDTO handleUpdate(ReqUpdateFullJobDescriptionDTO req) {

        JobDescription jd = repository.findById(req.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy JD."));

        if (jd.getStatus() != JDStatus.DRAFT) {
            throw new IdInvalidException("Chỉ được sửa khi JD đang DRAFT.");
        }

        if (req.getCompanyName() != null)
            jd.setCompanyName(req.getCompanyName());
        if (req.getIssueNumber() != null)
            jd.setIssueNumber(req.getIssueNumber());
        if (req.getIssueDate() != null)
            jd.setIssueDate(req.getIssueDate());
        if (req.getPageTotal() != null)
            jd.setPageTotal(req.getPageTotal());

        if (req.getCode() != null)
            jd.setCode(req.getCode());
        if (req.getRevision() != null)
            jd.setRevision(req.getRevision());

        if (req.getJobTitleName() != null)
            jd.setJobTitleName(req.getJobTitleName());
        if (req.getDepartmentName() != null)
            jd.setDepartmentName(req.getDepartmentName());
        if (req.getBelongsTo() != null)
            jd.setBelongsTo(req.getBelongsTo());
        if (req.getDirectManager() != null)
            jd.setDirectManager(req.getDirectManager());
        if (req.getWorkWith() != null)
            jd.setWorkWith(req.getWorkWith());
        if (req.getAssignerTitle() != null)
            jd.setAssignerTitle(req.getAssignerTitle());
        if (req.getAssignerName() != null)
            jd.setAssignerName(req.getAssignerName());

        repository.save(jd);
        Long jdId = jd.getId();

        // ===== DUTY =====
        JDDuty duty = dutyRepository.findByJobDescriptionIdAndActiveTrue(jdId)
                .orElse(new JDDuty());

        duty.setJobDescriptionId(jdId);
        duty.setContent(req.getDutyContent());
        duty.setActive(true);
        dutyRepository.save(duty);

        // ===== REQUIREMENT =====
        JDRequirement requirement = requirementRepository
                .findByJobDescriptionIdAndActiveTrue(jdId)
                .orElse(new JDRequirement());

        requirement.setJobDescriptionId(jdId);
        requirement.setKnowledge(req.getKnowledge());
        requirement.setExperience(req.getExperience());
        requirement.setSkills(req.getSkills());
        requirement.setQualities(req.getQualities());
        requirement.setOtherRequirements(req.getOtherRequirements());
        requirement.setActive(true);
        requirementRepository.save(requirement);

        // ===== POSITION CHART =====
        chartRepository.deleteByJobDescriptionId(jdId);

        if (req.getPositionNodes() != null) {
            req.getPositionNodes().forEach(node -> {
                JDPositionChart chart = new JDPositionChart();
                chart.setJobDescriptionId(jdId);
                chart.setNodeKey(node.getNodeKey());
                chart.setTitle(node.getTitle());
                chart.setParentId(node.getParentId());
                chart.setSortOrder(node.getSortOrder());
                chart.setActive(true);
                chartRepository.save(chart);
            });
        }

        return fetchFullById(jdId);
    }

    // =====================================================
    // FETCH FULL
    // =====================================================
    public ResFullJobDescriptionDTO fetchFullById(Long id) {

        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại."));

        ResFullJobDescriptionDTO res = convertFull(jd);

        dutyRepository.findByJobDescriptionIdAndActiveTrue(id)
                .ifPresent(d -> res.setDutyContent(d.getContent()));

        requirementRepository.findByJobDescriptionIdAndActiveTrue(id)
                .ifPresent(r -> {
                    res.setKnowledge(r.getKnowledge());
                    res.setExperience(r.getExperience());
                    res.setSkills(r.getSkills());
                    res.setQualities(r.getQualities());
                    res.setOtherRequirements(r.getOtherRequirements());
                });

        List<JDPositionChart> charts = chartRepository.findByJobDescriptionIdAndActiveTrueOrderBySortOrderAsc(id);

        res.setPositionNodes(
                charts.stream().map(c -> {
                    ResFullJobDescriptionDTO.PositionNodeDTO node = new ResFullJobDescriptionDTO.PositionNodeDTO();
                    node.setNodeKey(c.getNodeKey());
                    node.setTitle(c.getTitle());
                    node.setParentId(c.getParentId());
                    node.setSortOrder(c.getSortOrder());
                    return node;
                }).collect(Collectors.toList()));

        return res;
    }

    // =====================================================
    // FETCH ALL — SEARCH
    // =====================================================
    public ResultPaginationDTO fetchAll(String search, Pageable pageable) {

        Specification<JobDescription> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("jobTitleName")), pattern),
                    cb.like(cb.lower(root.get("departmentName")), pattern),
                    cb.like(cb.lower(root.get("belongsTo")), pattern),
                    cb.like(cb.lower(root.get("directManager")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern));
        };

        Page<JobDescription> page = repository.findAll(spec, pageable);

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
                        .map(this::convertBasic)
                        .collect(Collectors.toList()));

        return rs;
    }

    // =====================================================
    // CONVERT — FULL
    // =====================================================
    private ResFullJobDescriptionDTO convertFull(JobDescription jd) {

        ResFullJobDescriptionDTO res = new ResFullJobDescriptionDTO();

        res.setId(jd.getId());
        res.setCompanyName(jd.getCompanyName());
        res.setIssueNumber(jd.getIssueNumber());
        res.setIssueDate(jd.getIssueDate());
        res.setPageTotal(jd.getPageTotal());

        res.setCode(jd.getCode());
        res.setRevision(jd.getRevision());

        res.setJobTitleName(jd.getJobTitleName());
        res.setDepartmentName(jd.getDepartmentName());
        res.setBelongsTo(jd.getBelongsTo());
        res.setDirectManager(jd.getDirectManager());
        res.setWorkWith(jd.getWorkWith());
        res.setAssignerTitle(jd.getAssignerTitle());
        res.setAssignerName(jd.getAssignerName());
        res.setStatus(jd.getStatus());
        res.setCreatedAt(jd.getCreatedAt());
        res.setUpdatedAt(jd.getUpdatedAt());
        res.setCreatedBy(jd.getCreatedBy());
        res.setUpdatedBy(jd.getUpdatedBy());

        return res;
    }

    // =====================================================
    // CONVERT — BASIC
    // =====================================================
    private ResJobDescriptionDTO convertBasic(JobDescription jd) {

        ResJobDescriptionDTO res = new ResJobDescriptionDTO();

        res.setId(jd.getId());
        res.setCompanyName(jd.getCompanyName());
        res.setIssueNumber(jd.getIssueNumber());
        res.setIssueDate(jd.getIssueDate());
        res.setPageTotal(jd.getPageTotal());

        res.setCode(jd.getCode());
        res.setRevision(jd.getRevision());

        res.setJobTitleName(jd.getJobTitleName());
        res.setDepartmentName(jd.getDepartmentName());
        res.setBelongsTo(jd.getBelongsTo());
        res.setDirectManager(jd.getDirectManager());
        res.setWorkWith(jd.getWorkWith());
        res.setAssignerTitle(jd.getAssignerTitle());
        res.setAssignerName(jd.getAssignerName());
        res.setStatus(jd.getStatus());
        res.setCreatedAt(jd.getCreatedAt());
        res.setUpdatedAt(jd.getUpdatedAt());
        res.setCreatedBy(jd.getCreatedBy());
        res.setUpdatedBy(jd.getUpdatedBy());

        return res;
    }
}