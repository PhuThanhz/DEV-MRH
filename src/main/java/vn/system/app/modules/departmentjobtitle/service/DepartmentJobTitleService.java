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

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.request.ReqDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.domain.response.ResDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.service.JobTitleService;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;

@Service
public class DepartmentJobTitleService {

    private final DepartmentJobTitleRepository repository;
    private final SectionJobTitleRepository sectionRepo;
    private final JobTitleService jobTitleService;
    private final DepartmentService departmentService;

    public DepartmentJobTitleService(
            DepartmentJobTitleRepository repository,
            SectionJobTitleRepository sectionRepo,
            JobTitleService jobTitleService,
            DepartmentService departmentService) {

        this.repository = repository;
        this.sectionRepo = sectionRepo;
        this.jobTitleService = jobTitleService;
        this.departmentService = departmentService;
    }

    /*
     * =====================================================
     * CREATE + REACTIVATE (GÁN CHỨC DANH → PHÒNG BAN)
     * =====================================================
     */
    @Transactional
    public DepartmentJobTitle handleCreate(ReqDepartmentJobTitleDTO dto) {

        JobTitle jobTitle = jobTitleService.fetchEntityById(dto.getJobTitleId());
        Department department = departmentService.fetchEntityById(dto.getDepartmentId());

        Long deptId = department.getId();
        Long jobId = jobTitle.getId();

        // VALIDATE 1: Không cho gán trực tiếp nếu đang active ở bất kỳ Section nào
        boolean existsInSection = sectionRepo
                .existsBySection_Department_IdAndJobTitle_IdAndActiveTrue(deptId, jobId);

        if (existsInSection) {
            throw new IdInvalidException(
                    "Chức danh này đang được gán trong một bộ phận thuộc phòng ban. Không thể gán trực tiếp vào phòng ban.");
        }

        // Tìm mapping hiện có (bất kể active/inactive)
        DepartmentJobTitle existing = repository.findByDepartment_IdAndJobTitle_Id(deptId, jobId);

        if (existing != null) {
            if (existing.isActive()) {
                throw new IdInvalidException("Chức danh đã được gán và đang hoạt động trong phòng ban.");
            }

            // REACTIVATE + cập nhật audit field
            existing.setActive(true);
            existing.setUpdatedAt(Instant.now());
            existing.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));
            return repository.save(existing);
        }

        // Tạo mới
        DepartmentJobTitle entity = new DepartmentJobTitle();
        entity.setDepartment(department);
        entity.setJobTitle(jobTitle);
        entity.setActive(true);

        return repository.save(entity);
    }

    /*
     * =====================================================
     * RESTORE (KÍCH HOẠT LẠI SAU KHI ĐÃ DEACTIVATE)
     * =====================================================
     */
    @Transactional
    public DepartmentJobTitle restore(Long id) {
        DepartmentJobTitle entity = fetchEntityById(id);

        if (entity.isActive()) {
            throw new IdInvalidException("Bản ghi đang hoạt động, không cần khôi phục.");
        }

        entity.setActive(true);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));

        return repository.save(entity);
    }

    /*
     * =====================================================
     * CHECK ACTIVE JOB-TITLE IN DEPARTMENT
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
        Department dept = departmentService.fetchEntityById(departmentId);

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
     * SOFT DELETE / DEACTIVATE (REQUEST FROM UI)
     * =====================================================
     */
    @Transactional
    public void handleDelete(Long id) {
        DepartmentJobTitle entity = fetchEntityById(id);
        if (!entity.isActive()) {
            return; // đã deactivate rồi thì không làm gì thêm
        }
        entity.setActive(false);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));
        repository.save(entity);
    }

    /*
     * =====================================================
     * FETCH ENTITY
     * =====================================================
     */
    public DepartmentJobTitle fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy gán chức danh - phòng ban với id: " + id));
    }

    // ────────────────────────────────────────────────────────────────
    // Các method còn lại giữ nguyên (chỉ format code sạch hơn)
    // ────────────────────────────────────────────────────────────────

    public List<Long> fetchIdsByDepartment(Long departmentId) {
        return repository.findByDepartment_Id(departmentId)
                .stream()
                .map(DepartmentJobTitle::getId)
                .collect(Collectors.toList());
    }

    public ResultPaginationDTO fetchAll(Specification<DepartmentJobTitle> spec, Pageable pageable) {
        Page<DepartmentJobTitle> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(
                page.getContent()
                        .stream()
                        .map(this::convertToResDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    public List<DepartmentJobTitle> fetchByDepartment(Long departmentId) {
        return repository.findByDepartment_IdAndActiveTrue(departmentId);
    }

    public Map<String, List<ResDepartmentJobTitleDTO>> fetchCareerPathByBand(Long departmentId) {
        List<DepartmentJobTitle> list = fetchByDepartment(departmentId);

        return list.stream().collect(
                Collectors.groupingBy(
                        d -> d.getJobTitle().getPositionLevel().getCode().replaceAll("[0-9]", ""),
                        Collectors.collectingAndThen(Collectors.toList(), sub -> {
                            sub.sort(Comparator.comparingInt(
                                    d -> d.getJobTitle().getPositionLevel().getBandOrder()));
                            return sub.stream()
                                    .map(this::convertToResDTO)
                                    .collect(Collectors.toList());
                        })));
    }

    public List<ResDepartmentJobTitleDTO> fetchGlobalCareerPath(Long departmentId) {
        List<DepartmentJobTitle> list = fetchByDepartment(departmentId);

        list.sort(Comparator
                .comparingInt((DepartmentJobTitle d) -> d.getJobTitle().getPositionLevel().getBandOrder())
                .thenComparingInt(d -> {
                    try {
                        return Integer.parseInt(d.getJobTitle()
                                .getPositionLevel()
                                .getCode()
                                .replaceAll("[^0-9]", ""));
                    } catch (Exception e) {
                        return Integer.MAX_VALUE;
                    }
                }));

        return list.stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    public ResDepartmentJobTitleDTO convertToResDTO(DepartmentJobTitle e) {
        ResDepartmentJobTitleDTO dto = new ResDepartmentJobTitleDTO();

        dto.setId(e.getId());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        // Job Title Info
        ResDepartmentJobTitleDTO.JobTitleInfo jt = new ResDepartmentJobTitleDTO.JobTitleInfo();
        jt.setId(e.getJobTitle().getId());
        jt.setNameVi(e.getJobTitle().getNameVi());

        if (e.getJobTitle().getPositionLevel() != null) {
            var pl = e.getJobTitle().getPositionLevel();
            jt.setPositionCode(pl.getCode());
            jt.setBand(pl.getCode().replaceAll("[0-9]", ""));
            jt.setLevel(Integer.parseInt(pl.getCode().replaceAll("[^0-9]", "")));
            jt.setBandOrder(pl.getBandOrder());
            jt.setLevelNumber(jt.getLevel());
        }
        dto.setJobTitle(jt);

        // Department Info
        ResDepartmentJobTitleDTO.DepartmentInfo dep = new ResDepartmentJobTitleDTO.DepartmentInfo();
        dep.setId(e.getDepartment().getId());
        dep.setName(e.getDepartment().getName());
        dto.setDepartment(dep);

        return dto;
    }
}