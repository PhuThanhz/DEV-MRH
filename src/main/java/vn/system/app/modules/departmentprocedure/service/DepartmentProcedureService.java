package vn.system.app.modules.departmentprocedure.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;

import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.repository.SectionRepository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.request.ReqCreateDepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureDTO;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;

@Service
public class DepartmentProcedureService {

    private final DepartmentProcedureRepository repository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;

    public DepartmentProcedureService(
            DepartmentProcedureRepository repository,
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository) {

        this.repository = repository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
    }

    /*
     * CREATE
     */
    public DepartmentProcedure handleCreate(ReqCreateDepartmentProcedure req) throws IdInvalidException {

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new IdInvalidException("Company không tồn tại"));

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Department không tồn tại"));

        Section section = null;

        if (req.getSectionId() != null) {
            section = sectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new IdInvalidException("Section không tồn tại"));
        }

        DepartmentProcedure p = new DepartmentProcedure();

        p.setProcedureName(req.getProcedureName());
        p.setStatus(req.getStatus());
        p.setPlanYear(req.getPlanYear());
        p.setFileUrl(req.getFileUrl());
        p.setNote(req.getNote());
        p.setActive(req.isActive());

        p.setCompany(company);
        p.setDepartment(department);
        p.setSection(section);

        return repository.save(p);
    }

    /*
     * DELETE
     */
    public void handleDelete(Long id) throws IdInvalidException {

        DepartmentProcedure procedure = fetchById(id);

        if (procedure == null) {
            throw new IdInvalidException("Procedure không tồn tại");
        }

        repository.deleteById(id);
    }

    /*
     * FIND BY ID
     */
    public DepartmentProcedure fetchById(Long id) {
        Optional<DepartmentProcedure> procedureOptional = repository.findById(id);
        return procedureOptional.orElse(null);
    }

    /*
     * UPDATE
     */
    public DepartmentProcedure handleUpdate(Long id, ReqCreateDepartmentProcedure req) throws IdInvalidException {

        DepartmentProcedure current = fetchById(id);

        if (current == null) {
            throw new IdInvalidException("Procedure không tồn tại");
        }

        current.setProcedureName(req.getProcedureName());
        current.setStatus(req.getStatus());
        current.setPlanYear(req.getPlanYear());
        current.setFileUrl(req.getFileUrl());
        current.setNote(req.getNote());
        current.setActive(req.isActive());

        return repository.save(current);
    }

    /*
     * FETCH ALL
     */
    public ResultPaginationDTO fetchAll(
            Specification<DepartmentProcedure> spec,
            Pageable pageable) {

        Page<DepartmentProcedure> pageProcedure = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageProcedure.getTotalPages());
        mt.setTotal(pageProcedure.getTotalElements());

        rs.setMeta(mt);

        List<ResDepartmentProcedureDTO> listProcedure = pageProcedure.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        rs.setResult(listProcedure);

        return rs;
    }

    /*
     * CONVERT ENTITY → DTO
     */
    public ResDepartmentProcedureDTO convertToDTO(DepartmentProcedure procedure) {

        ResDepartmentProcedureDTO dto = new ResDepartmentProcedureDTO();

        dto.setId(procedure.getId());
        dto.setProcedureName(procedure.getProcedureName());

        dto.setCompanyName(
                procedure.getCompany() != null
                        ? procedure.getCompany().getName()
                        : null);

        dto.setDepartmentName(
                procedure.getDepartment() != null
                        ? procedure.getDepartment().getName()
                        : null);

        dto.setSectionName(
                procedure.getSection() != null
                        ? procedure.getSection().getName()
                        : null);

        dto.setStatus(procedure.getStatus());
        dto.setPlanYear(procedure.getPlanYear());
        dto.setFileUrl(procedure.getFileUrl());
        dto.setNote(procedure.getNote());
        dto.setActive(procedure.isActive());
        dto.setCreatedAt(procedure.getCreatedAt());

        return dto;
    }
}