package vn.system.app.modules.permissioncategory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.permissionassignment.domain.PermissionAssignment;
import vn.system.app.modules.permissionassignment.repository.PermissionAssignmentRepository;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.domain.response.PermissionCategoryMatrixResponse;
import vn.system.app.modules.permissioncategory.repository.PermissionCategoryRepository;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.permissioncontent.repository.PermissionContentRepository;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.processaction.domain.ProcessAction;

@Service
public class PermissionMatrixService {

    private final PermissionCategoryRepository categoryRepository;
    private final PermissionContentRepository contentRepository;
    private final PermissionAssignmentRepository assignmentRepository;

    public PermissionMatrixService(
            PermissionCategoryRepository categoryRepository,
            PermissionContentRepository contentRepository,
            PermissionAssignmentRepository assignmentRepository) {

        this.categoryRepository = categoryRepository;
        this.contentRepository = contentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // ==================================================
    // BUILD PERMISSION MATRIX
    // ==================================================
    @Transactional(readOnly = true)
    public PermissionCategoryMatrixResponse buildMatrix(Long categoryId) {

        // ===== VALIDATE CATEGORY =====
        PermissionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IdInvalidException("Danh mục phân quyền không tồn tại"));

        // ===== LOAD CONTENTS =====
        List<PermissionContent> contents = contentRepository.findByCategory_Id(categoryId);

        PermissionCategoryMatrixResponse res = new PermissionCategoryMatrixResponse();

        // ===== CATEGORY INFO =====
        PermissionCategoryMatrixResponse.Category cat = new PermissionCategoryMatrixResponse.Category();
        cat.setId(category.getId());
        cat.setCode(category.getCode());
        cat.setName(category.getName());
        res.setCategory(cat);

        // ===== MATRIX ROWS =====
        List<PermissionCategoryMatrixResponse.ContentRow> rows = new ArrayList<>();

        for (PermissionContent content : contents) {

            PermissionCategoryMatrixResponse.ContentRow row = new PermissionCategoryMatrixResponse.ContentRow();

            row.setContentId(content.getId());
            row.setContentName(content.getName());

            // ===== LOAD ASSIGNMENTS =====
            List<PermissionAssignment> assignments = assignmentRepository.findByPermissionContent_Id(content.getId());

            List<PermissionCategoryMatrixResponse.JobTitlePermission> jobTitles = assignments.stream().map(assign -> {

                DepartmentJobTitle djt = assign.getDepartmentJobTitle();
                ProcessAction action = assign.getProcessAction();

                PermissionCategoryMatrixResponse.JobTitlePermission jp = new PermissionCategoryMatrixResponse.JobTitlePermission();

                jp.setDepartmentJobTitleId(djt.getId());
                jp.setJobTitleId(djt.getJobTitle().getId());
                jp.setJobTitleName(djt.getJobTitle().getNameVi());
                jp.setProcessActionCode(action.getCode());

                return jp;

            }).collect(Collectors.toList());

            row.setJobTitles(jobTitles);
            rows.add(row);
        }

        res.setContents(rows);
        return res;
    }
}
