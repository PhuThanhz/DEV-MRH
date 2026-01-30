package vn.system.app.modules.careerpath.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.CareerPath;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.repository.CareerPathRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.repository.JobTitleRepository;

@Service
public class CareerPathService {

    private final CareerPathRepository repository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;

    public CareerPathService(
            CareerPathRepository repository,
            DepartmentRepository departmentRepository,
            JobTitleRepository jobTitleRepository) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.jobTitleRepository = jobTitleRepository;
    }

    // ===== CREATE =====
    public CareerPath handleCreate(CareerPathRequest request) {

        if (repository.existsByDepartment_IdAndJobTitle_Id(
                request.getDepartmentId(),
                request.getJobTitleId())) {
            throw new IdInvalidException(
                    "Phòng ban này đã có lộ trình cho chức danh này");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phòng ban"));

        JobTitle jobTitle = jobTitleRepository.findById(request.getJobTitleId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chức danh"));

        CareerPath entity = new CareerPath();
        entity.setDepartment(department);
        entity.setJobTitle(jobTitle);
        entity.setJobStandard(request.getJobStandard());
        entity.setTrainingRequirement(request.getTrainingRequirement());
        entity.setEvaluationMethod(request.getEvaluationMethod());
        entity.setRequiredTime(request.getRequiredTime());
        entity.setTrainingOutcome(request.getTrainingOutcome());
        entity.setPerformanceRequirement(request.getPerformanceRequirement());
        entity.setSalaryNote(request.getSalaryNote());
        entity.setStatus(request.getStatus());

        return repository.save(entity);
    }

    // ===== DELETE =====
    public void handleDelete(Long id) {
        repository.deleteById(id);
    }

    // ===== FETCH BY ID =====
    public CareerPath fetchById(Long id) {
        Optional<CareerPath> optional = repository.findById(id);
        return optional.orElse(null);
    }

    // ===== UPDATE =====
    public CareerPath handleUpdate(Long id, CareerPathRequest request) {
        CareerPath current = fetchById(id);
        if (current == null)
            return null;

        current.setJobStandard(request.getJobStandard());
        current.setTrainingRequirement(request.getTrainingRequirement());
        current.setEvaluationMethod(request.getEvaluationMethod());
        current.setRequiredTime(request.getRequiredTime());
        current.setTrainingOutcome(request.getTrainingOutcome());
        current.setPerformanceRequirement(request.getPerformanceRequirement());
        current.setSalaryNote(request.getSalaryNote());
        current.setStatus(request.getStatus());

        return repository.save(current);
    }

    // ===== GET BY DEPARTMENT =====
    public List<CareerPathResponse> fetchByDepartment(Long departmentId) {

        return repository
                .findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(departmentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ===== CONVERT RESPONSE =====
    public CareerPathResponse convertToResponse(CareerPath e) {
        CareerPathResponse r = new CareerPathResponse();
        r.setId(e.getId());
        r.setDepartmentId(e.getDepartment().getId());
        r.setDepartmentName(e.getDepartment().getName());
        r.setJobTitleId(e.getJobTitle().getId());
        r.setJobTitleName(e.getJobTitle().getNameVi());
        r.setPositionLevelCode(e.getJobTitle().getPositionLevel().getCode());
        r.setBandOrder(e.getJobTitle().getPositionLevel().getBandOrder());
        r.setJobStandard(e.getJobStandard());
        r.setTrainingRequirement(e.getTrainingRequirement());
        r.setEvaluationMethod(e.getEvaluationMethod());
        r.setRequiredTime(e.getRequiredTime());
        r.setTrainingOutcome(e.getTrainingOutcome());
        r.setPerformanceRequirement(e.getPerformanceRequirement());
        r.setSalaryNote(e.getSalaryNote());
        r.setStatus(e.getStatus());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
