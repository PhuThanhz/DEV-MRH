// vn.system.app.modules.jobtitle.domain.response.JobTitleByLevelResponse

package vn.system.app.modules.jobtitle.domain.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobTitleByLevelResponse {

    private Long id;
    private String nameVi;
    private String nameEn;
    private String positionLevelCode;
    private Integer bandOrder;

    /**
     * true nếu CareerPath đã tồn tại trong department này.
     * Frontend dùng để grey-out checkbox.
     */
    private boolean alreadyExists;
}