package vn.system.app.modules.procedure.controller;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.departmentprocedure.domain.request.DepartmentProcedureRequest;
import vn.system.app.modules.confidentialprocedure.domain.request.ConfidentialProcedureRequest;

@Getter
@Setter
public class ProcedureRequestWrapper {

    @NotBlank(message = "Mã quy trình không được để trống")
    private String procedureCode; // ← THÊM MỚI

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;

    private String status;
    private Integer planYear;
    private List<String> fileUrls;
    private String note;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private Long sectionId;

    private List<Long> userIds;
    private List<Long> roleIds;

    // ===== Convert sang CompanyProcedureRequest =====
    public CompanyProcedureRequest toCompanyRequest() {
        CompanyProcedureRequest req = new CompanyProcedureRequest();
        req.setProcedureCode(this.procedureCode); // ← THÊM
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentId(this.departmentId);
        req.setSectionId(this.sectionId);
        return req;
    }

    // ===== Convert sang DepartmentProcedureRequest =====
    public DepartmentProcedureRequest toDepartmentRequest() {
        DepartmentProcedureRequest req = new DepartmentProcedureRequest();
        req.setProcedureCode(this.procedureCode); // ← THÊM
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentId(this.departmentId);
        req.setSectionId(this.sectionId);
        return req;
    }

    // ===== Convert sang ConfidentialProcedureRequest =====
    public ConfidentialProcedureRequest toConfidentialRequest() {
        ConfidentialProcedureRequest req = new ConfidentialProcedureRequest();
        req.setProcedureCode(this.procedureCode); // ← THÊM
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentId(this.departmentId);
        req.setSectionId(this.sectionId);
        req.setUserIds(this.userIds);
        req.setRoleIds(this.roleIds);
        return req;
    }
}