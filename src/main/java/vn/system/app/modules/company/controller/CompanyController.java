package vn.system.app.modules.company.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.domain.request.ReqCreateCompanyDTO;
import vn.system.app.modules.company.domain.request.ReqUpdateCompanyDTO;
import vn.system.app.modules.company.domain.response.ResCompanyDTO;
import vn.system.app.modules.company.domain.response.ResCreateCompanyDTO;
import vn.system.app.modules.company.domain.response.ResUpdateCompanyDTO;
import vn.system.app.modules.company.service.CompanyService;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // ================= CREATE =================
    @PostMapping("/companies")
    @ApiMessage("Create a new company")
    public ResponseEntity<ResCreateCompanyDTO> createCompany(
            @Valid @RequestBody ReqCreateCompanyDTO req)
            throws IdInvalidException {

        boolean isCodeExist = this.companyService.isCodeExist(req.getCode());
        if (isCodeExist) {
            throw new IdInvalidException(
                    "Mã công ty " + req.getCode() + " đã tồn tại");
        }

        Company company = this.companyService.convertCreateReqToEntity(req);
        Company savedCompany = this.companyService.handleCreateCompany(company);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.companyService.convertToResCreateCompanyDTO(savedCompany));
    }

    // ================= GET BY ID =================
    @GetMapping("/companies/{id}")
    @ApiMessage("Fetch company by id")
    public ResponseEntity<ResCompanyDTO> getCompanyById(
            @PathVariable("id") long id)
            throws IdInvalidException {

        Company company = this.companyService.fetchCompanyById(id);
        if (company == null) {
            throw new IdInvalidException(
                    "Company với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(
                this.companyService.convertToResCompanyDTO(company));
    }

    // ================= GET ALL =================
    @GetMapping("/companies")
    @ApiMessage("Fetch all companies")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                this.companyService.fetchAllCompany(spec, pageable));
    }

    // ================= UPDATE =================
    @PutMapping("/companies")
    @ApiMessage("Update a company")
    public ResponseEntity<ResUpdateCompanyDTO> updateCompany(
            @Valid @RequestBody ReqUpdateCompanyDTO req)
            throws IdInvalidException {

        Company updatedCompany = this.companyService.handleUpdateCompany(req);
        if (updatedCompany == null) {
            throw new IdInvalidException(
                    "Company với id = " + req.getId() + " không tồn tại");
        }

        return ResponseEntity.ok(
                this.companyService.convertToResUpdateCompanyDTO(updatedCompany));
    }

    // ================= INACTIVE (KHÔNG XOÁ) =================
    @PutMapping("/companies/{id}/inactive")
    @ApiMessage("Inactive a company")
    public ResponseEntity<ResCompanyDTO> inactiveCompany(
            @PathVariable("id") long id)
            throws IdInvalidException {

        Company company = this.companyService.handleInactiveCompany(id);
        if (company == null) {
            throw new IdInvalidException(
                    "Company với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(
                this.companyService.convertToResCompanyDTO(company));
    }
}
