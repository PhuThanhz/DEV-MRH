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
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentCategoryRepository documentCategoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DocumentCategoryRepository documentCategoryRepository,
            JdbcTemplate jdbcTemplate) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.documentCategoryRepository = documentCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
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
        addPermissionIfMissing(newPerms, "Kiểm tra chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}/check", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách bộ chứng từ chờ tôi duyệt", "/api/v1/accounting-dossiers/pending-my-approval", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Phê duyệt bước bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/approve", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Từ chối bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/reject", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Chấm dứt bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/terminate", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Đưa bộ chứng từ kế toán vào lưu trữ", "/api/v1/accounting-dossiers/{id}/archive", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Phản hồi yêu cầu hoàn bộ chứng từ", "/api/v1/accounting-dossiers/{id}/return-response", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Từ chối đồng bộ bộ chứng từ phi cấu trúc thành mẫu", "/api/v1/accounting-dossiers/{id}/sync-template/reject", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách tiến trình duyệt bộ chứng từ", "/api/v1/accounting-dossiers/{id}/approval-steps", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Duyệt hàng loạt bộ chứng từ kế toán", "/api/v1/accounting-dossiers/bulk-approve", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Duyệt hàng loạt bộ chứng từ kế toán (lộ trình)", "/api/v1/accounting-dossiers/bulk/approve", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Từ chối hàng loạt bộ chứng từ kế toán", "/api/v1/accounting-dossiers/bulk/reject", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Kiểm tra hàng loạt chứng từ con", "/api/v1/accounting-dossiers/{id}/documents/bulk-check", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Kiểm tra hàng loạt chứng từ con (lộ trình)", "/api/v1/accounting-dossiers/{id}/documents/bulk/check", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Cập nhật trạng thái hết thời hạn lưu trữ", "/api/v1/accounting-dossiers/storage/refresh-expired", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Tổng quan lưu trữ bộ chứng từ kế toán", "/api/v1/accounting-dossiers/dashboard/summary", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Thống kê bộ chứng từ đang chờ duyệt theo vai trò", "/api/v1/accounting-dossiers/dashboard/pending-by-role", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Báo cáo bộ chứng từ theo trạng thái", "/api/v1/accounting-dossiers/reports/by-status", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Báo cáo bộ chứng từ theo phòng ban", "/api/v1/accounting-dossiers/reports/by-department", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Báo cáo bộ chứng từ theo danh mục", "/api/v1/accounting-dossiers/reports/by-category", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Tra cứu bộ chứng từ qua QR", "/api/v1/accounting-dossiers/qr/{token}", "GET", dossierModule);


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

        // Ensure KETOAN role and user exist
        Role ketoanRole = this.roleRepository.findByName("KETOAN");
        if (ketoanRole == null) {
            ketoanRole = new Role();
            ketoanRole.setName("KETOAN");
            ketoanRole.setDescription("Kế toán viên");
            ketoanRole.setActive(true);
            List<Permission> allPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> "ACCOUNTING_DOSSIERS".equals(p.getModule()) || "ACCOUNTING_DOCUMENTS".equals(p.getModule()))
                    .toList();
            ketoanRole.setPermissions(allPerms);
            ketoanRole = this.roleRepository.save(ketoanRole);
        }

        User ketoanUser = this.userRepository.findByEmail("ketoan@gmail.com");
        if (ketoanUser == null) {
            ketoanUser = new User();
            ketoanUser.setEmail("ketoan@gmail.com");
            ketoanUser.setName("Kế Toán Viên Test");
            ketoanUser.setPassword(this.passwordEncoder.encode("123456"));
            ketoanUser.setActive(true);
            ketoanUser.setRole(ketoanRole);
            this.userRepository.save(ketoanUser);
        }

        // Ensure KETOANTRUONG role and user exist
        Role ketoanTruongRole = this.roleRepository.findByName("KETOANTRUONG");
        if (ketoanTruongRole == null) {
            ketoanTruongRole = new Role();
            ketoanTruongRole.setName("KETOANTRUONG");
            ketoanTruongRole.setDescription("Kế toán trưởng");
            ketoanTruongRole.setActive(true);
            List<Permission> allPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> "ACCOUNTING_DOSSIERS".equals(p.getModule()) || "ACCOUNTING_DOCUMENTS".equals(p.getModule()))
                    .toList();
            ketoanTruongRole.setPermissions(allPerms);
            ketoanTruongRole = this.roleRepository.save(ketoanTruongRole);
        }

        User ketoanTruongUser = this.userRepository.findByEmail("ketoantruong@gmail.com");
        if (ketoanTruongUser == null) {
            ketoanTruongUser = new User();
            ketoanTruongUser.setEmail("ketoantruong@gmail.com");
            ketoanTruongUser.setName("Kế Toán Trưởng Test");
            ketoanTruongUser.setPassword(this.passwordEncoder.encode("123456"));
            ketoanTruongUser.setActive(true);
            ketoanTruongUser.setRole(ketoanTruongRole);
            this.userRepository.save(ketoanTruongUser);
        }

        // Ensure EMPLOYEE and DEPARTMENT_MANAGER roles have accounting permissions
        Role employeeRole = this.roleRepository.findByName("EMPLOYEE");
        if (employeeRole != null) {
            // Keep non-accounting permissions
            List<Permission> nonAccPerms = employeeRole.getPermissions().stream()
                    .filter(p -> !"ACCOUNTING_DOSSIERS".equals(p.getModule()) && !"ACCOUNTING_DOCUMENTS".equals(p.getModule()))
                    .toList();
            
            // Get restricted accounting permissions for EMPLOYEE
            List<Permission> allowedAccPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> "ACCOUNTING_DOSSIERS".equals(p.getModule()) || "ACCOUNTING_DOCUMENTS".equals(p.getModule()))
                    .filter(p -> {
                        String path = p.getApiPath();
                        // Filter out approval, template, and report paths
                        if (path.contains("/approve") || path.contains("/reject") || path.contains("/terminate") || path.contains("/archive") || path.contains("/bulk")) {
                            return false;
                        }
                        if (path.equals("/api/v1/accounting-dossiers/categories") && "POST".equalsIgnoreCase(p.getMethod())) {
                            return false;
                        }
                        if (path.startsWith("/api/v1/accounting-dossiers/categories/{categoryId}")) {
                            return false;
                        }
                        if (path.contains("/dashboard/") || path.contains("/reports/")) {
                            return false;
                        }
                        return true;
                    })
                    .toList();
                    
            List<Permission> finalPerms = new ArrayList<>(nonAccPerms);
            finalPerms.addAll(allowedAccPerms);
            employeeRole.setPermissions(finalPerms);
            this.roleRepository.save(employeeRole);
        }

        Role deptMgrRole = this.roleRepository.findByName("DEPARTMENT_MANAGER");
        if (deptMgrRole != null) {
            List<Permission> dossierPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> "ACCOUNTING_DOSSIERS".equals(p.getModule()) || "ACCOUNTING_DOCUMENTS".equals(p.getModule()))
                    .toList();
            List<Permission> currentPerms = new ArrayList<>(deptMgrRole.getPermissions());
            for (Permission p : dossierPerms) {
                if (!currentPerms.contains(p)) {
                    currentPerms.add(p);
                }
            }
            deptMgrRole.setPermissions(currentPerms);
            this.roleRepository.save(deptMgrRole);
        }

        // Create manager@gmail.com
        User managerUser = this.userRepository.findByEmail("manager@gmail.com");
        if (managerUser == null) {
            managerUser = new User();
            managerUser.setEmail("manager@gmail.com");
            managerUser.setName("Trưởng Bộ Phận Test");
            managerUser.setPassword(this.passwordEncoder.encode("123456"));
            managerUser.setActive(true);
            managerUser.setRole(deptMgrRole);
            managerUser = this.userRepository.save(managerUser);
        }

        // Create creator@gmail.com and link to manager
        User creatorUser = this.userRepository.findByEmail("creator@gmail.com");
        if (creatorUser == null) {
            creatorUser = new User();
            creatorUser.setEmail("creator@gmail.com");
            creatorUser.setName("Nhân Viên Lập Test");
            creatorUser.setPassword(this.passwordEncoder.encode("123456"));
            creatorUser.setActive(true);
            creatorUser.setRole(employeeRole);
            creatorUser.setDirectManager(managerUser);
            this.userRepository.save(creatorUser);
        }

        seedDepartmentObjectivePermissionsAndRole();
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

        // Link UserInfo and UserPositions for test accounts using JdbcTemplate to prevent circular entity dependencies
        try {
            // Check if user_info already exists for ketoantruong
            Integer infoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_info WHERE user_id = '5226bbeb-e360-4700-a76a-a0ed0a332d4a'", Integer.class);
            if (infoCount == null || infoCount == 0) {
                jdbcTemplate.update("insert into user_info (user_id, employee_code, phone, gender, created_at, created_by) values " +
                    "('5226bbeb-e360-4700-a76a-a0ed0a332d4a', 'NV-KTT-01', '0912345678', 'MALE', now(), 'system')");
            }
            
            Integer infoCount2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_info WHERE user_id = '8937e8c6-c695-4602-af78-93ff80449f1e'", Integer.class);
            if (infoCount2 == null || infoCount2 == 0) {
                jdbcTemplate.update("insert into user_info (user_id, employee_code, phone, gender, created_at, created_by) values " +
                    "('8937e8c6-c695-4602-af78-93ff80449f1e', 'NV-KTV-02', '0987654321', 'FEMALE', now(), 'system')");
            }
            
            Integer posCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_positions WHERE user_id = '5226bbeb-e360-4700-a76a-a0ed0a332d4a'", Integer.class);
            if (posCount == null || posCount == 0) {
                jdbcTemplate.update("insert into user_positions (active, created_at, department_job_title_id, source, user_id, created_by, updated_by) values " +
                    "(1, now(), 123, 'DEPARTMENT', '5226bbeb-e360-4700-a76a-a0ed0a332d4a', 'system', 'system')");
            }
            
            Integer posCount2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_positions WHERE user_id = '8937e8c6-c695-4602-af78-93ff80449f1e'", Integer.class);
            if (posCount2 == null || posCount2 == 0) {
                jdbcTemplate.update("insert into user_positions (active, created_at, department_job_title_id, source, user_id, created_by, updated_by) values " +
                    "(1, now(), 128, 'DEPARTMENT', '8937e8c6-c695-4602-af78-93ff80449f1e', 'system', 'system')");
            }
        } catch (Exception e) {
            System.err.println(">>> SEED: Error creating test accountant user positions/info: " + e.getMessage());
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

    private void seedDepartmentObjectivePermissionsAndRole() {
        List<Permission> newPerms = new ArrayList<>();
        addPermissionIfMissing(newPerms, "Tổng hợp mục tiêu phòng ban",
                "/api/v1/department-objectives/summary", "GET", "DEPARTMENT_OBJECTIVES");
        addPermissionIfMissing(newPerms, "Lịch sử phiên bản mục tiêu phòng ban",
                "/api/v1/departments/{departmentId}/objectives/versions", "GET", "DEPARTMENT_OBJECTIVES");
        addPermissionIfMissing(newPerms, "Ban hành mục tiêu phòng ban",
                "/api/v1/department-objectives/publish", "POST", "DEPARTMENT_OBJECTIVES");

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: DEPARTMENT_OBJECTIVES extra permissions created");
        }

        List<Permission> departmentObjectivePermissions = this.permissionRepository.findAll().stream()
                .filter(p -> "DEPARTMENT_OBJECTIVES".equals(p.getModule()))
                .toList();

        addPermissionsToRoleIfMissing("SUPER_ADMIN", departmentObjectivePermissions);
        addPermissionsToRoleIfMissing("HR_MANAGER", departmentObjectivePermissions);
        addPermissionsToRoleIfMissing("DEPARTMENT_MANAGER", departmentObjectivePermissions);
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
