package vn.system.app.modules.positionlevel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.system.app.modules.positionlevel.domain.PositionLevel;

public interface PositionLevelRepository
        extends JpaRepository<PositionLevel, Long>, JpaSpecificationExecutor<PositionLevel> {

    boolean existsByCode(String code);

    // Kiểm tra xem bandOrder đã tồn tại chưa
    boolean existsByBandOrder(Integer bandOrder);
}
