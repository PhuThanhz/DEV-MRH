package vn.system.app.modules.permissionassignment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.service.DepartmentJobTitleService;
import vn.system.app.modules.permissionassignment.domain.PermissionAssignment;
import vn.system.app.modules.permissionassignment.domain.request.ReqPermissionAssignmentDTO;
import vn.system.app.modules.permissionassignment.domain.response.ResPermissionAssignmentDTO;
import vn.system.app.modules.permissionassignment.repository.PermissionAssignmentRepository;
import vn.system.app.modules.permissioncategoryscope.repository.PermissionCategoryScopeRepository;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.permissioncontent.repository.PermissionContentRepository;
import vn.system.app.modules.processaction.domain.ProcessAction;
import vn.system.app.modules.processaction.service.ProcessActionService;

@Service
public class PermissionAssignmentService {

    private final PermissionAssignmentRepository repository;
    private final PermissionContentRepository permissionContentRepository;
    private final PermissionCategoryScopeRepository categoryScopeRepository;
    private final DepartmentJobTitleService departmentJobTitleService;
    private final ProcessActionService processActionService;

    public PermissionAssignmentService(
            PermissionAssignmentRepository repository,
            PermissionContentRepository permissionContentRepository,
            PermissionCategoryScopeRepository categoryScopeRepository,
            DepartmentJobTitleService departmentJobTitleService,
            ProcessActionService processActionService) {

        this.repository = repository;
        this.permissionContentRepository = permissionContentRepository;
        this.categoryScopeRepository = categoryScopeRepository;
        this.departmentJobTitleService = departmentJobTitleService;
        this.processActionService = processActionService;
    }

    // ==================================================
    // CREATE
    // ==================================================
    @Transactional
    public ResPermissionAssignmentDTO create(ReqPermissionAssignmentDTO req) {

        PermissionContent content = permissionContentRepository
                .findById(req.getPermissionContentId())
                .orElseThrow(() -> new IdInvalidException(
                        "PermissionContent không tồn tại"));

        DepartmentJobTitle djt = departmentJobTitleService
                .fetchEntityById(req.getDepartmentJobTitleId());

        // ===== VALIDATE: DJT thuộc category scope =====
        boolean isAllowed = categoryScopeRepository
                .existsByCategory_IdAndDepartmentJobTitleId(
                        content.getCategory().getId(),
                        djt.getId());

        if (!isAllowed) {
            throw new IdInvalidException(
                    "Chức danh không thuộc phạm vi áp dụng của danh mục quyền");
        }

        ProcessAction action = processActionService
                .fetchById(req.getProcessActionId());

        if (action == null) {
            throw new IdInvalidException("ProcessAction không tồn tại");
        }

        if (repository.existsByPermissionContent_IdAndDepartmentJobTitle_Id(
                content.getId(), djt.getId())) {
            throw new IdInvalidException(
                    "Chức danh đã được gán quyền cho nội dung này");
        }

        PermissionAssignment entity = new PermissionAssignment();
        entity.setPermissionContent(content);
        entity.setDepartmentJobTitle(djt);
        entity.setProcessAction(action);

        entity = repository.save(entity);
        return convertToDTO(entity);
    }

    // ==================================================
    // UPDATE
    // ==================================================
    @Transactional
    public ResPermissionAssignmentDTO update(
            Long id,
            ReqPermissionAssignmentDTO req) {

        PermissionAssignment entity = fetchEntityById(id);

        ProcessAction action = processActionService
                .fetchById(req.getProcessActionId());

        if (action == null) {
            throw new IdInvalidException("ProcessAction không tồn tại");
        }

        entity.setProcessAction(action);
        entity = repository.save(entity);

        return convertToDTO(entity);
    }

    // ==================================================
    // DELETE
    // ==================================================
    @Transactional
    public void delete(Long id) {
        repository.delete(fetchEntityById(id));
    }

    // ==================================================
    // FETCH ONE
    // ==================================================
    public ResPermissionAssignmentDTO fetchDetail(Long id) {
        return convertToDTO(fetchEntityById(id));
    }

    private PermissionAssignment fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "PermissionAssignment không tồn tại"));
    }

    // ==================================================
    // FETCH BY PERMISSION CONTENT
    // ==================================================
    public List<ResPermissionAssignmentDTO> fetchByPermissionContent(
            Long permissionContentId) {

        return repository
                .findByPermissionContent_Id(permissionContentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================================================
    // CONVERT RESPONSE
    // ==================================================
    private ResPermissionAssignmentDTO convertToDTO(
            PermissionAssignment entity) {

        ResPermissionAssignmentDTO res = new ResPermissionAssignmentDTO();

        res.setId(entity.getId());
        res.setPermissionContentId(
                entity.getPermissionContent().getId());

        res.setDepartmentJobTitleId(
                entity.getDepartmentJobTitle().getId());
        res.setJobTitleId(
                entity.getDepartmentJobTitle().getJobTitle().getId());
        res.setJobTitleName(
                entity.getDepartmentJobTitle().getJobTitle().getNameVi());

        res.setDepartmentId(
                entity.getDepartmentJobTitle().getDepartment().getId());
        res.setDepartmentName(
                entity.getDepartmentJobTitle().getDepartment().getName());

        res.setProcessActionId(
                entity.getProcessAction().getId());
        res.setProcessActionCode(
                entity.getProcessAction().getCode());
        res.setProcessActionName(
                entity.getProcessAction().getName());

        return res;
    }
}
