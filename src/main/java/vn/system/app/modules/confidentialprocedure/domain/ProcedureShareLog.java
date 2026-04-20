package vn.system.app.modules.confidentialprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "procedure_share_log")
@Getter
@Setter
public class ProcedureShareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procedure_id", nullable = false)
    private Long procedureId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    // để sau mở rộng thêm action: "SHARE" / "REVOKE"
    @Column(name = "action", nullable = false)
    private String action;
}