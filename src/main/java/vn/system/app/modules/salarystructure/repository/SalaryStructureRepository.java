package vn.system.app.modules.salarystructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.system.app.modules.salarystructure.domain.OwnerLevel;
import vn.system.app.modules.salarystructure.domain.SalaryStructure;

public interface SalaryStructureRepository
        extends JpaRepository<SalaryStructure, Long>,
        JpaSpecificationExecutor<SalaryStructure> {

    Optional<SalaryStructure> findByOwnerLevelAndOwnerJobTitleIdAndSalaryGradeId(
            OwnerLevel ownerLevel,
            Long ownerJobTitleId,
            Long salaryGradeId);
}
