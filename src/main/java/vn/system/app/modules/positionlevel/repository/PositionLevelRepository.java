package vn.system.app.modules.positionlevel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.system.app.modules.positionlevel.domain.PositionLevel;

public interface PositionLevelRepository
        extends JpaRepository<PositionLevel, Long>, JpaSpecificationExecutor<PositionLevel> {

    // ⭐ THAY — check code trùng trong phạm vi công ty
    boolean existsByCodeAndCompanyId(String code, Long companyId);

    // ⭐ THAY — check bandOrder trùng trong phạm vi công ty
    boolean existsByBandOrderAndCompanyId(Integer bandOrder, Long companyId);

    // ⭐ THÊM — lấy toàn bộ bậc của một công ty (dùng trong helper findBandOrder)
    List<PositionLevel> findByCompanyId(Long companyId);
}