package vn.system.app.modules.careerpathtemplate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplateStep;

@Repository
public interface CareerPathTemplateStepRepository
        extends JpaRepository<CareerPathTemplateStep, Long> {

    // Lấy tất cả bước của 1 template, sort theo thứ tự
    List<CareerPathTemplateStep> findByTemplate_IdOrderByStepOrderAsc(Long templateId);

    // Lấy bước cụ thể theo template + order
    Optional<CareerPathTemplateStep> findByTemplate_IdAndStepOrder(
            Long templateId, Integer stepOrder);

    // ✅ FIX: đổi Optional → List
    // Lý do: query "step_order > x" trả về nhiều row, Optional crash
    // NonUniqueResultException
    // Service sẽ lấy steps.get(0) — tức bước gần nhất phía trên
    List<CareerPathTemplateStep> findByTemplate_IdAndStepOrderGreaterThanOrderByStepOrderAsc(
            Long templateId, Integer currentStepOrder);

    // Đếm số bước
    int countByTemplate_Id(Long templateId);

    // Kiểm tra careerPath đang được dùng trong template nào không
    boolean existsByCareerPath_Id(Long careerPathId);
}