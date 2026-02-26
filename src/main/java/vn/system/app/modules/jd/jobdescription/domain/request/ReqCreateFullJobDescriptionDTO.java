package vn.system.app.modules.jd.jobdescription.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateFullJobDescriptionDTO {

        @NotBlank
        private String companyName;

        private String issueNumber;
        private Instant issueDate;
        private String pageTotal;

        // ⭐ NEW FIELD — Mã số JD
        private String code;

        // ⭐ NEW FIELD — Lần ban hành
        private String revision;

        @NotBlank
        private String jobTitleName;

        @NotBlank
        private String departmentName;

        private String belongsTo;
        private String directManager;
        private String workWith;

        private String assignerTitle;
        private String assignerName;

        @NotBlank
        private String dutyContent;

        /*
         * ===== REQUIREMENT 5 FIELD =====
         */
        private String knowledge;
        private String experience;
        private String skills;
        private String qualities;
        private String otherRequirements;

        private List<PositionNodeDTO> positionNodes;

        @Getter
        @Setter
        public static class PositionNodeDTO {
                private String nodeKey;
                private String title;
                private Long parentId;
                private Integer sortOrder;
        }
}