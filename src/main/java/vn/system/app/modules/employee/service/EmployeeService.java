package vn.system.app.modules.employee.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.employee.domain.request.ReqCreateEmployeeDTO;
import vn.system.app.modules.employee.domain.request.ReqUpdateEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResCreateEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResUpdateEmployeeDTO;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.role.service.RoleService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userinfo.domain.UserInfo;
import vn.system.app.modules.userinfo.repository.UserInfoRepository;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserPositionRepository userPositionRepository;
    private final RoleService roleService;

    public EmployeeService(
            UserRepository userRepository,
            UserInfoRepository userInfoRepository,
            UserPositionRepository userPositionRepository,
            RoleService roleService) {
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.userPositionRepository = userPositionRepository;
        this.roleService = roleService;
    }

    // ======================================================
    // CREATE EMPLOYEE
    // ======================================================
    public ResCreateEmployeeDTO create(ReqCreateEmployeeDTO req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IdInvalidException("Email đã tồn tại");
        }

        if (req.getEmployeeCode() != null
                && userInfoRepository.existsByEmployeeCode(req.getEmployeeCode())) {
            throw new IdInvalidException("Mã nhân viên đã tồn tại");
        }

        // ===== USER =====
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setActive(req.getActive() != null ? req.getActive() : false);
        Role role = roleService.findByName("EMPLOYEE");
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // ===== USER INFO =====
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(savedUser);
        userInfo.setEmployeeCode(req.getEmployeeCode());
        userInfo.setPhone(req.getPhone());
        userInfo.setDateOfBirth(req.getDateOfBirth());
        userInfo.setGender(req.getGender());
        userInfo.setStartDate(req.getStartDate());
        userInfo.setContractSignDate(req.getContractSignDate());
        userInfo.setContractExpireDate(req.getContractExpireDate());

        UserInfo savedInfo = userInfoRepository.save(userInfo);

        return mapToCreateDTO(savedUser, savedInfo);
    }

    // ======================================================
    // UPDATE EMPLOYEE
    // ======================================================
    public ResUpdateEmployeeDTO update(ReqUpdateEmployeeDTO req) {

        User user = userRepository.findById(req.getId())
                .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

        // ===== USER =====
        if (req.getName() != null) {
            user.setName(req.getName());
        }

        if (req.getActive() != null) {
            user.setActive(req.getActive());
        }

        userRepository.save(user);

        // ===== USER INFO =====
        UserInfo userInfo = userInfoRepository.findByUser_Id(user.getId())
                .orElse(null);

        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUser(user);
        }

        userInfo.setPhone(req.getPhone());
        userInfo.setDateOfBirth(req.getDateOfBirth());
        userInfo.setGender(req.getGender());
        userInfo.setStartDate(req.getStartDate());
        userInfo.setContractSignDate(req.getContractSignDate());
        userInfo.setContractExpireDate(req.getContractExpireDate());

        userInfoRepository.save(userInfo);

        return mapToUpdateDTO(user);
    }

    // ======================================================
    // DELETE EMPLOYEE
    // ======================================================
    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new IdInvalidException("User không tồn tại");
        }
        userRepository.deleteById(id);
    }

    // ======================================================
    // GET BY ID
    // ======================================================
    public ResEmployeeDTO getById(String id) {

        User user = userRepository.findWithUserInfoById(id);

        if (user == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        return mapToDTO(user);
    }

    // ======================================================
    // GET ALL (PAGINATION)
    // ======================================================
    public ResultPaginationDTO getAll(Specification<User> spec, Pageable pageable) {

        // ── ADMIN_SUB_2 filter ────────────────────────────────
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && !scope.isSuperAdmin()) {
            if (scope.companyIds().isEmpty()) {
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
            Specification<User> scopeSpec = (root, query, cb) -> {
                var sub = query.subquery(Long.class);
                var posRoot = sub.from(UserPosition.class);
                sub.select(posRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.isTrue(posRoot.get("active")),
                                cb.or(
                                        cb.and(cb.equal(posRoot.get("source"), "COMPANY"),
                                                posRoot.get("companyJobTitle").get("company").get("id")
                                                        .in(scope.companyIds())),
                                        cb.and(cb.equal(posRoot.get("source"), "DEPARTMENT"),
                                                posRoot.get("departmentJobTitle").get("department").get("company")
                                                        .get("id").in(scope.companyIds())),
                                        cb.and(cb.equal(posRoot.get("source"), "SECTION"),
                                                posRoot.get("sectionJobTitle").get("section").get("department")
                                                        .get("company").get("id").in(scope.companyIds())))));
                return root.get("id").in(sub);
            };
            spec = Specification.where(spec).and(scopeSpec); // ← gộp vào spec gốc
        }
        // ── HẾT FILTER ───────────────────────────────────────

        // specWithJoin bọc bên ngoài, bao gồm cả scopeSpec
        Specification<User> finalSpec = spec;
        Specification<User> specWithJoin = (root, query, cb) -> {
            root.join("userInfo", jakarta.persistence.criteria.JoinType.LEFT);
            query.distinct(true);
            return finalSpec == null ? null : finalSpec.toPredicate(root, query, cb);
        };

        Page<User> page = userRepository.findAll(specWithJoin, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);

        List<ResEmployeeDTO> data = page.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        rs.setResult(data);

        return rs;
    }

    // ======================================================
    // MAPPING
    // ======================================================
    private ResEmployeeDTO mapToDTO(User user) {

        ResEmployeeDTO dto = new ResEmployeeDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // ===== ROLE =====
        if (user.getRole() != null) {
            ResEmployeeDTO.RoleBasic role = new ResEmployeeDTO.RoleBasic();
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            dto.setRole(role);
        }

        // ===== USER INFO =====
        UserInfo info = user.getUserInfo();
        if (info != null) {
            ResEmployeeDTO.UserInfoBasic ui = new ResEmployeeDTO.UserInfoBasic();
            ui.setEmployeeCode(info.getEmployeeCode());
            ui.setPhone(info.getPhone());
            ui.setDateOfBirth(info.getDateOfBirth());
            ui.setGender(info.getGender() != null ? info.getGender().name() : null);
            ui.setStartDate(info.getStartDate());
            ui.setContractSignDate(info.getContractSignDate());
            ui.setContractExpireDate(info.getContractExpireDate());
            dto.setUserInfo(ui);
        }

        // ===== POSITIONS =====
        List<UserPosition> positions = userPositionRepository
                .findByUser_IdAndActiveTrue(user.getId());

        if (positions != null && !positions.isEmpty()) {
            dto.setPositions(
                    positions.stream().map(p -> {
                        ResEmployeeDTO.PositionBasic pb = new ResEmployeeDTO.PositionBasic();
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
                    }).collect(Collectors.toList()));
        }

        return dto;
    }

    private ResCreateEmployeeDTO mapToCreateDTO(User user, UserInfo info) {
        ResCreateEmployeeDTO dto = new ResCreateEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());

        if (info != null) {
            ResCreateEmployeeDTO.UserInfoBasic ui = new ResCreateEmployeeDTO.UserInfoBasic();
            ui.setEmployeeCode(info.getEmployeeCode());
            ui.setPhone(info.getPhone());
            dto.setUserInfo(ui);
        }

        return dto;
    }

    private ResUpdateEmployeeDTO mapToUpdateDTO(User user) {
        ResUpdateEmployeeDTO dto = new ResUpdateEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setActive(user.isActive());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}