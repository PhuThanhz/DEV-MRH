package vn.system.app.modules.confidentialprocedure.domain.response;

import lombok.Data;
import java.time.Instant;

@Data
public class ResShareLogDTO {
    private Long id;
    private Long procedureId;
    private String procedureCode;
    private String procedureName;
    private String procedureStatus; // ← thêm
    private Integer procedureVersion; // ← thêm
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String senderRole; // ← thêm
    private String receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverRole; // ← thêm
    private String action;
    private Instant sentAt;
    private Instant procedureIssuedDate; // ← thêm
    private Long companyId;
    private Long departmentId;
}