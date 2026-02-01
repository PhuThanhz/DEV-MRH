package vn.system.app.modules.companyprocedure.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.companyprocedure.domain.response.CompanyProcedureResponse;
import vn.system.app.modules.companyprocedure.repository.CompanyProcedureRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

@Service
public class CompanyProcedureService {

    private final CompanyProcedureRepository repository;
    private final SectionRepository sectionRepository;

    public CompanyProcedureService(
            CompanyProcedureRepository repository,
            SectionRepository sectionRepository) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
    }

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @Transactional
    public CompanyProcedureResponse handleCreate(CompanyProcedureRequest request) {

        if (repository.existsBySection_IdAndProcedureName(
                request.getSectionId(),
                request.getProcedureName())) {
            throw new IdInvalidException("Quy trình đã tồn tại trong bộ phận này");
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bộ phận"));

        CompanyProcedure entity = new CompanyProcedure();
        entity.setSection(section);
        entity.setProcedureName(request.getProcedureName());
        entity.setFileUrl(request.getFileUrl());
        entity.setStatus(request.getStatus());
        entity.setPlanYear(request.getPlanYear());
        entity.setNote(request.getNote());
        entity.setActive(true); // mặc định bật khi tạo mới

        entity = repository.save(entity);
        return convertToResponse(entity);
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @Transactional
    public CompanyProcedureResponse handleUpdate(Long id, CompanyProcedureRequest request) {

        CompanyProcedure current = fetchById(id);

        current.setFileUrl(request.getFileUrl());
        current.setStatus(request.getStatus());
        current.setPlanYear(request.getPlanYear());
        current.setNote(request.getNote());

        current = repository.save(current);
        return convertToResponse(current);
    }

    /*
     * =====================================================
     * TOGGLE ACTIVE (BẬT / TẮT)
     * =====================================================
     */
    @Transactional
    public void handleToggleActive(Long id) {
        CompanyProcedure current = fetchById(id);
        current.setActive(!current.isActive());
        repository.save(current);
    }

    /*
     * =====================================================
     * FETCH ONE
     * =====================================================
     */
    public CompanyProcedure fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy quy trình"));
    }

    /*
     * =====================================================
     * FETCH ALL (PAGINATION + FILTER)
     * =====================================================
     */
    public ResultPaginationDTO fetchAll(Specification<CompanyProcedure> spec, Pageable pageable) {
        Page<CompanyProcedure> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        List<CompanyProcedureResponse> list = page.getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        rs.setResult(list);

        return rs;
    }

    /*
     * =====================================================
     * FETCH BY DEPARTMENT
     * =====================================================
     */
    public List<CompanyProcedureResponse> fetchByDepartment(Long departmentId) {
        return repository.findBySection_Department_Id(departmentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * FETCH BY SECTION
     * =====================================================
     */
    public List<CompanyProcedureResponse> fetchBySection(Long sectionId) {
        return repository.findBySection_Id(sectionId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * CONVERT TO RESPONSE
     * =====================================================
     */
    public CompanyProcedureResponse convertToResponse(CompanyProcedure e) {
        CompanyProcedureResponse r = new CompanyProcedureResponse();

        r.setId(e.getId());

        // ===== Company =====
        r.setCompanyCode(e.getSection().getDepartment().getCompany().getCode());
        r.setCompanyName(e.getSection().getDepartment().getCompany().getName());

        // ===== Department =====
        r.setDepartmentId(e.getSection().getDepartment().getId());
        r.setDepartmentName(e.getSection().getDepartment().getName());

        // ===== Section =====
        r.setSectionId(e.getSection().getId());
        r.setSectionName(e.getSection().getName());

        // ===== Procedure =====
        r.setProcedureName(e.getProcedureName());
        r.setFileUrl(e.getFileUrl());
        r.setStatus(e.getStatus());
        r.setPlanYear(e.getPlanYear());
        r.setNote(e.getNote());
        r.setActive(e.isActive());

        // ===== Audit =====
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setCreatedBy(e.getCreatedBy());
        r.setUpdatedBy(e.getUpdatedBy());

        return r;
    }
}
