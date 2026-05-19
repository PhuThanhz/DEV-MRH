package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationNotification;

@Repository
public interface EvaluationNotificationRepository extends JpaRepository<EvaluationNotification, Long> {

    List<EvaluationNotification> findByRecipientIdOrderByCreatedAtDesc(String userId);

    List<EvaluationNotification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String userId);

    long countByRecipientIdAndReadFalse(String userId);
}
