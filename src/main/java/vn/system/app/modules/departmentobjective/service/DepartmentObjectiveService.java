package vn.system.app.modules.departmentobjective.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.service.SectionService;
import vn.system.app.modules.departmentobjective.domain.DepartmentObjective;
import vn.system.app.modules.departmentobjective.domain.request.ReqCreateDepartmentObjective;
import vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionTreeDTO;
import vn.system.app.modules.departmentobjective.domain.response.ResDepartmentObjectiveDTO;
import vn.system.app.modules.departmentobjective.repository.DepartmentObjectiveRepository;

@Service
public class DepartmentObjectiveService {

    private final DepartmentObjectiveRepository repository;
    private final DepartmentService departmentService;
    private final SectionService sectionService;

    public DepartmentObjectiveService(
            DepartmentObjectiveRepository repository,
            DepartmentService departmentService,
            SectionService sectionService) {

        this.repository = repository;
        this.departmentService = departmentService;
        this.sectionService = sectionService;
    }

    /*
     * ==========================================
     * CREATE MISSION TREE
     * ==========================================
     */
    @Transactional
    public void handleCreate(ReqCreateDepartmentObjective req) {

        Department department = departmentService.fetchEntityById(req.getDepartmentId());

        /*
         * XÓA TOÀN BỘ MISSION CŨ
         */
        repository.deleteByDepartmentId(department.getId());

        LocalDate issueDate = req.getIssueDate() != null
                ? req.getIssueDate()
                : LocalDate.now();

        /*
         * CREATE OBJECTIVES
         */
        if (req.getObjectives() != null) {

            for (ReqCreateDepartmentObjective.ObjectiveItem o : req.getObjectives()) {

                DepartmentObjective entity = new DepartmentObjective();

                entity.setType("OBJECTIVE");
                entity.setContent(o.getContent());
                entity.setOrderNo(o.getOrderNo());
                entity.setIssueDate(issueDate);
                entity.setDepartment(department);

                repository.save(entity);
            }
        }

        /*
         * CREATE TASKS
         */
        if (req.getTasks() != null) {

            for (ReqCreateDepartmentObjective.SectionTask st : req.getTasks()) {

                Section section = sectionService.fetchEntityById(st.getSectionId());

                if (!section.getDepartment().getId()
                        .equals(department.getId())) {

                    throw new IdInvalidException(
                            "Section không thuộc phòng ban này");
                }

                if (st.getItems() != null) {

                    for (ReqCreateDepartmentObjective.TaskItem t : st.getItems()) {

                        DepartmentObjective entity = new DepartmentObjective();

                        entity.setType("TASK");
                        entity.setContent(t.getContent());
                        entity.setOrderNo(t.getOrderNo());
                        entity.setIssueDate(issueDate);
                        entity.setDepartment(department);
                        entity.setSection(section);

                        repository.save(entity);
                    }
                }
            }
        }
    }

    /*
     * ==========================================
     * DELETE
     * ==========================================
     */
    public void handleDelete(Long id) {

        DepartmentObjective entity = fetchById(id);

        if (entity == null) {
            throw new IdInvalidException("Không tìm thấy id = " + id);
        }

        repository.deleteById(id);
    }

    /*
     * ==========================================
     * FIND BY ID
     * ==========================================
     */
    public DepartmentObjective fetchById(Long id) {

        Optional<DepartmentObjective> optional = repository.findById(id);

        return optional.orElse(null);
    }

    /*
     * ==========================================
     * FETCH ALL
     * ==========================================
     */
    public ResultPaginationDTO fetchAll(
            Specification<DepartmentObjective> spec,
            Pageable pageable) {

        Page<DepartmentObjective> page = repository.findAll(spec, pageable);

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
                        .map(this::convert)
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * ==========================================
     * CONVERT ENTITY -> DTO
     * ==========================================
     */
    public ResDepartmentObjectiveDTO convert(
            DepartmentObjective e) {

        ResDepartmentObjectiveDTO res = new ResDepartmentObjectiveDTO();

        res.setId(e.getId());
        res.setType(e.getType());
        res.setContent(e.getContent());
        res.setOrderNo(e.getOrderNo());
        res.setIssueDate(e.getIssueDate());
        res.setStatus(e.getStatus());
        res.setCreatedAt(e.getCreatedAt());
        res.setUpdatedAt(e.getUpdatedAt());

        ResDepartmentObjectiveDTO.DepartmentInfo d = new ResDepartmentObjectiveDTO.DepartmentInfo();

        d.setId(e.getDepartment().getId());
        d.setName(e.getDepartment().getName());

        res.setDepartment(d);

        ResDepartmentObjectiveDTO.CompanyInfo c = new ResDepartmentObjectiveDTO.CompanyInfo();

        c.setId(e.getDepartment().getCompany().getId());
        c.setName(e.getDepartment().getCompany().getName());

        res.setCompany(c);

        if (e.getSection() != null) {

            ResDepartmentObjectiveDTO.SectionInfo s = new ResDepartmentObjectiveDTO.SectionInfo();

            s.setId(e.getSection().getId());
            s.setName(e.getSection().getName());

            res.setSection(s);
        }

        return res;
    }

    /*
     * ==========================================
     * LOAD MISSION TREE
     * ==========================================
     */
    public ResDepartmentMissionTreeDTO fetchMissionTree(
            Long departmentId) {

        Department department = departmentService.fetchEntityById(departmentId);

        List<DepartmentObjective> list = repository.findByDepartmentId(departmentId);

        ResDepartmentMissionTreeDTO res = new ResDepartmentMissionTreeDTO();

        ResDepartmentMissionTreeDTO.DepartmentInfo d = new ResDepartmentMissionTreeDTO.DepartmentInfo();

        d.setId(department.getId());
        d.setName(department.getName());

        res.setDepartment(d);

        res.setIssueDate(
                list.isEmpty()
                        ? null
                        : list.get(0).getIssueDate());

        /*
         * OBJECTIVES
         */
        res.setObjectives(
                list.stream()
                        .filter(i -> "OBJECTIVE".equals(i.getType()))
                        .map(i -> {

                            ResDepartmentMissionTreeDTO.ObjectiveItem o = new ResDepartmentMissionTreeDTO.ObjectiveItem();

                            o.setId(i.getId());
                            o.setContent(i.getContent());

                            return o;

                        })
                        .collect(Collectors.toList()));

        /*
         * TASK GROUP BY SECTION
         */
        Map<Section, List<DepartmentObjective>> map = list.stream()
                .filter(i -> "TASK".equals(i.getType())
                        && i.getSection() != null)
                .collect(Collectors.groupingBy(
                        DepartmentObjective::getSection));

        List<ResDepartmentMissionTreeDTO.SectionTask> sectionTasks = new ArrayList<>();

        map.forEach((section, tasks) -> {

            ResDepartmentMissionTreeDTO.SectionTask st = new ResDepartmentMissionTreeDTO.SectionTask();

            st.setSectionId(section.getId());
            st.setSectionName(section.getName());

            st.setTasks(
                    tasks.stream()
                            .map(t -> {

                                ResDepartmentMissionTreeDTO.TaskItem ti = new ResDepartmentMissionTreeDTO.TaskItem();

                                ti.setId(t.getId());
                                ti.setContent(t.getContent());

                                return ti;

                            })
                            .collect(Collectors.toList()));

            sectionTasks.add(st);
        });

        res.setTasks(sectionTasks);

        return res;
    }
}