package vn.system.app.modules.userinfo.service;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userinfo.domain.UserInfo;
import vn.system.app.modules.userinfo.domain.request.ReqUserInfoDTO;
import vn.system.app.modules.userinfo.domain.response.ResUserInfoDTO;
import vn.system.app.modules.userinfo.repository.UserInfoRepository;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;

    public UserInfoService(UserInfoRepository userInfoRepository,
            UserRepository userRepository) {
        this.userInfoRepository = userInfoRepository;
        this.userRepository = userRepository;
    }

    // ======================================================
    // CREATE
    // ======================================================
    public ResUserInfoDTO handleCreate(String userId, ReqUserInfoDTO req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy user ID = " + userId));

        if (userInfoRepository.existsByUser_Id(userId)) {
            throw new IdInvalidException("User này đã có thông tin cá nhân rồi, vui lòng dùng chức năng cập nhật.");
        }

        if (userInfoRepository.existsByEmployeeCode(req.getEmployeeCode())) {
            throw new IdInvalidException("Mã nhân viên " + req.getEmployeeCode() + " đã tồn tại.");
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setEmployeeCode(req.getEmployeeCode());
        userInfo.setPhone(req.getPhone());
        userInfo.setDateOfBirth(req.getDateOfBirth());
        userInfo.setGender(req.getGender());
        userInfo.setStartDate(req.getStartDate());
        userInfo.setContractSignDate(req.getContractSignDate());
        userInfo.setContractExpireDate(req.getContractExpireDate());

        userInfo = userInfoRepository.save(userInfo);
        return convertToResDTO(userInfo);
    }

    // ======================================================
    // UPDATE
    // ======================================================
    public ResUserInfoDTO handleUpdate(String userId, ReqUserInfoDTO req) {

        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("Không tìm thấy user ID = " + userId);
        }

        UserInfo userInfo = userInfoRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IdInvalidException("User này chưa có thông tin cá nhân."));

        // ⭐ Fix null-safe: tránh NullPointerException nếu employeeCode cũ là null
        if (req.getEmployeeCode() != null
                && !req.getEmployeeCode().equals(userInfo.getEmployeeCode())
                && userInfoRepository.existsByEmployeeCode(req.getEmployeeCode())) {
            throw new IdInvalidException("Mã nhân viên " + req.getEmployeeCode() + " đã tồn tại.");
        }

        userInfo.setEmployeeCode(req.getEmployeeCode());
        userInfo.setPhone(req.getPhone());
        userInfo.setDateOfBirth(req.getDateOfBirth());
        userInfo.setGender(req.getGender());
        userInfo.setStartDate(req.getStartDate());
        userInfo.setContractSignDate(req.getContractSignDate());
        userInfo.setContractExpireDate(req.getContractExpireDate());

        userInfo = userInfoRepository.save(userInfo);
        return convertToResDTO(userInfo);
    }

    // ======================================================
    // GET BY USER
    // ======================================================
    public ResUserInfoDTO fetchByUserId(String userId) {

        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("Không tìm thấy user ID = " + userId);
        }

        UserInfo userInfo = userInfoRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IdInvalidException("User này chưa có thông tin cá nhân."));

        return convertToResDTO(userInfo);
    }

    // ======================================================
    // CONVERTER
    // ======================================================
    private ResUserInfoDTO convertToResDTO(UserInfo userInfo) {

        ResUserInfoDTO res = new ResUserInfoDTO();
        res.setId(userInfo.getId());
        res.setEmployeeCode(userInfo.getEmployeeCode());
        res.setPhone(userInfo.getPhone());
        res.setDateOfBirth(userInfo.getDateOfBirth());
        res.setGender(userInfo.getGender());
        res.setStartDate(userInfo.getStartDate());
        res.setContractSignDate(userInfo.getContractSignDate());
        res.setContractExpireDate(userInfo.getContractExpireDate());
        res.setCreatedAt(userInfo.getCreatedAt());
        res.setUpdatedAt(userInfo.getUpdatedAt());
        res.setCreatedBy(userInfo.getCreatedBy());
        res.setUpdatedBy(userInfo.getUpdatedBy());

        ResUserInfoDTO.UserBasic userBasic = new ResUserInfoDTO.UserBasic();
        userBasic.setId(userInfo.getUser().getId());
        userBasic.setName(userInfo.getUser().getName());
        userBasic.setEmail(userInfo.getUser().getEmail());
        res.setUser(userBasic);

        return res;
    }
}