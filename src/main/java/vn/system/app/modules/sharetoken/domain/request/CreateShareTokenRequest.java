package vn.system.app.modules.sharetoken.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShareTokenRequest {

    private String procedureType; // "COMPANY" | "DEPARTMENT" | "CONFIDENTIAL" | "DOCUMENT"

    private String pin; // nullable, tự nhập

    private Boolean autoGeneratePin = false; // true = tự động sinh PIN 6 số

    private Instant expiresAt; // nullable

    @Min(value = 1, message = "Giới hạn lượt xem phải lớn hơn 0")
    private Integer maxAccessCount; // nullable
}