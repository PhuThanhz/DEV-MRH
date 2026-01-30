package vn.system.app.modules.deptmission.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.deptmission.domain.DeptMission;
import vn.system.app.modules.deptmission.domain.request.ReqDeptMissionDTO;
import vn.system.app.modules.deptmission.domain.response.ResDeptMissionDTO;
import vn.system.app.modules.deptmission.repository.DeptMissionRepository;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.section.service.SectionService;

@Service
public class DeptMissionService {

    private final DeptMissionRepository repository;
    private final DepartmentService departmentService;
    private final SectionService sectionService;

    public DeptMissionService(
            DeptMissionRepository repository,
            DepartmentService departmentService,
            SectionService sectionService) {
        this.repository = repository;
        this.departmentService = departmentService;
        this.sectionService = sectionService;
    }

    /* ================= CREATE ================= */

    @Transactional
    public ResDeptMissionDTO create(ReqDeptMissionDTO req) {

        if (repository.existsByDepartmentId(req.getDepartmentId())) {
            throw new IdInvalidException(
                    "Phòng ban đã tồn tại mục tiêu & nhiệm vụ");
        }

        return saveInternal(req, false);
    }

    /* ================= UPDATE ================= */

    @Transactional
    public ResDeptMissionDTO update(ReqDeptMissionDTO req) {

        if (!repository.existsByDepartmentId(req.getDepartmentId())) {
            throw new IdInvalidException(
                    "Phòng ban chưa có mục tiêu & nhiệm vụ để cập nhật");
        }

        repository.deleteAllByDepartmentId(req.getDepartmentId());
        return saveInternal(req, true);
    }

    /* ================= SAVE CORE ================= */

    private ResDeptMissionDTO saveInternal(
            ReqDeptMissionDTO req, boolean isUpdate) {

        Department department = departmentService.fetchEntityById(req.getDepartmentId());

        for (ReqDeptMissionDTO.SectionTaskDTO item : req.getSectionTasks()) {

            Section section = sectionService.fetchEntityById(item.getSectionId());

            if (!section.getDepartment().getId()
                    .equals(department.getId())) {

                throw new IdInvalidException(
                        "Bộ phận '" + section.getName()
                                + "' không thuộc phòng ban '"
                                + department.getName() + "'");
            }

            DeptMission dm = new DeptMission();
            dm.setDepartment(department);
            dm.setSection(section);
            dm.setObjectives(req.getObjectives());
            dm.setContent(item.getContent());

            repository.save(dm);
        }

        return fetchByDepartmentId(department.getId());
    }

    /* ================= FETCH ================= */

    public ResDeptMissionDTO fetchByDepartmentId(Long departmentId) {

        List<DeptMission> list = repository.findAllByDepartmentId(departmentId);

        if (list.isEmpty()) {
            throw new IdInvalidException(
                    "Phòng ban chưa có mục tiêu & nhiệm vụ");
        }

        DeptMission first = list.get(0);

        ResDeptMissionDTO res = new ResDeptMissionDTO();
        res.setObjectives(first.getObjectives());
        res.setCreatedAt(first.getCreatedAt());
        res.setUpdatedAt(first.getUpdatedAt());

        ResDeptMissionDTO.DepartmentDTO dept = new ResDeptMissionDTO.DepartmentDTO();
        dept.setId(first.getDepartment().getId());
        dept.setCode(first.getDepartment().getCode());
        dept.setName(first.getDepartment().getName());
        res.setDepartment(dept);

        List<ResDeptMissionDTO.SectionTaskDTO> tasks = new ArrayList<>();

        for (DeptMission dm : list) {
            ResDeptMissionDTO.SectionTaskDTO dto = new ResDeptMissionDTO.SectionTaskDTO();
            dto.setSectionId(dm.getSection().getId());
            dto.setSectionName(dm.getSection().getName());
            dto.setContent(dm.getContent());
            tasks.add(dto);
        }

        res.setSectionTasks(tasks);
        return res;
    }

    /* ================= DELETE ================= */

    @Transactional
    public void deleteByDepartment(Long departmentId) {
        repository.deleteAllByDepartmentId(departmentId);
    }
}
