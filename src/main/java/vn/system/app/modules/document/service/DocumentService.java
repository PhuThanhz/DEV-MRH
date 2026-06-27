package vn.system.app.modules.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.domain.DocumentAccess;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.domain.request.DocumentRequest;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
import vn.system.app.modules.document.repository.AccountingDocumentCategoryRepository;
import vn.system.app.modules.document.repository.DocumentAccessRepository;
import vn.system.app.modules.document.repository.DocumentRepository;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.documentcategory.repository.DocumentCategoryRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;
import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.companyprocedure.service.CompanyProcedureService;
import vn.system.app.modules.departmentprocedure.domain.request.DepartmentProcedureRequest;
import vn.system.app.modules.departmentprocedure.service.DepartmentProcedureService;
import vn.system.app.modules.confidentialprocedure.domain.request.ConfidentialProcedureRequest;
import vn.system.app.modules.confidentialprocedure.service.ConfidentialProcedureService;
import vn.system.app.modules.procedure.qr.service.ProcedureQrService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.procedure.enums.ProcedureType;
import vn.system.app.modules.documentfolder.domain.DocumentFolder;
import vn.system.app.modules.documentfolder.repository.DocumentFolderRepository;
import vn.system.app.modules.documentfolder.service.DocumentFolderService;
import vn.system.app.modules.document.domain.DocumentShortcut;
import vn.system.app.modules.document.repository.DocumentShortcutRepository;
import vn.system.app.modules.document.domain.DocumentAudit;
import vn.system.app.modules.document.repository.DocumentAuditRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final DocumentAccessRepository accessRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final CompanyProcedureService companyProcedureService;
    private final DepartmentProcedureService departmentProcedureService;
    private final ConfidentialProcedureService confidentialProcedureService;
    private final ProcedureQrService qrService;
    private final UserRepository userRepository;
    private final DocumentFolderRepository folderRepository;
    private final DocumentFolderService folderService;
    private final DocumentShortcutRepository shortcutRepository;
    private final AccountingDocumentCategoryRepository accountingCategoryRepository;
    private final DocumentAuditRepository auditRepository;
    private final vn.system.app.modules.document.repository.DocumentTargetCompanyRepository targetCompanyRepository;

    private static final ObjectMapper mapper = new ObjectMapper();

    public DocumentService(
            DocumentRepository repository,
            DocumentAccessRepository accessRepository,
            DocumentCategoryRepository categoryRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository,
            CompanyProcedureService companyProcedureService,
            DepartmentProcedureService departmentProcedureService,
            ConfidentialProcedureService confidentialProcedureService,
            ProcedureQrService qrService,
            UserRepository userRepository,
            DocumentFolderRepository folderRepository,
            DocumentFolderService folderService,
            DocumentShortcutRepository shortcutRepository,
            AccountingDocumentCategoryRepository accountingCategoryRepository,
            DocumentAuditRepository auditRepository,
            vn.system.app.modules.document.repository.DocumentTargetCompanyRepository targetCompanyRepository) {
        this.repository = repository;
        this.accessRepository = accessRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
        this.companyProcedureService = companyProcedureService;
        this.departmentProcedureService = departmentProcedureService;
        this.confidentialProcedureService = confidentialProcedureService;
        this.qrService = qrService;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.folderService = folderService;
        this.shortcutRepository = shortcutRepository;
        this.accountingCategoryRepository = accountingCategoryRepository;
        this.auditRepository = auditRepository;
        this.targetCompanyRepository = targetCompanyRepository;
    }

    private void logAudit(Document document, String actionType, String changes) {
        DocumentAudit audit = new DocumentAudit();
        audit.setDocumentId(document.getId());
        audit.setActionType(actionType);
        audit.setChanges(changes);
        audit.setCreatedBy(SecurityUtil.getCurrentUserLogin().orElse("System"));
        auditRepository.save(audit);
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Transactional
    public ResDocumentDTO handleCreate(DocumentRequest req) {
        String code = req.getDocumentCode().trim().toUpperCase();

        if (repository.existsByDocumentCode(code)) {
            throw new IdInvalidException("Mã văn bản đã tồn tại: " + code);
        }

        DocumentCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IdInvalidException("Loại văn bản không tồn tại"));

        // Validate logic Mapping Procedure
        if (category.isMappingProcedure()) {
            if (req.getProcedureType() == null) {
                throw new IdInvalidException("Loại văn bản này yêu cầu chọn loại quy trình");
            }
            validateProcedureRequirements(req);
        } else if (category.isCrossCompany()) {
            boolean hasUsers = req.getUserIds() != null && !req.getUserIds().isEmpty();
            boolean hasCompanies = req.getTargetCompanyIds() != null && !req.getTargetCompanyIds().isEmpty();
            if (!hasUsers && !hasCompanies) {
                throw new IdInvalidException("Vui lòng chọn danh sách người nhận hoặc công ty cho văn bản liên công ty");
            }
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }
        Long requestedCompanyId = getCompanyId(department);
        if (requestedCompanyId != null || req.getFolderId() == null) {
            validateScope(requestedCompanyId);
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
            // Validate Section thuộc về Department
            if (department != null && !section.getDepartment().getId().equals(department.getId())) {
                throw new IdInvalidException("Bộ phận không thuộc phòng ban đã chọn");
            }
        }

        Document entity = new Document();
        entity.setDocumentCode(code);
        entity.setDocumentName(req.getDocumentName());
        entity.setCategory(category);
        entity.setAccountingCategory(resolveAccountingCategory(req.getAccountingCategoryId()));
        entity.setDepartment(department);
        entity.setSection(section);
        entity.setStatus(req.getStatus());
        entity.setIssuedDate(req.getIssuedDate());
        entity.setFileUrls(toJsonArray(req.getFileUrls()));
        entity.setNote(req.getNote());

        if (req.getFolderId() != null) {
            DocumentFolder folder = folderRepository.findById(req.getFolderId())
                    .orElseThrow(() -> new IdInvalidException("Thư mục không tồn tại"));
            folderService.validateFolderScope(folder, true);
            entity.setFolder(folder);
        }

        if (req.getProcedureType() != null) {
            entity.setProcedureType(req.getProcedureType());
            if (category.isMappingProcedure()) {
                Long procedureId = autoCreateProcedure(req, code);
                entity.setProcedureId(procedureId);
            }
        }

        Document saved = repository.save(entity);

        saved.setQrToken(qrService.buildQrToken());
        saved.setQrCode(qrService.buildQrBase64(saved.getQrToken()));
        repository.save(saved);

        if (!category.isMappingProcedure()) {
            if (req.getProcedureType() == null || req.getProcedureType() == ProcedureType.CONFIDENTIAL) {
                saveAccessList(saved, req.getUserIds());
            }
        }
        
        saveTargetCompanies(saved, req.getTargetCompanyIds());

        logAudit(saved, "CREATE", "Tạo mới chứng từ: " + saved.getDocumentCode());

        return convertToDTO(saved);
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResDocumentDTO handleUpdate(Long id, DocumentRequest req) {
        Document current = fetchById(id);
        
        if (current.getIsLocked() != null && current.getIsLocked()) {
            throw new PermissionException("Chứng từ này đã được kế toán thu thập và khoá. Không thể chỉnh sửa.");
        }

        validateWriteAccess(current);

        String code = req.getDocumentCode().trim().toUpperCase();

        if (repository.existsByDocumentCodeAndIdNot(code, id)) {
            throw new IdInvalidException("Mã văn bản đã tồn tại: " + code);
        }

        DocumentCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IdInvalidException("Loại văn bản không tồn tại"));

        // Validate logic Mapping Procedure
        if (category.isMappingProcedure()) {
            if (req.getProcedureType() == null) {
                throw new IdInvalidException("Loại văn bản này yêu cầu chọn loại quy trình");
            }
            validateProcedureRequirements(req);
        } else if (category.isCrossCompany()) {
            boolean hasUsers = req.getUserIds() != null && !req.getUserIds().isEmpty();
            boolean hasCompanies = req.getTargetCompanyIds() != null && !req.getTargetCompanyIds().isEmpty();
            if (!hasUsers && !hasCompanies) {
                throw new IdInvalidException("Vui lòng chọn danh sách người nhận hoặc công ty cho văn bản liên công ty");
            }
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }
        Long requestedCompanyId = getCompanyId(department);
        if (requestedCompanyId != null || req.getFolderId() == null) {
            validateScope(requestedCompanyId);
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
            // Validate Section thuộc về Department
            if (department != null && !section.getDepartment().getId().equals(department.getId())) {
                throw new IdInvalidException("Bộ phận không thuộc phòng ban đã chọn");
            }
        }

        current.setDocumentCode(code);
        current.setDocumentName(req.getDocumentName());
        current.setCategory(category);
        current.setAccountingCategory(resolveAccountingCategory(req.getAccountingCategoryId()));
        current.setDepartment(department);
        current.setSection(section);
        current.setStatus(req.getStatus());
        current.setIssuedDate(req.getIssuedDate());
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());

        if (req.getFolderId() != null) {
            DocumentFolder folder = folderRepository.findById(req.getFolderId())
                    .orElseThrow(() -> new IdInvalidException("Thư mục không tồn tại"));
            folderService.validateFolderScope(folder, true);
            current.setFolder(folder);
        } else {
            current.setFolder(null);
        }

        if (req.getProcedureType() != null) {
            current.setProcedureType(req.getProcedureType());
            if (category.isMappingProcedure()) {
                if (current.getProcedureId() != null
                        && current.getProcedureType() == req.getProcedureType()) {
                    autoUpdateProcedure(req, code, current.getProcedureId());
                } else {
                    Long procedureId = autoCreateProcedure(req, code);
                    current.setProcedureId(procedureId);
                }
                accessRepository.deleteByDocument_Id(current.getId());
            } else {
                current.setProcedureId(null);
                accessRepository.deleteByDocument_Id(current.getId());
                if (req.getProcedureType() == ProcedureType.CONFIDENTIAL) {
                    saveAccessList(current, req.getUserIds());
                }
            }
        } else {
            current.setProcedureType(null);
            current.setProcedureId(null);
            accessRepository.deleteByDocument_Id(current.getId());
            if (category.isCrossCompany()) {
                saveAccessList(current, req.getUserIds());
            }
        }

        Document saved = repository.save(current);
        
        targetCompanyRepository.deleteByDocument_Id(saved.getId());
        saveTargetCompanies(saved, req.getTargetCompanyIds());
        
        logAudit(saved, "UPDATE", "Cập nhật chứng từ: " + saved.getDocumentCode());
        return convertToDTO(saved);
    }

    /**
     * Kiểm tra yêu cầu dữ liệu theo từng loại quy trình
     */
    private void validateProcedureRequirements(DocumentRequest req) {
        if (req.getProcedureType() == null)
            return;

        switch (req.getProcedureType()) {
            case DEPARTMENT -> {
                if (req.getDepartmentIds() == null || req.getDepartmentIds().isEmpty()) {
                    throw new IdInvalidException("Vui lòng chọn danh sách phòng ban áp dụng");
                }
            }
            case CONFIDENTIAL -> {
                if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
                    throw new IdInvalidException("Vui lòng chọn danh sách người dùng được quyền xem");
                }
            }
            default -> {
            }
        }
    }

    private AccountingDocumentCategory resolveAccountingCategory(Long accountingCategoryId) {
        if (accountingCategoryId == null) {
            return null;
        }

        AccountingDocumentCategory accountingCategory = accountingCategoryRepository.findById(accountingCategoryId)
                .orElseThrow(() -> new IdInvalidException("Loại chứng từ kế toán không tồn tại"));

        if (!accountingCategory.isActive()) {
            throw new IdInvalidException("Loại chứng từ kế toán đã bị tắt");
        }

        return accountingCategory;
    }

    /**
     * Kiểm tra phạm vi truy cập của người dùng
     */
    private void validateScope(Long companyId) {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null)
            return;

        if (scope.isSuperAdmin() || scope.isAdminLevel())
            return;

        if (companyId == null) {
            throw new PermissionException("Chỉ Quản trị viên hệ thống mới có quyền thao tác dữ liệu toàn cục");
        }

        if (scope.isCompanyLevel()) {
            if (scope.companyIds() == null || !scope.companyIds().contains(companyId)) {
                throw new PermissionException("Bạn không có quyền thao tác dữ liệu cho công ty này");
            }
        } else {
            // Trường hợp User bình thường (không phải admin)
            throw new PermissionException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @Transactional
    public void handleToggleActive(Long id) {
        Document current = fetchById(id);
        validateWriteAccess(current);
        current.setActive(!current.isActive());
        Document saved = repository.save(current);
        logAudit(saved, "STATUS_CHANGE", "Thay đổi hiệu lực sang " + (saved.isActive() ? "Còn hiệu lực" : "Đã hủy"));
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Transactional
    public void handleDelete(Long id) {
        Document current = fetchById(id);
        
        if (current.getIsLocked() != null && current.getIsLocked()) {
            throw new PermissionException("Chứng từ này đã được kế toán thu thập và khoá. Không thể xoá.");
        }

        validateWriteAccess(current);
        accessRepository.deleteByDocument_Id(id);
        targetCompanyRepository.deleteByDocument_Id(id);
        logAudit(current, "DELETE", "Xóa chứng từ: " + current.getDocumentCode());
        repository.deleteById(id);
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public Document fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Văn bản không tồn tại"));
    }

    public Document fetchByIdForRead(Long id) {
        Document document = fetchById(id);
        validateReadAccess(document);
        return document;
    }

    public Document fetchByIdForWrite(Long id) {
        Document document = fetchById(id);
        validateWriteAccess(document);
        return document;
    }

    public void validateAccountingReadAccess(Document document) {
        validateCompanyReadAccess(document);
    }

    public void validateAccountingWriteAccess(Document document) {
        validateWriteAccess(document);
    }

    // =====================================================
    // FETCH ALL
    // =====================================================
    public ResultPaginationDTO fetchAll(Specification<Document> spec, Pageable pageable) {
        Page<Document> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return rs;
    }

    public List<Document> fetchAllList(Specification<Document> spec) {
        return repository.findAll(spec);
    }

    // =====================================================
    // FETCH BY COMPANY
    // =====================================================
    public List<ResDocumentDTO> fetchByCompany(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream()
                .filter(this::canReadDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY DEPARTMENT
    // =====================================================
    public List<ResDocumentDTO> fetchByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream()
                .filter(this::canReadDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY SECTION
    // =====================================================
    public List<ResDocumentDTO> fetchBySection(Long sectionId) {
        return repository.findBySection_Id(sectionId)
                .stream()
                .filter(this::canReadDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY CATEGORY
    // =====================================================
    public List<ResDocumentDTO> fetchByCategory(Long categoryId) {
        return repository.findByCategory_Id(categoryId)
                .stream()
                .filter(this::canReadDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // SAVE ACCESS LIST (private)
    // =====================================================
    private void saveAccessList(Document document, List<String> userIds) {
        if (userIds == null || userIds.isEmpty())
            return;
        String assignedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        Instant now = Instant.now();
        List<DocumentAccess> accessList = userIds.stream().map(userId -> {
            DocumentAccess access = new DocumentAccess();
            access.setDocument(document);
            access.setUserId(userId);
            access.setAssignedBy(assignedBy);
            access.setAssignedAt(now);
            return access;
        }).collect(Collectors.toList());
        accessRepository.saveAll(accessList);
    }

    private void saveTargetCompanies(Document document, List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) return;
        List<vn.system.app.modules.document.domain.DocumentTargetCompany> targetCompanies = companyIds.stream().map(companyId -> {
            vn.system.app.modules.document.domain.DocumentTargetCompany targetCompany = new vn.system.app.modules.document.domain.DocumentTargetCompany();
            targetCompany.setDocument(document);
            targetCompany.setCompanyId(companyId);
            return targetCompany;
        }).collect(Collectors.toList());
        targetCompanyRepository.saveAll(targetCompanies);
    }

    // =====================================================
    // AUTO CREATE PROCEDURE (private)
    // =====================================================
    private Long autoCreateProcedure(DocumentRequest req, String code) {
        return switch (req.getProcedureType()) {
            case COMPANY -> {
                CompanyProcedureRequest r = new CompanyProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentId(req.getDepartmentId());
                r.setSectionId(req.getSectionId());
                yield companyProcedureService.handleCreate(r).getId();
            }
            case DEPARTMENT -> {
                DepartmentProcedureRequest r = new DepartmentProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentIds(req.getDepartmentIds());
                r.setSectionId(req.getSectionId());
                yield departmentProcedureService.handleCreate(r).getId();
            }
            case CONFIDENTIAL -> {
                ConfidentialProcedureRequest r = new ConfidentialProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentId(req.getDepartmentId());
                r.setSectionId(req.getSectionId());
                r.setUserIds(req.getUserIds());
                yield confidentialProcedureService.handleCreate(r).getId();
            }
            default -> throw new IdInvalidException("Loại quy trình không hỗ trợ: " + req.getProcedureType());
        };
    }

    // =====================================================
    // AUTO UPDATE PROCEDURE (private)
    // =====================================================
    private void autoUpdateProcedure(DocumentRequest req, String code, Long procedureId) {
        switch (req.getProcedureType()) {
            case COMPANY -> {
                CompanyProcedureRequest r = new CompanyProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentId(req.getDepartmentId());
                r.setSectionId(req.getSectionId());
                companyProcedureService.handleUpdate(procedureId, r);
            }
            case DEPARTMENT -> {
                DepartmentProcedureRequest r = new DepartmentProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentIds(req.getDepartmentIds());
                r.setSectionId(req.getSectionId());
                departmentProcedureService.handleUpdate(procedureId, r);
            }
            case CONFIDENTIAL -> {
                ConfidentialProcedureRequest r = new ConfidentialProcedureRequest();
                r.setProcedureCode(code);
                r.setProcedureName(req.getDocumentName());
                r.setStatus(req.getStatus());
                r.setIssuedDate(req.getIssuedDate());
                r.setFileUrls(req.getFileUrls());
                r.setNote(req.getNote());
                r.setDepartmentId(req.getDepartmentId());
                r.setSectionId(req.getSectionId());
                r.setUserIds(req.getUserIds());
                confidentialProcedureService.handleUpdate(procedureId, r);
            }
            default -> {
                // Không xử lý cho loại DOCUMENT trong update procedure vì không có procedureId tương ứng
            }
        }
    }

    // =====================================================
    // JSON HELPER
    // =====================================================
    private String toJsonArray(List<String> urls) {
        try {
            return mapper.writeValueAsString(urls != null ? urls : new ArrayList<>());
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonArray(String json) {
        if (json == null || json.isBlank())
            return new ArrayList<>();
        try {
            if (!json.trim().startsWith("["))
                return List.of(json);
            return mapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // =====================================================
    // CONVERT TO DTO
    // =====================================================
    public ResDocumentDTO convertToDTO(Document e) {
        ResDocumentDTO dto = new ResDocumentDTO();
        dto.setId(e.getId());
        dto.setDocumentCode(e.getDocumentCode());
        dto.setDocumentName(e.getDocumentName());

        if (e.getCategory() != null) {
            ResDocumentDTO.CategoryRef cat = new ResDocumentDTO.CategoryRef();
            cat.setId(e.getCategory().getId());
            cat.setCategoryCode(e.getCategory().getCategoryCode());
            cat.setCategoryName(e.getCategory().getCategoryName());
            cat.setSymbol(e.getCategory().getSymbol());
            cat.setMappingProcedure(e.getCategory().isMappingProcedure());
            cat.setCrossCompany(e.getCategory().isCrossCompany());
            dto.setCategory(cat);
        }

        if (e.getAccountingCategory() != null) {
            ResDocumentDTO.AccountingCategoryRef accountingCategory = new ResDocumentDTO.AccountingCategoryRef();
            accountingCategory.setId(e.getAccountingCategory().getId());
            accountingCategory.setCategoryCode(e.getAccountingCategory().getCategoryCode());
            accountingCategory.setCategoryName(e.getAccountingCategory().getCategoryName());
            accountingCategory.setSymbol(e.getAccountingCategory().getSymbol());
            dto.setAccountingCategory(accountingCategory);
        }

        if (e.getDepartment() != null) {
            ResDocumentDTO.DepartmentRef dept = new ResDocumentDTO.DepartmentRef();
            dept.setId(e.getDepartment().getId());
            dept.setName(e.getDepartment().getName());
            if (e.getDepartment().getCompany() != null) {
                dept.setCompanyId(e.getDepartment().getCompany().getId());
                dept.setCompanyName(e.getDepartment().getCompany().getName());
                dept.setCompanyCode(e.getDepartment().getCompany().getCode());
            }
            dto.setDepartment(dept);
        }

        if (e.getSection() != null) {
            ResDocumentDTO.SectionRef sec = new ResDocumentDTO.SectionRef();
            sec.setId(e.getSection().getId());
            sec.setName(e.getSection().getName());
            dto.setSection(sec);
        }

        dto.setStatus(e.getStatus());
        dto.setIssuedDate(e.getIssuedDate());
        dto.setFileUrls(fromJsonArray(e.getFileUrls()));
        dto.setNote(e.getNote());
        dto.setActive(e.isActive());
        dto.setVersion(e.getVersion());

        if (e.getFolder() != null) {
            ResDocumentDTO.FolderRef folderRef = new ResDocumentDTO.FolderRef();
            folderRef.setId(e.getFolder().getId());
            folderRef.setFolderName(e.getFolder().getFolderName());
            dto.setFolder(folderRef);
        }
        dto.setProcedureType(e.getProcedureType());
        dto.setProcedureId(e.getProcedureId());

        if (e.getQrToken() != null) {
            dto.setQrCode(qrService.buildQrBase64(e.getQrToken()));
        }

        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());
        
        List<vn.system.app.modules.document.domain.DocumentTargetCompany> targetComps = targetCompanyRepository.findByDocument_Id(e.getId());
        if (!targetComps.isEmpty()) {
            dto.setTargetCompanyIds(targetComps.stream().map(vn.system.app.modules.document.domain.DocumentTargetCompany::getCompanyId).collect(Collectors.toList()));
        }

        if (e.getCategory() != null && !e.getCategory().isMappingProcedure()) {
            List<DocumentAccess> accesses = accessRepository.findByDocument_Id(e.getId());
            List<String> userIds = accesses.stream()
                    .map(DocumentAccess::getUserId)
                    .collect(Collectors.toList());
            dto.setUserIds(userIds);

            Map<String, String> userNamesMap = new java.util.HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userRepository.findAllById(userIds);
                for (User u : users) {
                    if (u.getId() != null && u.getName() != null) {
                        userNamesMap.put(u.getId(), u.getName());
                    }
                }
            }

            List<ResDocumentDTO.UserAccessRef> accessDetails = accesses.stream().map(a -> {
                ResDocumentDTO.UserAccessRef ref = new ResDocumentDTO.UserAccessRef();
                ref.setUserId(a.getUserId());
                ref.setUserName(userNamesMap.getOrDefault(a.getUserId(), ""));
                ref.setIsRead(a.getIsRead() != null ? a.getIsRead() : false);
                ref.setReadAt(a.getReadAt());
                ref.setAssignedAt(a.getAssignedAt());
                return ref;
            }).collect(Collectors.toList());
            dto.setAccessDetails(accessDetails);
        }

        return dto;
    }


    // =====================================================
    // LOCK DOCUMENT (Accounting)
    // =====================================================
    @Transactional
    public void handleLockDocument(Long id, boolean lockStatus) {
        Document document = fetchById(id);
        
        vn.system.app.common.util.UserScopeContext.UserScope scope = vn.system.app.common.util.UserScopeContext.get();
        if (scope == null || (!scope.isSuperAdmin() && !scope.isAdminLevel())) {
            throw new PermissionException("Chỉ Quản trị viên Kế toán mới có quyền thực hiện thao tác này");
        }

        document.setIsLocked(lockStatus);
        
        String currentUser = SecurityUtil.getCurrentUserLogin().orElse("System");
        if (lockStatus) {
            document.setLockedAt(Instant.now());
            document.setLockedBy(currentUser);
            logAudit(document, "LOCK", "Đã thu thập và khoá chứng từ bởi " + currentUser);
        } else {
            document.setLockedAt(null);
            document.setLockedBy(null);
            logAudit(document, "UNLOCK", "Đã mở khoá chứng từ bởi " + currentUser);
        }
        
        repository.save(document);
    }

    // =====================================================
    // MARK AS READ
    // =====================================================
    @Transactional
    public void handleMarkAsRead(Long id) {
        fetchByIdForRead(id);
        String currentUserId = SecurityUtil.getCurrentUserId().orElseThrow(() -> new PermissionException("Chưa đăng nhập"));
        List<DocumentAccess> accesses = accessRepository.findByDocument_IdAndUserId(id, currentUserId);
        for (DocumentAccess access : accesses) {
            if (access.getIsRead() == null || !access.getIsRead()) {
                access.setIsRead(true);
                access.setReadAt(Instant.now());
                accessRepository.save(access);
            }
        }
    }

    // =====================================================
    // MANAGE SHORTCUTS
    // =====================================================
    @Transactional
    public void createShortcut(Long documentId, Long folderId) {
        Document document = fetchById(documentId);
        checkAccessForShortcut(document);

        DocumentFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IdInvalidException("Thư mục không tồn tại"));
        folderService.validateFolderScope(folder, true);

        if (shortcutRepository.existsByDocumentIdAndFolderId(documentId, folderId)) {
            throw new IdInvalidException("Lối tắt này đã tồn tại trong thư mục");
        }

        DocumentShortcut shortcut = new DocumentShortcut();
        shortcut.setDocument(document);
        shortcut.setFolder(folder);
        shortcut.setUserId(SecurityUtil.getCurrentUserId().orElse(""));
        shortcut.setCreatedAt(Instant.now());
        
        shortcutRepository.save(shortcut);
    }

    @Transactional
    public void deleteShortcut(Long documentId, Long folderId) {
        DocumentFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IdInvalidException("Thư mục không tồn tại"));
        folderService.validateFolderScope(folder, true);

        shortcutRepository.deleteByDocumentIdAndFolderId(documentId, folderId);
    }

    private void checkAccessForShortcut(Document document) {
        validateReadAccess(document);
    }

    private boolean canReadDocument(Document document) {
        try {
            validateReadAccess(document);
            return true;
        } catch (PermissionException | IdInvalidException ex) {
            return false;
        }
    }

    private void validateReadAccess(Document document) {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && (scope.isSuperAdmin() || scope.isAdminLevel())) {
            return;
        }
        if (scope == null) {
            return;
        }

        String currentUserId = SecurityUtil.getCurrentUserId().orElse("");
        String currentUserLogin = SecurityUtil.getCurrentUserLogin().orElse("");

        if (document.getCreatedBy() != null && document.getCreatedBy().equals(currentUserLogin)) {
            return;
        }

        boolean inAccessList = accessRepository.existsByDocument_IdAndUserId(document.getId(), currentUserId);
        if (inAccessList) {
            return;
        }

        if (document.getFolder() != null) {
            try {
                folderService.validateFolderScope(document.getFolder(), false);
                return;
            } catch (PermissionException ignored) {
                // Continue with document-level scope checks below.
            }
        }

        Long companyId = getCompanyId(document.getDepartment());
        if (scope.isCompanyLevel() && companyId != null && scope.companyIds() != null
                && scope.companyIds().contains(companyId)) {
            return;
        }

        if (document.getProcedureType() == ProcedureType.COMPANY && companyId != null
                && scope.companyIds() != null && scope.companyIds().contains(companyId)) {
            return;
        }

        if (document.getProcedureType() == ProcedureType.DEPARTMENT && scope.departmentIds() != null) {
            if (document.getDepartment() != null) {
                if (scope.departmentIds().contains(document.getDepartment().getId())) {
                    return;
                }
            }
        }
        
        if (scope.companyIds() != null && !scope.companyIds().isEmpty()) {
            boolean inTargetCompany = targetCompanyRepository.existsByDocument_IdAndCompanyIdIn(document.getId(), scope.companyIds());
            if (inTargetCompany) return;
        }

        throw new PermissionException("Bạn không có quyền truy cập tài liệu này");
    }

    private void validateWriteAccess(Document document) {
        if (document.getFolder() != null) {
            folderService.validateFolderScope(document.getFolder(), true);
            return;
        }
        validateScope(getCompanyId(document.getDepartment()));
    }

    private void validateCompanyReadAccess(Document document) {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
            return;
        }

        Long companyId = getCompanyId(document.getDepartment());
        if (companyId == null) {
            throw new PermissionException("Bạn không có quyền truy cập tài liệu toàn cục");
        }

        if (scope.companyIds() == null || !scope.companyIds().contains(companyId)) {
            throw new PermissionException("Bạn không có quyền truy cập tài liệu của công ty này");
        }
    }

    private Long getCompanyId(Department department) {
        return department != null && department.getCompany() != null
                ? department.getCompany().getId()
                : null;
    }
}
