package vn.system.app.modules.salarygradeincome.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.salarygrade.domain.SalaryGrade;
import vn.system.app.modules.salarygrade.repository.SalaryGradeRepository;
import vn.system.app.modules.salarygradeincome.domain.PayType;
import vn.system.app.modules.salarygradeincome.domain.SalaryGradeIncome;
import vn.system.app.modules.salarygradeincome.domain.request.SalaryGradeIncomeRequest;
import vn.system.app.modules.salarygradeincome.domain.response.SalaryGradeIncomeResponse;
import vn.system.app.modules.salarygradeincome.repository.SalaryGradeIncomeRepository;

@Service
public class SalaryGradeIncomeService {

    private final SalaryGradeIncomeRepository repository;
    private final SalaryGradeRepository salaryGradeRepository;

    public SalaryGradeIncomeService(
            SalaryGradeIncomeRepository repository,
            SalaryGradeRepository salaryGradeRepository) {
        this.repository = repository;
        this.salaryGradeRepository = salaryGradeRepository;
    }

    /*
     * ===============================
     * CREATE
     * ===============================
     */
    public SalaryGradeIncome handleCreate(SalaryGradeIncomeRequest request) {

        if (repository.existsBySalaryGrade_IdAndPayType(
                request.getSalaryGradeId(), request.getPayType())) {
            throw new IdInvalidException(
                    "Bậc lương này đã có khung thu nhập cho hình thức "
                            + request.getPayType());
        }

        SalaryGrade salaryGrade = salaryGradeRepository
                .findById(request.getSalaryGradeId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy bậc lương"));

        SalaryGradeIncome entity = new SalaryGradeIncome();
        entity.setSalaryGrade(salaryGrade);
        entity.setPayType(request.getPayType());

        entity.setBaseSalary(request.getBaseSalary());
        entity.setPositionAllowance(request.getPositionAllowance());
        entity.setMealAllowance(request.getMealAllowance());
        entity.setFuelAllowance(request.getFuelAllowance());
        entity.setPhoneAllowance(request.getPhoneAllowance());
        entity.setOtherAllowance(request.getOtherAllowance());

        entity.setBonusA(request.getBonusA());
        entity.setBonusB(request.getBonusB());
        entity.setBonusC(request.getBonusC());
        entity.setBonusD(request.getBonusD());

        entity.setStatus(request.getStatus());

        return repository.save(entity);
    }

    /*
     * ===============================
     * FETCH BY ID
     * ===============================
     */
    public SalaryGradeIncome fetchById(Long id) {
        Optional<SalaryGradeIncome> optional = repository.findById(id);
        return optional.orElse(null);
    }

    /*
     * ===============================
     * FETCH BY SALARY GRADE + PAY TYPE
     * ===============================
     */
    public SalaryGradeIncome fetchBySalaryGradeAndPayType(
            Long salaryGradeId, PayType payType) {

        Optional<SalaryGradeIncome> optional = repository.findBySalaryGrade_IdAndPayType(salaryGradeId, payType);

        return optional.orElse(null);
    }

    /*
     * ===============================
     * UPDATE
     * ===============================
     */
    public SalaryGradeIncome handleUpdate(
            Long id,
            SalaryGradeIncomeRequest request) {

        SalaryGradeIncome current = this.fetchById(id);

        if (current != null) {
            current.setBaseSalary(request.getBaseSalary());
            current.setPositionAllowance(request.getPositionAllowance());
            current.setMealAllowance(request.getMealAllowance());
            current.setFuelAllowance(request.getFuelAllowance());
            current.setPhoneAllowance(request.getPhoneAllowance());
            current.setOtherAllowance(request.getOtherAllowance());

            current.setBonusA(request.getBonusA());
            current.setBonusB(request.getBonusB());
            current.setBonusC(request.getBonusC());
            current.setBonusD(request.getBonusD());

            current.setStatus(request.getStatus());

            current = repository.save(current);
        }

        return current;
    }

    /*
     * ===============================
     * DELETE
     * ===============================
     */
    public void handleDelete(Long id) {
        repository.deleteById(id);
    }

    /*
     * ===============================
     * CONVERT RESPONSE
     * ===============================
     */
    public SalaryGradeIncomeResponse convertToResponse(
            SalaryGradeIncome entity) {

        SalaryGradeIncomeResponse res = new SalaryGradeIncomeResponse();

        res.setId(entity.getId());
        res.setSalaryGradeId(entity.getSalaryGrade().getId());
        res.setGradeLevel(entity.getSalaryGrade().getGradeLevel());
        res.setPayType(entity.getPayType());

        res.setBaseSalary(entity.getBaseSalary());
        res.setPositionAllowance(entity.getPositionAllowance());
        res.setMealAllowance(entity.getMealAllowance());
        res.setFuelAllowance(entity.getFuelAllowance());
        res.setPhoneAllowance(entity.getPhoneAllowance());
        res.setOtherAllowance(entity.getOtherAllowance());

        res.setBonusA(entity.getBonusA());
        res.setBonusB(entity.getBonusB());
        res.setBonusC(entity.getBonusC());
        res.setBonusD(entity.getBonusD());

        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setCreatedBy(entity.getCreatedBy());
        res.setUpdatedBy(entity.getUpdatedBy());

        return res;
    }
}
