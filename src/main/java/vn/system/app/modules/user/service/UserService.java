package vn.system.app.modules.user.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.repository.JobTitleRepository;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.role.service.RoleService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.domain.response.ResCreateUserDTO;
import vn.system.app.modules.user.domain.response.ResUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResUserDTO;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final JobTitleRepository jobTitleRepository;

    public UserService(UserRepository userRepository,
            RoleService roleService,
            JobTitleRepository jobTitleRepository) {

        this.userRepository = userRepository;
        this.roleService = roleService;
        this.jobTitleRepository = jobTitleRepository;
    }

    // ==========================================
    // CREATE USER
    // ==========================================
    public User handleCreateUser(User user) {

        if (user.getRole() != null) {
            Role r = this.roleService.fetchById(user.getRole().getId());
            user.setRole(r != null ? r : null);
        }

        return this.userRepository.save(user);
    }

    // ==========================================
    // DELETE USER
    // ==========================================
    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    // ==========================================
    // FIND USER
    // ==========================================
    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.orElse(null);
    }

    // ==========================================
    // FETCH ALL WITH PAGINATION
    // ==========================================
    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {
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

    // ==========================================
    // UPDATE USER
    // ==========================================
    public User handleUpdateUser(User reqUser) {
        User currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {

            currentUser.setAddress(reqUser.getAddress());
            currentUser.setAge(reqUser.getAge());
            currentUser.setName(reqUser.getName());

            if (reqUser.getRole() != null) {
                Role r = this.roleService.fetchById(reqUser.getRole().getId());
                currentUser.setRole(r != null ? r : null);
            }

            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    // ==========================================
    // LOGIN SUPPORT
    // ==========================================
    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    // ==========================================
    // TOKEN UPDATE
    // ==========================================
    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    // ==========================================
    // CONVERT DTO
    // ==========================================
    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setAddress(user.getAddress());

        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();

        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setAddress(user.getAddress());

        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        ResUserDTO.RoleUser roleUser = new ResUserDTO.RoleUser();

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            res.setRole(roleUser);
        }

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setAddress(user.getAddress());

        return res;
    }

    // ==========================================
    // ⭐ NEW — ASSIGN JOB TITLES TO USER
    // ==========================================
    @Transactional
    public void assignJobTitles(Long userId, List<Long> jobTitleIds) {

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy user ID = " + userId));

        List<JobTitle> titles = jobTitleRepository.findAllById(jobTitleIds);

        if (titles.isEmpty()) {
            throw new IdInvalidException("Danh sách jobTitleIds không hợp lệ.");
        }

        user.setJobTitles(titles);

        this.userRepository.save(user);
    }
}
