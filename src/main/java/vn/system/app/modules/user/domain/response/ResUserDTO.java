package vn.system.app.modules.user.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;
    private String email;
    private String name;
    private String address;
    private int age;
    private Instant updatedAt;
    private Instant createdAt;

    private RoleUser role;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private long id;
        private String name;
    }
}
