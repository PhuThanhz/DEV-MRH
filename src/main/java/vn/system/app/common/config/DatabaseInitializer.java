package vn.system.app.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import org.springframework.core.env.Environment;

@Service
public class DatabaseInitializer implements CommandLineRunner {
    private static final String ACCOUNTING_DOSSIERS_MODULE = "ACCOUNTING_DOSSIERS";
    private static final String ACCOUNTING_DOCUMENTS_MODULE = "ACCOUNTING_DOCUMENTS";
    private static final String ACCOUNTING_WORKFLOWS_MODULE = "ACCOUNTING_WORKFLOWS";
    private static final String ACCOUNTING_DELEGATIONS_MODULE = "ACCOUNTING_DELEGATIONS";
    private static final String DIRECTOR_APPROVAL_PERMISSION_NAME = "Phê duyệt bộ chứng từ kế toán - Giám đốc";
    private static final String ACCOUNTANT_APPROVAL_PERMISSION_NAME = "Phê duyệt bộ chứng từ kế toán - Kế toán";
    private static final String CHIEF_ACCOUNTANT_APPROVAL_PERMISSION_NAME = "Phê duyệt bộ chứng từ kế toán - Kế toán trưởng";
    private static final Set<String> NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES = Set.of(
            ACCOUNTANT_APPROVAL_PERMISSION_NAME,
            CHIEF_ACCOUNTANT_APPROVAL_PERMISSION_NAME
    );

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentCategoryRepository documentCategoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DocumentCategoryRepository documentCategoryRepository,
            JdbcTemplate jdbcTemplate,
            Environment environment) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.documentCategoryRepository = documentCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        // Only seed test data on local/demo/UAT, never on production
        boolean isProd = false;
        if (this.environment != null) {
            for (String profile : this.environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                    isProd = true;
                    break;
                }
            }
            String dbUrl = this.environment.getProperty("spring.datasource.url");
            if (dbUrl != null && !dbUrl.contains("localhost") && !dbUrl.contains("127.0.0.1") && !dbUrl.contains("hrm_0107")) {
                isProd = true;
            }
        }

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
        String accModule = ACCOUNTING_DOCUMENTS_MODULE;
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

        String dossierModule = ACCOUNTING_DOSSIERS_MODULE;
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
        addPermissionIfMissing(newPerms, "Xóa mẫu bộ chứng từ kế toán", "/api/v1/accounting-dossiers/categories/{categoryId}", "DELETE", dossierModule);
        addPermissionIfMissing(newPerms, "Tra cứu chứng từ con theo bộ chứng từ", "/api/v1/accounting-dossiers/documents", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Thêm chứng từ con vào bộ", "/api/v1/accounting-dossiers/{id}/documents", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Sửa chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}", "PUT", dossierModule);
        addPermissionIfMissing(newPerms, "Xóa chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}", "DELETE", dossierModule);
        addPermissionIfMissing(newPerms, "Kiểm tra chứng từ con trong bộ", "/api/v1/accounting-dossiers/{id}/documents/{docId}/check", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Danh sách bộ chứng từ chờ tôi duyệt", "/api/v1/accounting-dossiers/pending-my-approval", "GET", dossierModule);
        addPermissionIfMissing(newPerms, "Phê duyệt bước bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/approve", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Từ chối bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/reject", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Chấm dứt bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/terminate", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Mở lại bộ chứng từ kế toán", "/api/v1/accounting-dossiers/{id}/reopen", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Đưa bộ chứng từ kế toán vào lưu trữ", "/api/v1/accounting-dossiers/{id}/archive", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Phản hồi yêu cầu hoàn bộ chứng từ", "/api/v1/accounting-dossiers/{id}/return-response", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Nhận xử lý bước duyệt bộ chứng từ", "/api/v1/accounting-dossiers/{id}/claim", "POST", dossierModule);
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
        addPermissionByNameIfMissing(newPerms, DIRECTOR_APPROVAL_PERMISSION_NAME, "/api/v1/accounting-dossiers/{id}/approve", "POST", dossierModule);
        addPermissionByNameIfMissing(newPerms, ACCOUNTANT_APPROVAL_PERMISSION_NAME, "/api/v1/accounting-dossiers/{id}/approve", "POST", dossierModule);
        addPermissionByNameIfMissing(newPerms, CHIEF_ACCOUNTANT_APPROVAL_PERMISSION_NAME, "/api/v1/accounting-dossiers/{id}/approve", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Emergency Reassign Giám đốc", "/api/v1/accounting-dossiers/{id}/reassign-director", "POST", dossierModule);
        addPermissionIfMissing(newPerms, "Quét quá hạn SLA duyệt bộ chứng từ", "/api/v1/accounting-approval-sla/scan-overdue", "POST", dossierModule);

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
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .toList();
            ketoanRole.setPermissions(allPerms);
            ketoanRole = this.roleRepository.save(ketoanRole);
        }

        if (!isProd) {
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
        }

        // Ensure KETOANTRUONG role and user exist
        Role ketoanTruongRole = this.roleRepository.findByName("KETOANTRUONG");
        if (ketoanTruongRole == null) {
            ketoanTruongRole = new Role();
            ketoanTruongRole.setName("KETOANTRUONG");
            ketoanTruongRole.setDescription("Kế toán trưởng");
            ketoanTruongRole.setActive(true);
            List<Permission> allPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .toList();
            ketoanTruongRole.setPermissions(allPerms);
            ketoanTruongRole = this.roleRepository.save(ketoanTruongRole);
        }

        if (!isProd) {
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
        }

        // Mỗi role nghiệp vụ chỉ giữ đúng permission phê duyệt của mình, không giữ chéo permission phê duyệt
        // của cấp khác (kể cả role đã tồn tại từ trước) — nếu không resolver permission-based sẽ chọn nhầm người duyệt.
        List<Permission> accountantApprovalPerm = this.permissionRepository.findAll().stream()
                .filter(p -> ACCOUNTANT_APPROVAL_PERMISSION_NAME.equals(p.getName()))
                .toList();
        List<Permission> chiefApprovalPerm = this.permissionRepository.findAll().stream()
                .filter(p -> CHIEF_ACCOUNTANT_APPROVAL_PERMISSION_NAME.equals(p.getName()))
                .toList();
        addPermissionsToRoleIfMissing("KETOAN", accountantApprovalPerm);
        removePermissionsFromRoleIfPresent("KETOAN",
                Set.of(CHIEF_ACCOUNTANT_APPROVAL_PERMISSION_NAME, DIRECTOR_APPROVAL_PERMISSION_NAME));
        addPermissionsToRoleIfMissing("KETOANTRUONG", chiefApprovalPerm);
        removePermissionsFromRoleIfPresent("KETOANTRUONG",
                Set.of(ACCOUNTANT_APPROVAL_PERMISSION_NAME, DIRECTOR_APPROVAL_PERMISSION_NAME));

        // Ensure EMPLOYEE and DEPARTMENT_MANAGER roles have accounting permissions
        Role employeeRole = this.roleRepository.findByName("EMPLOYEE");
        if (employeeRole != null) {
            // Keep non-accounting permissions
            List<Permission> nonAccPerms = employeeRole.getPermissions().stream()
                    .filter(p -> !ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) && !ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .toList();
            
            // Get restricted accounting permissions for EMPLOYEE
            List<Permission> allowedAccPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
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
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .filter(p -> !DIRECTOR_APPROVAL_PERMISSION_NAME.equals(p.getName()))
                    .filter(p -> !NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(p.getName()))
                    .toList();
            List<Permission> currentPerms = new ArrayList<>(deptMgrRole.getPermissions());
            currentPerms.removeIf(p -> p != null && DIRECTOR_APPROVAL_PERMISSION_NAME.equals(p.getName()));
            currentPerms.removeIf(p -> p != null && NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(p.getName()));
            for (Permission p : dossierPerms) {
                boolean exists = currentPerms.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(item -> item.getId() == p.getId());
                if (!exists) {
                    currentPerms.add(p);
                }
            }
            deptMgrRole.setPermissions(currentPerms);
            this.roleRepository.save(deptMgrRole);
        }
        if (!isProd) {
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
        }

        // Ensure DIRECTOR role and user exist (only for non-production environments)
        Role directorRole = this.roleRepository.findByName("DIRECTOR");
        if (directorRole == null) {
            directorRole = new Role();
            directorRole.setName("DIRECTOR");
            directorRole.setDescription("Giám đốc");
            directorRole.setActive(true);
            List<Permission> allPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .filter(p -> !NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(p.getName()))
                    .toList();
            directorRole.setPermissions(allPerms);
            directorRole = this.roleRepository.save(directorRole);
        } else {
            // Đảm bảo DIRECTOR role luôn có đầy đủ ACCOUNTING_DOSSIERS permissions (bao gồm cả permission mới thêm)
            List<Permission> dossierPerms = this.permissionRepository.findAll().stream()
                    .filter(p -> ACCOUNTING_DOSSIERS_MODULE.equals(p.getModule()) || ACCOUNTING_DOCUMENTS_MODULE.equals(p.getModule()))
                    .filter(p -> !NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(p.getName()))
                    .toList();
            List<Permission> currentDirPerms = new ArrayList<>(directorRole.getPermissions());
            currentDirPerms.removeIf(p -> p != null && NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(p.getName()));
            boolean updated = false;
            for (Permission p : dossierPerms) {
                if (!currentDirPerms.contains(p)) {
                    currentDirPerms.add(p);
                    updated = true;
                }
            }
            if (updated) {
                directorRole.setPermissions(currentDirPerms);
                directorRole = this.roleRepository.save(directorRole);
                System.out.println(">>> SEED: Updated DIRECTOR role with new accounting permissions");
            }
        }

        // Only seed director@gmail.com on local/demo/UAT, never on production
        if (!isProd) {
            User directorUser = this.userRepository.findByEmail("director@gmail.com");
            if (directorUser == null) {
                directorUser = new User();
                directorUser.setEmail("director@gmail.com");
                directorUser.setName("Giám Đốc Test");
                directorUser.setPassword(this.passwordEncoder.encode("123456"));
                directorUser.setActive(true);
                directorUser.setRole(directorRole);
                directorUser = this.userRepository.save(directorUser);

                // Add user position mapping for Director dynamically to map to Company ID 1
                try {
                    String dId = directorUser.getId();
                    jdbcTemplate.update("insert into user_info (user_id, employee_code, phone, gender, created_at, created_by) values " +
                        "(?, 'NV-GD-01', '0900000001', 'MALE', now(), 'system')", dId);

                    Long deptJobTitleId = null;
                    try {
                        deptJobTitleId = jdbcTemplate.queryForObject(
                            "SELECT djt.id FROM department_job_titles djt " +
                            "JOIN departments d ON djt.department_id = d.id " +
                            "WHERE d.company_id = 1 LIMIT 1", Long.class);
                    } catch (Exception ex) {
                        try {
                            deptJobTitleId = jdbcTemplate.queryForObject(
                                "SELECT id FROM department_job_titles LIMIT 1", Long.class);
                        } catch (Exception e2) {}
                    }

                    if (deptJobTitleId != null) {
                        Integer posCount = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM user_positions WHERE user_id = ?", Integer.class, dId);
                        if (posCount == null || posCount == 0) {
                            jdbcTemplate.update(
                                "insert into user_positions (active, created_at, department_job_title_id, source, user_id, created_by, updated_by) values " +
                                "(1, now(), ?, 'DEPARTMENT', ?, 'system', 'system')",
                                deptJobTitleId, dId);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println(">>> SEED: Error linking position for director: " + ex.getMessage());
                }
            }
        }

        seedDepartmentObjectivePermissionsAndRole();
        seedAdminScopePermissionsAndRole();
        seedAccountingWorkflowPermissionsAndRole();
        seedAccountingDelegationPermissionsAndRole();
        seedEvaluationPermissionsAndRole();
        seedEvaluationTemplateDeletePermission();
        seedEvaluationReassignPermission();
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

        if (!isProd) {
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

            seedUatUsers();
            seedAccountingApprovalDelegations();
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else
            System.out.println(">>> END INIT DATABASE");
    }

    private void seedAccountingApprovalDelegations() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounting_approval_delegations", Integer.class);
            if (count != null && count > 0) {
                return;
            }

            System.out.println(">>> SEED: Seeding mock accounting approval delegations...");

            String delegatorId = null;
            try {
                delegatorId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'manager@gmail.com' LIMIT 1", String.class);
            } catch (Exception e) {}

            String delegateId = null;
            try {
                delegateId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'ketoan@gmail.com' LIMIT 1", String.class);
            } catch (Exception e) {}

            String directorId = null;
            try {
                directorId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'director@gmail.com' LIMIT 1", String.class);
            } catch (Exception e) {}

            String chiefAccountantId = null;
            try {
                chiefAccountantId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'ketoantruong@gmail.com' LIMIT 1", String.class);
            } catch (Exception e) {}

            String adminId = null;
            try {
                adminId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'admin@gmail.com' LIMIT 1", String.class);
            } catch (Exception e) {}

            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            java.sql.Timestamp yesterday = new java.sql.Timestamp(System.currentTimeMillis() - 86400000L);
            java.sql.Timestamp fiveDaysLater = new java.sql.Timestamp(System.currentTimeMillis() + 5L * 86400000L);
            java.sql.Timestamp tenDaysAgo = new java.sql.Timestamp(System.currentTimeMillis() - 10L * 86400000L);
            java.sql.Timestamp twoDaysAgo = new java.sql.Timestamp(System.currentTimeMillis() - 2L * 86400000L);
            java.sql.Timestamp threeDaysAgo = new java.sql.Timestamp(System.currentTimeMillis() - 3L * 86400000L);
            java.sql.Timestamp threeDaysLater = new java.sql.Timestamp(System.currentTimeMillis() + 3L * 86400000L);
            java.sql.Timestamp twoDaysLater = new java.sql.Timestamp(System.currentTimeMillis() + 2L * 86400000L);
            java.sql.Timestamp twelveDaysLater = new java.sql.Timestamp(System.currentTimeMillis() + 12L * 86400000L);

            if (delegatorId != null && delegateId != null) {
                // 1. Active delegation
                jdbcTemplate.update(
                    "INSERT INTO accounting_approval_delegations " +
                    "(company_id, delegator_user_id, delegate_user_id, valid_from, valid_to, scope_type, scope_ref_id, reason, status, created_at, created_by) " +
                    "VALUES (1, ?, ?, ?, ?, 'ALL', NULL, 'Duyệt hồ sơ đề nghị tạm ứng chi phí đi công tác', 'ACTIVE', ?, 'system')",
                    delegatorId, delegateId, yesterday, fiveDaysLater, now
                );

                // 2. Expired delegation
                jdbcTemplate.update(
                    "INSERT INTO accounting_approval_delegations " +
                    "(company_id, delegator_user_id, delegate_user_id, valid_from, valid_to, scope_type, scope_ref_id, reason, status, created_at, created_by) " +
                    "VALUES (1, ?, ?, ?, ?, 'ALL', NULL, 'Ủy quyền duyệt chứng từ thanh toán tạm thời khi đi công tác nước ngoài', 'EXPIRED', ?, 'system')",
                    delegatorId, delegateId, tenDaysAgo, twoDaysAgo, now
                );
            }

            if (directorId != null && chiefAccountantId != null) {
                // 3. Revoked delegation
                jdbcTemplate.update(
                    "INSERT INTO accounting_approval_delegations " +
                    "(company_id, delegator_user_id, delegate_user_id, valid_from, valid_to, scope_type, scope_ref_id, reason, status, created_at, created_by, revoked_at, revoked_by) " +
                    "VALUES (1, ?, ?, ?, ?, 'ALL', NULL, 'Ủy quyền phê duyệt thanh toán hợp đồng dự án UAT', 'REVOKED', ?, 'system', ?, 'director@gmail.com')",
                    directorId, chiefAccountantId, threeDaysAgo, threeDaysLater, now, yesterday
                );
            }

            if (adminId != null && delegateId != null) {
                // 4. Draft delegation
                jdbcTemplate.update(
                    "INSERT INTO accounting_approval_delegations " +
                    "(company_id, delegator_user_id, delegate_user_id, valid_from, valid_to, scope_type, scope_ref_id, reason, status, created_at, created_by) " +
                    "VALUES (1, ?, ?, ?, ?, 'ALL', NULL, 'Ủy quyền dự phòng duyệt chứng từ trước kỳ nghỉ phép năm', 'DRAFT', ?, 'system')",
                    adminId, delegateId, twoDaysLater, twelveDaysLater, now
                );
            }

            System.out.println(">>> SEED: Mock delegations successfully created");

        } catch (Exception e) {
            System.err.println(">>> SEED: Error seeding delegations: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void addPermissionByNameIfMissing(List<Permission> target, String name, String apiPath, String method, String module) {
        boolean exists = this.permissionRepository.findAll().stream()
                .anyMatch(p -> name.equals(p.getName()));
        if (!exists) {
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

        List<Permission> deptManagerPermissions = managerPermissions.stream()
                .filter(p -> !"USERS".equals(p.getModule()))
                .toList();

        addPermissionsToRoleIfMissing("DEPARTMENT_MANAGER", deptManagerPermissions);

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

    /**
     * Xóa mẫu đánh giá chỉ áp dụng cho bản nháp. Cấp quyền này cho đúng các role
     * vốn đã có quyền sửa mẫu, tránh mở rộng quyền xóa cho các role chỉ được xem.
     */
    private void seedEvaluationTemplateDeletePermission() {
        final String module = "EVALUATION_TEMPLATE";
        final String deletePath = "/api/v1/evaluation/templates/{id}";
        final String updatePath = "/api/v1/evaluation/templates/{id}";

        Permission deletePermission = this.permissionRepository.findAll().stream()
                .filter(permission -> module.equals(permission.getModule())
                        && deletePath.equals(permission.getApiPath())
                        && "DELETE".equalsIgnoreCase(permission.getMethod()))
                .findFirst()
                .orElseGet(() -> this.permissionRepository.save(
                        createPermission("Xóa mẫu đánh giá", deletePath, "DELETE", module)));

        for (Role role : this.roleRepository.findAll()) {
            boolean canUpdateTemplate = role.getPermissions().stream()
                    .filter(Objects::nonNull)
                    .anyMatch(permission -> module.equals(permission.getModule())
                            && updatePath.equals(permission.getApiPath())
                            && "PUT".equalsIgnoreCase(permission.getMethod()));
            if (canUpdateTemplate) {
                addPermissionsToRoleIfMissing(role.getName(), List.of(deletePermission));
            }
        }
    }

    private void seedEvaluationReassignPermission() {
        final String module = "EVALUATION_MANAGER";
        final String path = "/api/v1/evaluation/records/reassign-evaluator";
        final String method = "PATCH";
        final String name = "Điều chuyển người chấm/duyệt bản đánh giá";

        Permission reassignPermission = this.permissionRepository.findAll().stream()
                .filter(p -> path.equals(p.getApiPath()) && method.equalsIgnoreCase(p.getMethod()))
                .findFirst()
                .orElseGet(() -> {
                    Permission newPerm = createPermission(name, path, method, module);
                    return this.permissionRepository.save(newPerm);
                });

        // Add to admins
        addPermissionsToRoleIfMissing("SUPER_ADMIN", List.of(reassignPermission));
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", List.of(reassignPermission));
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", List.of(reassignPermission));
    }

    /**
     * Quyền cấu hình luồng duyệt được tách riêng khỏi quyền thao tác bộ chứng từ.
     * ADMIN_SUB_2 chỉ có thể chuẩn bị và kiểm tra nháp; áp dụng/ngưng áp dụng
     * thuộc nhóm quản trị cấp cao hơn (SUPER_ADMIN, ADMIN_SUB_1).
     */
    private void seedAccountingWorkflowPermissionsAndRole() {
        List<Permission> newPerms = new ArrayList<>();
        addPermissionIfMissing(newPerms, "Xem danh sách luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows", "GET", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Tạo nháp luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Cập nhật nháp luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows/{id}/draft", "PUT", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Kiểm tra cấu hình luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows/{id}/validate", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Áp dụng luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows/{id}/publish", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Ngưng áp dụng luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows/{id}/deactivate", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Kích hoạt lại luồng duyệt chứng từ kế toán",
                "/api/v1/accounting-approval-workflows/{id}/reactivate", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Sao chép luồng duyệt chứng từ kế toán thành bản nháp",
                "/api/v1/accounting-approval-workflows/{id}/copy", "POST", ACCOUNTING_WORKFLOWS_MODULE);
        addPermissionIfMissing(newPerms, "Xem trước luồng duyệt bộ chứng từ",
                "/api/v1/accounting-approval-workflows/dossiers/{dossierId}/preview", "POST", ACCOUNTING_WORKFLOWS_MODULE);

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: ACCOUNTING_WORKFLOWS permissions created");
        }

        List<Permission> workflowPermissions = this.permissionRepository.findAll().stream()
                .filter(p -> ACCOUNTING_WORKFLOWS_MODULE.equals(p.getModule()))
                .toList();
        List<Permission> draftWorkflowPermissions = workflowPermissions.stream()
                .filter(p -> "/api/v1/accounting-approval-workflows".equals(p.getApiPath())
                        || "/api/v1/accounting-approval-workflows/{id}/draft".equals(p.getApiPath())
                        || "/api/v1/accounting-approval-workflows/{id}/validate".equals(p.getApiPath())
                        || "/api/v1/accounting-approval-workflows/{id}/copy".equals(p.getApiPath()))
                .toList();

        addPermissionsToRoleIfMissing("SUPER_ADMIN", workflowPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", workflowPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", draftWorkflowPermissions);
    }

    private void seedAccountingDelegationPermissionsAndRole() {
        List<Permission> newPerms = new ArrayList<>();
        addPermissionIfMissing(newPerms, "Xem danh sách ủy quyền xử lý phê duyệt chứng từ",
                "/api/v1/accounting-approval-delegations", "GET", ACCOUNTING_DELEGATIONS_MODULE);
        addPermissionIfMissing(newPerms, "Tạo ủy quyền xử lý phê duyệt chứng từ",
                "/api/v1/accounting-approval-delegations", "POST", ACCOUNTING_DELEGATIONS_MODULE);
        addPermissionIfMissing(newPerms, "Kích hoạt ủy quyền xử lý phê duyệt chứng từ",
                "/api/v1/accounting-approval-delegations/{id}/activate", "POST", ACCOUNTING_DELEGATIONS_MODULE);
        addPermissionIfMissing(newPerms, "Thu hồi ủy quyền xử lý phê duyệt chứng từ",
                "/api/v1/accounting-approval-delegations/{id}/revoke", "POST", ACCOUNTING_DELEGATIONS_MODULE);

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: ACCOUNTING_DELEGATIONS permissions created");
        }

        List<Permission> delegationPermissions = this.permissionRepository.findAll().stream()
                .filter(p -> ACCOUNTING_DELEGATIONS_MODULE.equals(p.getModule()))
                .toList();
        addPermissionsToRoleIfMissing("SUPER_ADMIN", delegationPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", delegationPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", delegationPermissions);
        addPermissionsToRoleIfMissing("KETOANTRUONG", delegationPermissions);
    }

    private void seedEvaluationPermissionsAndRole() {
        List<Permission> newPerms = new ArrayList<>();

        final String templateModule = "EVALUATION_TEMPLATE";
        addPermissionIfMissing(newPerms, "Tạo mẫu đánh giá", "/api/v1/evaluation/templates", "POST", templateModule);
        addPermissionIfMissing(newPerms, "Cập nhật mẫu đánh giá", "/api/v1/evaluation/templates/{id}", "PUT", templateModule);
        addPermissionIfMissing(newPerms, "Công bố mẫu đánh giá", "/api/v1/evaluation/templates/{id}/publish", "PATCH", templateModule);
        addPermissionIfMissing(newPerms, "Lưu trữ mẫu đánh giá", "/api/v1/evaluation/templates/{id}/archive", "PATCH", templateModule);
        addPermissionIfMissing(newPerms, "Xóa mẫu đánh giá", "/api/v1/evaluation/templates/{id}", "DELETE", templateModule);
        addPermissionIfMissing(newPerms, "Chi tiết mẫu đánh giá", "/api/v1/evaluation/templates/{id}", "GET", templateModule);
        addPermissionIfMissing(newPerms, "Danh sách mẫu đánh giá", "/api/v1/evaluation/templates", "GET", templateModule);
        addPermissionIfMissing(newPerms, "Danh sách mẫu đánh giá đang dùng", "/api/v1/evaluation/templates/active", "GET", templateModule);
        addPermissionIfMissing(newPerms, "Tạo nhóm tiêu chí mẫu đánh giá", "/api/v1/evaluation/templates/{templateId}/sections", "POST", templateModule);
        addPermissionIfMissing(newPerms, "Cập nhật nhóm tiêu chí mẫu đánh giá", "/api/v1/evaluation/sections/{sectionId}", "PUT", templateModule);
        addPermissionIfMissing(newPerms, "Xóa nhóm tiêu chí mẫu đánh giá", "/api/v1/evaluation/sections/{sectionId}", "DELETE", templateModule);
        addPermissionIfMissing(newPerms, "Danh sách nhóm tiêu chí mẫu đánh giá", "/api/v1/evaluation/templates/{templateId}/sections", "GET", templateModule);
        addPermissionIfMissing(newPerms, "Tạo tiêu chí đánh giá", "/api/v1/evaluation/sections/{sectionId}/criteria", "POST", templateModule);
        addPermissionIfMissing(newPerms, "Cập nhật tiêu chí đánh giá", "/api/v1/evaluation/criteria/{criteriaId}", "PUT", templateModule);
        addPermissionIfMissing(newPerms, "Xóa tiêu chí đánh giá", "/api/v1/evaluation/criteria/{criteriaId}", "DELETE", templateModule);
        addPermissionIfMissing(newPerms, "Tạo mức điểm tiêu chí", "/api/v1/evaluation/criteria/{criteriaId}/levels", "POST", templateModule);
        addPermissionIfMissing(newPerms, "Cập nhật mức điểm tiêu chí", "/api/v1/evaluation/levels/{levelId}", "PUT", templateModule);
        addPermissionIfMissing(newPerms, "Danh sách mức điểm tiêu chí", "/api/v1/evaluation/criteria/{criteriaId}/levels", "GET", templateModule);

        final String periodModule = "EVALUATION_PERIOD";
        addPermissionIfMissing(newPerms, "Tạo kỳ đánh giá", "/api/v1/evaluation/periods", "POST", periodModule);
        addPermissionIfMissing(newPerms, "Cập nhật kỳ đánh giá", "/api/v1/evaluation/periods/{id}", "PUT", periodModule);
        addPermissionIfMissing(newPerms, "Chi tiết kỳ đánh giá", "/api/v1/evaluation/periods/{id}", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Tiến độ kỳ đánh giá", "/api/v1/evaluation/periods/{id}/progress", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Danh sách kỳ đánh giá", "/api/v1/evaluation/periods", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Gắn mẫu vào kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/templates", "POST", periodModule);
        addPermissionIfMissing(newPerms, "Danh sách mẫu trong kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/templates", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Thêm nhân viên vào kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/employees", "POST", periodModule);
        addPermissionIfMissing(newPerms, "Hủy hồ sơ nhân viên trong kỳ đánh giá", "/api/v1/evaluation/period-employees/{id}/cancel", "PATCH", periodModule);
        addPermissionIfMissing(newPerms, "Danh sách nhân viên trong kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/employees", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Kích hoạt kỳ đánh giá", "/api/v1/evaluation/periods/{id}/activate", "PATCH", periodModule);
        addPermissionIfMissing(newPerms, "Đóng kỳ đánh giá", "/api/v1/evaluation/periods/{id}/close", "PATCH", periodModule);
        addPermissionIfMissing(newPerms, "Danh sách hồ sơ chưa hoàn tất trong kỳ", "/api/v1/evaluation/periods/{id}/unfinished-records", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Gia hạn hạn xử lý bản đánh giá", "/api/v1/evaluation/records/deadline-extension", "PATCH", periodModule);
        addPermissionIfMissing(newPerms, "Báo cáo tổng hợp đánh giá hoàn tất", "/api/v1/evaluation/summary/completed", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Phân bổ trạng thái trong kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/status-distribution", "GET", periodModule);
        addPermissionIfMissing(newPerms, "Phân bổ xếp loại trong kỳ đánh giá", "/api/v1/evaluation/periods/{periodId}/grade-distribution", "GET", periodModule);

        final String employeeModule = "EVALUATION_EMPLOYEE";
        addPermissionIfMissing(newPerms, "Danh sách bản đánh giá cá nhân", "/api/v1/evaluation/my-records", "GET", employeeModule);
        addPermissionIfMissing(newPerms, "Nhân viên chấm điểm tự đánh giá", "/api/v1/evaluation/records/{recordId}/employee-scores", "POST", employeeModule);
        addPermissionIfMissing(newPerms, "Nhân viên nộp tự đánh giá", "/api/v1/evaluation/records/{recordId}/employee-submit", "POST", employeeModule);
        addPermissionIfMissing(newPerms, "Nhân viên lưu tự nhận xét", "/api/v1/evaluation/records/{recordId}/self-review", "POST", employeeModule);
        addPermissionIfMissing(newPerms, "Nhân viên xác nhận kết quả đánh giá", "/api/v1/evaluation/records/{recordId}/employee-confirm", "POST", employeeModule);

        final String managerModule = "EVALUATION_MANAGER";
        addPermissionIfMissing(newPerms, "Chi tiết bản đánh giá", "/api/v1/evaluation/records/{id}", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Danh sách bản đánh giá cho quản lý trực tiếp theo kỳ", "/api/v1/evaluation/manager/periods/{periodId}/records", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Danh sách chờ quản lý trực tiếp chấm", "/api/v1/evaluation/manager/pending", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Lịch sử chấm điểm của quản lý trực tiếp", "/api/v1/evaluation/manager/records", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Danh sách bản đánh giá cho quản lý gián tiếp theo kỳ", "/api/v1/evaluation/approval/periods/{periodId}/records", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Danh sách chờ duyệt cấp trên", "/api/v1/evaluation/approval/pending", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Lịch sử duyệt cấp trên", "/api/v1/evaluation/approval/records", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Quản lý trực tiếp chấm điểm", "/api/v1/evaluation/records/{recordId}/manager-scores", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Quản lý trực tiếp gửi duyệt cấp trên", "/api/v1/evaluation/records/{recordId}/manager-submit", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Quản lý trực tiếp lưu nhận xét", "/api/v1/evaluation/records/{recordId}/manager-feedback", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Quản lý lưu kế hoạch đào tạo", "/api/v1/evaluation/records/{recordId}/training-plans", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Cấp trên điều chỉnh điểm duyệt", "/api/v1/evaluation/records/{recordId}/approver-scores", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Duyệt cấp trên bản đánh giá", "/api/v1/evaluation/records/{recordId}/approve", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Duyệt cấp trên hàng loạt", "/api/v1/evaluation/records/batch-approve", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Trả lại bản đánh giá", "/api/v1/evaluation/records/{recordId}/reject", "POST", managerModule);
        addPermissionIfMissing(newPerms, "Điều chuyển người chấm/duyệt bản đánh giá", "/api/v1/evaluation/records/reassign-evaluator", "PATCH", managerModule);
        addPermissionIfMissing(newPerms, "Lịch sử trạng thái bản đánh giá", "/api/v1/evaluation/records/{recordId}/history", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Audit thay đổi điểm bản đánh giá", "/api/v1/evaluation/records/{recordId}/score-audits", "GET", managerModule);
        addPermissionIfMissing(newPerms, "Số lần bản đánh giá bị trả lại", "/api/v1/evaluation/records/{recordId}/rejection-count", "GET", managerModule);

        if (!newPerms.isEmpty()) {
            this.permissionRepository.saveAll(newPerms);
            System.out.println(">>> SEED: EVALUATION permissions created");
        }

        List<Permission> templatePermissions = this.permissionRepository.findAll().stream()
                .filter(p -> templateModule.equals(p.getModule()))
                .toList();
        List<Permission> periodPermissions = this.permissionRepository.findAll().stream()
                .filter(p -> periodModule.equals(p.getModule()))
                .toList();
        List<Permission> employeePermissions = this.permissionRepository.findAll().stream()
                .filter(p -> employeeModule.equals(p.getModule()))
                .toList();
        List<Permission> managerPermissions = this.permissionRepository.findAll().stream()
                .filter(p -> managerModule.equals(p.getModule()))
                .toList();
        List<Permission> employeeSharedReadPermissions = managerPermissions.stream()
                .filter(p -> ("GET".equalsIgnoreCase(p.getMethod()))
                        && ("/api/v1/evaluation/records/{id}".equals(p.getApiPath())
                        || "/api/v1/evaluation/records/{recordId}/history".equals(p.getApiPath())
                        || "/api/v1/evaluation/records/{recordId}/rejection-count".equals(p.getApiPath())))
                .toList();

        addPermissionsToRoleIfMissing("SUPER_ADMIN", this.permissionRepository.findAll().stream()
                .filter(p -> templateModule.equals(p.getModule())
                        || periodModule.equals(p.getModule())
                        || employeeModule.equals(p.getModule())
                        || managerModule.equals(p.getModule()))
                .toList());
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", this.permissionRepository.findAll().stream()
                .filter(p -> templateModule.equals(p.getModule())
                        || periodModule.equals(p.getModule())
                        || employeeModule.equals(p.getModule())
                        || managerModule.equals(p.getModule()))
                .toList());
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", periodPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_2", templatePermissions);
        addPermissionsToRoleIfMissing("HR_MANAGER", periodPermissions);
        addPermissionsToRoleIfMissing("HR_MANAGER", templatePermissions);
        addPermissionsToRoleIfMissing("HR_MANAGER", managerPermissions);
        addPermissionsToRoleIfMissing("DEPARTMENT_MANAGER", managerPermissions);
        addPermissionsToRoleIfMissing("EMPLOYEE", employeePermissions);
        addPermissionsToRoleIfMissing("EMPLOYEE", employeeSharedReadPermissions);
    }

    private void syncFullPermissionRoles() {
        List<Permission> adminPermissions = this.permissionRepository.findAll().stream()
                .filter(permission -> !NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES.contains(permission.getName()))
                .toList();
        addPermissionsToRoleIfMissing("SUPER_ADMIN", adminPermissions);
        addPermissionsToRoleIfMissing("ADMIN_SUB_1", adminPermissions);
        removePermissionsFromRoleIfPresent("SUPER_ADMIN", NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES);
        removePermissionsFromRoleIfPresent("ADMIN_SUB_1", NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES);
        removePermissionsFromRoleIfPresent("ADMIN_SUB_2", NON_ADMIN_BUSINESS_APPROVER_PERMISSION_NAMES);
    }

    private void removePermissionsFromRoleIfPresent(String roleName, Set<String> permissionNames) {
        Role role = this.roleRepository.findByName(roleName);
        if (role == null || role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return;
        }

        List<Permission> filteredPermissions = role.getPermissions().stream()
                .filter(permission -> permission == null || !permissionNames.contains(permission.getName()))
                .toList();
        if (filteredPermissions.size() != role.getPermissions().size()) {
            role.setPermissions(new ArrayList<>(filteredPermissions));
            this.roleRepository.save(role);
        }
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

    private void seedUatUsers() {
        Role ketoanRole = this.roleRepository.findByName("KETOAN");
        Role ketoanTruongRole = this.roleRepository.findByName("KETOANTRUONG");

        createUserIfNotExist("emp1@lotte.vn", "UAT EMP 1 Gương mẫu", ketoanRole);
        createUserIfNotExist("emp2@lotte.vn", "UAT EMP 2 Nghỉ việc", ketoanRole);
        createUserIfNotExist("emp3@lotte.vn", "UAT EMP 3 Hay quên", ketoanRole);
        createUserIfNotExist("mgr1@lotte.vn", "UAT MGR 1 Quản lý", ketoanTruongRole);
        createUserIfNotExist("mgr2@lotte.vn", "UAT MGR 2 Quản lý mới", ketoanTruongRole);
        createUserIfNotExist("director@lotte.vn", "UAT Director / Approver", ketoanTruongRole);
    }

    private void createUserIfNotExist(String email, String name, Role role) {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(this.passwordEncoder.encode("123456"));
            user.setActive(true);
            user.setRole(role);
            this.userRepository.save(user);
            System.out.println(">>> SEED: UAT User created: " + email);
        }
    }
}
