package vn.system.app.modules.jobpositionnode.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateNode {

    private Long id;

    private String name;

    private String level;

    private String holderName;

    private Boolean isGoal;

    private Long parentId;

    // ── Vị trí trên canvas ──────────────────
    private Double posX;

    private Double posY;

    // gửi null = giữ nguyên, gửi 0 = gỡ JD, gửi id > 0 = gắn JD mới
    private Long jobDescriptionId;
}