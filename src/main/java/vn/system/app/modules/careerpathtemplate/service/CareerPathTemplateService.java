package vn.system.app.modules.careerpathtemplate.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.CareerPath;
import vn.system.app.modules.careerpath.repository.CareerPathRepository;
import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplate;
import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplateStep;
import vn.system.app.modules.careerpathtemplate.domain.request.CareerPathTemplateRequest;
import vn.system.app.modules.careerpathtemplate.domain.response.CareerPathTemplateResponse;
import vn.system.app.modules.careerpathtemplate.repository.CareerPathTemplateRepository;
import vn.system.app.modules.careerpathtemplate.repository.CareerPathTemplateStepRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;

@Service
public class CareerPathTemplateService {

    private final CareerPathTemplateRepository templateRepo;
    private final CareerPathTemplateStepRepository stepRepo;
    private final CareerPathRepository careerPathRepo;
    private final DepartmentRepository departmentRepo;

    public CareerPathTemplateService(
            CareerPathTemplateRepository templateRepo,
            CareerPathTemplateStepRepository stepRepo,
            CareerPathRepository careerPathRepo,
            DepartmentRepository departmentRepo) {
        this.templateRepo = templateRepo;
        this.stepRepo = stepRepo;
        this.careerPathRepo = careerPathRepo;
        this.departmentRepo = departmentRepo;
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Transactional
    public CareerPathTemplateResponse handleCreate(CareerPathTemplateRequest req) {

        if (templateRepo.existsByNameAndDepartment_Id(req.getName(), req.getDepartmentId())) {
            throw new IdInvalidException(
                    "Ten lo trinh da ton tai trong phong ban nay: " + req.getName());
        }

        Department department = departmentRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Khong tim thay phong ban"));

        CareerPathTemplate template = new CareerPathTemplate();
        template.setName(req.getName());
        template.setDescription(req.getDescription());
        template.setDepartment(department);
        template.setActive(true);

        List<CareerPathTemplateStep> steps = req.getSteps().stream()
                .map(s -> buildStep(template, s, req.getDepartmentId()))
                .collect(Collectors.toList());

        template.setSteps(steps);

        return convertToResponse(templateRepo.save(template));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public CareerPathTemplateResponse handleUpdate(Long id, CareerPathTemplateRequest req) {

        CareerPathTemplate template = fetchById(id);

        if (templateRepo.existsByNameAndDepartment_IdAndIdNot(
                req.getName(), req.getDepartmentId(), id)) {
            throw new IdInvalidException(
                    "Ten lo trinh da ton tai trong phong ban nay: " + req.getName());
        }

        Department department = departmentRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Khong tim thay phong ban"));

        template.setName(req.getName());
        template.setDescription(req.getDescription());
        template.setDepartment(department);

        template.getSteps().clear();
        List<CareerPathTemplateStep> newSteps = req.getSteps().stream()
                .map(s -> buildStep(template, s, req.getDepartmentId()))
                .collect(Collectors.toList());
        template.getSteps().addAll(newSteps);

        return convertToResponse(templateRepo.save(template));
    }

    // =====================================================
    // DEACTIVATE
    // =====================================================
    @Transactional
    public void handleDeactivate(Long id) {
        CareerPathTemplate template = fetchById(id);
        template.setActive(false);
        templateRepo.save(template);
    }

    // =====================================================
    // ACTIVATE
    // =====================================================
    @Transactional
    public void handleActivate(Long id) {
        CareerPathTemplate template = fetchById(id);
        template.setActive(true);
        templateRepo.save(template);
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public CareerPathTemplateResponse fetchOne(Long id) {
        return convertToResponse(fetchById(id));
    }

    // =====================================================
    // FETCH ALL ACTIVE
    // =====================================================
    public List<CareerPathTemplateResponse> fetchAllActive() {
        return templateRepo.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH ALL (ke inactive cho admin)
    // =====================================================
    public List<CareerPathTemplateResponse> fetchAll() {
        return templateRepo.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH BY DEPARTMENT chi active
    // =====================================================
    public List<CareerPathTemplateResponse> fetchByDepartment(Long departmentId) {
        return templateRepo.findByDepartment_IdAndActiveTrueOrderByNameAsc(departmentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH ALL BY DEPARTMENT ke inactive
    // =====================================================
    public List<CareerPathTemplateResponse> fetchAllByDepartment(Long departmentId) {
        return templateRepo.findByDepartment_IdOrderByNameAsc(departmentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FETCH STEP KE TIEP
    // FIX: dung List thay Optional tranh NonUniqueResultException
    // query "step_order > x" tra ve nhieu buoc, lay buoc gan nhat (index 0)
    // =====================================================
    public CareerPathTemplateStep fetchNextStep(Long templateId, Integer currentStepOrder) {
        List<CareerPathTemplateStep> steps = stepRepo
                .findByTemplate_IdAndStepOrderGreaterThanOrderByStepOrderAsc(
                        templateId, currentStepOrder);
        return steps.isEmpty() ? null : steps.get(0);
    }

    // =====================================================
    // FETCH STEP CU THE
    // =====================================================
    public CareerPathTemplateStep fetchStep(Long templateId, Integer stepOrder) {
        return stepRepo.findByTemplate_IdAndStepOrder(templateId, stepOrder)
                .orElseThrow(() -> new IdInvalidException(
                        "Khong tim thay buoc " + stepOrder + " trong lo trinh nay"));
    }

    // =====================================================
    // FETCH ALL STEPS cua 1 template
    // =====================================================
    public List<CareerPathTemplateStep> fetchSteps(Long templateId) {
        return stepRepo.findByTemplate_IdOrderByStepOrderAsc(templateId);
    }

    // =====================================================
    // HELPER fetch entity
    // =====================================================
    public CareerPathTemplate fetchById(Long id) {
        return templateRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Khong tim thay lo trinh: " + id));
    }

    // =====================================================
    // HELPER build step + validate cung phong ban
    // =====================================================
    private CareerPathTemplateStep buildStep(
            CareerPathTemplate template,
            CareerPathTemplateRequest.StepRequest s,
            Long expectedDepartmentId) {

        CareerPath cp = careerPathRepo.findById(s.getCareerPathId())
                .orElseThrow(() -> new IdInvalidException(
                        "Khong tim thay chuc danh: " + s.getCareerPathId()));

        if (!cp.getDepartment().getId().equals(expectedDepartmentId)) {
            throw new IdInvalidException(
                    "Buoc " + s.getStepOrder() + ": chuc danh '"
                            + cp.getJobTitle().getNameVi()
                            + "' khong thuoc phong ban nay");
        }

        CareerPathTemplateStep step = new CareerPathTemplateStep();
        step.setTemplate(template);
        step.setStepOrder(s.getStepOrder());
        step.setCareerPath(cp);
        step.setDurationMonths(s.getDurationMonths());
        step.setDescription(s.getDescription());
        return step;
    }

    // =====================================================
    // CONVERT TO RESPONSE
    // =====================================================
    public CareerPathTemplateResponse convertToResponse(CareerPathTemplate t) {

        CareerPathTemplateResponse res = new CareerPathTemplateResponse();
        res.setId(t.getId());
        res.setName(t.getName());
        res.setDescription(t.getDescription());
        res.setActive(t.isActive());

        if (t.getDepartment() != null) {
            res.setDepartmentId(t.getDepartment().getId());
            res.setDepartmentName(t.getDepartment().getName());
        }

        res.setCreatedAt(t.getCreatedAt());
        res.setUpdatedAt(t.getUpdatedAt());
        res.setCreatedBy(t.getCreatedBy());
        res.setUpdatedBy(t.getUpdatedBy());

        res.setSteps(t.getSteps().stream()
                .map(this::convertStepToResponse)
                .collect(Collectors.toList()));

        return res;
    }

    private CareerPathTemplateResponse.StepResponse convertStepToResponse(CareerPathTemplateStep s) {

        CareerPathTemplateResponse.StepResponse r = new CareerPathTemplateResponse.StepResponse();
        r.setId(s.getId());
        r.setStepOrder(s.getStepOrder());
        r.setDurationMonths(s.getDurationMonths());
        r.setDescription(s.getDescription());

        CareerPath cp = s.getCareerPath();
        r.setCareerPathId(cp.getId());
        r.setJobTitleName(cp.getJobTitle().getNameVi());
        r.setDepartmentId(cp.getDepartment().getId());
        r.setDepartmentName(cp.getDepartment().getName());

        var pl = cp.getJobTitle().getPositionLevel();
        if (pl != null) {
            r.setPositionLevelCode(pl.getCode());
            try {
                r.setLevelNumber(Integer.parseInt(pl.getCode().replaceAll("[^0-9]", "")));
            } catch (Exception ignored) {
            }
        }

        return r;
    }
}