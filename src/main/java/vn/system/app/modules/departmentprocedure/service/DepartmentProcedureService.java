package vn.system.app.modules.departmentprocedure.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;

import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedureHistory;
import vn.system.app.modules.departmentprocedure.domain.request.DepartmentProcedureRequest;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureDTO;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureHistoryDTO;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureHistoryRepository;
import vn.system.app.common.util.UserScopeContext;

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

        if (req.getDepartmentId() != null &&
                repository.existsByDepartment_IdAndProcedureName(
                        req.getDepartmentId(), req.getProcedureName())) {
            throw new IdInvalidException("Quy trình đã tồn tại trong phòng ban này");
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        DepartmentProcedure entity = new DepartmentProcedure();
        entity.setProcedureName(req.getProcedureName());
        entity.setStatus(req.getStatus());
        entity.setPlanYear(req.getPlanYear());
        entity.setFileUrls(toJsonArray(req.getFileUrls())); // ← đổi
        entity.setNote(req.getNote());
        entity.setActive(true);
        entity.setVersion(1);
        entity.setDepartment(department);
        entity.setSection(section);

        return convertToDTO(repository.save(entity));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResDepartmentProcedureDTO handleUpdate(Long id, DepartmentProcedureRequest req) {

        DepartmentProcedure current = fetchById(id);
        saveHistory(current, "EDIT");

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setFileUrls(toJsonArray(req.getFileUrls())); // ← đổi
        current.setNote(req.getNote());
        current.setDepartment(department);
        current.setSection(section);

        return convertToDTO(repository.save(current));
    }

    // =====================================================
    // REVISE
    // =====================================================
    @Transactional
    public ResDepartmentProcedureDTO handleRevise(Long id, DepartmentProcedureRequest req) {

        DepartmentProcedure current = fetchById(id);
        saveHistory(current, "REVISE");

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        }

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setFileUrls(toJsonArray(req.getFileUrls())); // ← đổi
        current.setNote(req.getNote());
        current.setDepartment(department);
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
        // ── ADMIN_SUB_2 filter ────────────────────────────────
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && !scope.isSuperAdmin()) {
            Specification<DepartmentProcedure> scopeSpec = ScopeSpec.byCompanyScope("department.company.id");
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
    // FETCH BY DEPARTMENT
    // =====================================================
    public List<ResDepartmentProcedureDTO> fetchByDepartment(Long departmentId) {
        Specification<DepartmentProcedure> spec = (root, query, cb) -> cb.equal(root.get("department").get("id"),
                departmentId);
        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return repository.findAll(spec)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY SECTION
    // =====================================================
    public List<ResDepartmentProcedureDTO> fetchBySection(Long sectionId) {
        Specification<DepartmentProcedure> spec = (root, query, cb) -> cb.equal(root.get("section").get("id"),
                sectionId);
        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return repository.findAll(spec)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH HISTORY
    // =====================================================
    public List<ResDepartmentProcedureHistoryDTO> fetchHistory(Long procedureId) {
        fetchById(procedureId);
        return historyRepository.findByProcedure_IdOrderByVersionDesc(procedureId)
                .stream()
                .map(this::convertHistoryToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY COMPANY
    // =====================================================
    public List<ResDepartmentProcedureDTO> fetchByCompany(Long companyId) {
        return repository.findByDepartment_Company_Id(companyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // SAVE HISTORY (private)
    // =====================================================
    private void saveHistory(DepartmentProcedure e, String action) {
        DepartmentProcedureHistory history = new DepartmentProcedureHistory();
        history.setProcedure(e);
        history.setVersion(e.getVersion());
        history.setProcedureName(e.getProcedureName());
        history.setStatus(e.getStatus());
        history.setPlanYear(e.getPlanYear());
        history.setFileUrls(e.getFileUrls()); // ← đổi
        history.setNote(e.getNote());
        history.setDepartmentName(e.getDepartment() != null ? e.getDepartment().getName() : null);
        history.setSectionName(e.getSection() != null ? e.getSection().getName() : null);
        history.setAction(action);
        history.setChangedAt(Instant.now());
        history.setChangedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        historyRepository.save(history);
    }

    // =====================================================
    // HELPER — JSON array
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
                return List.of(json); // backward compatible
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

        if (e.getDepartment() != null) {
            dto.setDepartmentId(e.getDepartment().getId());
            dto.setDepartmentName(e.getDepartment().getName());
            if (e.getDepartment().getCompany() != null) {
                dto.setCompanyCode(e.getDepartment().getCompany().getCode());
                dto.setCompanyName(e.getDepartment().getCompany().getName());
            }
        }

        if (e.getSection() != null) {
            dto.setSectionId(e.getSection().getId());
            dto.setSectionName(e.getSection().getName());
        }

        dto.setProcedureName(e.getProcedureName());
        dto.setStatus(e.getStatus());
        dto.setPlanYear(e.getPlanYear());
        dto.setFileUrls(fromJsonArray(e.getFileUrls())); // ← đổi
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
        dto.setProcedureName(h.getProcedureName());
        dto.setStatus(h.getStatus());
        dto.setPlanYear(h.getPlanYear());
        dto.setFileUrls(fromJsonArray(h.getFileUrls())); // ← đổi
        dto.setNote(h.getNote());
        dto.setDepartmentName(h.getDepartmentName());
        dto.setSectionName(h.getSectionName());
        dto.setAction(h.getAction());
        dto.setChangedAt(h.getChangedAt());
        dto.setChangedBy(h.getChangedBy());
        return dto;
    }
}