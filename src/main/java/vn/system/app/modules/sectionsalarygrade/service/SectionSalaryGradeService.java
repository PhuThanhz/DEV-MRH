package vn.system.app.modules.sectionsalarygrade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;
import vn.system.app.modules.sectionsalarygrade.domain.SectionSalaryGrade;
import vn.system.app.modules.sectionsalarygrade.domain.request.*;
import vn.system.app.modules.sectionsalarygrade.domain.response.*;
import vn.system.app.modules.sectionsalarygrade.repository.SectionSalaryGradeRepository;

@Service
public class SectionSalaryGradeService {

    private final SectionSalaryGradeRepository repo;
    private final SectionJobTitleRepository sectionJobTitleRepo;

    public SectionSalaryGradeService(
            SectionSalaryGradeRepository repo,
            SectionJobTitleRepository sectionJobTitleRepo) {

        this.repo = repo;
        this.sectionJobTitleRepo = sectionJobTitleRepo;
    }

    private void validateGrade(Integer grade) {
        if (grade == null || grade <= 0) {
            throw new IdInvalidException("gradeLevel phải lớn hơn 0");
        }
    }

    /*
     * ============================
     * CREATE
     * ============================
     */
    @Transactional
    public ResSectionSalaryGradeDTO create(ReqCreateSectionSalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        if (!sectionJobTitleRepo.existsById(req.getSectionJobTitleId())) {
            throw new IdInvalidException("SectionJobTitle ID không tồn tại");
        }

        if (repo.existsBySectionJobTitleIdAndGradeLevel(
                req.getSectionJobTitleId(),
                req.getGradeLevel())) {

            throw new IdInvalidException("Bậc lương đã tồn tại");
        }

        SectionSalaryGrade sg = new SectionSalaryGrade();
        sg.setSectionJobTitleId(req.getSectionJobTitleId());
        sg.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(sg));
    }

    /*
     * ============================
     * UPDATE
     * ============================
     */
    @Transactional
    public ResSectionSalaryGradeDTO update(Long id, ReqUpdateSectionSalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        SectionSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID = " + id));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu");
        }

        boolean existed = repo.existsBySectionJobTitleIdAndGradeLevel(
                sg.getSectionJobTitleId(),
                req.getGradeLevel());

        if (existed && !req.getGradeLevel().equals(sg.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương đã tồn tại");
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

        SectionSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID = " + id));

        if (!sg.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu trước đó");
        }

        sg.setActive(false);
        repo.save(sg);
    }

    /*
     * ============================
     * RESTORE
     * ============================
     */
    @Transactional
    public ResSectionSalaryGradeDTO restore(Long id) {

        SectionSalaryGrade sg = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID = " + id));

        if (sg.isActive()) {
            throw new IdInvalidException("Bậc lương đang hoạt động, không cần khôi phục");
        }

        sg.setActive(true);
        return toDTO(repo.save(sg));
    }

    /*
     * ============================
     * FETCH LIST
     * ============================
     */
    public List<ResSectionSalaryGradeDTO> fetchBySectionJobTitle(Long sectionJobTitleId) {

        if (sectionJobTitleId == null || sectionJobTitleId <= 0) {
            throw new IdInvalidException("sectionJobTitleId không hợp lệ");
        }

        return repo.findBySectionJobTitleIdOrderByGradeLevelAsc(sectionJobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================
     * MAPPER
     * ============================
     */
    private ResSectionSalaryGradeDTO toDTO(SectionSalaryGrade sg) {

        ResSectionSalaryGradeDTO res = new ResSectionSalaryGradeDTO();

        res.setId(sg.getId());
        res.setSectionJobTitleId(sg.getSectionJobTitleId());
        res.setGradeLevel(sg.getGradeLevel());
        res.setActive(sg.isActive());
        res.setCreatedAt(sg.getCreatedAt());
        res.setUpdatedAt(sg.getUpdatedAt());
        res.setCreatedBy(sg.getCreatedBy());
        res.setUpdatedBy(sg.getUpdatedBy());

        return res;
    }
}
