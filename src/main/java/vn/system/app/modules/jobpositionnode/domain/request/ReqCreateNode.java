package vn.system.app.modules.jobpositionnode.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateNode {

    private Long chartId;

    // tên chức danh
    private String name;

    // mã cấp bậc
    private String level;

    // tên người giữ chức danh
    private String holderName;

    // node mục tiêu
    private Boolean isGoal = false;

    private Long parentId;

    // ── Vị trí trên canvas ──────────────────
    private Double posX;

    private Double posY;

    // ID của JD đã PUBLISHED muốn gắn (nullable)
    private Long jobDescriptionId;
}