package vn.system.app.modules.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.email.service.EmailService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.domain.request.ReqCreateUserDTO; // ⭐ THÊM
import vn.system.app.modules.user.domain.request.ReqUpdateProfileDTO;
import vn.system.app.modules.user.domain.request.ReqUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResCreateUserDTO;
import vn.system.app.modules.user.domain.response.ResUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResUserDTO;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.user.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {

        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final UserRepository userRepository;

        public UserController(UserService userService,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService,
                        UserRepository userRepository) {
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
                this.emailService = emailService;
                this.userRepository = userRepository;
        }

        // ===========================================================
        // GỬI MÃ XÁC NHẬN (CHO NGƯỜI MỚI HOẶC QUÊN MẬT KHẨU)
        // ===========================================================
        @PostMapping("/users/request-password-code")
        @ApiMessage("Gửi mã xác nhận để tạo hoặc đặt lại mật khẩu")
        public ResponseEntity<?> requestPasswordCode(@RequestBody Map<String, String> req) {
                String email = req.get("email");
                if (email == null || email.trim().isEmpty()) {
                        throw new IdInvalidException("Email không được để trống.");
                }

                User user = userService.handleGetUserByUsername(email);
                if (user == null) {
                        return ResponseEntity.ok(Map.of(
                                        "success", true,
                                        "mode", user.isActive() ? "reset" : "activate",
                                        "message", "Hệ thống đã gửi mã xác nhận."));
                }

                // Sinh mã 6 số
                String resetCode = String.format("%06d", (int) (Math.random() * 1000000));
                user.setResetCode(resetCode);
                user.setResetCodeExpire(Instant.now().plus(10, ChronoUnit.MINUTES));
                userService.save(user);

                boolean isActivateFlow = !user.isActive();

                if (isActivateFlow) {
                        emailService.sendTemplateEmail(
                                        user.getEmail(),
                                        "Kích hoạt tài khoản LotusGroup",
                                        Map.of(
                                                        "type", "activate",
                                                        "title", "Kích hoạt tài khoản LotusGroup",
                                                        "name", user.getName(),
                                                        "code", resetCode));
                } else {
                        emailService.sendTemplateEmail(
                                        user.getEmail(),
                                        "Xác nhận đặt lại mật khẩu - LotusGroup",
                                        Map.of(
                                                        "type", "reset",
                                                        "title", "Đặt lại mật khẩu LotusGroup",
                                                        "name", user.getName(),
                                                        "code", resetCode));
                }

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Hệ thống đã gửi mã xác nhận."));
        }

        // ===========================================================
        // XÁC NHẬN VÀ ĐẶT MẬT KHẨU MỚI
        // ===========================================================
        @PostMapping("/users/confirm-reset-password")
        @ApiMessage("Xác nhận mã và đặt mật khẩu mới")
        public ResponseEntity<?> confirmResetPassword(@RequestBody Map<String, String> req) {
                String email = req.get("email");
                String code = req.get("code");
                String newPassword = req.get("newPassword");

                if (email == null || code == null || newPassword == null) {
                        throw new IdInvalidException("Email, mã xác nhận và mật khẩu mới là bắt buộc.");
                }

                User user = userService.handleGetUserByUsername(email);
                if (user == null || user.getResetCode() == null) {
                        throw new IdInvalidException("Không tìm thấy yêu cầu đặt lại mật khẩu.");
                }

                if (!user.getResetCode().equals(code)) {
                        throw new IdInvalidException("Mã xác nhận không đúng.");
                }

                if (user.getResetCodeExpire().isBefore(Instant.now())) {
                        throw new IdInvalidException("Mã xác nhận đã hết hạn, vui lòng yêu cầu lại.");
                }

                if (newPassword.length() < 6) {
                        throw new IdInvalidException("Mật khẩu mới phải có ít nhất 6 ký tự.");
                }

                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetCode(null);
                user.setResetCodeExpire(null);
                if (!user.isActive()) {
                        user.setActive(true);
                }
                userRepository.save(user);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Đặt mật khẩu thành công."));
        }

        // ===========================================================
        // TẠO USER + USERINFO
        // ===========================================================
        @PostMapping("/users")
        @ApiMessage("Create a new user")
        public ResponseEntity<ResCreateUserDTO> createNewUser(
                        @Valid @RequestBody ReqCreateUserDTO req) { // ⭐ ĐỔI từ User → ReqCreateUserDTO

                // ⭐ Kiểm tra email trùng (validate sớm ở controller)
                if (userService.isEmailExist(req.getEmail())) {
                        throw new IdInvalidException("Email " + req.getEmail() + " đã tồn tại");
                }

                // ⭐ handleCreateUser đã xử lý: encode password, tạo UserInfo, trả về DTO
                ResCreateUserDTO res = userService.handleCreateUser(req);
                return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }

        // ===========================================================
        // DELETE USER
        // ===========================================================
        @DeleteMapping("/users/{id}")
        @ApiMessage("Delete a user")
        public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
                this.userService.handleDeleteUser(id);
                return ResponseEntity.ok().build();
        }

        // ===========================================================
        // GET USER BY ID
        // ===========================================================
        @GetMapping("/users/{id}")
        @ApiMessage("fetch user by id")
        public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) {
                User user = this.userService.fetchUserById(id);
                if (user == null) {
                        throw new IdInvalidException("User với id = " + id + " không tồn tại");
                }
                return ResponseEntity.ok(this.userService.convertToResUserDTO(user));
        }

        // ===========================================================
        // GET ALL USERS
        // ===========================================================
        @GetMapping("/users")
        @ApiMessage("fetch all users")
        public ResponseEntity<ResultPaginationDTO> getAllUser(
                        @Filter Specification<User> spec,
                        Pageable pageable) {
                return ResponseEntity.ok(this.userService.fetchAllUser(spec, pageable));
        }

        // ===========================================================
        // UPDATE USER
        // ===========================================================
        @PutMapping("/users")
        @ApiMessage("Update a user")
        public ResponseEntity<ResUpdateUserDTO> updateUser(
                        @RequestBody ReqUpdateUserDTO req) {

                User updated = this.userService.handleUpdateUser(req);
                return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(updated));
        }

        // ===========================================================
        // UPDATE PROFILE
        // ===========================================================
        @PutMapping("/users/profile")
        @ApiMessage("Cập nhật thông tin cá nhân")
        public ResponseEntity<ResUserDTO> updateProfile(
                        @Valid @RequestBody ReqUpdateProfileDTO req) {
                User updated = this.userService.updateProfile(req.getName(), req.getAvatar());
                return ResponseEntity.ok(this.userService.convertToResUserDTO(updated));
        }
}