// package vn.system.app.modules.companyprocedure.controller;

// import java.util.List;

// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.turkraft.springfilter.boot.Filter;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// import vn.system.app.common.response.ResultPaginationDTO;
// import vn.system.app.common.util.annotation.ApiMessage;

// import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
// import
// vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
// import
// vn.system.app.modules.companyprocedure.domain.response.ResCompanyProcedureDTO;
// import
// vn.system.app.modules.companyprocedure.service.CompanyProcedureService;

// @RestController
// @RequestMapping("/api/v1/company-procedures")
// @RequiredArgsConstructor
// public class CompanyProcedureController {

// private final CompanyProcedureService service;

// @PostMapping
// @ApiMessage("Tạo quy trình công ty")
// public ResponseEntity<ResCompanyProcedureDTO> create(
// @Valid @RequestBody CompanyProcedureRequest req) {
// return ResponseEntity.status(HttpStatus.CREATED)
// .body(service.handleCreate(req));
// }

// @PutMapping("/{id}")
// @ApiMessage("Cập nhật quy trình công ty")
// public ResponseEntity<ResCompanyProcedureDTO> update(
// @PathVariable Long id,
// @Valid @RequestBody CompanyProcedureRequest req) {
// return ResponseEntity.ok(service.handleUpdate(id, req));
// }

// @PutMapping("/{id}/active")
// @ApiMessage("Thay đổi trạng thái kích hoạt")
// public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
// service.handleToggleActive(id);
// return ResponseEntity.ok().build();
// }

// @DeleteMapping("/{id}")
// @ApiMessage("Xoá quy trình công ty")
// public ResponseEntity<Void> delete(@PathVariable Long id) {
// service.handleDelete(id);
// return ResponseEntity.ok().build();
// }

// @GetMapping("/{id}")
// @ApiMessage("Chi tiết quy trình công ty")
// public ResponseEntity<ResCompanyProcedureDTO> getOne(@PathVariable Long id) {
// return ResponseEntity.ok(
// service.convertToDTO(service.fetchById(id)));
// }

// @GetMapping
// @ApiMessage("Danh sách quy trình công ty")
// public ResponseEntity<ResultPaginationDTO> getAll(
// @Filter Specification<CompanyProcedure> spec,
// Pageable pageable) {
// return ResponseEntity.ok(service.fetchAll(spec, pageable));
// }

// @GetMapping("/by-department/{departmentId}")
// @ApiMessage("Danh sách quy trình theo phòng ban")
// public ResponseEntity<List<ResCompanyProcedureDTO>> getByDepartment(
// @PathVariable Long departmentId) {
// return ResponseEntity.ok(service.fetchByDepartment(departmentId));
// }

// @GetMapping("/by-section/{sectionId}")
// @ApiMessage("Danh sách quy trình theo bộ phận")
// public ResponseEntity<List<ResCompanyProcedureDTO>> getBySection(
// @PathVariable Long sectionId) {
// return ResponseEntity.ok(service.fetchBySection(sectionId));
// }
// }