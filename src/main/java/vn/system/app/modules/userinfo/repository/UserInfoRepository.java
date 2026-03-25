package vn.system.app.modules.userinfo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.userinfo.domain.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    boolean existsByEmployeeCode(String employeeCode);
}