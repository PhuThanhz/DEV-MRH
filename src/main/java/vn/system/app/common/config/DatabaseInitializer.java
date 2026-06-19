package vn.system.app.common.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
