package vn.system.app.modules.company.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.domain.request.ReqCreateCompanyDTO;
import vn.system.app.modules.company.domain.request.ReqUpdateCompanyDTO;
import vn.system.app.modules.company.domain.response.ResCompanyDTO;
import vn.system.app.modules.company.domain.response.ResCreateCompanyDTO;
import vn.system.app.modules.company.domain.response.ResUpdateCompanyDTO;
import vn.system.app.modules.company.repository.CompanyRepository;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // ================= CREATE =================
    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    // ================= DELETE (KHÔNG XOÁ, CHỈ INACTIVE) =================
    public Company handleInactiveCompany(long id) {
        Company currentCompany = this.fetchCompanyById(id);
        if (currentCompany != null) {
            currentCompany.setStatus(0); // inactive
            currentCompany = this.companyRepository.save(currentCompany);
        }
        return currentCompany;
    }

    // ================= FETCH =================
    public Company fetchCompanyById(long id) {
        Optional<Company> companyOptional = this.companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            return companyOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllCompany(
            Specification<Company> spec,
            Pageable pageable) {

        Page<Company> pageCompany = this.companyRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageCompany.getTotalPages());
        meta.setTotal(pageCompany.getTotalElements());

        rs.setMeta(meta);

        List<ResCompanyDTO> listCompany = pageCompany.getContent()
                .stream()
                .map(this::convertToResCompanyDTO)
                .collect(Collectors.toList());

        rs.setResult(listCompany);
        return rs;
    }

    // ================= UPDATE =================
    public Company handleUpdateCompany(ReqUpdateCompanyDTO req) {
        Company currentCompany = this.fetchCompanyById(req.getId());
        if (currentCompany != null) {
            currentCompany.setName(req.getName());
            currentCompany.setEnglishName(req.getEnglishName());

            currentCompany = this.companyRepository.save(currentCompany);
        }
        return currentCompany;
    }

    // ================= CHECK =================
    public boolean isCodeExist(String code) {
        return this.companyRepository.existsByCode(code);
    }

    // ================= CONVERT REQUEST → ENTITY =================
    public Company convertCreateReqToEntity(ReqCreateCompanyDTO req) {
        Company company = new Company();
        company.setCode(req.getCode());
        company.setName(req.getName());
        company.setEnglishName(req.getEnglishName());
        return company;
    }

    // ================= CONVERT ENTITY → RESPONSE =================
    public ResCreateCompanyDTO convertToResCreateCompanyDTO(Company company) {
        ResCreateCompanyDTO res = new ResCreateCompanyDTO();
        res.setId(company.getId());
        res.setCode(company.getCode());
        res.setName(company.getName());
        res.setEnglishName(company.getEnglishName());
        res.setCreatedAt(company.getCreatedAt());
        return res;
    }

    public ResUpdateCompanyDTO convertToResUpdateCompanyDTO(Company company) {
        ResUpdateCompanyDTO res = new ResUpdateCompanyDTO();
        res.setId(company.getId());
        res.setName(company.getName());
        res.setEnglishName(company.getEnglishName());
        res.setUpdatedAt(company.getUpdatedAt());
        return res;
    }

    public ResCompanyDTO convertToResCompanyDTO(Company company) {
        ResCompanyDTO res = new ResCompanyDTO();
        res.setId(company.getId());
        res.setCode(company.getCode());
        res.setName(company.getName());
        res.setEnglishName(company.getEnglishName());
        res.setStatus(company.getStatus());
        res.setCreatedAt(company.getCreatedAt());
        res.setUpdatedAt(company.getUpdatedAt());
        return res;
    }
}
