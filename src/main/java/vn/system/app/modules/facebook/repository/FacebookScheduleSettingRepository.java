package vn.system.app.modules.facebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.system.app.modules.facebook.domain.FacebookScheduleSetting;

public interface FacebookScheduleSettingRepository extends JpaRepository<FacebookScheduleSetting, Long> {
    FacebookScheduleSetting findBySourceGroup_Id(Long groupId);
}
