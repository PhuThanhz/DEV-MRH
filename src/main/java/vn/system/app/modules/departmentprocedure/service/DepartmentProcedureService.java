package vn.system.app.modules.departmentprocedure.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedureHistory;
import vn.system.app.modules.departmentprocedure.domain.request.DepartmentProcedureRequest;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureDTO;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureHistoryDTO;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureHistoryRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentProcedureService {

    private final DepartmentProcedureRepository repository;
    private final DepartmentProcedureHistoryRepository historyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;

    private static final ObjectMapper mapper = new ObjectMapper();

    public DepartmentProcedureService(
            DepartmentProcedureRepository repository,
            DepartmentProcedureHistoryRepository historyRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Transactional
    public ResDepartmentProcedureDTO handleCreate(DepartmentProcedureRequest req) {

        String code = req.getProcedureCode().trim().toUpperCase();

        List<Department> departments = departmentRepository.findAllById(req.getDepartmentIds());
        if (departments.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy phòng ban nào");
        }

        for (Department dept : departments) {
            if (repository.existsByDepartmentIdAndProcedureCode(dept.getId(), code)) {
                throw new IdInvalidException(
                        "Mã quy trình đã tồn tại trong phòng ban: " + dept.getName());
            }
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        DepartmentProcedure entity = new DepartmentProcedure();
        entity.setProcedureCode(code);
        entity.setProcedureName(req.getProcedureName());
        entity.setStatus(req.getStatus());
        entity.setPlanYear(req.getPlanYear());
        entity.setIssuedDate(req.getIssuedDate());
        entity.setFileUrls(toJsonArray(req.getFileUrls()));
        entity.setNote(req.getNote());
        entity.setActive(true);
        entity.setVersion(1);
        entity.setDepartments(departments);
        entity.setSection(section);

        return convertToDTO(repository.save(entity));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResDepartmentProcedureDTO handleUpdate(Long id, DepartmentProcedureRequest req) {

        DepartmentProcedure current = fetchById(id);
        String code = req.getProcedureCode().trim().toUpperCase();

        List<Department> departments = departmentRepository.findAllById(req.getDepartmentIds());
        if (departments.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy phòng ban nào");
        }

        for (Department dept : departments) {
            if (repository.existsByDepartmentIdAndProcedureCodeAndIdNot(dept.getId(), code, id)) {
                throw new IdInvalidException(
                        "Mã quy trình đã tồn tại trong phòng ban: " + dept.getName());
            }
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        saveHistory(current, "EDIT");

        current.setProcedureCode(code);
        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setIssuedDate(req.getIssuedDate());
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());
        current.setDepartments(departments);
        current.setSection(section);

        return convertToDTO(repository.save(current));
    }

    // =====================================================
    // REVISE
    // =====================================================
    @Transactional
    public ResDepartmentProcedureDTO handleRevise(Long id, DepartmentProcedureRequest req) {

        DepartmentProcedure current = fetchById(id);
        String code = req.getProcedureCode().trim().toUpperCase();

        List<Department> departments = departmentRepository.findAllById(req.getDepartmentIds());
        if (departments.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy phòng ban nào");
        }

        for (Department dept : departments) {
            if (repository.existsByDepartmentIdAndProcedureCodeAndIdNot(dept.getId(), code, id)) {
                throw new IdInvalidException(
                        "Mã quy trình đã tồn tại trong phòng ban: " + dept.getName());
            }
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        saveHistory(current, "REVISE");

        current.setProcedureCode(code);
        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setIssuedDate(req.getIssuedDate());
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());
        current.setDepartments(departments);
        current.setSection(section);
        current.setVersion(current.getVersion() + 1);

        return convertToDTO(repository.save(current));
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @Transactional
    public void handleToggleActive(Long id) {
        DepartmentProcedure current = fetchById(id);
        current.setActive(!current.isActive());
        repository.save(current);
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Transactional
    public void handleDelete(Long id) {
        fetchById(id);
        repository.deleteById(id);
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public DepartmentProcedure fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quy trình không tồn tại"));
    }

    // =====================================================
    // FETCH ALL
    // =====================================================
    public ResultPaginationDTO fetchAll(
            Specification<DepartmentProcedure> spec, Pageable pageable) {

        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && !scope.isSuperAdmin()) {
            Specification<DepartmentProcedure> scopeSpec = ScopeSpec.byCompanyScope("departments.company.id");
            spec = Specification.where(spec).and(scopeSpec);
        }

        Page<DepartmentProcedure> page = repository.findAll(spec, pageable);

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
    public List<ResDepartmentProcedureDTO> fetchByCompany(Long companyId) {
        return repository.findByCompanyId(companyId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY DEPARTMENT
    // =====================================================
    public List<ResDepartmentProcedureDTO> fetchByDepartment(Long departmentId) {
        return repository.findByDepartmentId(departmentId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY SECTION
    // =====================================================
    public List<ResDepartmentProcedureDTO> fetchBySection(Long sectionId) {
        Specification<DepartmentProcedure> spec = (root, query, cb) -> cb.equal(
                root.get("section").get("id"), sectionId);
        return repository.findAll(spec)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH HISTORY
    // =====================================================
    public List<ResDepartmentProcedureHistoryDTO> fetchHistory(Long procedureId) {
        fetchById(procedureId);
        return historyRepository.findByProcedure_IdOrderByVersionDesc(procedureId)
                .stream().map(this::convertHistoryToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // SAVE HISTORY (private)
    // =====================================================
    private void saveHistory(DepartmentProcedure e, String action) {
        DepartmentProcedureHistory history = new DepartmentProcedureHistory();
        history.setProcedure(e);
        history.setVersion(e.getVersion());
        history.setProcedureCode(e.getProcedureCode());
        history.setProcedureName(e.getProcedureName());
        history.setStatus(e.getStatus());
        history.setPlanYear(e.getPlanYear());
        history.setIssuedDate(e.getIssuedDate());
        history.setFileUrls(e.getFileUrls());
        history.setNote(e.getNote());
        // ✅ Ghép tên các phòng ban thành 1 string, ngăn cách bởi dấu phẩy
        history.setDepartmentName(
                e.getDepartments() != null
                        ? e.getDepartments().stream()
                                .map(Department::getName)
                                .collect(Collectors.joining(", "))
                        : null);
        history.setSectionName(e.getSection() != null ? e.getSection().getName() : null);
        history.setAction(action);
        history.setChangedAt(Instant.now());
        history.setChangedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        historyRepository.save(history);
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
    public ResDepartmentProcedureDTO convertToDTO(DepartmentProcedure e) {
        ResDepartmentProcedureDTO dto = new ResDepartmentProcedureDTO();
        dto.setId(e.getId());

        if (e.getDepartments() != null) {
            List<ResDepartmentProcedureDTO.DepartmentRef> deptRefs = e.getDepartments()
                    .stream()
                    .map(d -> {
                        ResDepartmentProcedureDTO.DepartmentRef ref = new ResDepartmentProcedureDTO.DepartmentRef();
                        ref.setId(d.getId());
                        ref.setName(d.getName());
                        if (d.getCompany() != null) {
                            ref.setCompanyId(d.getCompany().getId()); // ✅ THÊM
                            ref.setCompanyName(d.getCompany().getName());
                            ref.setCompanyCode(d.getCompany().getCode());
                        }
                        return ref;
                    })
                    .collect(Collectors.toList());
            dto.setDepartments(deptRefs);
        }

        if (e.getSection() != null) {
            dto.setSectionId(e.getSection().getId());
            dto.setSectionName(e.getSection().getName());
        }

        dto.setProcedureCode(e.getProcedureCode());
        dto.setProcedureName(e.getProcedureName());
        dto.setStatus(e.getStatus());
        dto.setPlanYear(e.getPlanYear());
        dto.setIssuedDate(e.getIssuedDate());
        dto.setFileUrls(fromJsonArray(e.getFileUrls()));
        dto.setNote(e.getNote());
        dto.setActive(e.isActive());
        dto.setVersion(e.getVersion());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        return dto;
    }

    // =====================================================
    // CONVERT HISTORY TO DTO
    // =====================================================
    public ResDepartmentProcedureHistoryDTO convertHistoryToDTO(DepartmentProcedureHistory h) {
        ResDepartmentProcedureHistoryDTO dto = new ResDepartmentProcedureHistoryDTO();
        dto.setId(h.getId());
        dto.setProcedureId(h.getProcedure().getId());
        dto.setVersion(h.getVersion());
        dto.setProcedureCode(h.getProcedureCode());
        dto.setProcedureName(h.getProcedureName());
        dto.setStatus(h.getStatus());
        dto.setPlanYear(h.getPlanYear());
        dto.setIssuedDate(h.getIssuedDate());
        dto.setFileUrls(fromJsonArray(h.getFileUrls()));
        dto.setNote(h.getNote());
        dto.setDepartmentName(h.getDepartmentName()); // ✅ String bình thường
        dto.setSectionName(h.getSectionName());
        dto.setAction(h.getAction());
        dto.setChangedAt(h.getChangedAt());
        dto.setChangedBy(h.getChangedBy());
        return dto;
    }
}