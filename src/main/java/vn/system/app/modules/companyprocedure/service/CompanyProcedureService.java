package vn.system.app.modules.companyprocedure.service;

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
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;

import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.domain.CompanyProcedureHistory;
import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.companyprocedure.domain.response.ResCompanyProcedureDTO;
import vn.system.app.modules.companyprocedure.domain.response.ResCompanyProcedureHistoryDTO;
import vn.system.app.modules.companyprocedure.repository.CompanyProcedureRepository;
import vn.system.app.modules.companyprocedure.repository.CompanyProcedureHistoryRepository;

@Service
public class CompanyProcedureService {

    private final CompanyProcedureRepository repository;
    private final CompanyProcedureHistoryRepository historyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private static final ObjectMapper mapper = new ObjectMapper();

    public CompanyProcedureService(
            CompanyProcedureRepository repository,
            CompanyProcedureHistoryRepository historyRepository,
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
    public ResCompanyProcedureDTO handleCreate(CompanyProcedureRequest req) {

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

        CompanyProcedure entity = new CompanyProcedure();
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
    public ResCompanyProcedureDTO handleUpdate(Long id, CompanyProcedureRequest req) {

        CompanyProcedure current = fetchById(id);
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
    public ResCompanyProcedureDTO handleRevise(Long id, CompanyProcedureRequest req) {

        CompanyProcedure current = fetchById(id);
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
        CompanyProcedure current = fetchById(id);
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
    public CompanyProcedure fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quy trình không tồn tại"));
    }

    // =====================================================
    // FETCH ALL
    // =====================================================
    public ResultPaginationDTO fetchAll(
            Specification<CompanyProcedure> spec, Pageable pageable) {

        Page<CompanyProcedure> page = repository.findAll(spec, pageable);

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
    public List<ResCompanyProcedureDTO> fetchByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY SECTION
    // =====================================================
    public List<ResCompanyProcedureDTO> fetchBySection(Long sectionId) {
        return repository.findBySection_Id(sectionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH HISTORY
    // =====================================================
    public List<ResCompanyProcedureHistoryDTO> fetchHistory(Long procedureId) {
        fetchById(procedureId);
        return historyRepository.findByProcedure_IdOrderByVersionDesc(procedureId)
                .stream()
                .map(this::convertHistoryToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY COMPANY
    // =====================================================
    public List<ResCompanyProcedureDTO> fetchByCompany(Long companyId) {
        return repository.findByDepartment_Company_Id(companyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // SAVE HISTORY (private)
    // =====================================================
    private void saveHistory(CompanyProcedure e, String action) {
        CompanyProcedureHistory history = new CompanyProcedureHistory();
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
    public ResCompanyProcedureDTO convertToDTO(CompanyProcedure e) {

        ResCompanyProcedureDTO dto = new ResCompanyProcedureDTO();

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
    public ResCompanyProcedureHistoryDTO convertHistoryToDTO(CompanyProcedureHistory h) {
        ResCompanyProcedureHistoryDTO dto = new ResCompanyProcedureHistoryDTO();
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