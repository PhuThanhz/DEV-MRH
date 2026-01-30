package vn.system.app.modules.companyprocedure.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

    // ================= CREATE =================
    public CompanyProcedure handleCreate(CompanyProcedureRequest request) {

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

        return repository.save(entity);
    }

    // ================= UPDATE (THÊM MỚI) =================
    public CompanyProcedure handleUpdate(Long id, CompanyProcedureRequest request) {

        CompanyProcedure current = fetchById(id);
        if (current == null) {
            throw new IdInvalidException("Quy trình không tồn tại");
        }

        // CHỈ CHO PHÉP UPDATE CÁC FIELD SAU
        current.setFileUrl(request.getFileUrl());
        current.setStatus(request.getStatus());
        current.setPlanYear(request.getPlanYear());
        current.setNote(request.getNote());

        return repository.save(current);
    }

    // ================= FETCH BY ID =================
    public CompanyProcedure fetchById(Long id) {
        Optional<CompanyProcedure> optional = repository.findById(id);
        return optional.orElse(null);
    }

    // ================= DELETE =================
    public void handleDelete(Long id) {
        repository.deleteById(id);
    }

    // ================= GET ALL COMPANY =================
    // Xem toàn bộ quy trình của toàn công ty
    public List<CompanyProcedureResponse> fetchAllCompany() {
        return repository.findAllByOrderBySection_Department_NameAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY DEPARTMENT =================
    // Xem toàn bộ quy trình theo phòng ban
    public List<CompanyProcedureResponse> fetchByDepartment(Long departmentId) {
        return repository.findBySection_Department_Id(departmentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY SECTION =================
    // Xem toàn bộ quy trình theo bộ phận / team
    public List<CompanyProcedureResponse> fetchBySection(Long sectionId) {
        return repository.findBySection_Id(sectionId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ================= CONVERT RESPONSE =================
    public CompanyProcedureResponse convertToResponse(CompanyProcedure e) {

        CompanyProcedureResponse r = new CompanyProcedureResponse();

        r.setId(e.getId());

        // ===== Company (TRẢ CODE, KHÔNG TRẢ ID) =====
        r.setCompanyCode(
                e.getSection()
                        .getDepartment()
                        .getCompany()
                        .getCode());
        r.setCompanyName(
                e.getSection()
                        .getDepartment()
                        .getCompany()
                        .getName());

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

        // ===== Audit =====
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());

        return r;
    }
}
