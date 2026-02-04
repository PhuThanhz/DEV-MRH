package vn.system.app.modules.companyjobtitle.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyJobTitleDTO {

    private Long id;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;
    private CompanyInfo company;

    // ⭐ Thêm trường này để frontend biết nguồn gán
    private String source; // "COMPANY", "DEPARTMENT", "SECTION", "MULTIPLE"

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;

        private String positionCode;
        private String band;
        private Integer level;

        private Integer bandOrder;
        private Integer levelNumber;
    }

    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id;
        private String name;
    }
}