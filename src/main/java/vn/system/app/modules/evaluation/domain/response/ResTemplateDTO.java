package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.TemplateStatus;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;
import vn.system.app.modules.company.domain.response.ResCompanyDTO;
import java.time.Instant;
import java.util.List;

@Data
public class ResTemplateDTO {
    private Long id;
    private String name;
    private TemplateType type;
    private String description;
    private TemplateStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private ResCompanyDTO company;
    private List<ResTargetJobTitleDTO> targetJobTitles;
    private List<ResSectionDTO> sections;

    @Data
    public static class ResTargetJobTitleDTO {
        private Long id;
        private String nameVi;
        private String nameEn;
    }

    @Data
    public static class ResSectionDTO {
        private Long id;
        private String code;
        private String name;
        private Double weight;
        private Integer displayOrder;
        private List<ResCriteriaDTO> criteria;
    }

    @Data
    public static class ResCriteriaDTO {
        private Long id;
        private String name;
        private String measurementMethod;
        private String description;
        private Double weight;
        /** Trọng số thực dùng để tính điểm; tiêu chí con được chia từ tiêu chí cha. */
        private Double effectiveWeight;
        private Integer displayOrder;
        private List<ResCriteriaDTO> subCriteria;
        private List<ResCriteriaLevelDTO> levels;
    }

    @Data
    public static class ResCriteriaLevelDTO {
        private Long id;
        private Integer level;
        private String description;
    }
}
