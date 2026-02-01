package vn.system.app.modules.section.service;

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
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.domain.request.ReqCreateSectionDTO;
import vn.system.app.modules.section.domain.request.ReqUpdateSectionDTO;
import vn.system.app.modules.section.domain.response.ResSectionDTO;
import vn.system.app.modules.section.repository.SectionRepository;

@Service
public class SectionService {

    private final SectionRepository sectionRepo;
    private final DepartmentService departmentService;

    public SectionService(
            SectionRepository sectionRepo,
            DepartmentService departmentService) {

        this.sectionRepo = sectionRepo;
        this.departmentService = departmentService;
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */
    @Transactional
    public ResSectionDTO createSection(ReqCreateSectionDTO req) {

        // Validate duplicate: code + department
        if (sectionRepo.existsByCodeAndDepartmentId(req.getCode(), req.getDepartmentId())) {
            throw new IdInvalidException("Mã bộ phận đã tồn tại trong phòng ban này");
        }

        Department dept = departmentService.fetchEntityById(req.getDepartmentId());

        Section s = new Section();
        s.setCode(req.getCode());
        s.setName(req.getName());
        s.setDepartment(dept);

        s = sectionRepo.save(s);

        return convertToDTO(s);
    }

    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */
    @Transactional
    public ResSectionDTO updateSection(ReqUpdateSectionDTO req) {

        Section s = fetchEntityById(req.getId());

        if (req.getName() != null) {
            s.setName(req.getName());
        }

        if (req.getStatus() != null) {
            s.setStatus(req.getStatus());
        }

        s = sectionRepo.save(s);

        return convertToDTO(s);
    }

    /*
     * ============================================================
     * INACTIVATE (TẮT BỘ PHẬN)
     * ============================================================
     */
    @Transactional
    public void setInactive(Long id) {
        Section s = fetchEntityById(id);
        s.setStatus(0);
        sectionRepo.save(s);
    }

    /*
     * ============================================================
     * ACTIVATE (BẬT LẠI BỘ PHẬN)
     * ============================================================
     */
    @Transactional
    public void setActive(Long id) {
        Section s = fetchEntityById(id);
        s.setStatus(1);
        sectionRepo.save(s);
    }

    /*
     * ============================================================
     * FETCH ENTITY
     * ============================================================
     */
    public Section fetchEntityById(Long id) {
        return sectionRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bộ phận với id = " + id));
    }

    /*
     * ============================================================
     * FETCH ONE DTO
     * ============================================================
     */
    public ResSectionDTO fetchOne(Long id) {
        return convertToDTO(fetchEntityById(id));
    }

    /*
     * ============================================================
     * FETCH ALL (PAGINATION + FILTER)
     * ============================================================
     */
    public ResultPaginationDTO fetchAll(
            Specification<Section> spec,
            Pageable pageable) {

        Page<Section> page = sectionRepo.findAll(spec, pageable);

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
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * ============================================================
     * CONVERT ENTITY -> DTO
     * ============================================================
     */
    private ResSectionDTO convertToDTO(Section s) {

        ResSectionDTO res = new ResSectionDTO();

        res.setId(s.getId());
        res.setCode(s.getCode());
        res.setName(s.getName());

        res.setStatus(s.getStatus());
        res.setActive(s.getStatus() != null && s.getStatus() == 1);

        res.setCreatedAt(s.getCreatedAt());
        res.setUpdatedAt(s.getUpdatedAt());
        res.setCreatedBy(s.getCreatedBy());
        res.setUpdatedBy(s.getUpdatedBy());

        ResSectionDTO.DepartmentInfo d = new ResSectionDTO.DepartmentInfo();
        d.setId(s.getDepartment().getId());
        d.setName(s.getDepartment().getName());
        res.setDepartment(d);
        return res;
    }
}
