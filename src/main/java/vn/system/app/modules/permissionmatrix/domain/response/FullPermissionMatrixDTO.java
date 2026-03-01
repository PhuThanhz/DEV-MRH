package vn.system.app.modules.permissionmatrix.domain.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullPermissionMatrixDTO {

    private Long categoryId;
    private String categoryName;
    private String departmentName;

    private List<Column> columns;
    private List<Row> rows;

    @Getter
    @Setter
    public static class Column {
        private Long departmentJobTitleId;
        private String jobTitleName;

        public Column(Long id, String name) {
            this.departmentJobTitleId = id;
            this.jobTitleName = name;
        }
    }

    @Getter
    @Setter
    public static class Row {
        private Long contentId;
        private String contentName;
        private List<Cell> cells;
    }

    @Getter
    @Setter
    public static class Cell {
        private Long departmentJobTitleId;
        private String processActionCode; // XD / RS / TĐ / KS / TH / PD
    }
}