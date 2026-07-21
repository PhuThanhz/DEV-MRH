package vn.system.app.modules.evaluation.service;

import org.springframework.stereotype.Component;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.response.*;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.company.domain.response.ResCompanyDTO;

import vn.system.app.modules.userposition.repository.UserPositionRepository;
import vn.system.app.modules.userposition.domain.UserPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import vn.system.app.modules.evaluation.repository.EvaluationRecordRepository;

@Component
public class EvaluationMapper {

    private final UserPositionRepository userPositionRepository;
    private final EvaluationRecordRepository recordRepo;

    public EvaluationMapper(UserPositionRepository userPositionRepository, EvaluationRecordRepository recordRepo) {
        this.userPositionRepository = userPositionRepository;
        this.recordRepo = recordRepo;
    }

    public ResTemplateDTO toResTemplateDTO(EvaluationTemplate entity) {
        if (entity == null) return null;
        ResTemplateDTO dto = new ResTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        
        if (entity.getCompany() != null && entity.getCompany().getId() > 0) {
            ResCompanyDTO compDto = new ResCompanyDTO();
            compDto.setId(entity.getCompany().getId());
            compDto.setCode(entity.getCompany().getCode());
            compDto.setName(entity.getCompany().getName());
            dto.setCompany(compDto);
        }
        
        if (entity.getTargetJobTitles() != null) {
            dto.setTargetJobTitles(entity.getTargetJobTitles().stream().map(jt -> {
                ResTemplateDTO.ResTargetJobTitleDTO targetDto = new ResTemplateDTO.ResTargetJobTitleDTO();
                targetDto.setId(jt.getId());
                targetDto.setNameVi(jt.getNameVi());
                targetDto.setNameEn(jt.getNameEn());
                return targetDto;
            }).collect(Collectors.toList()));
        }
        
        if (entity.getSections() != null) {
            dto.setSections(entity.getSections().stream().map(this::toResSectionDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private ResTemplateDTO toResTemplateSummaryDTO(EvaluationTemplate entity) {
        if (entity == null) return null;
        ResTemplateDTO dto = new ResTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        if (entity.getCompany() != null && entity.getCompany().getId() > 0) {
            ResCompanyDTO company = new ResCompanyDTO();
            company.setId(entity.getCompany().getId());
            company.setCode(entity.getCompany().getCode());
            company.setName(entity.getCompany().getName());
            dto.setCompany(company);
        }
        return dto;
    }

    public ResTemplateDTO.ResSectionDTO toResSectionDTO(TemplateSection entity) {
        if (entity == null) return null;
        ResTemplateDTO.ResSectionDTO dto = new ResTemplateDTO.ResSectionDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setWeight(entity.getWeight());
        dto.setDisplayOrder(entity.getDisplayOrder());
        
        if (entity.getCriteria() != null) {
            List<TemplateCriteria> rootCriteria = entity.getCriteria().stream()
                    .filter(c -> c.getParentCriteria() == null)
                    .collect(Collectors.toList());
            dto.setCriteria(rootCriteria.stream().map(this::toResCriteriaDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    public ResTemplateDTO.ResCriteriaDTO toResCriteriaDTO(TemplateCriteria entity) {
        if (entity == null) return null;
        ResTemplateDTO.ResCriteriaDTO dto = new ResTemplateDTO.ResCriteriaDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMeasurementMethod(entity.getMeasurementMethod());
        dto.setDescription(entity.getDescription());
        dto.setWeight(entity.getWeight());
        dto.setEffectiveWeight(resolveEffectiveWeight(entity));
        dto.setDisplayOrder(entity.getDisplayOrder());
        
        if (entity.getSubCriteria() != null) {
            dto.setSubCriteria(entity.getSubCriteria().stream().map(this::toResCriteriaDTO).collect(Collectors.toList()));
        }
        
        if (entity.getLevels() != null) {
            dto.setLevels(entity.getLevels().stream().map(this::toResCriteriaLevelDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    public ResTemplateDTO.ResCriteriaLevelDTO toResCriteriaLevelDTO(TemplateCriteriaLevel entity) {
        if (entity == null) return null;
        ResTemplateDTO.ResCriteriaLevelDTO dto = new ResTemplateDTO.ResCriteriaLevelDTO();
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public ResPeriodDTO toResPeriodDTO(EvaluationPeriod entity) {
        if (entity == null) return null;
        ResPeriodDTO dto = new ResPeriodDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setEmployeeStartDate(entity.getEmployeeStartDate());
        dto.setEmployeeDeadline(entity.getEmployeeDeadline());
        dto.setManagerDeadline(entity.getManagerDeadline());
        dto.setApprovalDeadline(entity.getApprovalDeadline());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        if (entity.getCompany() != null && entity.getCompany().getId() > 0) {
            ResPeriodDTO.CompanyDTO compDto = new ResPeriodDTO.CompanyDTO();
            compDto.setId(entity.getCompany().getId());
            compDto.setName(entity.getCompany().getName());
            dto.setCompany(compDto);
        }
        
        return dto;
    }

    public ResPeriodTemplateDTO toResPeriodTemplateDTO(PeriodTemplate entity) {
        if (entity == null) return null;
        ResPeriodTemplateDTO dto = new ResPeriodTemplateDTO();
        dto.setId(entity.getId());
        dto.setPeriodId(entity.getPeriod() != null ? entity.getPeriod().getId() : null);
        dto.setTemplate(toResTemplateDTO(entity.getTemplate()));
        return dto;
    }

    public ResPeriodEmployeeDTO toResPeriodEmployeeDTO(PeriodEmployee entity) {
        if (entity == null) return null;
        ResPeriodEmployeeDTO dto = new ResPeriodEmployeeDTO();
        dto.setId(entity.getId());
        dto.setPeriodId(entity.getPeriod() != null ? entity.getPeriod().getId() : null);
        dto.setEmployee(toResEmployeeInfo(entity.getEmployee()));
        dto.setDirectManager(toResEmployeeInfo(entity.getDirectManager()));
        dto.setIndirectManager(toResEmployeeInfo(entity.getIndirectManager()));
        dto.setTemplate(toResTemplateDTO(entity.getTemplate()));
        dto.setStatus(entity.getStatus());
        
        if (entity.getPeriod() != null && entity.getEmployee() != null) {
            recordRepo.findByPeriodIdAndEmployeeId(entity.getPeriod().getId(), entity.getEmployee().getId())
                    .ifPresent(record -> {
                        dto.setRecordId(record.getId());
                        dto.setRecordStatus(record.getStatus() != null ? record.getStatus().toString() : null);
                        dto.setEmployeeDeadlineOverride(record.getEmployeeDeadlineOverride());
                        dto.setManagerDeadlineOverride(record.getManagerDeadlineOverride());
                        dto.setApprovalDeadlineOverride(record.getApprovalDeadlineOverride());
                    });
        }
        
        return dto;
    }

    public ResEvaluationRecordDTO toResEvaluationRecordDTO(EvaluationRecord entity) {
        return toResEvaluationRecordDTO(entity, null, null, null);
    }

    public ResEvaluationRecordDTO toResEvaluationRecordDTO(EvaluationRecord entity, List<EvaluationScore> scores, List<EvaluationComment> comments, List<EvaluationTrainingPlan> plans) {
        return mapEvaluationRecord(entity, scores, comments, plans, Collections.emptyMap(), true);
    }

    public List<ResEvaluationRecordDTO> toResEvaluationRecordSummaryDTOs(List<EvaluationRecord> entities) {
        if (entities == null || entities.isEmpty()) return List.of();

        Set<String> userIds = entities.stream()
                .flatMap(record -> java.util.stream.Stream.of(
                        record.getEmployee(), record.getDirectManager(), record.getIndirectManager()))
                .filter(java.util.Objects::nonNull)
                .map(User::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, List<UserPosition>> positionsByUserId = userIds.isEmpty()
                ? Collections.emptyMap()
                : userPositionRepository.findActiveFullByUserIds(new ArrayList<>(userIds)).stream()
                        .filter(position -> position.getUser() != null)
                        .collect(Collectors.groupingBy(position -> position.getUser().getId()));

        return entities.stream()
                .map(entity -> mapEvaluationRecord(
                        entity, null, null, null, positionsByUserId, false))
                .toList();
    }

    private ResEvaluationRecordDTO mapEvaluationRecord(
            EvaluationRecord entity,
            List<EvaluationScore> scores,
            List<EvaluationComment> comments,
            List<EvaluationTrainingPlan> plans,
            Map<String, List<UserPosition>> positionsByUserId,
            boolean includeFullTemplate) {
        if (entity == null) return null;
        ResEvaluationRecordDTO dto = new ResEvaluationRecordDTO();
        dto.setId(entity.getId());
        dto.setPeriodId(entity.getPeriod().getId());
        dto.setPeriod(toResPeriodDTO(entity.getPeriod()));
        dto.setEmployee(includeFullTemplate
                ? toResEmployeeInfo(entity.getEmployee())
                : toResEmployeeInfo(entity.getEmployee(), positionsByUserId));
        dto.setDirectManager(includeFullTemplate
                ? toResEmployeeInfo(entity.getDirectManager())
                : toResEmployeeInfo(entity.getDirectManager(), positionsByUserId));
        dto.setIndirectManager(includeFullTemplate
                ? toResEmployeeInfo(entity.getIndirectManager())
                : toResEmployeeInfo(entity.getIndirectManager(), positionsByUserId));
        dto.setTemplate(includeFullTemplate
                ? toResTemplateDTO(entity.getTemplate())
                : toResTemplateSummaryDTO(entity.getTemplate()));
        dto.setStatus(entity.getStatus());
        dto.setEmployeeSubmittedAt(entity.getEmployeeSubmittedAt());
        dto.setManagerSubmittedAt(entity.getManagerSubmittedAt());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setEmployeeDeadlineOverride(entity.getEmployeeDeadlineOverride());
        dto.setManagerDeadlineOverride(entity.getManagerDeadlineOverride());
        dto.setApprovalDeadlineOverride(entity.getApprovalDeadlineOverride());
        dto.setEffectiveEmployeeDeadline(entity.getEmployeeDeadlineOverride() != null
                ? entity.getEmployeeDeadlineOverride()
                : entity.getPeriod().getEmployeeDeadline());
        dto.setEffectiveManagerDeadline(entity.getManagerDeadlineOverride() != null
                ? entity.getManagerDeadlineOverride()
                : entity.getPeriod().getManagerDeadline());
        dto.setEffectiveApprovalDeadline(entity.getApprovalDeadlineOverride() != null
                ? entity.getApprovalDeadlineOverride()
                : entity.getPeriod().getApprovalDeadline());
        dto.setEmployeeTotalScore(entity.getEmployeeTotalScore());
        dto.setManagerTotalScore(entity.getManagerTotalScore());
        dto.setApproverTotalScore(entity.getApproverTotalScore());
        dto.setFinalGrade(entity.getFinalGrade());
        
        if (scores != null) {
            dto.setScores(scores.stream().map(this::toResScoreDTO).collect(Collectors.toList()));
            dto.setScoringSummary(toResScoringSummaryDTO(entity, scores));
        }
        if (comments != null) {
            dto.setComments(comments.stream().map(this::toResCommentDTO).collect(Collectors.toList()));
        }
        if (plans != null) {
            dto.setTrainingPlans(plans.stream().map(this::toResTrainingPlanDTO).collect(Collectors.toList()));
        }
        
        return dto;
    }

    public ResEvaluationRecordDTO.ResEmployeeInfo toResEmployeeInfo(User entity) {
        if (entity == null) return null;
        List<UserPosition> positions;
        try {
            positions = userPositionRepository.findActiveFullByUserId(entity.getId());
        } catch (Exception ignored) {
            positions = List.of();
        }
        return toResEmployeeInfo(entity, positions);
    }

    private ResEvaluationRecordDTO.ResEmployeeInfo toResEmployeeInfo(
            User entity,
            Map<String, List<UserPosition>> positionsByUserId) {
        if (entity == null) return null;
        return toResEmployeeInfo(entity,
                positionsByUserId.getOrDefault(entity.getId(), List.of()));
    }

    private ResEvaluationRecordDTO.ResEmployeeInfo toResEmployeeInfo(
            User entity,
            List<UserPosition> posList) {
        if (entity == null) return null;
        ResEvaluationRecordDTO.ResEmployeeInfo dto = new ResEvaluationRecordDTO.ResEmployeeInfo();
        dto.setId(entity.getId());
        dto.setUsername(entity.getName()); // Tạm lấy Name vì model User trong dự án map Name
        if (entity.getUserInfo() != null) {
            dto.setEmployeeCode(entity.getUserInfo().getEmployeeCode());
        }
        dto.setFullName(entity.getName());
        dto.setEmail(entity.getEmail());
        
        try {
            if (posList != null && !posList.isEmpty()) {
                UserPosition chosen = null;
                for (UserPosition p : posList) {
                    if ("SECTION".equalsIgnoreCase(p.getSource())) {
                        chosen = p;
                        break;
                    }
                }
                if (chosen == null) {
                    for (UserPosition p : posList) {
                        if ("DEPARTMENT".equalsIgnoreCase(p.getSource())) {
                            chosen = p;
                            break;
                        }
                    }
                }
                if (chosen == null) {
                    chosen = posList.get(0);
                }
                
                vn.system.app.modules.jobtitle.domain.JobTitle jt = null;
                switch (chosen.getSource().toUpperCase()) {
                    case "COMPANY" -> {
                        if (chosen.getCompanyJobTitle() != null) {
                            jt = chosen.getCompanyJobTitle().getJobTitle();
                            if (chosen.getCompanyJobTitle().getCompany() != null) {
                                dto.setCompanyId(chosen.getCompanyJobTitle().getCompany().getId());
                                dto.setCompanyName(chosen.getCompanyJobTitle().getCompany().getName());
                            }
                        }
                    }
                    case "DEPARTMENT" -> {
                        if (chosen.getDepartmentJobTitle() != null) {
                            jt = chosen.getDepartmentJobTitle().getJobTitle();
                            if (chosen.getDepartmentJobTitle().getDepartment() != null) {
                                dto.setDepartmentId(chosen.getDepartmentJobTitle().getDepartment().getId());
                                dto.setDepartmentName(chosen.getDepartmentJobTitle().getDepartment().getName());
                                if (chosen.getDepartmentJobTitle().getDepartment().getCompany() != null) {
                                    dto.setCompanyId(chosen.getDepartmentJobTitle().getDepartment().getCompany().getId());
                                    dto.setCompanyName(chosen.getDepartmentJobTitle().getDepartment().getCompany().getName());
                                }
                            }
                        }
                    }
                    case "SECTION" -> {
                        if (chosen.getSectionJobTitle() != null) {
                            jt = chosen.getSectionJobTitle().getJobTitle();
                            if (chosen.getSectionJobTitle().getSection() != null) {
                                dto.setSectionId(chosen.getSectionJobTitle().getSection().getId());
                                dto.setSectionName(chosen.getSectionJobTitle().getSection().getName());
                                if (chosen.getSectionJobTitle().getSection().getDepartment() != null) {
                                    dto.setDepartmentId(chosen.getSectionJobTitle().getSection().getDepartment().getId());
                                    dto.setDepartmentName(chosen.getSectionJobTitle().getSection().getDepartment().getName());
                                    if (chosen.getSectionJobTitle().getSection().getDepartment().getCompany() != null) {
                                        dto.setCompanyId(chosen.getSectionJobTitle().getSection().getDepartment().getCompany().getId());
                                        dto.setCompanyName(chosen.getSectionJobTitle().getSection().getDepartment().getCompany().getName());
                                    }
                                }
                            }
                        }
                    }
                }
                if (jt != null) {
                    dto.setJobTitle(jt.getNameVi());
                    if (jt.getPositionLevel() != null) {
                        dto.setPositionLevel(jt.getPositionLevel().getCode());
                    }
                }
            }
        } catch (Exception e) {
            // Safe fallback
        }
        
        return dto;
    }

    public ResEvaluationRecordDTO.ResScoreDTO toResScoreDTO(EvaluationScore entity) {
        if (entity == null) return null;
        ResEvaluationRecordDTO.ResScoreDTO dto = new ResEvaluationRecordDTO.ResScoreDTO();
        dto.setId(entity.getId());
        dto.setCriteriaId(entity.getCriteria().getId());
        dto.setScoredBy(entity.getScoredBy());
        dto.setScore(entity.getScore());
        dto.setEffectiveWeight(resolveEffectiveWeight(entity.getCriteria()));
        dto.setWeightedScore(entity.getWeightedScore());
        return dto;
    }

    public ResScoreUpdateDTO toResScoreUpdateDTO(EvaluationRecord record, List<EvaluationScore> scores) {
        ResScoreUpdateDTO dto = new ResScoreUpdateDTO();
        dto.setScores(scores.stream().map(this::toResScoreDTO).toList());
        dto.setScoringSummary(toResScoringSummaryDTO(record, scores));
        return dto;
    }

    public ResEvaluationRecordDTO.ResScoringSummaryDTO toResScoringSummaryDTO(
            EvaluationRecord record, List<EvaluationScore> scores) {
        ResEvaluationRecordDTO.ResScoringSummaryDTO summary = new ResEvaluationRecordDTO.ResScoringSummaryDTO();
        List<ResEvaluationRecordDTO.ResSectionScoreDTO> sectionSummaries = new ArrayList<>();
        Map<Long, ResEvaluationRecordDTO.ResSectionScoreDTO> sectionById = new java.util.LinkedHashMap<>();

        if (record.getTemplate() != null && record.getTemplate().getSections() != null) {
            record.getTemplate().getSections().forEach(section -> {
                ResEvaluationRecordDTO.ResSectionScoreDTO sectionSummary = new ResEvaluationRecordDTO.ResSectionScoreDTO();
                sectionSummary.setSectionId(section.getId());
                sectionById.put(section.getId(), sectionSummary);
                sectionSummaries.add(sectionSummary);
            });
        }

        Set<Long> parentCriteriaIds = scores.stream()
                .map(EvaluationScore::getCriteria)
                .map(TemplateCriteria::getParentCriteria)
                .filter(java.util.Objects::nonNull)
                .map(TemplateCriteria::getId)
                .collect(Collectors.toSet());

        double employeeTotal = 0;
        double managerTotal = 0;
        double approverTotal = 0;
        boolean hasEmployee = false;
        boolean hasManager = false;
        boolean hasApprover = false;

        for (EvaluationScore score : scores) {
            if (score.getCriteria() == null || parentCriteriaIds.contains(score.getCriteria().getId())) continue;
            double weightedScore = score.getWeightedScore() == null ? 0 : score.getWeightedScore();
            Long sectionId = score.getCriteria().getSection().getId();
            ResEvaluationRecordDTO.ResSectionScoreDTO section = sectionById.get(sectionId);
            switch (score.getScoredBy()) {
                case EMPLOYEE -> {
                    employeeTotal += weightedScore;
                    hasEmployee = true;
                    if (section != null) section.setEmployeeScore(valueOrZero(section.getEmployeeScore()) + weightedScore);
                }
                case MANAGER -> {
                    managerTotal += weightedScore;
                    hasManager = true;
                    if (section != null) section.setManagerScore(valueOrZero(section.getManagerScore()) + weightedScore);
                }
                case APPROVER -> {
                    approverTotal += weightedScore;
                    hasApprover = true;
                    if (section != null) section.setApproverScore(valueOrZero(section.getApproverScore()) + weightedScore);
                }
            }
        }

        summary.setEmployeeTotalScore(hasEmployee ? employeeTotal : null);
        summary.setManagerTotalScore(hasManager ? managerTotal : null);
        summary.setApproverTotalScore(hasApprover ? approverTotal : null);
        summary.setSections(sectionSummaries);
        return summary;
    }

    private double resolveEffectiveWeight(TemplateCriteria criteria) {
        if (criteria == null) return 0;
        if (criteria.getParentCriteria() == null) return criteria.getWeight() == null ? 0 : criteria.getWeight();
        TemplateCriteria parent = criteria.getParentCriteria();
        int siblingCount = parent.getSubCriteria() == null ? 0 : parent.getSubCriteria().size();
        return siblingCount == 0 || parent.getWeight() == null ? 0 : parent.getWeight() / siblingCount;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    public ResEvaluationRecordDTO.ResCommentDTO toResCommentDTO(EvaluationComment entity) {
        if (entity == null) return null;
        ResEvaluationRecordDTO.ResCommentDTO dto = new ResEvaluationRecordDTO.ResCommentDTO();
        dto.setId(entity.getId());
        dto.setCommentType(entity.getCommentType());
        dto.setContent(entity.getContent());
        dto.setWrittenBy(toResEmployeeInfo(entity.getWrittenBy()));
        dto.setWrittenAt(entity.getWrittenAt());
        return dto;
    }

    public ResEvaluationRecordDTO.ResTrainingPlanDTO toResTrainingPlanDTO(EvaluationTrainingPlan entity) {
        if (entity == null) return null;
        ResEvaluationRecordDTO.ResTrainingPlanDTO dto = new ResEvaluationRecordDTO.ResTrainingPlanDTO();
        dto.setId(entity.getId());
        dto.setTrainingGroup(entity.getTrainingGroup());
        dto.setContent(entity.getContent());
        dto.setRequirements(entity.getRequirements());
        dto.setSolution(entity.getSolution());
        dto.setCompletionTimeline(entity.getCompletionTimeline());
        return dto;
    }

    public ResEvaluationHistoryDTO toResEvaluationHistoryDTO(EvaluationHistory entity) {
        if (entity == null) return null;
        ResEvaluationHistoryDTO dto = new ResEvaluationHistoryDTO();
        dto.setId(entity.getId());
        dto.setRecordId(entity.getEvaluationRecord() != null ? entity.getEvaluationRecord().getId() : null);
        dto.setFromStatus(entity.getFromStatus());
        dto.setToStatus(entity.getToStatus());
        dto.setPerformedBy(toResEmployeeInfo(entity.getPerformedBy()));
        dto.setNote(entity.getNote());
        dto.setPerformedAt(entity.getPerformedAt());
        return dto;
    }

    public ResultPaginationDTO mapPagination(ResultPaginationDTO source, Object mappedResult) {
        ResultPaginationDTO dto = new ResultPaginationDTO();
        dto.setMeta(source.getMeta());
        dto.setResult(mappedResult);
        return dto;
    }
}
