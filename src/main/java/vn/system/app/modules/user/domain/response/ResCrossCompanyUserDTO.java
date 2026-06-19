package vn.system.app.modules.user.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResCrossCompanyUserDTO {
    private String id;
    private String name;
    private String email;
    private String companyName;
    private String departmentName;
    private String directManagerId;
    private String directManagerName;
    private String employeeCode;
    private String jobTitle;
    private String positionLevel;
    private String avatar;
}
