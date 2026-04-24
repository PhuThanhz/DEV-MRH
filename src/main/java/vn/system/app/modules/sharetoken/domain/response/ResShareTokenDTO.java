package vn.system.app.modules.sharetoken.domain.response;

import java.time.Instant;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResShareTokenDTO {

    private Long id;
    private Long procedureId;
    private String procedureType;
    private String token;
    private String permission;
    private Instant expiresAt;
    private Integer maxAccessCount;
    private Integer accessCount;
    private String qrCode; // chỉ trả khi mới tạo, null khi fetch list
    private String createdBy;
    private Instant createdAt;
    private Boolean isRevoked;
    private Boolean hasPin; // ← thêm vào đây
    private String pin; // ← trả về cho người tạo xem lại

}