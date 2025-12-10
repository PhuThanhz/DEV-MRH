package vn.system.app.modules.sourcelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.system.app.modules.sourcelink.domain.SourceGroupMain;

import java.util.Optional;

public interface SourceGroupMainRepository
        extends JpaRepository<SourceGroupMain, Long>, JpaSpecificationExecutor<SourceGroupMain> {

    // Kiểm tra tên đã tồn tại
    boolean existsByName(String name);

    // Tìm nhóm chính theo tên (dùng cho kiểm tra trùng dữ liệu)
    Optional<SourceGroupMain> findByName(String name);
}
