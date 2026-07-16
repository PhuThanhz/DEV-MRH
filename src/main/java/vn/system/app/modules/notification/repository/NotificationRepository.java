package vn.system.app.modules.notification.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.notification.domain.AppNotification;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    
    List<AppNotification> findTop50ByRecipientIdOrderByCreatedAtDesc(String recipientId);

    Page<AppNotification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);
    
    List<AppNotification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);
    
    long countByRecipientIdAndReadFalse(String recipientId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.read = true WHERE n.recipient.id = :userId AND n.read = false")
    void markAllAsReadByRecipientId(String userId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.read = true WHERE n.recipient.id = :userId AND n.module = :module AND n.read = false")
    void markAllAsReadByRecipientIdAndModule(String userId, String module);

    boolean existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(String recipientId, String type, String actionLink, java.time.Instant createdAt);
}
