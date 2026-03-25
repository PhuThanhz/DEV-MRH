package vn.system.app.modules.jobpositionnode.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResJobPositionNodeDTO {

    private Long id;

    private String name;

    // mã cấp bậc
    private String level;

    // tên người giữ chức danh
    private String holderName;

    // node mục tiêu
    private Boolean isGoal;

    private Long parentId;

    // ── Vị trí trên canvas ──────────────────
    private Double posX;

    private Double posY;

    // ── JD đã ban hành gắn vào node ──────────
    private Long jobDescriptionId;
    private String jobDescriptionCode;
    private String jobDescriptionStatus;
}