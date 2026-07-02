package vn.system.app.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.permission.domain.Permission;
import vn.system.app.modules.permission.repository.PermissionRepository;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.role.repository.RoleRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.documentcategory.repository.DocumentCategoryRepository;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentCategoryRepository documentCategoryRepository;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DocumentCategoryRepository documentCategoryRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.documentCategoryRepository = documentCategoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        // Kiểm tra và khởi tạo danh mục chứng từ kế toán
        DocumentCategory accCategory = null;
        try {
            accCategory = this.documentCategoryRepository.findByCategoryCode("ACCOUNTING_DOC").orElse(null);
        } catch (Exception e) {}

        if (accCategory == null) {
            accCategory = new DocumentCategory();
            accCategory.setCategoryCode("ACCOUNTING_DOC");
            accCategory.setCategoryName("Chứng từ Kế toán");
            accCategory.setSymbol("CTKT");
            accCategory.setDefinition("Danh mục dùng riêng cho kho lưu trữ chứng từ của phòng Kế toán");
            accCategory.setActive(true);
            accCategory.setMappingProcedure(false);
            accCategory.setCrossCompany(false);
            this.documentCategoryRepository.save(accCategory);
            System.out.println(">>> SEED: ACCOUNTING_DOC Category created");
        }

        if (countPermissions == 0) {
            ArrayList<Permission> arr = new ArrayList<>();
            this.permissionRepository.saveAll(arr);
        }

        // Tự động seed quyền cho ACCOUNTING_DOCUMENTS
        String accModule = "ACCOUNTING_DOCUMENTS";
        List<Permission> existingAccPerms = this.permissionRepository.findAll().stream()
                .filter(p -> accModule.equals(p.getModule()))
                .toList();
        
        if (existingAccPerms.isEmpty()) {
            List<Permission> newPerms = new ArrayList<>();
            newPerms.add(createPermission("Danh sách chứng từ kế toán", "/api/v1/accounting-documents", "GET", accModule));
            newPerms.add(createPermission("Chi tiết chứng từ kế toán", "/api/v1/accounting-documents/{id}", "GET", accModule));
            newPerms.add(createPermission("Tạo mới chứng từ kế toán", "/api/v1/accounting-documents", "POST", accModule));
            newPerms.add(createPermission("Cập nhật chứng từ kế toán", "/api/v1/accounting-documents/{id}", "PUT", accModule));
            newPerms.add(createPermission("Xóa chứng từ kế toán", "/api/v1/accounting-documents/{id}", "DELETE", accModule));
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: ACCOUNTING_DOCUMENTS permissions created");
            
            // Cập nhật lại adminRole nếu nó đã tồn tại
            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                List<Permission> allPerms = new ArrayList<>(adminRole.getPermissions());
                allPerms.addAll(newPerms);
                adminRole.setPermissions(allPerms);
                this.roleRepository.save(adminRole);
            }
        }

        String dossierModule = "ACCOUNTING_DOSSIERS";
        List<Permission> newPerms = new ArrayList<>();
        addPermissionIfMissing(newPerms, "Danh sách bộ chứng từ kế toán", "/api/v1/accounting-dossiers", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Chi tiết bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Tạo mới bộ chứng từ kế toán", "/api/v1/accounting-dossiers", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Cập nhật bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}", "PUT", dossierModule);
        addPermissionIfMissing(newPerms, "Xóa bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}", "DELETE", dossierModule);
        addPermissionIfMissing(newPerms, "Chuyển xử lý bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/submit", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Yêu cầu hoàn bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/request-return", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Xem lịch sử bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/logs", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách mẫu bộ chứng từ kế toán", "/api/v1/accounting-dossiers/categories", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách mẫu bộ chứng từ kế toán đang dùng", "/api/v1/accounting-dossiers/categories/active", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Tạo mẫu bộ chứng từ kế toán", "/api/v1/accounting-dossiers/categories", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Cập nhật mẫu bộ chứng từ kế toán", "/api/v1/accounting-dossiers/categories/{categoryId}", "PUT", dossierModule);
        addPermissionIfMissing(newPerms, "Bật/tắt mẫu bộ chứng từ kế toán", "/api/v1/accounting-dossiers/categories/{categoryId}/active", "PUT", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Thêm chứng từ con vào bộ", "/api/v1/accounting-dossiers/{id}/documents", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Sửa chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}", "PUT", dossierModule);
        addPermissionIfMissing(newPerms, "Xóa chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}", "DELETE", dossierModule);

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: ACCOUNTING_DOSSIERS permissions created");

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                List<Permission> allPerms = new ArrayList<>(adminRole.getPermissions());
                allPerms.addAll(newPerms);
                adminRole.setPermissions(allPerms);
                this.roleRepository.save(adminRole);
            }
        }

        seedAdminScopePermissionsAndRole();
        syncFullPermissionRoles();

        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);

            this.roleRepository.save(adminRole);
        }

        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setName("I'm super admin");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));
            adminUser.setActive(true); // ✅ FIX CHÍNH Ở ĐÂY

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            this.userRepository.save(adminUser);
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else
            System.out.println(">>> END INIT DATABASE");
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission p = new Permission();
        p.setName(name);
        p.setApiPath(apiPath);
        p.setMethod(method);
        p.setModule(module);
        return p;
    }

    private void addPermissionIfMissing(List<Permission> target, String name, String apiPath, String method, String module) {
        if (!this.permissionRepository.existsByModuleAndApiPathAndMethod(module, apiPath, method)) {
            target.add(createPermission(name, apiPath, method, module));
        }
    }

    private void seedAdminScopePermissionsAndRole() {
        List<Permission> newPerms = new ArrayList<>();
        addPermissionIfMissing(newPerms, "Danh sách phạm vi quản trị user",
                "/api/v1/users/{userId}/admin-scopes", "GET", "USERS");
        addPermissionIfMissing(newPerms, "Cập nhật phạm vi quản trị user",
                "/api/v1/users/{userId}/admin-scopes", "PUT", "USERS");

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: USER ADMIN SCOPES permissions created");

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                List<Permission> allPerms = new ArrayList<>(adminRole.getPermissions());
                allPerms.addAll(newPerms);
                adminRole.setPermissions(allPerms);
                this.roleRepository.save(adminRole);
            }
        }

        List<Permission> adminScopePermissions = this.permissionRepository.findAll().stream()
                .filter(p -> "USERS".equals(p.getModule()))
                .filter(p -> "/api/v1/users/{userId}/admin-scopes".equals(p.getApiPath()))
                .filter(p -> "GET".equalsIgnoreCase(p.getMethod()) || "PUT".equalsIgnoreCase(p.getMethod()))
                .toList();

        addPermissionsToRoleIfMissing("SUPER_ADMIN", adminScopePermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", adminScopePermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", adminScopePermissions);

        Role departmentManagerRole = this.roleRepository.findByName("DEPARTMENT_MANAGER");
        if (departmentManagerRole == null) {
            departmentManagerRole = new Role();
            departmentManagerRole.setName("DEPARTMENT_MANAGER");
            departmentManagerRole.setActive(true);
            departmentManagerRole.setDescription("Trưởng bộ phận - chỉ quản lý phòng ban trực thuộc chức danh active");
            this.roleRepository.save(departmentManagerRole);
        } else if (departmentManagerRole.getDescription() == null) {
            departmentManagerRole.setDescription("Trưởng bộ phận - chỉ quản lý phòng ban trực thuộc chức danh active");
            this.roleRepository.save(departmentManagerRole);
        }

        List<Permission> managerPermissions = new ArrayList<>(this.permissionRepository.findAll().stream()
                .filter(p -> "USERS".equals(p.getModule())
                        || "DEPARTMENTS".equals(p.getModule())
                        || "SECTIONS".equals(p.getModule())
                        || "DASHBOARD".equals(p.getModule())
                        || "DEPARTMENT_OBJECTIVES".equals(p.getModule())
                        || "DEPARTMENT_JOB_TITLES".equals(p.getModule())
                        || "USER_POSITION".equals(p.getModule()))
                .filter(p -> "GET".equalsIgnoreCase(p.getMethod()))
                .filter(p -> !"/api/v1/users/{userId}/admin-scopes".equals(p.getApiPath()))
                .toList());

        addPermissionsToRoleIfMissing("DEPARTMENT_MANAGER", managerPermissions);

        Role adminSub3Role = this.roleRepository.findByName("ADMIN_SUB_3");
        if (adminSub3Role == null) {
            adminSub3Role = new Role();
            adminSub3Role.setName("ADMIN_SUB_3");
            adminSub3Role.setActive(true);
            adminSub3Role.setDescription("Quản trị viên cấp Phòng ban - quản trị danh sách phòng ban được gán");
            this.roleRepository.save(adminSub3Role);
        } else if (adminSub3Role.getDescription() == null) {
            adminSub3Role.setDescription("Quản trị viên cấp Phòng ban - quản trị danh sách phòng ban được gán");
            this.roleRepository.save(adminSub3Role);
        }
        addPermissionsToRoleIfMissing("ADMIN_SUB_3", managerPermissions);
    }

    private void syncFullPermissionRoles() {
        List<Permission> allPermissions = this.permissionRepository.findAll();
        addPermissionsToRoleIfMissing("SUPER_ADMIN", allPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", allPermissions);
    }

    private void addPermissionsToRoleIfMissing(String roleName, List<Permission> permissions) {
        Role role = this.roleRepository.findByName(roleName);
        if (role == null || permissions == null || permissions.isEmpty()) {
            return;
        }

        List<Permission> currentPermissions = role.getPermissions() != null
                ? new ArrayList<>(role.getPermissions())
                : new ArrayList<>();

        boolean changed = false;
        for (Permission permission : permissions) {
            boolean exists = currentPermissions.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(item -> item.getId() == permission.getId());
            if (!exists) {
                currentPermissions.add(permission);
                changed = true;
            }
        }

        if (changed) {
            role.setPermissions(currentPermissions);
            this.roleRepository.save(role);
        }
    }
}
