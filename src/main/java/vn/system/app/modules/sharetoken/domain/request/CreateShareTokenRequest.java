package vn.system.app.modules.sharetoken.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShareTokenRequest {

    @NotNull
    private String procedureType; // "COMPANY" | "DEPARTMENT" | "CONFIDENTIAL"

    private String pin; // nullable, tự nhập

    private Boolean autoGeneratePin = false; // true = tự động sinh PIN 6 số

    @NotNull
    private String permission; // "VIEW_INFO" | "VIEW_FILE" | "VIEW_ALL"

    private Instant expiresAt; // nullable

    private Integer maxAccessCount; // nullable
}