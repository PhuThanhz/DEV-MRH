package vn.system.app.modules.processaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.processaction.domain.ProcessAction;

@Repository
public interface ProcessActionRepository
        extends JpaRepository<ProcessAction, Long>,
        JpaSpecificationExecutor<ProcessAction> {

    boolean existsByCode(String code);

    ProcessAction findByCode(String code);
}
