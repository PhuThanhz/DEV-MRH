package vn.system.app.modules.departmentjobtitle.service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;
import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.request.ReqDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.domain.response.ResDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;
import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;

@Service
public class DepartmentJobTitleService {

    private final DepartmentJobTitleRepository repository;
    private final SectionJobTitleRepository sectionRepo;
    private final CompanyJobTitleRepository companyRepo;
    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;

    public DepartmentJobTitleService(
            DepartmentJobTitleRepository repository,
            SectionJobTitleRepository sectionRepo,
            CompanyJobTitleRepository companyRepo,
            JobTitleService jobTitleService,
            DepartmentService departmentService) {

        this.repository = repository;
        this.sectionRepo = sectionRepo;
        this.companyRepo = companyRepo;
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
    }

    // ⭐ Thêm method mới để PermissionMatrixService hoạt động
    public List<DepartmentJobTitle> fetchEntitiesByIds(List<Long> ids) {
        return repository.findAllById(ids);
    }

    /*
     * =====================================================
     * CREATE + REACTIVATE
     * =====================================================
     */
    @Transactional
    public DepartmentJobTitle handleCreate(ReqDepartmentJobTitleDTO dto) {

        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        Department department = departmentService.fetchEntityById(dto.getDepartmentId());

        Long deptId = department.getId();
        Long jobId = jobTitle.getId();
        Long companyId = department.getCompany().getId();

        // block nếu đang active ở company
        if (companyRepo.existsByCompany_IdAndJobTitle_IdAndActiveTrue(companyId, jobId)) {
            throw new IdInvalidException(
                    "Chức danh đã được gán ở cấp công ty, không thể gán ở phòng ban.");
        }

        // block nếu đang active ở section
        if (sectionRepo.existsBySection_Department_IdAndJobTitle_IdAndActiveTrue(deptId, jobId)) {
            throw new IdInvalidException(
                    "Chức danh đang được gán ở bộ phận, không thể gán trực tiếp vào phòng ban.");
        }

        DepartmentJobTitle existing = repository.findByDepartment_IdAndJobTitle_Id(deptId, jobId);

        if (existing != null) {
            if (existing.isActive()) {
                throw new IdInvalidException(
                        "Chức danh đã được gán và đang hoạt động trong phòng ban.");
            }
            existing.setActive(true);
            existing.setUpdatedAt(Instant.now());
            existing.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));
            return repository.save(existing);
        }

        DepartmentJobTitle entity = new DepartmentJobTitle();
        entity.setDepartment(department);
        entity.setJobTitle(jobTitle);
        entity.setActive(true);

        return repository.save(entity);
    }

    /*
     * =====================================================
     * SOFT DELETE
     * =====================================================
     */
    @Transactional
    public void handleDelete(Long id) {

        DepartmentJobTitle entity = fetchEntityById(id);

        if (!entity.isActive()) {
            return;
        }

        entity.setActive(false);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));

        repository.save(entity);
    }

    /*
     * =====================================================
     * RESTORE
     * =====================================================
     */
    @Transactional
    public DepartmentJobTitle restore(Long id) {

        DepartmentJobTitle entity = fetchEntityById(id);

        if (entity.isActive()) {
            throw new IdInvalidException("Bản ghi đang hoạt động, không cần khôi phục.");
        }

        Long companyId = entity.getDepartment().getCompany().getId();
        Long jobId = entity.getJobTitle().getId();

        if (companyRepo.existsByCompany_IdAndJobTitle_IdAndActiveTrue(companyId, jobId)) {
            throw new IdInvalidException(
                    "Chức danh đang được gán ở cấp công ty, không thể khôi phục.");
        }

        entity.setActive(true);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));

        return repository.save(entity);
    }

    /*
     * =====================================================
     * CHECK ACTIVE IN DEPARTMENT
     * =====================================================
     */
    public boolean existsActiveInDepartment(Long departmentId, Long jobTitleId) {
        return repository.existsByDepartment_IdAndJobTitle_IdAndActiveTrue(departmentId, jobTitleId);
    }

    /*
     * =====================================================
     * AUTO SYNC FROM SECTION → DEPARTMENT
     * =====================================================
     */
    @Transactional
    public void assignIfNotExists(Long departmentId, Long jobTitleId) {

        Department dept = departmentService.fetchEntityById(departmentId);
        Long companyId = dept.getCompany().getId();

        if (companyRepo.existsByCompany_IdAndJobTitle_IdAndActiveTrue(companyId, jobTitleId)) {
            return;
        }

        DepartmentJobTitle existing = repository.findByDepartment_IdAndJobTitle_Id(departmentId, jobTitleId);

        if (existing != null) {
            if (!existing.isActive()) {
                existing.setActive(true);
                existing.setUpdatedAt(Instant.now());
                existing.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));
                repository.save(existing);
            }
            return;
        }

        JobTitle jt = jobTitleService.fetchEntityById(jobTitleId);

        DepartmentJobTitle entity = new DepartmentJobTitle();
        entity.setDepartment(dept);
        entity.setJobTitle(jt);
        entity.setActive(true);

        repository.save(entity);
    }

    /*
     * =====================================================
     * AUTO DEACTIVATE WHEN SECTION REMOVE
     * =====================================================
     */
    @Transactional
    public void inactiveIfExists(Long departmentId, Long jobTitleId) {

        DepartmentJobTitle entity = repository.findByDepartment_IdAndJobTitle_Id(departmentId, jobTitleId);

        if (entity != null && entity.isActive()) {
            entity.setActive(false);
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));
            repository.save(entity);
        }
    }

    /*
     * =====================================================
     * VIEW TỔNG HỢP COMPANY_JOB_TITLE TẠI PHÒNG BAN
     * =====================================================
     */
    @Transactional(readOnly = true)
    public List<ResDepartmentJobTitleDTO> fetchAllCompanyJobTitlesOfDepartment(Long departmentId) {

        Department department = departmentService.fetchEntityById(departmentId);
        Long companyId = department.getCompany().getId();

        List<SectionJobTitle> sectionList = sectionRepo.findBySection_Department_IdAndActiveTrue(departmentId);

        List<DepartmentJobTitle> departmentList = repository.findByDepartment_IdAndActiveTrue(departmentId);

        List<CompanyJobTitle> companyList = companyRepo.findByCompany_IdAndActiveTrue(companyId);

        Map<Long, ResDepartmentJobTitleDTO> resultMap = new LinkedHashMap<>();

        // SECTION
        for (SectionJobTitle sjt : sectionList) {
            Long jobId = sjt.getJobTitle().getId();
            ResDepartmentJobTitleDTO dto = convertToResDTO(
                    buildVirtualDepartmentJobTitle(department, sjt.getJobTitle()));
            dto.setSource("SECTION");
            resultMap.put(jobId, dto);
        }

        // DEPARTMENT
        for (DepartmentJobTitle djt : departmentList) {
            Long jobId = djt.getJobTitle().getId();
            if (!resultMap.containsKey(jobId)) {
                ResDepartmentJobTitleDTO dto = convertToResDTO(djt);
                dto.setSource("DEPARTMENT");
                resultMap.put(jobId, dto);
            }
        }

        // COMPANY (virtual)
        for (CompanyJobTitle cjt : companyList) {
            Long jobId = cjt.getJobTitle().getId();
            if (!resultMap.containsKey(jobId)) {
                ResDepartmentJobTitleDTO dto = convertToResDTO(
                        buildVirtualDepartmentJobTitle(department, cjt.getJobTitle()));
                dto.setSource("COMPANY");
                resultMap.put(jobId, dto);
            }
        }

        return resultMap.values().stream()
                .sorted(Comparator
                        .comparing((ResDepartmentJobTitleDTO dto) -> dto.getJobTitle().getBandOrder() != null
                                ? dto.getJobTitle().getBandOrder()
                                : Integer.MAX_VALUE)
                        .thenComparing(dto -> dto.getJobTitle().getLevelNumber() != null
                                ? dto.getJobTitle().getLevelNumber()
                                : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * FETCH ENTITY
     * =====================================================
     */
    public DepartmentJobTitle fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy gán chức danh - phòng ban với id: " + id));
    }

    /*
     * =====================================================
     * FETCH IDS BY DEPARTMENT
     * =====================================================
     */
    public List<Long> fetchIdsByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream()
                .map(DepartmentJobTitle::getId)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * FETCH ALL (PAGINATION)
     * =====================================================
     */
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
        rs.setResult(
                page.getContent().stream()
                        .map(this::convertToResDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * =====================================================
     * FETCH ALL DEPARTMENT_JOB_TITLE (ACTIVE)
     * =====================================================
     */
    public List<DepartmentJobTitle> fetchAllDepartmentJobTitles() {
        return repository.findAll()
                .stream()
                .filter(DepartmentJobTitle::isActive)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * FETCH DEPARTMENT_JOB_TITLE BY ID (STANDARD)
     * =====================================================
     */
    public DepartmentJobTitle fetchDepartmentJobTitleById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy DepartmentJobTitle với id = " + id));
    }

    /*
     * =====================================================
     * FETCH ACTIVE BY DEPARTMENT
     * =====================================================
     */
    public List<DepartmentJobTitle> fetchByDepartment(Long departmentId) {
        return repository.findByDepartment_IdAndActiveTrue(departmentId);
    }

    /*
     * =====================================================
     * FETCH ALL ACTIVE (FOR PERMISSION MATRIX)
     * =====================================================
     */
    public List<DepartmentJobTitle> fetchAll() {
        return repository.findAll()
                .stream()
                .filter(DepartmentJobTitle::isActive)
                .collect(Collectors.toList());
    }

    /*
     * =====================================================
     * CONVERT TO DTO
     * =====================================================
     */
    public ResDepartmentJobTitleDTO convertToResDTO(DepartmentJobTitle e) {

        ResDepartmentJobTitleDTO dto = new ResDepartmentJobTitleDTO();

        dto.setId(e.getId());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        ResDepartmentJobTitleDTO.JobTitleInfo jt = new ResDepartmentJobTitleDTO.JobTitleInfo();
        jt.setId(e.getJobTitle().getId());
        jt.setNameVi(e.getJobTitle().getNameVi());
        jt.setNameEn(e.getJobTitle().getNameEn()); // ⭐ THÊM DÒNG NÀY ⭐

        if (e.getJobTitle().getPositionLevel() != null) {
            var pl = e.getJobTitle().getPositionLevel();
            jt.setPositionCode(pl.getCode());
            jt.setBand(pl.getCode().replaceAll("[0-9]", ""));
            jt.setLevel(Integer.parseInt(pl.getCode().replaceAll("[^0-9]", "")));
            jt.setBandOrder(pl.getBandOrder());
            jt.setLevelNumber(jt.getLevel());
        }

        dto.setJobTitle(jt);

        ResDepartmentJobTitleDTO.DepartmentInfo dep = new ResDepartmentJobTitleDTO.DepartmentInfo();
        dep.setId(e.getDepartment().getId());
        dep.setName(e.getDepartment().getName());
        dto.setDepartment(dep);

        return dto;
    }

    /*
     * =====================================================
     * BUILD VIRTUAL DEPARTMENT_JOB_TITLE
     * =====================================================
     */
    private DepartmentJobTitle buildVirtualDepartmentJobTitle(
            Department department,
            JobTitle jobTitle) {

        DepartmentJobTitle fake = new DepartmentJobTitle();
        fake.setDepartment(department);
        fake.setJobTitle(jobTitle);
        fake.setActive(false);
        return fake;
    }
}
