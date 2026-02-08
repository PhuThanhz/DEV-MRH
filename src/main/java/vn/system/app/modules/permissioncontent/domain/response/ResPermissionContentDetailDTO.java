package vn.system.app.modules.permissioncontent.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPermissionContentDetailDTO {

    private Long id;
    private String name;
    private CategoryDTO category;
    private boolean active;

    @Getter
    @Setter
    public static class CategoryDTO {
        private Long id;
        private String code;
        private String name;
    }
}
