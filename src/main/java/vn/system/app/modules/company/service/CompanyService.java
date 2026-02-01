package vn.system.app.modules.company.service;

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

    /* ================= CREATE ================= */

    @Transactional
    public Company handleCreateCompany(Company company) {
        return companyRepository.save(company);
    }

    /* ================= UPDATE ================= */

    @Transactional
    public Company handleUpdateCompany(ReqUpdateCompanyDTO req) {
        Company current = fetchEntityById(req.getId());
        current.setName(req.getName());
        current.setEnglishName(req.getEnglishName());
        return companyRepository.save(current);
    }

    /* ================= INACTIVE (SOFT DELETE) ================= */

    @Transactional
    public void handleInactiveCompany(Long id) {
        Company company = fetchEntityById(id);
        company.setStatus(0);
        companyRepository.save(company);
    }

    /* ================= ACTIVE ================= */

    @Transactional
    public void handleActiveCompany(Long id) {
        Company company = fetchEntityById(id);
        company.setStatus(1);
        companyRepository.save(company);
    }

    /* ================= FETCH ENTITY ================= */

    public Company fetchEntityById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy công ty"));
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAllCompany(
            Specification<Company> spec,
            Pageable pageable) {

        Page<Company> page = companyRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        List<ResCompanyDTO> list = page.getContent()
                .stream()
                .map(this::convertToResCompanyDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /* ================= CHECK ================= */

    public boolean isCodeExist(String code) {
        return companyRepository.existsByCode(code);
    }

    /* ================= CONVERT ================= */

    public Company convertCreateReqToEntity(ReqCreateCompanyDTO req) {
        Company c = new Company();
        c.setCode(req.getCode());
        c.setName(req.getName());
        c.setEnglishName(req.getEnglishName());
        return c;
    }

    public ResCreateCompanyDTO convertToResCreateCompanyDTO(Company c) {
        ResCreateCompanyDTO res = new ResCreateCompanyDTO();
        res.setId(c.getId());
        res.setCode(c.getCode());
        res.setName(c.getName());
        res.setEnglishName(c.getEnglishName());
        res.setCreatedAt(c.getCreatedAt());
        return res;
    }

    public ResUpdateCompanyDTO convertToResUpdateCompanyDTO(Company c) {
        ResUpdateCompanyDTO res = new ResUpdateCompanyDTO();
        res.setId(c.getId());
        res.setName(c.getName());
        res.setEnglishName(c.getEnglishName());
        res.setUpdatedAt(c.getUpdatedAt());
        return res;
    }

    public ResCompanyDTO convertToResCompanyDTO(Company c) {
        ResCompanyDTO res = new ResCompanyDTO();
        res.setId(c.getId());
        res.setCode(c.getCode());
        res.setName(c.getName());
        res.setEnglishName(c.getEnglishName());
        res.setStatus(c.getStatus());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        return res;
    }
}
