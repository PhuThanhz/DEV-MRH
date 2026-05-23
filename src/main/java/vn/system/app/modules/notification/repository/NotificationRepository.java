package vn.system.app.modules.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.notification.domain.AppNotification;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    
    List<AppNotification> findTop50ByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<AppNotification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);
    
    long countByRecipientIdAndReadFalse(String recipientId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.read = true WHERE n.recipient.id = :userId AND n.read = false")
    void markAllAsReadByRecipientId(String userId);
}
