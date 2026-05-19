package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationComment;
import vn.system.app.modules.evaluation.domain.enums.CommentType;

@Repository
public interface EvaluationCommentRepository extends JpaRepository<EvaluationComment, Long> {

    List<EvaluationComment> findByEvaluationRecordId(Long evaluationRecordId);

    List<EvaluationComment> findByEvaluationRecordIdAndCommentType(Long evaluationRecordId, CommentType commentType);
}
