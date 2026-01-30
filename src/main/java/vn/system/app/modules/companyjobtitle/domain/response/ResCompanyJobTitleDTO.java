package vn.system.app.modules.companyjobtitle.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyJobTitleDTO {

    private Long id;
    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;
    private CompanyInfo company;

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
    }

    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id;
        private String name;
    }
}
