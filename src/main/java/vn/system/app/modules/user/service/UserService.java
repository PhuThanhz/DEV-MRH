package vn.system.app.modules.user.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.system.app.common.util.UserScopeContext;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.email.service.EmailService;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.role.service.RoleService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.domain.request.ReqCreateUserDTO;
import vn.system.app.modules.user.domain.request.ReqUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResCreateUserDTO;
import vn.system.app.modules.user.domain.response.ResUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResUserDTO;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userinfo.domain.UserInfo;
import vn.system.app.modules.userinfo.repository.UserInfoRepository;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserPositionRepository userPositionRepository; // ✅ THÊM
    private final RoleService roleService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserInfoRepository userInfoRepository,
            UserPositionRepository userPositionRepository, // ✅ THÊM
            RoleService roleService,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.userPositionRepository = userPositionRepository; // ✅ THÊM
        this.roleService = roleService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================================================
    // CREATE USER + USERINFO (Admin - UserController)
    // ======================================================
    public ResCreateUserDTO handleCreateUser(ReqCreateUserDTO req) {

        if (this.isEmailExist(req.getEmail())) {
            throw new IdInvalidException("Email " + req.getEmail() + " đã tồn tại");
        }

        if (req.getEmployeeCode() != null
                && userInfoRepository.existsByEmployeeCode(req.getEmployeeCode())) {
            throw new IdInvalidException("Mã nhân viên " + req.getEmployeeCode() + " đã tồn tại");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());

        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        user.setActive(true);

        if (req.getActive() != null) {
            user.setActive(req.getActive());
        }

        if (req.getRoleId() != null) {
            Role r = roleService.fetchById(req.getRoleId());
            user.setRole(r);
        }

        User savedUser = userRepository.save(user);

        boolean hasUserInfo = req.getEmployeeCode() != null
                || req.getPhone() != null
                || req.getDateOfBirth() != null
                || req.getGender() != null
                || req.getStartDate() != null
                || req.getContractSignDate() != null
                || req.getContractExpireDate() != null;

        UserInfo savedUserInfo = null;

        if (hasUserInfo) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUser(savedUser);
            userInfo.setEmployeeCode(req.getEmployeeCode());
            userInfo.setPhone(req.getPhone());
            userInfo.setDateOfBirth(req.getDateOfBirth());
            userInfo.setGender(req.getGender());
            userInfo.setStartDate(req.getStartDate());
            userInfo.setContractSignDate(req.getContractSignDate());
            userInfo.setContractExpireDate(req.getContractExpireDate());
            savedUserInfo = userInfoRepository.save(userInfo);
        }

        return convertToResCreateUserDTO(savedUser, savedUserInfo);
    }

    // ======================================================
    // CREATE USER đơn giản (AuthController - /auth/register)
    // ======================================================
    public User handleCreateUser(User user) {

        if (user.getRole() != null) {
            Role r = this.roleService.fetchById(user.getRole().getId());
            user.setRole(r != null ? r : null);
        }

        user.setActive(true); // luôn luôn active

        return this.userRepository.save(user);
    }

    // ======================================================
    // SAVE USER
    // ======================================================
    public User save(User user) {
        return this.userRepository.save(user);
    }

    // ======================================================
    // DELETE USER
    // ======================================================
    public void handleDeleteUser(long id) {

        if (!userRepository.existsById(id)) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        this.userRepository.deleteById(id);
    }

    // ======================================================
    // FIND USER
    // ======================================================
    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.orElse(null);
    }

    // ======================================================
    // FETCH ALL WITH PAGINATION
    // ======================================================
    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {

        // ── ADMIN_SUB_2: filter user theo company ──────────────
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && !scope.isSuperAdmin()) {

            if (scope.companyIds().isEmpty()) {
                // Không thuộc công ty nào → trả về rỗng
                ResultPaginationDTO rs = new ResultPaginationDTO();
                ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
                mt.setPage(pageable.getPageNumber() + 1);
                mt.setPageSize(pageable.getPageSize());
                mt.setPages(0);
                mt.setTotal(0);
                rs.setMeta(mt);
                rs.setResult(List.of());
                return rs;
            }

            // Filter: chỉ lấy user thuộc company của admin này
            // User không có company trực tiếp → đi qua UserPosition
            Specification<User> scopeSpec = (root, query, cb) -> {

                var sub = query.subquery(Long.class);
                var posRoot = sub.from(UserPosition.class);

                sub.select(posRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.isTrue(posRoot.get("active")),
                                cb.or(
                                        cb.and(
                                                cb.equal(posRoot.get("source"), "COMPANY"),
                                                posRoot.get("companyJobTitle").get("company").get("id")
                                                        .in(scope.companyIds())),
                                        cb.and(
                                                cb.equal(posRoot.get("source"), "DEPARTMENT"),
                                                posRoot.get("departmentJobTitle").get("department").get("company")
                                                        .get("id")
                                                        .in(scope.companyIds())),
                                        cb.and(
                                                cb.equal(posRoot.get("source"), "SECTION"),
                                                posRoot.get("sectionJobTitle").get("section").get("department")
                                                        .get("company").get("id")
                                                        .in(scope.companyIds())))));

                return root.get("id").in(sub);
            };

            spec = Specification.where(spec).and(scopeSpec);
        }
        // ── HẾT FILTER ────────────────────────────────────────

        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());
        rs.setMeta(mt);

        List<ResUserDTO> listUser = pageUser.getContent()
                .stream()
                .map(this::convertToResUserDTO)
                .collect(Collectors.toList());

        rs.setResult(listUser);
        return rs;
    }

    // ======================================================
    // UPDATE USER (ADMIN)
    // ======================================================
    public User handleUpdateUser(ReqUpdateUserDTO req) {

        User currentUser = this.fetchUserById(req.getId());

        if (currentUser == null) {
            throw new IdInvalidException("User với id = " + req.getId() + " không tồn tại");
        }

        // update name
        if (req.getName() != null) {
            currentUser.setName(req.getName());
        }

        // update active
        if (req.getActive() != null) {
            currentUser.setActive(req.getActive());

            if (!req.getActive()) {
                currentUser.setRefreshToken(null);
            }
        }

        // ⭐ QUAN TRỌNG NHẤT — update role
        if (req.getRoleId() != null) {
            Role r = this.roleService.fetchById(req.getRoleId());
            currentUser.setRole(r);
        }

        User saved = this.userRepository.save(currentUser);
        // ================= USER INFO =================
        UserInfo userInfo = userInfoRepository.findByUser_Id(currentUser.getId())
                .orElse(null);

        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUser(currentUser);
        }
        // update field
        userInfo.setEmployeeCode(req.getEmployeeCode());
        userInfo.setPhone(req.getPhone());
        userInfo.setDateOfBirth(req.getDateOfBirth());
        userInfo.setGender(req.getGender());
        userInfo.setStartDate(req.getStartDate());
        userInfo.setContractSignDate(req.getContractSignDate());
        userInfo.setContractExpireDate(req.getContractExpireDate());

        userInfoRepository.save(userInfo);
        // load lại để lấy userInfo
        User fullUser = this.userRepository.findWithUserInfoById(saved.getId());

        return fullUser;
    }

    // ======================================================
    // UPDATE PROFILE (USER TỰ CẬP NHẬT)
    // ======================================================
    public User getCurrentUser() {

        String email = SecurityUtil.getCurrentUserLogin().orElse(null);

        if (email == null) {
            return null;
        }

        return this.userRepository.findByEmail(email);
    }

    public User updateProfile(String name, String avatar) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new IdInvalidException("Không tìm thấy người dùng hiện tại");
        }

        currentUser.setName(name);
        currentUser.setAvatar(avatar);

        return this.userRepository.save(currentUser);
    }

    // ======================================================
    // LOGIN SUPPORT
    // ======================================================
    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    // ======================================================
    // TOKEN UPDATE
    // ======================================================
    public void updateUserToken(String token, String email) {

        User currentUser = this.handleGetUserByUsername(email);

        if (currentUser == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        currentUser.setRefreshToken(token);
        this.userRepository.save(currentUser);
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    // ======================================================
    // CONVERT DTO
    // ======================================================
    public ResCreateUserDTO convertToResCreateUserDTO(User user, UserInfo userInfo) {

        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setActive(user.isActive());
        res.setCreatedAt(user.getCreatedAt());

        if (userInfo != null) {
            ResCreateUserDTO.UserInfoBasic info = new ResCreateUserDTO.UserInfoBasic();
            info.setEmployeeCode(userInfo.getEmployeeCode());
            info.setPhone(userInfo.getPhone());
            info.setDateOfBirth(userInfo.getDateOfBirth());
            info.setGender(userInfo.getGender());
            info.setStartDate(userInfo.getStartDate());
            info.setContractSignDate(userInfo.getContractSignDate());
            info.setContractExpireDate(userInfo.getContractExpireDate());
            res.setUserInfo(info);
        }

        return res;
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        return convertToResCreateUserDTO(user, null);
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {

        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setActive(user.isActive());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {

        ResUserDTO res = new ResUserDTO();

        if (user.getRole() != null) {
            ResUserDTO.RoleUser roleUser = new ResUserDTO.RoleUser();
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            res.setRole(roleUser);
        }

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setActive(user.isActive());
        res.setAvatar(user.getAvatar());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setLastLoginAt(user.getLastLoginAt());
        res.setLastLoginIp(user.getLastLoginIp());
        // ⭐ Dùng thẳng từ entity đã JOIN FETCH — không query thêm
        UserInfo info = user.getUserInfo();
        if (info != null) {
            ResUserDTO.UserInfoBasic userInfoBasic = new ResUserDTO.UserInfoBasic();
            userInfoBasic.setEmployeeCode(info.getEmployeeCode());
            userInfoBasic.setPhone(info.getPhone());
            userInfoBasic.setDateOfBirth(info.getDateOfBirth());
            userInfoBasic.setGender(info.getGender());
            userInfoBasic.setStartDate(info.getStartDate());
            userInfoBasic.setContractSignDate(info.getContractSignDate());
            userInfoBasic.setContractExpireDate(info.getContractExpireDate());
            res.setUserInfo(userInfoBasic);
        }
        // ✅ THÊM ĐOẠN NÀY VÀO CUỐI convertToResUserDTO, TRƯỚC return res;
        List<UserPosition> posList = userPositionRepository
                .findByUser_IdAndActiveTrue(user.getId());

        if (posList != null && !posList.isEmpty()) {
            List<ResUserDTO.PositionBasic> positionBasics = posList.stream()
                    .map(p -> {
                        ResUserDTO.PositionBasic pb = new ResUserDTO.PositionBasic();
                        pb.setId(p.getId());
                        pb.setSource(p.getSource());

                        switch (p.getSource().toUpperCase()) {
                            case "COMPANY" -> {
                                pb.setCompanyName(p.getCompanyJobTitle().getCompany().getName());
                                pb.setJobTitleNameVi(p.getCompanyJobTitle().getJobTitle().getNameVi());
                            }
                            case "DEPARTMENT" -> {
                                pb.setCompanyName(p.getDepartmentJobTitle().getDepartment().getCompany().getName());
                                pb.setDepartmentName(p.getDepartmentJobTitle().getDepartment().getName());
                                pb.setJobTitleNameVi(p.getDepartmentJobTitle().getJobTitle().getNameVi());
                            }
                            case "SECTION" -> {
                                pb.setCompanyName(
                                        p.getSectionJobTitle().getSection().getDepartment().getCompany().getName());
                                pb.setDepartmentName(p.getSectionJobTitle().getSection().getDepartment().getName());
                                pb.setSectionName(p.getSectionJobTitle().getSection().getName());
                                pb.setJobTitleNameVi(p.getSectionJobTitle().getJobTitle().getNameVi());
                            }
                        }
                        return pb;
                    })
                    .collect(Collectors.toList());

            res.setPositions(positionBasics);
        }
        // ✅ HẾT PHẦN THÊM
        return res;
    }

    // ======================================================
    // CHANGE PASSWORD
    // ======================================================
    // ⭐ Giữ lại PasswordEncoder trong tham số — AuthController đang truyền vào
    public void changePassword(String oldPassword, String newPassword, PasswordEncoder passwordEncoder) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new IdInvalidException("Không tìm thấy người dùng");
        }

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new IdInvalidException("Mật khẩu cũ không chính xác");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(currentUser);
    }
}