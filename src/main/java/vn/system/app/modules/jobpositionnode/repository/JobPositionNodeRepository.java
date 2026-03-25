package vn.system.app.modules.jobpositionnode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobpositionnode.domain.JobPositionNode;

@Repository
public interface JobPositionNodeRepository
        extends JpaRepository<JobPositionNode, Long>, JpaSpecificationExecutor<JobPositionNode> {

    List<JobPositionNode> findByChartId(Long chartId);

}