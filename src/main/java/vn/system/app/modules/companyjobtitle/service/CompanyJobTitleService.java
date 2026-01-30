package vn.system.app.modules.companyjobtitle.service;

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
import vn.system.app.modules.company.service.CompanyService;
import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;
import vn.system.app.modules.companyjobtitle.domain.request.ReqCompanyJobTitleDTO;
import vn.system.app.modules.companyjobtitle.domain.response.ResCompanyJobTitleDTO;
import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;

@Service
public class CompanyJobTitleService {

    private final CompanyJobTitleRepository repository;
    private final JobTitleService jobTitleService;
    private final CompanyService companyService;

    public CompanyJobTitleService(
            CompanyJobTitleRepository repository,
            JobTitleService jobTitleService,
            CompanyService companyService) {
        this.repository = repository;
        this.jobTitleService = jobTitleService;
        this.companyService = companyService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public CompanyJobTitle handleCreate(ReqCompanyJobTitleDTO dto) {

        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        Company company = companyService.fetchEntityById(dto.getCompanyId());

        if (repository.existsByJobTitle_IdAndCompany_Id(
                dto.getJobTitleId(), dto.getCompanyId())) {
            throw new IdInvalidException("Chức danh đã tồn tại trong công ty");
        }

        CompanyJobTitle entity = new CompanyJobTitle();
        entity.setJobTitle(jobTitle);
        entity.setCompany(company);

        return repository.save(entity);
    }

    /* ================= DELETE (SOFT) ================= */

    @Transactional
    public void handleDelete(Long id) {
        CompanyJobTitle entity = fetchEntityById(id);
        entity.setStatus(0);
        repository.save(entity);
    }

    /* ================= FETCH ENTITY ================= */

    public CompanyJobTitle fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("CompanyJobTitle không tồn tại"));
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAll(
            Specification<CompanyJobTitle> spec,
            Pageable pageable) {

        Page<CompanyJobTitle> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        List<ResCompanyJobTitleDTO> list = page.getContent()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /* ================= CONVERT RESPONSE ================= */

    public ResCompanyJobTitleDTO convertToResDTO(CompanyJobTitle entity) {

        ResCompanyJobTitleDTO res = new ResCompanyJobTitleDTO();

        res.setId(entity.getId());
        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        ResCompanyJobTitleDTO.JobTitleInfo jt = new ResCompanyJobTitleDTO.JobTitleInfo();
        jt.setId(entity.getJobTitle().getId());
        jt.setNameVi(entity.getJobTitle().getNameVi());
        res.setJobTitle(jt);

        ResCompanyJobTitleDTO.CompanyInfo c = new ResCompanyJobTitleDTO.CompanyInfo();
        c.setId(entity.getCompany().getId());
        c.setName(entity.getCompany().getName());
        res.setCompany(c);

        return res;
    }
}
