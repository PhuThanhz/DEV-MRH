package vn.system.app.modules.departmentsalarygrade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.departmentsalarygrade.domain.DepartmentSalaryGrade;
import vn.system.app.modules.departmentsalarygrade.domain.request.*;
import vn.system.app.modules.departmentsalarygrade.domain.response.*;
import vn.system.app.modules.departmentsalarygrade.repository.DepartmentSalaryGradeRepository;

@Service
public class DepartmentSalaryGradeService {

    private final DepartmentSalaryGradeRepository repo;
    private final DepartmentJobTitleRepository deptJobTitleRepo;

    public DepartmentSalaryGradeService(
            DepartmentSalaryGradeRepository repo,
            DepartmentJobTitleRepository deptJobTitleRepo) {
        this.repo = repo;
        this.deptJobTitleRepo = deptJobTitleRepo;
    }

    private void validate(Integer grade) {
        if (grade == null || grade <= 0) {
            throw new IdInvalidException("gradeLevel phải > 0");
        }
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */
    @Transactional
    public ResDepartmentSalaryGradeDTO create(ReqCreateDepartmentSalaryGradeDTO req) {

        validate(req.getGradeLevel());

        if (!deptJobTitleRepo.existsById(req.getDepartmentJobTitleId())) {
            throw new IdInvalidException("DepartmentJobTitle ID không tồn tại");
        }

        if (repo.existsByDepartmentJobTitleIdAndGradeLevel(
                req.getDepartmentJobTitleId(),
                req.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương đã tồn tại");
        }

        DepartmentSalaryGrade sg = new DepartmentSalaryGrade();
        sg.setDepartmentJobTitleId(req.getDepartmentJobTitleId());
        sg.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(sg));
    }

    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */
    @Transactional
    public ResDepartmentSalaryGradeDTO update(Long id, ReqUpdateDepartmentSalaryGradeDTO req) {

        validate(req.getGradeLevel());

        DepartmentSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID"));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu");
        }

        boolean existed = repo.existsByDepartmentJobTitleIdAndGradeLevel(
                sg.getDepartmentJobTitleId(),
                req.getGradeLevel());

        if (existed && !req.getGradeLevel().equals(sg.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương đã tồn tại");
        }

        sg.setGradeLevel(req.getGradeLevel());
        return toDTO(repo.save(sg));
    }

    /*
     * ============================================================
     * DELETE (SOFT DELETE)
     * ============================================================
     */
    @Transactional
    public void delete(Long id) {

        DepartmentSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID"));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu trước đó");
        }

        sg.setActive(false);
        repo.save(sg);
    }

    /*
     * ============================================================
     * RESTORE
     * ============================================================
     */
    @Transactional
    public ResDepartmentSalaryGradeDTO restore(Long id) {

        DepartmentSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID"));

        if (sg.isActive()) {
            throw new IdInvalidException("Bậc lương đang hoạt động");
        }

        sg.setActive(true);
        return toDTO(repo.save(sg));
    }

    /*
     * ============================================================
     * FETCH LIST — ⭐ TRẢ FULL LIST (active + inactive)
     * ============================================================
     */
    public List<ResDepartmentSalaryGradeDTO> fetchByDepartmentJobTitle(Long deptJobTitleId) {

        if (deptJobTitleId == null || deptJobTitleId <= 0) {
            throw new IdInvalidException("departmentJobTitleId không hợp lệ");
        }

        return repo.findByDepartmentJobTitleIdOrderByGradeLevelAsc(deptJobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================================================
     * MAPPER
     * ============================================================
     */
    private ResDepartmentSalaryGradeDTO toDTO(DepartmentSalaryGrade sg) {
        ResDepartmentSalaryGradeDTO res = new ResDepartmentSalaryGradeDTO();

        res.setId(sg.getId());
        res.setDepartmentJobTitleId(sg.getDepartmentJobTitleId());
        res.setGradeLevel(sg.getGradeLevel());
        res.setActive(sg.isActive());
        res.setCreatedAt(sg.getCreatedAt());
        res.setUpdatedAt(sg.getUpdatedAt());
        res.setCreatedBy(sg.getCreatedBy());
        res.setUpdatedBy(sg.getUpdatedBy());

        return res;
    }
}
