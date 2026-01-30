package vn.system.app.modules.salarygradeperformancerating.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.salarygrade.domain.SalaryGrade;
import vn.system.app.modules.salarygrade.repository.SalaryGradeRepository;
import vn.system.app.modules.salarygradeperformancerating.domain.SalaryGradePerformanceRating;
import vn.system.app.modules.salarygradeperformancerating.domain.request.SalaryGradePerformanceRatingRequest;
import vn.system.app.modules.salarygradeperformancerating.domain.response.SalaryGradePerformanceRatingResponse;
import vn.system.app.modules.salarygradeperformancerating.repository.SalaryGradePerformanceRatingRepository;

@Service
public class SalaryGradePerformanceRatingService {

    private final SalaryGradePerformanceRatingRepository repository;
    private final SalaryGradeRepository salaryGradeRepository;

    public SalaryGradePerformanceRatingService(
            SalaryGradePerformanceRatingRepository repository,
            SalaryGradeRepository salaryGradeRepository) {
        this.repository = repository;
        this.salaryGradeRepository = salaryGradeRepository;
    }

    /*
     * ===============================
     * CREATE
     * ===============================
     */
    public SalaryGradePerformanceRating handleCreate(
            SalaryGradePerformanceRatingRequest request) {

        if (this.repository.existsBySalaryGrade_Id(request.getSalaryGradeId())) {
            throw new IdInvalidException(
                    "Bậc lương đã tồn tại xếp loại thực hiện hiệu quả công việc");
        }

        SalaryGrade salaryGrade = this.salaryGradeRepository
                .findById(request.getSalaryGradeId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        SalaryGradePerformanceRating entity = new SalaryGradePerformanceRating();

        entity.setSalaryGrade(salaryGrade);
        entity.setRatingAText(request.getRatingAText());
        entity.setRatingBText(request.getRatingBText());
        entity.setRatingCText(request.getRatingCText());
        entity.setRatingDText(request.getRatingDText());
        entity.setStatus(request.getStatus());

        return this.repository.save(entity);
    }

    /*
     * ===============================
     * FETCH BY ID
     * ===============================
     */
    public SalaryGradePerformanceRating fetchById(long id) {
        Optional<SalaryGradePerformanceRating> optional = this.repository.findById(id);
        return optional.orElse(null);
    }

    /*
     * ===============================
     * FETCH BY SALARY GRADE
     * ===============================
     */
    public SalaryGradePerformanceRating fetchBySalaryGrade(long salaryGradeId) {
        Optional<SalaryGradePerformanceRating> optional = this.repository.findBySalaryGrade_Id(salaryGradeId);
        return optional.orElse(null);
    }

    /*
     * ===============================
     * UPDATE
     * ===============================
     */
    public SalaryGradePerformanceRating handleUpdate(
            SalaryGradePerformanceRatingRequest request,
            long id) {

        SalaryGradePerformanceRating current = this.fetchById(id);

        if (current != null) {
            current.setRatingAText(request.getRatingAText());
            current.setRatingBText(request.getRatingBText());
            current.setRatingCText(request.getRatingCText());
            current.setRatingDText(request.getRatingDText());
            current.setStatus(request.getStatus());

            current = this.repository.save(current);
        }

        return current;
    }

    /*
     * ===============================
     * DELETE
     * ===============================
     */
    public void handleDeleteById(long id) {
        this.repository.deleteById(id);
    }

    /*
     * ===============================
     * FETCH ALL (FILTER + PAGINATION)
     * ===============================
     */
    public ResultPaginationDTO fetchAll(
            Specification<SalaryGradePerformanceRating> spec,
            Pageable pageable) {

        Page<SalaryGradePerformanceRating> page = this.repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);

        List<SalaryGradePerformanceRatingResponse> list = page.getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    /*
     * ===============================
     * CONVERT RESPONSE
     * ===============================
     */
    public SalaryGradePerformanceRatingResponse convertToResponse(
            SalaryGradePerformanceRating entity) {

        SalaryGradePerformanceRatingResponse res = new SalaryGradePerformanceRatingResponse();

        res.setId(entity.getId());
        res.setSalaryGradeId(entity.getSalaryGrade().getId());
        res.setGradeLevel(entity.getSalaryGrade().getGradeLevel());

        res.setRatingAText(entity.getRatingAText());
        res.setRatingBText(entity.getRatingBText());
        res.setRatingCText(entity.getRatingCText());
        res.setRatingDText(entity.getRatingDText());

        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        return res;
    }
}
