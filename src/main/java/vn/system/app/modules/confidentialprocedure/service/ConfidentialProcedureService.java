package vn.system.app.modules.confidentialprocedure.service;

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

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Join;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;
import vn.system.app.modules.confidentialprocedure.domain.*;
import vn.system.app.modules.confidentialprocedure.domain.request.ConfidentialProcedureRequest;
import vn.system.app.modules.confidentialprocedure.domain.response.ResConfidentialProcedureDTO;
import vn.system.app.modules.confidentialprocedure.domain.response.ResConfidentialProcedureHistoryDTO;
import vn.system.app.modules.confidentialprocedure.repository.*;

@Service
public class ConfidentialProcedureService {

    private final ConfidentialProcedureRepository repository;
    private final ConfidentialProcedureHistoryRepository historyRepository;
    private final ConfidentialProcedureAccessRepository accessRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ConfidentialProcedureService(
            ConfidentialProcedureRepository repository,
            ConfidentialProcedureHistoryRepository historyRepository,
            ConfidentialProcedureAccessRepository accessRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository) {

        this.repository = repository;
        this.historyRepository = historyRepository;
        this.accessRepository = accessRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Transactional
    public ResConfidentialProcedureDTO handleCreate(ConfidentialProcedureRequest req) {

        String code = req.getProcedureCode().trim().toUpperCase();

        if (req.getDepartmentId() != null &&
                repository.existsByDepartment_IdAndProcedureCode(req.getDepartmentId(), code)) {
            throw new IdInvalidException("Mã quy trình đã tồn tại trong phòng ban này");
        }

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        ConfidentialProcedure entity = new ConfidentialProcedure();
        entity.setProcedureCode(code);
        entity.setProcedureName(req.getProcedureName());
        entity.setStatus(req.getStatus());
        entity.setPlanYear(req.getPlanYear());
        entity.setFileUrls(toJsonArray(req.getFileUrls()));
        entity.setNote(req.getNote());
        entity.setActive(true);
        entity.setVersion(1);
        entity.setDepartment(department);
        entity.setSection(section);
        entity.setIssuedDate(req.getIssuedDate()); // ← THÊM

        ConfidentialProcedure saved = repository.save(entity);
        saveAccessList(saved, req);

        return convertToDTO(saved);
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResConfidentialProcedureDTO handleUpdate(Long id, ConfidentialProcedureRequest req) {

        ConfidentialProcedure current = fetchById(id);
        String code = req.getProcedureCode().trim().toUpperCase();

        if (req.getDepartmentId() != null &&
                repository.existsByDepartment_IdAndProcedureCodeAndIdNot(
                        req.getDepartmentId(), code, id)) {
            throw new IdInvalidException("Mã quy trình đã tồn tại trong phòng ban này");
        }

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

        Section section = null;
        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Bộ phận không tồn tại"));
        }

        current.setProcedureCode(code);
        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());
        current.setDepartment(department);
        current.setSection(section);
        current.setIssuedDate(req.getIssuedDate()); // ← THÊM

        current.getAccessList().clear();
        ConfidentialProcedure saved = repository.save(current);
        saveAccessList(saved, req);

        return convertToDTO(saved);
    }

    // =====================================================
    // REVISE
    // =====================================================
    @Transactional
    public ResConfidentialProcedureDTO handleRevise(Long id, ConfidentialProcedureRequest req) {

        ConfidentialProcedure current = fetchById(id);
        String code = req.getProcedureCode().trim().toUpperCase();

        if (req.getDepartmentId() != null &&
                repository.existsByDepartment_IdAndProcedureCodeAndIdNot(
                        req.getDepartmentId(), code, id)) {
            throw new IdInvalidException("Mã quy trình đã tồn tại trong phòng ban này");
        }

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

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
        current.setFileUrls(toJsonArray(req.getFileUrls()));
        current.setNote(req.getNote());
        current.setDepartment(department);
        current.setSection(section);
        current.setVersion(current.getVersion() + 1);
        current.setIssuedDate(req.getIssuedDate()); // ← THÊM

        current.getAccessList().clear();
        ConfidentialProcedure saved = repository.save(current);
        saveAccessList(saved, req);

        return convertToDTO(saved);
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @Transactional
    public void handleToggleActive(Long id) {
        ConfidentialProcedure current = fetchById(id);
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
    // CHECK PERMISSION
    // =====================================================
    public boolean hasAccess(Long procedureId, Long userId, List<Long> userRoleIds) {
        List<ConfidentialProcedureAccess> accessList = accessRepository.findByProcedure_Id(procedureId);

        return accessList.stream().anyMatch(a -> {
            if ("USER".equals(a.getAccessType())) {
                return userId.equals(a.getUserId());
            }
            if ("ROLE".equals(a.getAccessType())) {
                return userRoleIds != null && userRoleIds.contains(a.getRoleId());
            }
            return false;
        });
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public ConfidentialProcedure fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quy trình không tồn tại"));
    }

    // =====================================================
    // FETCH ALL
    // =====================================================
    public ResultPaginationDTO fetchAll(
            Specification<ConfidentialProcedure> spec, Pageable pageable) {

        Specification<ConfidentialProcedure> userSpec = filterByCurrentUser();
        Specification<ConfidentialProcedure> finalSpec = spec == null ? userSpec : spec.and(userSpec);

        Page<ConfidentialProcedure> page = repository.findAll(finalSpec, pageable);

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
    // FETCH BY COMPANY / DEPARTMENT / SECTION
    // =====================================================
    public List<ResConfidentialProcedureDTO> fetchByCompany(Long companyId) {
        return repository.findByDepartment_Company_Id(companyId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ResConfidentialProcedureDTO> fetchByDepartment(Long departmentId) {
        Specification<ConfidentialProcedure> spec = (root, query, cb) -> cb.equal(
                root.get("department").get("id"), departmentId);
        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        spec = spec.and(filterByCurrentUser());
        return repository.findAll(spec)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ResConfidentialProcedureDTO> fetchBySection(Long sectionId) {
        Specification<ConfidentialProcedure> spec = (root, query, cb) -> cb.equal(
                root.get("section").get("id"), sectionId);
        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        spec = spec.and(filterByCurrentUser());
        return repository.findAll(spec)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH HISTORY
    // =====================================================
    public List<ResConfidentialProcedureHistoryDTO> fetchHistory(Long procedureId) {
        fetchById(procedureId);
        return historyRepository.findByProcedure_IdOrderByVersionDesc(procedureId)
                .stream().map(this::convertHistoryToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // SAVE ACCESS LIST (private)
    // =====================================================
    private void saveAccessList(ConfidentialProcedure procedure, ConfidentialProcedureRequest req) {
        List<ConfidentialProcedureAccess> accesses = new ArrayList<>();

        if (req.getUserIds() != null) {

            Long currentUserId = getCurrentUserId(); // 🔥 THÊM DÒNG NÀY

            req.getUserIds().forEach(userId -> {
                ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
                access.setProcedure(procedure);
                access.setUserId(userId);
                access.setAccessType("USER");

                // 🔥 THÊM 2 DÒNG NÀY
                access.setAssignedBy(currentUserId);
                access.setAssignedAt(Instant.now());

                accesses.add(access);
            });
        }
        if (req.getRoleIds() != null) {
            req.getRoleIds().forEach(roleId -> {
                ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
                access.setProcedure(procedure);
                access.setRoleId(roleId);
                access.setAccessType("ROLE");
                accesses.add(access);
            });
        }

        accessRepository.saveAll(accesses);

    }

    // =====================================================
    // CHECK ACCESS
    // =====================================================
    public void checkAccess(Long procedureId) {
        String currentUser = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng"));

        User user = userRepository.findByEmail(currentUser);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        List<ConfidentialProcedureAccess> accessList = accessRepository.findByProcedure_Id(procedureId);

        boolean allowed = accessList.stream()
                .anyMatch(a -> "USER".equals(a.getAccessType()) && user.getId().equals(a.getUserId()));

        if (!allowed) {
            throw new IdInvalidException("Bạn không có quyền truy cập quy trình bảo mật này");
        }
    }

    // =====================================================
    // FILTER BY CURRENT USER (Specification)
    // =====================================================
    private Specification<ConfidentialProcedure> filterByCurrentUser() {
        return (root, query, cb) -> {

            String username = SecurityUtil.getCurrentUserLogin()
                    .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng"));

            User user = userRepository.findByEmail(username);
            if (user == null) {
                throw new IdInvalidException("Người dùng không tồn tại");
            }

            if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
                return cb.conjunction();
            }

            Long currentUserId = user.getId();

            Join<ConfidentialProcedure, ConfidentialProcedureAccess> join = root.join("accessList", JoinType.INNER);

            query.distinct(true);

            return cb.and(
                    cb.equal(join.get("accessType"), "USER"),
                    cb.equal(join.get("userId"), currentUserId));
        };
    }

    // =====================================================
    // SAVE HISTORY (private)
    // =====================================================
    private void saveHistory(ConfidentialProcedure e, String action) {
        ConfidentialProcedureHistory history = new ConfidentialProcedureHistory();
        history.setProcedure(e);
        history.setVersion(e.getVersion());
        history.setProcedureCode(e.getProcedureCode());
        history.setProcedureName(e.getProcedureName());
        history.setStatus(e.getStatus());
        history.setPlanYear(e.getPlanYear());
        history.setFileUrls(e.getFileUrls());
        history.setNote(e.getNote());
        history.setIssuedDate(e.getIssuedDate()); // ← THÊM
        history.setDepartmentName(e.getDepartment() != null ? e.getDepartment().getName() : null);
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
    public ResConfidentialProcedureDTO convertToDTO(ConfidentialProcedure e) {
        ResConfidentialProcedureDTO dto = new ResConfidentialProcedureDTO();
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

        dto.setProcedureCode(e.getProcedureCode());
        dto.setProcedureName(e.getProcedureName());
        dto.setStatus(e.getStatus());
        dto.setPlanYear(e.getPlanYear());
        dto.setFileUrls(fromJsonArray(e.getFileUrls()));
        dto.setNote(e.getNote());
        dto.setActive(e.isActive());
        dto.setVersion(e.getVersion());
        dto.setIssuedDate(e.getIssuedDate()); // ← THÊM
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        List<ConfidentialProcedureAccess> accessList = e.getAccessList();
        List<String> assignedByList = accessList.stream()
                .filter(a -> "USER".equals(a.getAccessType()))
                .map(a -> {
                    if (a.getAssignedBy() == null)
                        return null;
                    return userRepository.findById(a.getAssignedBy())
                            .map(User::getName)
                            .orElse(null);
                })
                .filter(x -> x != null)
                .distinct()
                .collect(Collectors.toList());

        dto.setAssignedByList(assignedByList);
        dto.setUserIds(accessList.stream()
                .filter(a -> "USER".equals(a.getAccessType()))
                .map(ConfidentialProcedureAccess::getUserId)
                .collect(Collectors.toList()));
        dto.setRoleIds(accessList.stream()
                .filter(a -> "ROLE".equals(a.getAccessType()))
                .map(ConfidentialProcedureAccess::getRoleId)
                .collect(Collectors.toList()));

        return dto;
    }

    // =====================================================
    // CONVERT HISTORY TO DTO
    // =====================================================
    public ResConfidentialProcedureHistoryDTO convertHistoryToDTO(ConfidentialProcedureHistory h) {
        ResConfidentialProcedureHistoryDTO dto = new ResConfidentialProcedureHistoryDTO();
        dto.setId(h.getId());
        dto.setProcedureId(h.getProcedure().getId());
        dto.setVersion(h.getVersion());
        dto.setProcedureCode(h.getProcedureCode());
        dto.setProcedureName(h.getProcedureName());
        dto.setStatus(h.getStatus());
        dto.setPlanYear(h.getPlanYear());
        dto.setIssuedDate(h.getIssuedDate()); // ← THÊM
        dto.setFileUrls(fromJsonArray(h.getFileUrls()));
        dto.setNote(h.getNote());
        dto.setDepartmentName(h.getDepartmentName());
        dto.setSectionName(h.getSectionName());
        dto.setAction(h.getAction());
        dto.setChangedAt(h.getChangedAt());
        dto.setChangedBy(h.getChangedBy());
        return dto;
    }

    private Long getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định user"));

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("User không tồn tại");
        }
        return user.getId();
    }
}