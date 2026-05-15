package vn.system.app.modules.procedure.qr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import vn.system.app.common.config.AppProperties;
import vn.system.app.common.util.QrCodeUtil;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.service.UserPositionService;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.repository.CompanyProcedureRepository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureRepository;
import vn.system.app.modules.confidentialprocedure.service.ConfidentialAccessService;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.repository.DocumentAccessRepository;
import vn.system.app.modules.document.repository.DocumentRepository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcedureQrService {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final UserPositionService userPositionService;
    private final CompanyProcedureRepository companyProcedureRepository;
    private final DepartmentProcedureRepository departmentProcedureRepository;
    private final ConfidentialProcedureRepository confidentialProcedureRepository;
    private final ConfidentialAccessService confidentialAccessService;
    private final DocumentRepository documentRepository;
    private final DocumentAccessRepository documentAccessRepository;

    // =====================================================
    // SINH TOKEN
    // =====================================================
    public String buildQrToken() {
        return UUID.randomUUID().toString();
    }

    // =====================================================
    // QR NỘI BỘ — trỏ vào frontend, yêu cầu đăng nhập
    // =====================================================
    public String buildQrBase64(String token) {
        String url = appProperties.getBaseUrl() + "/admin/procedures/qr/" + token;
        return QrCodeUtil.generateBase64(url);
    }

    // =====================================================
    // QR TEXT THUẦN — chỉ hiện mã, không vào website
    // Dùng cho người ngoài chỉ cần biết mã quy trình
    // =====================================================
    public String buildQrText(String procedureCode) {
        String text = "Mã quy trình: " + procedureCode;
        return QrCodeUtil.generateBase64(text);
    }

    // =====================================================
    // QR CÔNG KHAI (SHARE TOKEN) — trỏ vào /public/view
    // Dùng cho người ngoài muốn xem nội dung quy trình
    // Không cần đăng nhập, có giới hạn thời gian/lượt
    // =====================================================
    public String buildShareTokenQr(String shareToken) {
        String url = appProperties.getBaseUrl() + "/public/view/" + shareToken;
        return QrCodeUtil.generateBase64(url);
    }

    // =====================================================
    // SCAN QR NỘI BỘ — kiểm tra quyền rồi trả về loại + id
    // =====================================================
    public ScanResult resolveAndCheckAccess(String token) {

        CompanyProcedure company = companyProcedureRepository.findByQrToken(token).orElse(null);
        if (company != null) {
            if (!company.isActive())
                throw new IdInvalidException("Quy trình đã bị vô hiệu hóa");
            checkCompanyAccess(company);
            return new ScanResult("COMPANY", company.getId());
        }

        DepartmentProcedure department = departmentProcedureRepository.findByQrToken(token).orElse(null);
        if (department != null) {
            if (!department.isActive())
                throw new IdInvalidException("Quy trình đã bị vô hiệu hóa");
            checkDepartmentAccess(department);
            return new ScanResult("DEPARTMENT", department.getId());
        }

        ConfidentialProcedure confidential = confidentialProcedureRepository.findByQrToken(token).orElse(null);
        if (confidential != null) {
            if (!confidential.isActive())
                throw new IdInvalidException("Quy trình đã bị vô hiệu hóa");
            confidentialAccessService.checkAccess(confidential.getId());
            return new ScanResult("CONFIDENTIAL", confidential.getId());
        }
        
        Document document = documentRepository.findByQrToken(token).orElse(null);
        if (document != null) {
            if (!document.isActive())
                throw new IdInvalidException("Văn bản đã bị vô hiệu hóa");
            checkDocumentAccess(document);
            return new ScanResult("DOCUMENT", document.getId());
        }

        throw new IdInvalidException("Mã QR không hợp lệ hoặc không tồn tại");
    }

    // =====================================================
    // KIỂM TRA QUYỀN — COMPANY
    // Tất cả nhân viên cùng công ty đều xem được
    // =====================================================
    private void checkCompanyAccess(CompanyProcedure procedure) {
        User user = getCurrentUser();
        String roleName = user.getRole() != null ? user.getRole().getName() : "";

        if ("SUPER_ADMIN".equals(roleName) || "ADMIN_SUB_1".equals(roleName))
            return;

        Long procedureCompanyId = procedure.getDepartment() != null
                && procedure.getDepartment().getCompany() != null
                        ? procedure.getDepartment().getCompany().getId()
                        : null;

        if (procedureCompanyId == null)
            throw new IdInvalidException("Bạn không có quyền xem quy trình này");

        Set<Long> userCompanyIds = userPositionService.getCompanyIdsByUser(user.getId());
        if (!userCompanyIds.contains(procedureCompanyId))
            throw new IdInvalidException("Bạn không có quyền xem quy trình này");
    }

    // =====================================================
    // KIỂM TRA QUYỀN — DEPARTMENT
    // Chỉ nhân viên đúng phòng ban mới xem được
    // =====================================================
    private void checkDepartmentAccess(DepartmentProcedure procedure) {
        User user = getCurrentUser();
        String roleName = user.getRole() != null ? user.getRole().getName() : "";

        if ("SUPER_ADMIN".equals(roleName) || "ADMIN_SUB_1".equals(roleName))
            return;

        if (procedure.getDepartments() == null || procedure.getDepartments().isEmpty())
            throw new IdInvalidException("Bạn không có quyền xem quy trình này");

        if ("ADMIN_SUB_2".equals(roleName)) {
            Set<Long> userCompanyIds = userPositionService.getCompanyIdsByUser(user.getId());
            boolean sameCompany = procedure.getDepartments().stream()
                    .anyMatch(d -> d.getCompany() != null
                            && userCompanyIds.contains(d.getCompany().getId()));
            if (!sameCompany)
                throw new IdInvalidException("Bạn không có quyền xem quy trình này");
            return;
        }

        Set<Long> procedureDeptIds = procedure.getDepartments().stream()
                .map(d -> d.getId())
                .collect(Collectors.toSet());

        Set<Long> userDeptIds = userPositionService.getDepartmentIdsByUser(user.getId());

        if (procedureDeptIds.stream().noneMatch(userDeptIds::contains))
            throw new IdInvalidException("Bạn không có quyền xem quy trình này");
    }

    // =====================================================
    // KIỂM TRA QUYỀN — DOCUMENT
    // =====================================================
    private void checkDocumentAccess(Document document) {
        User user = getCurrentUser();
        String roleName = user.getRole() != null ? user.getRole().getName() : "";

        // 1. Super Admin & Admin cấp 1 có quyền xem mọi thứ
        if ("SUPER_ADMIN".equals(roleName) || "ADMIN_SUB_1".equals(roleName))
            return;

        // 2. Lấy thông tin công ty của văn bản
        Long docCompanyId = (document.getDepartment() != null && document.getDepartment().getCompany() != null)
                ? document.getDepartment().getCompany().getId()
                : null;

        Set<Long> userCompanyIds = userPositionService.getCompanyIdsByUser(user.getId());

        // 3. Nếu là Admin công ty (ADMIN_SUB_2)
        if ("ADMIN_SUB_2".equals(roleName)) {
            if (docCompanyId == null || !userCompanyIds.contains(docCompanyId)) {
                throw new IdInvalidException("Bạn không có quyền xem văn bản của công ty khác");
            }
            return; // Admin cùng công ty được xem hết
        }

        // 4. Kiểm tra văn bản mapping quy trình
        if (document.getCategory() != null && document.getCategory().isMappingProcedure()) {
            if (document.getProcedureType() == null || document.getProcedureId() == null) {
                throw new IdInvalidException("Văn bản đang ở chế độ mapping nhưng thiếu thông tin quy trình");
            }
            switch (document.getProcedureType()) {
                case COMPANY -> {
                    CompanyProcedure cp = companyProcedureRepository.findById(document.getProcedureId())
                            .orElseThrow(() -> new IdInvalidException("Quy trình công ty không tồn tại"));
                    checkCompanyAccess(cp);
                }
                case DEPARTMENT -> {
                    DepartmentProcedure dp = departmentProcedureRepository.findById(document.getProcedureId())
                            .orElseThrow(() -> new IdInvalidException("Quy trình phòng ban không tồn tại"));
                    checkDepartmentAccess(dp);
                }
                case CONFIDENTIAL -> {
                    confidentialAccessService.checkAccess(document.getProcedureId());
                }
                default -> throw new IdInvalidException("Loại quy trình không được hỗ trợ trong mapping: " + document.getProcedureType());
            }
            return;
        }

        // 5. Kiểm tra văn bản không mapping (Kiểm tra chéo công ty và danh sách đích danh)
        if (docCompanyId != null && !userCompanyIds.contains(docCompanyId)) {
             // Khác công ty thì bắt buộc phải có trong danh sách DocumentAccess (được share đích danh)
             boolean hasExplicitAccess = documentAccessRepository.existsByDocument_IdAndUserId(document.getId(), user.getId());
             if (!hasExplicitAccess) {
                 throw new IdInvalidException("Bạn không có quyền xem văn bản của công ty khác");
             }
        } else {
             // Cùng công ty nhưng không mapping quy trình -> kiểm tra danh sách DocumentAccess
             boolean hasAccess = documentAccessRepository.existsByDocument_IdAndUserId(document.getId(), user.getId());
             if (!hasAccess) {
                 throw new IdInvalidException("Bạn không có quyền xem văn bản này");
             }
        }
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================
    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng"));
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("Người dùng không tồn tại");
        return user;
    }

    // =====================================================
    // INNER CLASS — kết quả scan
    // =====================================================
    public static class ScanResult {
        public final String type;
        public final Long id;

        public ScanResult(String type, Long id) {
            this.type = type;
            this.id = id;
        }
    }
}