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
import vn.system.app.modules.document.domain.request.DocumentRequest;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
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
            UserRepository userRepository) {
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
            if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
                throw new IdInvalidException("Vui lòng chọn danh sách người nhận cho văn bản liên công ty");
            }
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }
        validateScope(department != null && department.getCompany() != null ? department.getCompany().getId() : null);

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
        entity.setDepartment(department);
        entity.setSection(section);
        entity.setStatus(req.getStatus());
        entity.setIssuedDate(req.getIssuedDate());
        entity.setFileUrls(toJsonArray(req.getFileUrls()));
        entity.setNote(req.getNote());

        if (category.isMappingProcedure()) {
            entity.setProcedureType(req.getProcedureType());
            Long procedureId = autoCreateProcedure(req, code);
            entity.setProcedureId(procedureId);
        }

        Document saved = repository.save(entity);

        saved.setQrToken(qrService.buildQrToken());
        saved.setQrCode(qrService.buildQrBase64(saved.getQrToken()));
        repository.save(saved);

        if (!category.isMappingProcedure()) {
            saveAccessList(saved, req.getUserIds());
        }

        return convertToDTO(saved);
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResDocumentDTO handleUpdate(Long id, DocumentRequest req) {
        Document current = fetchById(id);
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
            if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
                throw new IdInvalidException("Vui lòng chọn danh sách người nhận cho văn bản liên công ty");
            }
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }
        validateScope(department != null && department.getCompany() != null ? department.getCompany().getId() : null);

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
        current.setDepartment(department);
        current.setSection(section);
        current.setStatus(req.getStatus());
        current.setIssuedDate(req.getIssuedDate());
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());

        if (category.isMappingProcedure()) {
            current.setProcedureType(req.getProcedureType());
            if (current.getProcedureId() != null
                    && req.getProcedureType() != null
                    && req.getProcedureType() == current.getProcedureType()) {
                autoUpdateProcedure(req, code, current.getProcedureId());
            } else {
                Long procedureId = autoCreateProcedure(req, code);
                current.setProcedureId(procedureId);
            }
            accessRepository.deleteByDocument_Id(current.getId());
        } else {
            current.setProcedureType(null);
            current.setProcedureId(null);
            accessRepository.deleteByDocument_Id(current.getId());
            saveAccessList(current, req.getUserIds());
        }

        return convertToDTO(repository.save(current));
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
        validateScope(current.getDepartment() != null && current.getDepartment().getCompany() != null
                ? current.getDepartment().getCompany().getId() : null);
        current.setActive(!current.isActive());
        repository.save(current);
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Transactional
    public void handleDelete(Long id) {
        Document current = fetchById(id);
        validateScope(current.getDepartment() != null && current.getDepartment().getCompany() != null
                ? current.getDepartment().getCompany().getId() : null);
        accessRepository.deleteByDocument_Id(id);
        repository.deleteById(id);
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public Document fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Văn bản không tồn tại"));
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

    // =====================================================
    // FETCH BY COMPANY
    // =====================================================
    public List<ResDocumentDTO> fetchByCompany(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY DEPARTMENT
    // =====================================================
    public List<ResDocumentDTO> fetchByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY SECTION
    // =====================================================
    public List<ResDocumentDTO> fetchBySection(Long sectionId) {
        return repository.findBySection_Id(sectionId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY CATEGORY
    // =====================================================
    public List<ResDocumentDTO> fetchByCategory(Long categoryId) {
        return repository.findByCategory_Id(categoryId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
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
            dto.setCategory(cat);
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
        dto.setProcedureType(e.getProcedureType());
        dto.setProcedureId(e.getProcedureId());

        if (e.getQrToken() != null) {
            dto.setQrCode(qrService.buildQrBase64(e.getQrToken()));
        }

        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

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
    // MARK AS READ
    // =====================================================
    @Transactional
    public void handleMarkAsRead(Long id) {
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
}