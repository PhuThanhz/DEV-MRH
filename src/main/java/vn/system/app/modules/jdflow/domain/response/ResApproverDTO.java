package vn.system.app.modules.jdflow.domain.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ResApproverDTO {

    private Long id;
    private String fullName;
    private String email;

    public ResApproverDTO(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }
}
