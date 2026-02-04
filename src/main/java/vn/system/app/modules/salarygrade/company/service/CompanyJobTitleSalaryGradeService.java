package vn.system.app.modules.salarygrade.company.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;
import vn.system.app.modules.salarygrade.company.domain.CompanyJobTitleSalaryGrade;
import vn.system.app.modules.salarygrade.company.domain.request.*;
import vn.system.app.modules.salarygrade.company.domain.response.*;
import vn.system.app.modules.salarygrade.company.repository.CompanyJobTitleSalaryGradeRepository;

@Service
public class CompanyJobTitleSalaryGradeService {

    private final CompanyJobTitleSalaryGradeRepository repo;
    private final CompanyJobTitleRepository companyJobTitleRepo;

    public CompanyJobTitleSalaryGradeService(
            CompanyJobTitleSalaryGradeRepository repo,
            CompanyJobTitleRepository companyJobTitleRepo) {
        this.repo = repo;
        this.companyJobTitleRepo = companyJobTitleRepo;
    }

    /*
     * ============================
     * CREATE
     * ============================
     */
    @Transactional
    public ResCompanyJobTitleSalaryGradeDTO create(
            ReqCreateCompanyJobTitleSalaryGradeDTO req) {

        // ---- BUSINESS VALIDATIONS ----

        if (req.getCompanyJobTitleId() == null || req.getCompanyJobTitleId() <= 0) {
            throw new IdInvalidException("companyJobTitleId không hợp lệ");
        }

        if (req.getGradeLevel() == null || req.getGradeLevel() <= 0) {
            throw new IdInvalidException("gradeLevel phải lớn hơn 0");
        }

        if (!companyJobTitleRepo.existsById(req.getCompanyJobTitleId())) {
            throw new IdInvalidException(
                    "CompanyJobTitle ID = " + req.getCompanyJobTitleId() + " không tồn tại");
        }

        if (repo.existsByCompanyJobTitleIdAndGradeLevel(
                req.getCompanyJobTitleId(),
                req.getGradeLevel())) {

            throw new IdInvalidException(
                    "Bậc lương " + req.getGradeLevel() + " đã tồn tại");
        }

        // ---- CREATE ENTITY ----

        CompanyJobTitleSalaryGrade sg = new CompanyJobTitleSalaryGrade();
        sg.setCompanyJobTitleId(req.getCompanyJobTitleId());
        sg.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(sg));
    }

    /*
     * ============================
     * UPDATE
     * ============================
     */
    @Transactional
    public ResCompanyJobTitleSalaryGradeDTO update(
            Long id,
            ReqUpdateCompanyJobTitleSalaryGradeDTO req) {

        if (id == null || id <= 0) {
            throw new IdInvalidException("ID không hợp lệ");
        }

        if (req.getGradeLevel() == null || req.getGradeLevel() <= 0) {
            throw new IdInvalidException("gradeLevel phải lớn hơn 0");
        }

        CompanyJobTitleSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy bậc lương ID = " + id));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu, không thể cập nhật");
        }

        boolean existed = repo.existsByCompanyJobTitleIdAndGradeLevel(
                sg.getCompanyJobTitleId(),
                req.getGradeLevel());

        if (existed && !req.getGradeLevel().equals(sg.getGradeLevel())) {
            throw new IdInvalidException(
                    "Bậc lương " + req.getGradeLevel() + " đã tồn tại");
        }

        sg.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(sg));
    }

    /*
     * ============================
     * DELETE (SOFT)
     * ============================
     */
    @Transactional
    public void delete(Long id) {

        if (id == null || id <= 0) {
            throw new IdInvalidException("ID không hợp lệ");
        }

        CompanyJobTitleSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương ID = " + id));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu");
        }

        sg.setActive(false);
        repo.save(sg);
    }

    /*
     * ============================
     * FETCH
     * ============================
     */
    public List<ResCompanyJobTitleSalaryGradeDTO> fetchByCompanyJobTitle(
            Long companyJobTitleId) {

        if (companyJobTitleId == null || companyJobTitleId <= 0) {
            throw new IdInvalidException("companyJobTitleId không hợp lệ");
        }

        return repo
                .findByCompanyJobTitleIdAndActiveTrueOrderByGradeLevelAsc(companyJobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================
     * MAPPER
     * ============================
     */
    private ResCompanyJobTitleSalaryGradeDTO toDTO(
            CompanyJobTitleSalaryGrade sg) {

        ResCompanyJobTitleSalaryGradeDTO res = new ResCompanyJobTitleSalaryGradeDTO();

        res.setId(sg.getId());
        res.setCompanyJobTitleId(sg.getCompanyJobTitleId());
        res.setGradeLevel(sg.getGradeLevel());
        res.setActive(sg.isActive());
        res.setCreatedAt(sg.getCreatedAt());
        res.setUpdatedAt(sg.getUpdatedAt());
        res.setCreatedBy(sg.getCreatedBy());
        res.setUpdatedBy(sg.getUpdatedBy());

        return res;
    }
}
