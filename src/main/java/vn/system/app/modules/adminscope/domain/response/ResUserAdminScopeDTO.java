package vn.system.app.modules.adminscope.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUserAdminScopeDTO {
    private Long id;
    private String scopeType;
    private SimpleRef company;
    private SimpleRef department;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class SimpleRef {
        private Long id;
        private String name;
    }
}
