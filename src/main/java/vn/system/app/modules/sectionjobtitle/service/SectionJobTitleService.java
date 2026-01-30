package vn.system.app.modules.sectionjobtitle.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.service.SectionService;
import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.sectionjobtitle.domain.request.ReqSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.domain.response.ResSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;

@Service
public class SectionJobTitleService {

    private final SectionJobTitleRepository repository;
    private final JobTitleService jobTitleService;
    private final SectionService sectionService;

    public SectionJobTitleService(
            SectionJobTitleRepository repository,
            JobTitleService jobTitleService,
            SectionService sectionService) {
        this.repository = repository;
        this.jobTitleService = jobTitleService;
        this.sectionService = sectionService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public SectionJobTitle handleCreate(ReqSectionJobTitleDTO dto) {

        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        Section section = sectionService.fetchEntityById(dto.getSectionId());

        if (repository.existsByJobTitle_IdAndSection_Id(
                dto.getJobTitleId(), dto.getSectionId())) {
            throw new IdInvalidException("Chức danh đã tồn tại trong bộ phận");
        }

        SectionJobTitle entity = new SectionJobTitle();
        entity.setJobTitle(jobTitle);
        entity.setSection(section);

        return repository.save(entity);
    }

    /* ================= DELETE (SOFT) ================= */

    @Transactional
    public void handleDelete(Long id) {
        SectionJobTitle entity = fetchEntityById(id);
        entity.setStatus(0);
        repository.save(entity);
    }

    /* ================= FETCH ENTITY ================= */

    public SectionJobTitle fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("SectionJobTitle không tồn tại"));
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAll(
            Specification<SectionJobTitle> spec,
            Pageable pageable) {

        Page<SectionJobTitle> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        List<ResSectionJobTitleDTO> list = page.getContent()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /* ================= CONVERT RESPONSE ================= */

    public ResSectionJobTitleDTO convertToResDTO(SectionJobTitle entity) {

        ResSectionJobTitleDTO res = new ResSectionJobTitleDTO();

        res.setId(entity.getId());
        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        ResSectionJobTitleDTO.JobTitleInfo jt = new ResSectionJobTitleDTO.JobTitleInfo();
        jt.setId(entity.getJobTitle().getId());
        jt.setNameVi(entity.getJobTitle().getNameVi());
        res.setJobTitle(jt);

        ResSectionJobTitleDTO.SectionInfo s = new ResSectionJobTitleDTO.SectionInfo();
        s.setId(entity.getSection().getId());
        s.setName(entity.getSection().getName());
        res.setSection(s);

        return res;
    }
}
