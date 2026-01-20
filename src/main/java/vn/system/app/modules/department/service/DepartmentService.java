package vn.system.app.modules.department.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.domain.request.CreateDepartmentRequest;
import vn.system.app.modules.department.domain.request.UpdateDepartmentRequest;
import vn.system.app.modules.department.domain.response.DepartmentResponse;
import vn.system.app.modules.department.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            CompanyRepository companyRepository) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
    }

    // ============================================================
    // CREATE
    // ============================================================
    @Transactional
    public DepartmentResponse handleCreateDepartment(CreateDepartmentRequest req) {

        // check duplicate code
        if (this.departmentRepository.existsByCode(req.getCode())) {
            throw new IdInvalidException("Mã phòng ban " + req.getCode() + " đã tồn tại.");
        }

        // check company
        Company company = this.companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new IdInvalidException("Company không tồn tại."));

        // check parent
        Department parent = null;
        if (req.getParentId() != null) {
            parent = this.departmentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new IdInvalidException("Parent department không tồn tại."));
        }

        Department d = new Department();
        d.setCode(req.getCode()); // mã phòng ban
        d.setName(req.getName()); // tên phòng ban
        d.setEnglishName(req.getEnglishName()); // tên tiếng Anh
        d.setDescription(req.getDescription()); // mô tả
        d.setCompany(company); // thuộc công ty nào
        d.setParent(parent); // bộ phận cha (nếu có)

        d = this.departmentRepository.save(d);

        return this.convertToResponseDTO(d);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public DepartmentResponse handleUpdateDepartment(Long id, UpdateDepartmentRequest req) {

        Department d = this.departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Department không tồn tại."));

        if (req.getName() != null)
            d.setName(req.getName());
        if (req.getEnglishName() != null)
            d.setEnglishName(req.getEnglishName());
        if (req.getDescription() != null)
            d.setDescription(req.getDescription());
        if (req.getStatus() != null)
            d.setStatus(req.getStatus());

        if (req.getParentId() != null) {
            Department parent = this.departmentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new IdInvalidException("Parent department không tồn tại."));
            d.setParent(parent);
        }

        d = this.departmentRepository.save(d);

        return this.convertToResponseDTO(d);
    }

    // ============================================================
    // DELETE (soft delete)
    // ============================================================
    @Transactional
    public void handleDeleteDepartment(Long id) {
        Department d = this.departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Department không tồn tại."));

        d.setStatus(0); // xoá mềm
        this.departmentRepository.save(d);
    }

    // ============================================================
    // GET ONE
    // ============================================================
    public DepartmentResponse fetchDepartmentById(Long id) {
        Department d = this.departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Department không tồn tại."));
        return this.convertToResponseDTO(d);
    }

    // ============================================================
    // GET ALL (paginate + filter)
    // ============================================================
    public ResultPaginationDTO fetchAllDepartments(Specification<?> spec, Pageable pageable) {

        Page<Department> pageDept = this.departmentRepository.findAll((Specification<Department>) spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageDept.getTotalPages());
        mt.setTotal(pageDept.getTotalElements());
        rs.setMeta(mt);

        List<DepartmentResponse> list = pageDept.getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    // ============================================================
    // CONVERT ENTITY -> RESPONSE DTO
    // ============================================================
    public DepartmentResponse convertToResponseDTO(Department d) {
        DepartmentResponse res = new DepartmentResponse();

        res.setId(d.getId());
        res.setCode(d.getCode());
        res.setName(d.getName());
        res.setEnglishName(d.getEnglishName());
        res.setDescription(d.getDescription());

        // --------------------------
        // COMPANY INFO (id + name)
        // --------------------------
        DepartmentResponse.CompanyInfo ci = new DepartmentResponse.CompanyInfo();
        ci.setId(d.getCompany().getId());
        ci.setName(d.getCompany().getName());
        res.setCompany(ci);

        res.setParentId(d.getParent() != null ? d.getParent().getId() : null);
        res.setStatus(d.getStatus());
        res.setCreatedAt(d.getCreatedAt());
        res.setUpdatedAt(d.getUpdatedAt());
        res.setCreatedBy(d.getCreatedBy());
        res.setUpdatedBy(d.getUpdatedBy());

        return res;
    }
}
