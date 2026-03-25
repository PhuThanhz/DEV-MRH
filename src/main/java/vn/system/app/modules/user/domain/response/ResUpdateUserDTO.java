package vn.system.app.modules.user.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    // ⭐ XÓA address
    // ⭐ THÊM active
    private boolean active;
    private Instant updatedAt;
}