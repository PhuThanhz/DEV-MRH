package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierAuditLog;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategory;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierDocument;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierSequence;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStatus;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierCategoryRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierAuditLogDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierCategoryDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDocumentDTO;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierAuditLogRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierCategoryRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierDocumentRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierSequenceRepository;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.repository.AccountingDocumentCategoryRepository;
import vn.system.app.modules.document.repository.DocumentRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

@Service
public class AccountingDossierService {
    private static final Set<String> SUPPORTED_DOCUMENT_TYPES = Set.of(
            "PDF", "EXCEL", "WORD", "PPT", "CSV", "XML", "VIDEO_LINK", "NAS_PATH", "PNG", "JPG", "OTHER");

    private final AccountingDossierRepository repository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final AccountingDossierSequenceRepository sequenceRepository;
    private final AccountingDossierAuditLogRepository auditLogRepository;
    private final AccountingDossierDocumentRepository documentItemRepository;
    private final AccountingDossierCategoryRepository dossierCategoryRepository;
    private final AccountingDocumentCategoryRepository accountingCategoryRepository;
    private final DocumentRepository documentRepository;

    public AccountingDossierService(
            AccountingDossierRepository repository,
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository,
            AccountingDossierSequenceRepository sequenceRepository,
            AccountingDossierAuditLogRepository auditLogRepository,
            AccountingDossierDocumentRepository documentItemRepository,
            AccountingDossierCategoryRepository dossierCategoryRepository,
            AccountingDocumentCategoryRepository accountingCategoryRepository,
            DocumentRepository documentRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
        this.sequenceRepository = sequenceRepository;
        this.auditLogRepository = auditLogRepository;
        this.documentItemRepository = documentItemRepository;
        this.dossierCategoryRepository = dossierCategoryRepository;
        this.accountingCategoryRepository = accountingCategoryRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public ResAccountingDossierDTO create(AccountingDossierRequest req) {
        Company company = resolveCompany(req.getCompanyId());
        Department department = resolveDepartment(req.getDepartmentId(), company.getId());
        Section section = resolveSection(req.getSectionId(), department.getId());

        validateCompanyScope(company.getId());

        AccountingDossier entity = new AccountingDossier();
        applyRequest(entity, req, company, department, section);
        entity.setStatus(AccountingDossierStatus.DRAFT);

        AccountingDossier saved = repository.save(entity);
        createTemplateDocumentRows(saved);
        writeLog(saved, "CREATE_DOSSIER", saved.getDossierCategory() == null
                ? saved.isSyncCategoryRequested()
                        ? "Tạo bộ chứng từ nháp phi cấu trúc, có đề xuất lưu thành mẫu"
                        : "Tạo bộ chứng từ nháp phi cấu trúc"
                : "Tạo bộ chứng từ nháp theo mẫu: " + saved.getDossierCategory().getCategoryName());
        return convertToDTO(saved);
    }

    @Transactional
    public ResAccountingDossierDTO update(Long id, AccountingDossierRequest req) {
        AccountingDossier current = fetchById(id);
        validateEditable(current);

        Company company = resolveCompany(req.getCompanyId());
        Department department = resolveDepartment(req.getDepartmentId(), company.getId());
        Section section = resolveSection(req.getSectionId(), department.getId());

        validateCompanyScope(company.getId());
        Long oldCategoryId = current.getDossierCategory() == null ? null : current.getDossierCategory().getId();
        applyRequest(current, req, company, department, section);

        AccountingDossier saved = repository.save(current);
        Long newCategoryId = saved.getDossierCategory() == null ? null : saved.getDossierCategory().getId();
        writeLog(saved, "UPDATE_DOSSIER", !java.util.Objects.equals(oldCategoryId, newCategoryId)
                ? "Cập nhật bộ chứng từ, đổi mẫu sử dụng"
                : "Cập nhật bộ chứng từ");
        return convertToDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        AccountingDossier current = fetchById(id);
        validateEditable(current);
        validateCompanyScope(current.getCompany().getId());
        auditLogRepository.deleteByDossierId(id);
        documentItemRepository.deleteByDossierId(id);
        repository.delete(current);
    }

    @Transactional
    public ResAccountingDossierDTO submit(Long id) {
        AccountingDossier dossier = fetchById(id);
        if (dossier.getStatus() != AccountingDossierStatus.DRAFT
                && dossier.getStatus() != AccountingDossierStatus.RETURNED) {
            throw new PermissionException("Chỉ có thể chuyển xử lý bộ chứng từ đang ở trạng thái Nháp hoặc Bị hoàn trả");
        }

        long docsCount = documentItemRepository.countByDossierId(id);
        if (docsCount == 0) {
            throw new IdInvalidException("Không thể chuyển xử lý bộ chứng từ rỗng (cần ít nhất 1 chứng từ con)");
        }

        if (dossier.getDossierCode() == null || dossier.getDossierCode().trim().isEmpty()) {
            dossier.setDossierCode(generateDossierCode(dossier.getCompany()));
        }

        dossier.setStatus(AccountingDossierStatus.SUBMITTED);
        dossier.setSubmittedAt(Instant.now());

        AccountingDossier saved = repository.save(dossier);
        writeLog(saved, "SUBMIT_DOSSIER", "Chuyển bộ chứng từ và cấp mã hệ thống");
        return convertToDTO(saved);
    }

    @Transactional
    public ResAccountingDossierDTO requestReturn(Long id, AccountingDossierActionRequest req) {
        AccountingDossier dossier = fetchById(id);
        if (dossier.getStatus() == AccountingDossierStatus.DRAFT
                || dossier.getStatus() == AccountingDossierStatus.RETURNED
                || dossier.getStatus() == AccountingDossierStatus.APPROVED
                || dossier.getStatus() == AccountingDossierStatus.TERMINATED) {
            throw new PermissionException("Chỉ bộ chứng từ đã chuyển xử lý mới được yêu cầu hoàn chứng từ");
        }

        dossier.setStatus(AccountingDossierStatus.RETURN_REQUESTED);
        AccountingDossier saved = repository.save(dossier);
        writeLog(saved, "REQUEST_RETURN_DOSSIER",
                normalizeNote(req == null ? null : req.getNote(), "Người lập yêu cầu hoàn chứng từ"));
        return convertToDTO(saved);
    }

    public AccountingDossier fetchById(Long id) {
        AccountingDossier dossier = repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bộ chứng từ kế toán không tồn tại"));
        validateCompanyScope(dossier.getCompany().getId());
        return dossier;
    }

    public ResAccountingDossierDTO getOne(Long id) {
        return convertToDTO(fetchById(id));
    }

    public List<ResAccountingDossierAuditLogDTO> fetchLogs(Long id) {
        fetchById(id);
        return auditLogRepository.findByDossierIdOrderByCreatedAtDesc(id).stream()
                .map(this::convertToLogDTO)
                .collect(Collectors.toList());
    }

    public ResultPaginationDTO fetchAll(Specification<AccountingDossier> spec, Pageable pageable) {
        Specification<AccountingDossier> scopeSpec = ScopeSpec.byCompanyScope("company.id");
        Specification<AccountingDossier> finalSpec = spec == null ? scopeSpec : spec.and(scopeSpec);
        Page<AccountingDossier> page = repository.findAll(finalSpec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()));
        return rs;
    }

    private void applyRequest(
            AccountingDossier entity,
            AccountingDossierRequest req,
            Company company,
            Department department,
            Section section) {
        AccountingDossierCategoryMode mode = req.getCategoryMode() == null
                ? AccountingDossierCategoryMode.TEMPLATE
                : req.getCategoryMode();
        AccountingDossierCategory dossierCategory = resolveDossierCategory(req, company.getId(), mode);

        entity.setContent(req.getContent().trim());
        entity.setCategoryMode(mode);
        entity.setDossierCategory(dossierCategory);
        entity.setDossierCategoryVersion(dossierCategory == null ? null : dossierCategory.getVersion());
        entity.setCustomCategoryName(mode == AccountingDossierCategoryMode.UNSTRUCTURED
                ? req.getCustomCategoryName().trim()
                : null);
        entity.setSyncCategoryRequested(mode == AccountingDossierCategoryMode.UNSTRUCTURED
                && Boolean.TRUE.equals(req.getSyncCategoryRequested()));
        entity.setCompany(company);
        entity.setDepartment(department);
        entity.setSection(section);
        if (entity.getRetentionYears() == null) {
            entity.setRetentionYears(10);
        }
        if (entity.getRetentionUntil() == null) {
            entity.setRetentionUntil(ZonedDateTime.now(ZoneId.systemDefault())
                    .plusYears(entity.getRetentionYears())
                    .toInstant());
        }
    }

    private AccountingDossierCategory resolveDossierCategory(
            AccountingDossierRequest req,
            Long companyId,
            AccountingDossierCategoryMode mode) {
        if (mode == AccountingDossierCategoryMode.UNSTRUCTURED) {
            if (req.getCustomCategoryName() == null || req.getCustomCategoryName().trim().isEmpty()) {
                throw new IdInvalidException("Vui lòng nhập tên danh mục phi cấu trúc");
            }
            return null;
        }
        if (req.getDossierCategoryId() == null) {
            throw new IdInvalidException("Vui lòng chọn mẫu bộ chứng từ");
        }

        AccountingDossierCategory category = dossierCategoryRepository.findById(req.getDossierCategoryId())
                .orElseThrow(() -> new IdInvalidException("Mẫu bộ chứng từ không tồn tại"));
        if (!category.isActive()) {
            throw new IdInvalidException("Mẫu bộ chứng từ đã ngưng sử dụng");
        }
        if ("COMPANY".equalsIgnoreCase(category.getScope())
                && category.getCompanyId() != null
                && !category.getCompanyId().equals(companyId)) {
            throw new PermissionException("Mẫu bộ chứng từ không áp dụng cho công ty đã chọn");
        }
        return category;
    }

    private void createTemplateDocumentRows(AccountingDossier dossier) {
        if (dossier.getCategoryMode() != AccountingDossierCategoryMode.TEMPLATE
                || dossier.getDossierCategory() == null
                || dossier.getDossierCategory().getDocumentCategories() == null) {
            return;
        }
        for (AccountingDocumentCategory category : dossier.getDossierCategory().getDocumentCategories()) {
            AccountingDossierDocument item = new AccountingDossierDocument();
            item.setDossier(dossier);
            item.setAccountingCategory(category);
            item.setDocumentName(category.getCategoryName());
            item.setDocumentType("OTHER");
            documentItemRepository.save(item);
        }
    }

    private Company resolveCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại"));
    }

    private Department resolveDepartment(Long departmentId, Long companyId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        if (department.getCompany() == null || department.getCompany().getId() != companyId) {
            throw new IdInvalidException("Phòng ban không thuộc công ty đã chọn");
        }
        return department;
    }

    private Section resolveSection(Long sectionId, Long departmentId) {
        if (sectionId == null) {
            return null;
        }
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        if (section.getDepartment() == null || !section.getDepartment().getId().equals(departmentId)) {
            throw new IdInvalidException("Bộ phận không thuộc phòng ban đã chọn");
        }
        return section;
    }

    private void validateEditable(AccountingDossier dossier) {
        if (dossier.getStatus() != AccountingDossierStatus.DRAFT
                && dossier.getStatus() != AccountingDossierStatus.RETURNED) {
            throw new PermissionException("Chỉ bộ chứng từ ở trạng thái nháp/hoàn mới được chỉnh sửa hoặc xoá");
        }
    }

    private void validateCompanyScope(Long companyId) {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
            return;
        }
        if (scope.companyIds() == null || !scope.companyIds().contains(companyId)) {
            throw new PermissionException("Bạn không có quyền thao tác dữ liệu cho công ty này");
        }
    }

    public ResAccountingDossierDTO convertToDTO(AccountingDossier entity) {
        ResAccountingDossierDTO dto = new ResAccountingDossierDTO();
        dto.setId(entity.getId());
        dto.setDossierCode(entity.getDossierCode());
        dto.setContent(entity.getContent());
        dto.setCategoryMode(entity.getCategoryMode());
        dto.setCustomCategoryName(entity.getCustomCategoryName());
        dto.setDossierCategoryVersion(entity.getDossierCategoryVersion());
        dto.setSyncCategoryRequested(entity.isSyncCategoryRequested());
        dto.setCreatorId(entity.getCreatorId());
        dto.setStatus(entity.getStatus());
        dto.setStorageStatus(entity.getStorageStatus());
        dto.setRetentionYears(entity.getRetentionYears());
        dto.setRetentionUntil(entity.getRetentionUntil());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setTerminatedAt(entity.getTerminatedAt());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        if (entity.getCompany() != null) {
            dto.setCompany(toRef(entity.getCompany().getId(), entity.getCompany().getCode(), entity.getCompany().getName()));
        }
        if (entity.getDepartment() != null) {
            dto.setDepartment(toRef(entity.getDepartment().getId(), entity.getDepartment().getCode(), entity.getDepartment().getName()));
        }
        if (entity.getSection() != null) {
            dto.setSection(toRef(entity.getSection().getId(), entity.getSection().getCode(), entity.getSection().getName()));
        }
        if (entity.getDossierCategory() != null) {
            dto.setDossierCategory(toRef(entity.getDossierCategory().getId(),
                    entity.getDossierCategory().getCategoryCode(),
                    entity.getDossierCategory().getCategoryName()));
        }

        return dto;
    }

    private ResAccountingDossierDTO.Ref toRef(Long id, String code, String name) {
        ResAccountingDossierDTO.Ref ref = new ResAccountingDossierDTO.Ref();
        ref.setId(id);
        ref.setCode(code);
        ref.setName(name);
        return ref;
    }

    // ==================== DOSSIER TEMPLATE MASTER DATA ====================

    public ResultPaginationDTO fetchCategories(Specification<AccountingDossierCategory> spec, Pageable pageable) {
        Page<AccountingDossierCategory> page = dossierCategoryRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent().stream().map(this::convertToCategoryDTO).collect(Collectors.toList()));
        return rs;
    }

    public List<ResAccountingDossierCategoryDTO> fetchActiveCategories() {
        return dossierCategoryRepository.findByActiveTrueOrderByCategoryNameAsc().stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResAccountingDossierCategoryDTO createCategory(AccountingDossierCategoryRequest req) {
        AccountingDossierCategory entity = new AccountingDossierCategory();
        applyCategoryRequest(entity, req, false);
        return convertToCategoryDTO(dossierCategoryRepository.save(entity));
    }

    @Transactional
    public ResAccountingDossierCategoryDTO updateCategory(Long id, AccountingDossierCategoryRequest req) {
        AccountingDossierCategory entity = dossierCategoryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Mẫu bộ chứng từ không tồn tại"));
        applyCategoryRequest(entity, req, true);
        return convertToCategoryDTO(dossierCategoryRepository.save(entity));
    }

    @Transactional
    public ResAccountingDossierCategoryDTO toggleCategoryActive(Long id, boolean active) {
        AccountingDossierCategory entity = dossierCategoryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Mẫu bộ chứng từ không tồn tại"));
        entity.setActive(active);
        return convertToCategoryDTO(dossierCategoryRepository.save(entity));
    }

    private void applyCategoryRequest(AccountingDossierCategory entity, AccountingDossierCategoryRequest req, boolean updating) {
        entity.setCategoryName(req.getCategoryName().trim());
        entity.setDescription(req.getDescription() == null ? null : req.getDescription().trim());
        entity.setCompanyId(req.getCompanyId());
        entity.setScope(req.getScope() == null || req.getScope().trim().isEmpty() ? "GLOBAL" : req.getScope().trim().toUpperCase());
        entity.setSource(entity.getSource() == null ? "MANUAL" : entity.getSource());
        entity.setActive(req.isActive());
        entity.setVersion(entity.getVersion() == null ? 1 : updating ? entity.getVersion() + 1 : entity.getVersion());

        String code = req.getCategoryCode() == null ? null : req.getCategoryCode().trim();
        if (code == null || code.isEmpty()) {
            code = "BCTM-" + System.currentTimeMillis();
        }
        if (!code.equals(entity.getCategoryCode()) && dossierCategoryRepository.findByCategoryCode(code).isPresent()) {
            throw new IdInvalidException("Mã mẫu bộ chứng từ đã tồn tại");
        }
        entity.setCategoryCode(code);

        List<AccountingDocumentCategory> docs = req.getDocumentCategoryIds() == null
                ? List.of()
                : req.getDocumentCategoryIds().stream()
                        .map(docCategoryId -> accountingCategoryRepository.findById(docCategoryId)
                                .orElseThrow(() -> new IdInvalidException("Loại chứng từ không tồn tại: " + docCategoryId)))
                        .collect(Collectors.toList());
        entity.setDocumentCategories(docs);
    }

    private ResAccountingDossierCategoryDTO convertToCategoryDTO(AccountingDossierCategory entity) {
        ResAccountingDossierCategoryDTO dto = new ResAccountingDossierCategoryDTO();
        dto.setId(entity.getId());
        dto.setCategoryCode(entity.getCategoryCode());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setCompanyId(entity.getCompanyId());
        dto.setScope(entity.getScope());
        dto.setSource(entity.getSource());
        dto.setVersion(entity.getVersion());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        if (entity.getDocumentCategories() != null) {
            dto.setDocumentCategories(entity.getDocumentCategories().stream().map(item -> {
                ResAccountingDossierCategoryDTO.DocumentCategoryRef ref = new ResAccountingDossierCategoryDTO.DocumentCategoryRef();
                ref.setId(item.getId());
                ref.setCategoryCode(item.getCategoryCode());
                ref.setCategoryName(item.getCategoryName());
                ref.setSymbol(item.getSymbol());
                return ref;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    // ==================== DOCUMENT ITEMS ====================

    public List<ResAccountingDossierDocumentDTO> fetchAllDocuments(Long dossierId) {
        fetchById(dossierId);
        return documentItemRepository.findByDossierId(dossierId).stream()
                .map(this::convertToDocumentDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResAccountingDossierDocumentDTO addDocument(Long dossierId, AccountingDossierDocumentRequest req) {
        AccountingDossier dossier = fetchById(dossierId);
        validateEditable(dossier);

        AccountingDossierDocument item = new AccountingDossierDocument();
        item.setDossier(dossier);
        applyDocumentRequest(item, req);

        AccountingDossierDocument saved = documentItemRepository.save(item);
        writeLog(dossier, "ADD_DOCUMENT_ITEM", "Thêm chứng từ con: " + saved.getDocumentName());
        return convertToDocumentDTO(saved);
    }

    @Transactional
    public ResAccountingDossierDocumentDTO updateDocument(Long dossierId, Long docId, AccountingDossierDocumentRequest req) {
        AccountingDossier dossier = fetchById(dossierId);
        validateEditable(dossier);

        AccountingDossierDocument item = documentItemRepository.findById(docId)
                .orElseThrow(() -> new IdInvalidException("Chứng từ con không tồn tại"));
        if (!item.getDossier().getId().equals(dossierId)) {
            throw new IdInvalidException("Chứng từ con không thuộc bộ chứng từ này");
        }

        applyDocumentRequest(item, req);
        AccountingDossierDocument saved = documentItemRepository.save(item);
        writeLog(dossier, "UPDATE_DOCUMENT_ITEM", "Sửa chứng từ con: " + saved.getDocumentName());
        return convertToDocumentDTO(saved);
    }

    @Transactional
    public void deleteDocument(Long dossierId, Long docId) {
        AccountingDossier dossier = fetchById(dossierId);
        validateEditable(dossier);

        AccountingDossierDocument item = documentItemRepository.findById(docId)
                .orElseThrow(() -> new IdInvalidException("Chứng từ con không tồn tại"));
        if (!item.getDossier().getId().equals(dossierId)) {
            throw new IdInvalidException("Chứng từ con không thuộc bộ chứng từ này");
        }

        String docName = item.getDocumentName();
        documentItemRepository.delete(item);
        writeLog(dossier, "DELETE_DOCUMENT_ITEM", "Xóa chứng từ con: " + docName);
    }

    private void applyDocumentRequest(AccountingDossierDocument item, AccountingDossierDocumentRequest req) {
        AccountingDocumentCategory category = accountingCategoryRepository.findById(req.getAccountingCategoryId())
                .orElseThrow(() -> new IdInvalidException("Danh mục loại chứng từ không tồn tại"));
        item.setAccountingCategory(category);
        item.setDocumentName(req.getDocumentName().trim());
        item.setDocumentType(req.getDocumentType() == null || req.getDocumentType().trim().isEmpty()
                ? "OTHER"
                : req.getDocumentType().trim().toUpperCase());
        if (!SUPPORTED_DOCUMENT_TYPES.contains(item.getDocumentType())) {
            throw new IdInvalidException("Định dạng chứng từ không được hỗ trợ");
        }

        if (req.getDocumentId() != null) {
            Document doc = documentRepository.findById(req.getDocumentId())
                    .orElseThrow(() -> new IdInvalidException("File/Văn bản đính kèm không tồn tại"));
            item.setDocument(doc);
        } else {
            item.setDocument(null);
        }
    }

    private ResAccountingDossierDocumentDTO convertToDocumentDTO(AccountingDossierDocument entity) {
        ResAccountingDossierDocumentDTO dto = new ResAccountingDossierDocumentDTO();
        dto.setId(entity.getId());
        dto.setDossierId(entity.getDossier().getId());
        dto.setDocumentName(entity.getDocumentName());
        dto.setDocumentType(entity.getDocumentType());
        dto.setCheckStatus(entity.getCheckStatus());
        dto.setCheckNote(entity.getCheckNote());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        if (entity.getAccountingCategory() != null) {
            ResAccountingDossierDocumentDTO.CategoryRef ref = new ResAccountingDossierDocumentDTO.CategoryRef();
            ref.setId(entity.getAccountingCategory().getId());
            ref.setCategoryCode(entity.getAccountingCategory().getCategoryCode());
            ref.setCategoryName(entity.getAccountingCategory().getCategoryName());
            dto.setAccountingCategory(ref);
        }
        if (entity.getDocument() != null) {
            ResAccountingDossierDocumentDTO.DocumentRef ref = new ResAccountingDossierDocumentDTO.DocumentRef();
            ref.setId(entity.getDocument().getId());
            ref.setDocumentCode(entity.getDocument().getDocumentCode());
            ref.setDocumentName(entity.getDocument().getDocumentName());
            dto.setDocument(ref);
        }
        return dto;
    }

    private String generateDossierCode(Company company) {
        int currentYear = Year.now().getValue();
        AccountingDossierSequence seq = sequenceRepository.findByCompanyIdAndYearWithLock(company.getId(), currentYear)
                .orElseGet(() -> {
                    AccountingDossierSequence newSeq = new AccountingDossierSequence();
                    newSeq.setCompany(company);
                    newSeq.setYear(currentYear);
                    newSeq.setCurrentNumber(0);
                    return newSeq;
                });
        seq.setCurrentNumber(seq.getCurrentNumber() + 1);
        sequenceRepository.save(seq);
        return String.format("BCT-%d-%06d", currentYear, seq.getCurrentNumber());
    }

    private void writeLog(AccountingDossier dossier, String actionType, String note) {
        AccountingDossierAuditLog log = new AccountingDossierAuditLog();
        log.setDossier(dossier);
        log.setActionType(actionType);
        log.setNote(note);
        log.setIpAddress(resolveClientIp());
        auditLogRepository.save(log);
    }

    private String normalizeNote(String note, String fallback) {
        if (note == null || note.trim().isEmpty()) {
            return fallback;
        }
        return note.trim();
    }

    private String resolveClientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        String forwardedFor = attrs.getRequest().getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return attrs.getRequest().getRemoteAddr();
    }

    private ResAccountingDossierAuditLogDTO convertToLogDTO(AccountingDossierAuditLog entity) {
        ResAccountingDossierAuditLogDTO dto = new ResAccountingDossierAuditLogDTO();
        dto.setId(entity.getId());
        dto.setDossierId(entity.getDossier().getId());
        dto.setActionType(entity.getActionType());
        dto.setNote(entity.getNote());
        dto.setActorUserId(entity.getActorUserId());
        dto.setIpAddress(entity.getIpAddress());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }
}
