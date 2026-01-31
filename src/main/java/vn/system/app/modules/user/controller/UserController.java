package vn.system.app.modules.user.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.domain.request.ReqAssignJobTitle;
import vn.system.app.modules.user.domain.response.ResCreateUserDTO;
import vn.system.app.modules.user.domain.response.ResUpdateUserDTO;
import vn.system.app.modules.user.domain.response.ResUserDTO;
import vn.system.app.modules.user.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {

        private final UserService userService;
        private final PasswordEncoder passwordEncoder;

        public UserController(UserService userService, PasswordEncoder passwordEncoder) {
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
        }

        // ====================================================
        // CREATE USER
        // ====================================================
        @PostMapping("/users")
        @ApiMessage("Create a new user")
        public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postManUser)
                        throws IdInvalidException {

                boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
                if (isEmailExist) {
                        throw new IdInvalidException(
                                        "Email " + postManUser.getEmail()
                                                        + " đã tồn tại, vui lòng sử dụng email khác.");
                }

                String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
                postManUser.setPassword(hashPassword);

                User created = this.userService.handleCreateUser(postManUser);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(this.userService.convertToResCreateUserDTO(created));
        }

        // ====================================================
        // DELETE USER
        // ====================================================
        @DeleteMapping("/users/{id}")
        @ApiMessage("Delete a user")
        public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
                        throws IdInvalidException {

                User currentUser = this.userService.fetchUserById(id);
                if (currentUser == null) {
                        throw new IdInvalidException("User với id = " + id + " không tồn tại");
                }

                this.userService.handleDeleteUser(id);
                return ResponseEntity.ok(null);
        }

        // ====================================================
        // GET USER BY ID
        // ====================================================
        @GetMapping("/users/{id}")
        @ApiMessage("fetch user by id")
        public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id)
                        throws IdInvalidException {

                User user = this.userService.fetchUserById(id);
                if (user == null) {
                        throw new IdInvalidException("User với id = " + id + " không tồn tại");
                }

                return ResponseEntity.status(HttpStatus.OK)
                                .body(this.userService.convertToResUserDTO(user));
        }

        // ====================================================
        // GET ALL USERS
        // ====================================================
        @GetMapping("/users")
        @ApiMessage("fetch all users")
        public ResponseEntity<ResultPaginationDTO> getAllUser(
                        @Filter Specification<User> spec,
                        Pageable pageable) {

                return ResponseEntity.status(HttpStatus.OK)
                                .body(this.userService.fetchAllUser(spec, pageable));
        }

        // ====================================================
        // UPDATE USER
        // ====================================================
        @PutMapping("/users")
        @ApiMessage("Update a user")
        public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user)
                        throws IdInvalidException {

                User updated = this.userService.handleUpdateUser(user);
                if (updated == null) {
                        throw new IdInvalidException("User với id = " + user.getId() + " không tồn tại");
                }

                return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(updated));
        }

        // ====================================================
        // ⭐ ASSIGN JOB TITLES TO USER
        // ====================================================
        @PostMapping("/users/{userId}/job-titles")
        @ApiMessage("Gán chức danh cho user")
        public ResponseEntity<String> assignJobTitles(
                        @PathVariable Long userId,
                        @RequestBody ReqAssignJobTitle req)
                        throws IdInvalidException {

                this.userService.assignJobTitles(userId, req.getJobTitleIds());

                return ResponseEntity.ok("Gán chức danh thành công cho user ID = " + userId);
        }
}
