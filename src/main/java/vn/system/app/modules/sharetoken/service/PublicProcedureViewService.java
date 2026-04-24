package vn.system.app.modules.sharetoken.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import vn.system.app.modules.sharetoken.domain.ProcedureShareToken;
import vn.system.app.modules.sharetoken.domain.response.ResPublicProcedureDTO;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.repository.CompanyProcedureRepository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureRepository;

@Service
@RequiredArgsConstructor
public class PublicProcedureViewService {

    private final ProcedureShareTokenService shareTokenService;
    private final CompanyProcedureRepository companyProcedureRepository;
    private final DepartmentProcedureRepository departmentProcedureRepository;
    private final ConfidentialProcedureRepository confidentialProcedureRepository;
    private final ObjectMapper objectMapper;

    // =====================================================
    // GET KHÔNG CẦN PIN
    // =====================================================
    public Object handleView(String token, String ip, String userAgent) {
        ProcedureShareToken shareToken = shareTokenService.validateToken(token);

        if (shareToken.getPin() != null) {
            return RequirePinResponse.of(true);
        }

        shareTokenService.recordAccess(shareToken, ip, userAgent);
        return buildPublicDTO(shareToken);
    }

    // =====================================================
    // POST VERIFY PIN
    // =====================================================
    public ResPublicProcedureDTO handleVerifyPin(String token, String pin,
            String ip, String userAgent) {
        ProcedureShareToken shareToken = shareTokenService.validateToken(token);

        if (!pin.equals(shareToken.getPin())) {
            throw new RuntimeException("Mã PIN không đúng");
        }

        shareTokenService.recordAccess(shareToken, ip, userAgent);
        return buildPublicDTO(shareToken);
    }

    // =====================================================
    // BUILD DTO THEO PERMISSION
    // =====================================================
    public ResPublicProcedureDTO buildPublicDTO(ProcedureShareToken shareToken) {
        String type = shareToken.getProcedureType();
        Long id = shareToken.getProcedureId();
        String permission = shareToken.getPermission();

        return switch (type) {
            case "COMPANY" -> {
                CompanyProcedure p = companyProcedureRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy quy trình"));

                if (!p.isActive()) {
                    throw new RuntimeException("Quy trình đã bị vô hiệu hóa");
                }

                yield ResPublicProcedureDTO.builder()
                        .procedureCode(p.getProcedureCode())
                        .procedureName(p.getProcedureName())
                        .status(p.getStatus())
                        .version(p.getVersion())
                        .issuedDate(p.getIssuedDate())
                        .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                        .sectionName(p.getSection() != null ? p.getSection().getName() : null)
                        .note(p.getNote())
                        .fileUrls(resolveFileUrls(permission, p.getFileUrls()))
                        .allowDownload(resolveAllowDownload(permission))
                        .accessCount(shareToken.getAccessCount())
                        .maxAccessCount(shareToken.getMaxAccessCount())
                        .expiresAt(shareToken.getExpiresAt())
                        .build();
            }

            case "DEPARTMENT" -> {
                DepartmentProcedure p = departmentProcedureRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy quy trình"));

                if (!p.isActive()) {
                    throw new RuntimeException("Quy trình đã bị vô hiệu hóa");
                }

                String deptName = (p.getDepartments() != null && !p.getDepartments().isEmpty())
                        ? p.getDepartments().get(0).getName()
                        : null;

                yield ResPublicProcedureDTO.builder()
                        .procedureCode(p.getProcedureCode())
                        .procedureName(p.getProcedureName())
                        .status(p.getStatus())
                        .version(p.getVersion())
                        .issuedDate(p.getIssuedDate())
                        .departmentName(deptName)
                        .sectionName(p.getSection() != null ? p.getSection().getName() : null)
                        .note(p.getNote())
                        .fileUrls(resolveFileUrls(permission, p.getFileUrls()))
                        .allowDownload(resolveAllowDownload(permission))
                        .accessCount(shareToken.getAccessCount())
                        .maxAccessCount(shareToken.getMaxAccessCount())
                        .expiresAt(shareToken.getExpiresAt())
                        .build();
            }

            case "CONFIDENTIAL" -> {
                ConfidentialProcedure p = confidentialProcedureRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy quy trình"));

                if (!p.isActive()) {
                    throw new RuntimeException("Quy trình đã bị vô hiệu hóa");
                }

                yield ResPublicProcedureDTO.builder()
                        .procedureCode(p.getProcedureCode())
                        .procedureName(p.getProcedureName())
                        .status(p.getStatus())
                        .version(p.getVersion())
                        .issuedDate(p.getIssuedDate())
                        .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                        .sectionName(p.getSection() != null ? p.getSection().getName() : null)
                        .note(p.getNote())
                        .fileUrls(resolveFileUrls(permission, p.getFileUrls()))
                        .allowDownload(resolveAllowDownload(permission))
                        .accessCount(shareToken.getAccessCount())
                        .maxAccessCount(shareToken.getMaxAccessCount())
                        .expiresAt(shareToken.getExpiresAt())
                        .build();
            }

            default -> throw new RuntimeException("Loại quy trình không hợp lệ: " + type);
        };
    }

    // =====================================================
    // HELPER: parse fileUrls từ String JSON → List<String>
    // =====================================================
    private List<String> resolveFileUrls(String permission, String fileUrlsJson) {
        if ("VIEW_INFO".equals(permission)) {
            return null;
        }
        if (fileUrlsJson == null || fileUrlsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(fileUrlsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    // =====================================================
    // HELPER: allowDownload theo permission
    // =====================================================
    private Boolean resolveAllowDownload(String permission) {
        return "VIEW_ALL".equals(permission);
    }

    // =====================================================
    // INNER CLASS: response khi yêu cầu PIN
    // =====================================================
    public record RequirePinResponse(boolean requirePin) {
        public static RequirePinResponse of(boolean requirePin) {
            return new RequirePinResponse(requirePin);
        }
    }
}