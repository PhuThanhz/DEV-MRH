package vn.system.app.modules.jd.jdflow.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResJdApproverDTO {

    private Long id;

    private String name;

    private String email;

    private String avatar;

    private boolean isFinal; // ✅ thêm field này
}