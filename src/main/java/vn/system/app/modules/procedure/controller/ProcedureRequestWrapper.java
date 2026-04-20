package vn.system.app.modules.procedure.controller;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.departmentprocedure.domain.request.DepartmentProcedureRequest;
import vn.system.app.modules.confidentialprocedure.domain.request.ConfidentialProcedureRequest;

@Getter
@Setter
public class ProcedureRequestWrapper {

    @NotBlank(message = "Mã quy trình không được để trống")
    private String procedureCode;

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;

    private String status;
    private Integer planYear;
    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;

    private Long departmentId;
    private List<Long> departmentIds;

    private Long sectionId;
    private List<String> userIds;
    private List<Long> roleIds;

    public CompanyProcedureRequest toCompanyRequest() {
        CompanyProcedureRequest req = new CompanyProcedureRequest();
        req.setProcedureCode(this.procedureCode);
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setIssuedDate(this.issuedDate);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentId(this.departmentId);
        req.setSectionId(this.sectionId);
        return req;
    }

    public DepartmentProcedureRequest toDepartmentRequest() {
        DepartmentProcedureRequest req = new DepartmentProcedureRequest();
        req.setProcedureCode(this.procedureCode);
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setIssuedDate(this.issuedDate);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentIds(this.departmentIds);
        req.setSectionId(this.sectionId);
        return req;
    }

    public ConfidentialProcedureRequest toConfidentialRequest() {
        ConfidentialProcedureRequest req = new ConfidentialProcedureRequest();
        req.setProcedureCode(this.procedureCode);
        req.setProcedureName(this.procedureName);
        req.setStatus(this.status);
        req.setPlanYear(this.planYear);
        req.setIssuedDate(this.issuedDate);
        req.setFileUrls(this.fileUrls);
        req.setNote(this.note);
        req.setDepartmentId(this.departmentId);
        req.setSectionId(this.sectionId);
        req.setUserIds(this.userIds);
        req.setRoleIds(this.roleIds);
        return req;
    }
}