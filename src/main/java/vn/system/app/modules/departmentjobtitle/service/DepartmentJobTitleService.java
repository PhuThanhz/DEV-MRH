package vn.system.app.modules.departmentjobtitle.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.request.ReqDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.domain.response.ResDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;

@Service
public class DepartmentJobTitleService {

    private final DepartmentJobTitleRepository repository;
    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;

    public DepartmentJobTitleService(
            DepartmentJobTitleRepository repository,
            JobTitleService jobTitleService,
            DepartmentService departmentService) {
        this.repository = repository;
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public DepartmentJobTitle handleCreate(ReqDepartmentJobTitleDTO dto) {

        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        Department department = departmentService.fetchEntityById(dto.getDepartmentId());

        if (repository.existsByJobTitle_IdAndDepartment_Id(
                dto.getJobTitleId(), dto.getDepartmentId())) {
            throw new IdInvalidException("Chức danh đã tồn tại trong phòng ban");
        }

        DepartmentJobTitle entity = new DepartmentJobTitle();
        entity.setJobTitle(jobTitle);
        entity.setDepartment(department);

        return repository.save(entity);
    }

    /* ================= DELETE (SOFT) ================= */

    @Transactional
    public void handleDelete(Long id) {
        DepartmentJobTitle entity = fetchEntityById(id);
        entity.setStatus(0);
        repository.save(entity);
    }

    /* ================= FETCH ENTITY ================= */

    public DepartmentJobTitle fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("DepartmentJobTitle không tồn tại"));
    }

    /* ================= FETCH IDS BY DEPARTMENT ================= */
    // 🔥 METHOD BỔ SUNG – DÙNG CHO PERMISSION

    public List<Long> fetchIdsByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream()
                .map(DepartmentJobTitle::getId)
                .collect(Collectors.toList());
    }

    /* ================= FETCH ALL ================= */

    public ResultPaginationDTO fetchAll(
            Specification<DepartmentJobTitle> spec,
            Pageable pageable) {

        Page<DepartmentJobTitle> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        List<ResDepartmentJobTitleDTO> list = page.getContent()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /* ================= CONVERT RESPONSE ================= */

    public ResDepartmentJobTitleDTO convertToResDTO(DepartmentJobTitle entity) {

        ResDepartmentJobTitleDTO res = new ResDepartmentJobTitleDTO();

        res.setId(entity.getId());
        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        ResDepartmentJobTitleDTO.JobTitleInfo jt = new ResDepartmentJobTitleDTO.JobTitleInfo();
        jt.setId(entity.getJobTitle().getId());
        jt.setNameVi(entity.getJobTitle().getNameVi());
        res.setJobTitle(jt);

        ResDepartmentJobTitleDTO.DepartmentInfo d = new ResDepartmentJobTitleDTO.DepartmentInfo();
        d.setId(entity.getDepartment().getId());
        d.setName(entity.getDepartment().getName());
        res.setDepartment(d);

        return res;
    }
}