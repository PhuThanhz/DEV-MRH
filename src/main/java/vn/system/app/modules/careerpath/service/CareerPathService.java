package vn.system.app.modules.careerpath.service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.CareerPath;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.domain.response.ResCareerPathBandGroupDTO;
import vn.system.app.modules.careerpath.repository.CareerPathRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.repository.JobTitleRepository;

@Service
public class CareerPathService {

    private final CareerPathRepository repo;
    private final DepartmentRepository departmentRepo;
    private final JobTitleRepository jobTitleRepo;

    public CareerPathService(
            CareerPathRepository repo,
            DepartmentRepository departmentRepo,
            JobTitleRepository jobTitleRepo) {

        this.repo = repo;
        this.departmentRepo = departmentRepo;
        this.jobTitleRepo = jobTitleRepo;
    }

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @Transactional
    public CareerPathResponse handleCreate(CareerPathRequest req) {

        if (repo.existsByDepartment_IdAndJobTitle_Id(req.getDepartmentId(), req.getJobTitleId())) {
            throw new IdInvalidException("Phòng ban này đã có lộ trình cho chức danh này");
        }

        Department dep = departmentRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phòng ban"));

        JobTitle jt = jobTitleRepo.findById(req.getJobTitleId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chức danh"));

        CareerPath e = new CareerPath();
        e.setDepartment(dep);
        e.setJobTitle(jt);
        e.setJobStandard(req.getJobStandard());
        e.setTrainingRequirement(req.getTrainingRequirement());
        e.setEvaluationMethod(req.getEvaluationMethod());
        e.setRequiredTime(req.getRequiredTime());
        e.setTrainingOutcome(req.getTrainingOutcome());
        e.setPerformanceRequirement(req.getPerformanceRequirement());
        e.setSalaryNote(req.getSalaryNote());
        e.setStatus(req.getStatus());
        e.setActive(true);

        repo.save(e);
        return convertToResponse(e);
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @Transactional
    public CareerPathResponse handleUpdate(Long id, CareerPathRequest req) {
        CareerPath e = fetchById(id);

        e.setJobStandard(req.getJobStandard());
        e.setTrainingRequirement(req.getTrainingRequirement());
        e.setEvaluationMethod(req.getEvaluationMethod());
        e.setRequiredTime(req.getRequiredTime());
        e.setTrainingOutcome(req.getTrainingOutcome());
        e.setPerformanceRequirement(req.getPerformanceRequirement());
        e.setSalaryNote(req.getSalaryNote());
        e.setStatus(req.getStatus());

        repo.save(e);
        return convertToResponse(e);
    }

    /*
     * =====================================================
     * DEACTIVATE
     * =====================================================
     */
    @Transactional
    public void handleDeactivate(Long id) {
        CareerPath e = fetchById(id);

        if (!e.isActive())
            return;

        e.setActive(false);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(SecurityUtil.getCurrentUserLogin().orElse("system"));

        repo.save(e);
    }

    /*
     * =====================================================
     * FETCH BASIC
     * =====================================================
     */
    public CareerPath fetchById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy lộ trình thăng tiến"));
    }

    public List<CareerPathResponse> fetchByDepartment(Long departmentId) {
        return repo.findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(departmentId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<CareerPathResponse> fetchAllActive() {
        return repo.findByActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    /*
     * =====================================================
     * GROUP BY BAND
     * =====================================================
     */
    public List<ResCareerPathBandGroupDTO> fetchByDepartmentGroupedByBand(Long departmentId) {

        List<CareerPath> list = repo.findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(departmentId);

        var grouped = list.stream()
                .collect(Collectors.groupingBy(cp -> {
                    var pl = cp.getJobTitle().getPositionLevel();
                    return pl != null ? extractBand(pl.getCode()) : "Unknown";
                }));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String band = entry.getKey();
                    List<CareerPath> items = entry.getValue();

                    items.sort(
                            Comparator.comparingInt(cp -> extractLevel(cp.getJobTitle().getPositionLevel().getCode())));

                    ResCareerPathBandGroupDTO dto = new ResCareerPathBandGroupDTO();
                    dto.setBand(band);
                    dto.setBandOrder(items.get(0).getJobTitle().getPositionLevel().getBandOrder());
                    dto.setPositions(items.stream().map(this::convertToResponse).toList());

                    return dto;
                })
                .sorted(Comparator.comparingInt(ResCareerPathBandGroupDTO::getBandOrder).reversed())
                .toList();
    }

    /*
     * =====================================================
     * GLOBAL SORT
     * =====================================================
     */
    public List<CareerPathResponse> fetchGlobalCareerPath(Long departmentId) {
        return repo.findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(departmentId)
                .stream()
                .map(this::convertToResponse)
                .sorted(Comparator
                        .comparingInt((CareerPathResponse r) -> -r.getBandOrder())
                        .thenComparingInt(CareerPathResponse::getLevelNumber))
                .toList();
    }

    /*
     * =====================================================
     * HELPERS
     * =====================================================
     */
    private String extractBand(String code) {
        return code.replaceAll("[0-9]", "");
    }

    private int extractLevel(String code) {
        try {
            return Integer.parseInt(code.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /*
     * =====================================================
     * CONVERT RESPONSE
     * =====================================================
     */
    public CareerPathResponse convertToResponse(CareerPath e) {
        CareerPathResponse r = new CareerPathResponse();

        r.setId(e.getId());
        r.setDepartmentId(e.getDepartment().getId());
        r.setDepartmentName(e.getDepartment().getName());
        r.setJobTitleId(e.getJobTitle().getId());
        r.setJobTitleName(e.getJobTitle().getNameVi());

        var pl = e.getJobTitle().getPositionLevel();
        if (pl != null) {
            r.setPositionLevelCode(pl.getCode());
            r.setBandOrder(pl.getBandOrder());
            r.setLevelNumber(extractLevel(pl.getCode()));
        }

        r.setJobStandard(e.getJobStandard());
        r.setTrainingRequirement(e.getTrainingRequirement());
        r.setEvaluationMethod(e.getEvaluationMethod());
        r.setRequiredTime(e.getRequiredTime());
        r.setTrainingOutcome(e.getTrainingOutcome());
        r.setPerformanceRequirement(e.getPerformanceRequirement());
        r.setSalaryNote(e.getSalaryNote());

        r.setStatus(e.getStatus());
        r.setActive(e.isActive());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setCreatedBy(e.getCreatedBy());
        r.setUpdatedBy(e.getUpdatedBy());

        return r;
    }
}
