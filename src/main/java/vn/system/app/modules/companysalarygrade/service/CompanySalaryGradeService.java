package vn.system.app.modules.companysalarygrade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.companysalarygrade.domain.CompanySalaryGrade;
import vn.system.app.modules.companysalarygrade.domain.request.*;
import vn.system.app.modules.companysalarygrade.domain.response.*;
import vn.system.app.modules.companysalarygrade.repository.CompanySalaryGradeRepository;

import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;

@Service
@RequiredArgsConstructor
public class CompanySalaryGradeService {

    private final CompanySalaryGradeRepository repo;
    private final CompanyJobTitleRepository companyJobTitleRepo;

    private void validateGrade(Integer gradeLevel) {
        if (gradeLevel == null || gradeLevel <= 0) {
            throw new IdInvalidException("gradeLevel phải lớn hơn 0");
        }
    }

    /*
     * ======================================================
     * CREATE
     * ======================================================
     */
    @Transactional
    public ResCompanySalaryGradeDTO create(ReqCreateCompanySalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        if (!companyJobTitleRepo.existsById(req.getCompanyJobTitleId())) {
            throw new IdInvalidException("CompanyJobTitle ID không tồn tại");
        }

        if (repo.existsByCompanyJobTitleIdAndGradeLevel(
                req.getCompanyJobTitleId(),
                req.getGradeLevel())) {

            throw new IdInvalidException("Bậc lương đã tồn tại");
        }

        CompanySalaryGrade entity = new CompanySalaryGrade();
        entity.setCompanyJobTitleId(req.getCompanyJobTitleId());
        entity.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(entity));
    }

    /*
     * ======================================================
     * UPDATE
     * ======================================================
     */
    @Transactional
    public ResCompanySalaryGradeDTO update(Long id, ReqUpdateCompanySalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        if (!entity.isActive()) {
            throw new IdInvalidException("Bậc lương đã vô hiệu, không thể cập nhật");
        }

        boolean exists = repo.existsByCompanyJobTitleIdAndGradeLevel(
                entity.getCompanyJobTitleId(),
                req.getGradeLevel());

        // Nếu level mới đã tồn tại và khác level cũ → lỗi
        if (exists && !req.getGradeLevel().equals(entity.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương mới đã tồn tại");
        }

        entity.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(entity));
    }

    /*
     * ======================================================
     * DELETE (SOFT DELETE)
     * ======================================================
     */
    @Transactional
    public void delete(Long id) {

        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        if (!entity.isActive()) {
            throw new IdInvalidException("Bậc lương đã bị vô hiệu");
        }

        entity.setActive(false);
        repo.save(entity);
    }

    /*
     * ======================================================
     * RESTORE
     * ======================================================
     */
    @Transactional
    public ResCompanySalaryGradeDTO restore(Long id) {

        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        if (entity.isActive()) {
            throw new IdInvalidException("Bậc lương đang hoạt động");
        }

        entity.setActive(true);
        return toDTO(repo.save(entity));
    }

    /*
     * ======================================================
     * FETCH ALL (ACTIVE + INACTIVE)
     * ======================================================
     */
    public List<ResCompanySalaryGradeDTO> fetch(Long companyJobTitleId) {

        if (companyJobTitleId == null || companyJobTitleId <= 0) {
            throw new IdInvalidException("companyJobTitleId không hợp lệ");
        }

        return repo.findByCompanyJobTitleIdOrderByGradeLevelAsc(companyJobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /*
     * ======================================================
     * MAPPER
     * ======================================================
     */
    private ResCompanySalaryGradeDTO toDTO(CompanySalaryGrade e) {

        ResCompanySalaryGradeDTO dto = new ResCompanySalaryGradeDTO();

        dto.setId(e.getId());
        dto.setCompanyJobTitleId(e.getCompanyJobTitleId());
        dto.setGradeLevel(e.getGradeLevel());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        return dto;
    }
}
