package vn.system.app.modules.orgjobtitle.service;

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
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;
import vn.system.app.modules.orgjobtitle.domain.OrgJobTitle;
import vn.system.app.modules.orgjobtitle.domain.request.ReqCreateOrgJobTitleDTO;
import vn.system.app.modules.orgjobtitle.domain.response.ResOrgJobTitleDTO;
import vn.system.app.modules.orgjobtitle.repository.OrgJobTitleRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.service.SectionService;

@Service
public class OrgJobTitleService {

    private final OrgJobTitleRepository orgJobTitleRepository;
    private final JobTitleService jobTitleService;
    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final SectionService sectionService;

    public OrgJobTitleService(
            OrgJobTitleRepository orgJobTitleRepository,
            JobTitleService jobTitleService,
            CompanyService companyService,
            DepartmentService departmentService,
            SectionService sectionService) {

        this.orgJobTitleRepository = orgJobTitleRepository;
        this.jobTitleService = jobTitleService;
        this.companyService = companyService;
        this.departmentService = departmentService;
        this.sectionService = sectionService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public OrgJobTitle handleCreate(ReqCreateOrgJobTitleDTO dto) {

        validateRequest(dto);

        OrgJobTitle entity = new OrgJobTitle();

        // ===== JobTitle =====
        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        entity.setJobTitle(jobTitle);

        // ===== Company =====
        if (dto.getCompanyId() != null) {

            Company company = companyService.fetchEntityById(dto.getCompanyId());

            if (orgJobTitleRepository
                    .existsByJobTitle_IdAndCompany_Id(dto.getJobTitleId(), dto.getCompanyId())) {
                throw new IdInvalidException("Chức danh đã tồn tại trong công ty");
            }

            entity.setCompany(company);
        }

        // ===== Department =====
        if (dto.getDepartmentId() != null) {

            Department department = departmentService.fetchEntityById(dto.getDepartmentId());

            if (orgJobTitleRepository
                    .existsByJobTitle_IdAndDepartment_Id(dto.getJobTitleId(), dto.getDepartmentId())) {
                throw new IdInvalidException("Chức danh đã tồn tại trong phòng ban");
            }

            entity.setDepartment(department);
        }

        // ===== Section =====
        if (dto.getSectionId() != null) {

            Section section = sectionService.fetchEntityById(dto.getSectionId());

            if (orgJobTitleRepository
                    .existsByJobTitle_IdAndSection_Id(dto.getJobTitleId(), dto.getSectionId())) {
                throw new IdInvalidException("Chức danh đã tồn tại trong bộ phận");
            }

            entity.setSection(section);
        }

        return orgJobTitleRepository.save(entity);
    }

    /* ================= DELETE (SOFT) ================= */

    @Transactional
    public void handleDelete(long id) {
        OrgJobTitle entity = fetchEntityById(id);
        entity.setStatus(0);
        orgJobTitleRepository.save(entity);
    }

    /* ================= FETCH ENTITY ================= */

    public OrgJobTitle fetchEntityById(long id) {
        return orgJobTitleRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy OrgJobTitle"));
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAll(
            Specification<OrgJobTitle> spec,
            Pageable pageable) {

        Page<OrgJobTitle> page = orgJobTitleRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        List<ResOrgJobTitleDTO> list = page.getContent()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /* ================= VALIDATE ================= */

    private void validateRequest(ReqCreateOrgJobTitleDTO dto) {
        int count = 0;
        if (dto.getCompanyId() != null)
            count++;
        if (dto.getDepartmentId() != null)
            count++;
        if (dto.getSectionId() != null)
            count++;

        if (count != 1) {
            throw new IdInvalidException(
                    "Chỉ được truyền 1 trong 3: companyId / departmentId / sectionId");
        }
    }

    /* ================= MAPPER ================= */
    // 🔥 ĐỔI private → public

    public ResOrgJobTitleDTO convertToResDTO(OrgJobTitle entity) {

        ResOrgJobTitleDTO res = new ResOrgJobTitleDTO();
        res.setId(entity.getId());
        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        // ===== JobTitle =====
        ResOrgJobTitleDTO.JobTitleInfo jt = new ResOrgJobTitleDTO.JobTitleInfo();
        jt.setId(entity.getJobTitle().getId());
        jt.setNameVi(entity.getJobTitle().getNameVi());
        res.setJobTitle(jt);

        // ===== Org =====
        if (entity.getCompany() != null) {
            ResOrgJobTitleDTO.CompanyInfo c = new ResOrgJobTitleDTO.CompanyInfo();
            c.setId(entity.getCompany().getId());
            c.setName(entity.getCompany().getName());
            res.setCompany(c);
        }

        if (entity.getDepartment() != null) {
            ResOrgJobTitleDTO.DepartmentInfo d = new ResOrgJobTitleDTO.DepartmentInfo();
            d.setId(entity.getDepartment().getId());
            d.setName(entity.getDepartment().getName());
            res.setDepartment(d);
        }

        if (entity.getSection() != null) {
            ResOrgJobTitleDTO.SectionInfo s = new ResOrgJobTitleDTO.SectionInfo();
            s.setId(entity.getSection().getId());
            s.setName(entity.getSection().getName());
            res.setSection(s);
        }

        return res;
    }
}
