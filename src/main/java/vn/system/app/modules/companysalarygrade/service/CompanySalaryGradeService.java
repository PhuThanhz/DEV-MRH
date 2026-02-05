package vn.system.app.modules.companysalarygrade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;
import vn.system.app.modules.companysalarygrade.domain.CompanySalaryGrade;
import vn.system.app.modules.companysalarygrade.domain.request.*;
import vn.system.app.modules.companysalarygrade.domain.response.*;
import vn.system.app.modules.companysalarygrade.repository.CompanySalaryGradeRepository;

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

    @Transactional
    public ResCompanySalaryGradeDTO create(ReqCreateCompanySalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        if (!companyJobTitleRepo.existsById(req.getCompanyJobTitleId())) {
            throw new IdInvalidException("CompanyJobTitle ID không tồn tại");
        }

        if (repo.existsByCompanyJobTitleIdAndGradeLevel(
                req.getCompanyJobTitleId(), req.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương đã tồn tại");
        }

        CompanySalaryGrade entity = new CompanySalaryGrade();
        entity.setCompanyJobTitleId(req.getCompanyJobTitleId());
        entity.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(entity));
    }

    @Transactional
    public ResCompanySalaryGradeDTO update(Long id, ReqUpdateCompanySalaryGradeDTO req) {

        validateGrade(req.getGradeLevel());

        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        if (!entity.isActive()) {
            throw new IdInvalidException("Bậc lương đã vô hiệu");
        }

        boolean existed = repo.existsByCompanyJobTitleIdAndGradeLevel(
                entity.getCompanyJobTitleId(), req.getGradeLevel());

        if (existed && !req.getGradeLevel().equals(entity.getGradeLevel())) {
            throw new IdInvalidException("Bậc lương mới đã tồn tại");
        }

        entity.setGradeLevel(req.getGradeLevel());

        return toDTO(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        entity.setActive(false);
        repo.save(entity);
    }

    @Transactional
    public ResCompanySalaryGradeDTO restore(Long id) {
        CompanySalaryGrade entity = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        entity.setActive(true);
        return toDTO(repo.save(entity));
    }

    public List<ResCompanySalaryGradeDTO> fetch(Long companyJobTitleId) {

        if (companyJobTitleId == null || companyJobTitleId <= 0) {
            throw new IdInvalidException("companyJobTitleId không hợp lệ");
        }

        return repo.findByCompanyJobTitleIdAndActiveTrueOrderByGradeLevelAsc(companyJobTitleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

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
