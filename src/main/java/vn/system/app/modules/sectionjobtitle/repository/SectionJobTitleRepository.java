package vn.system.app.modules.sectionjobtitle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;

@Repository
public interface SectionJobTitleRepository
        extends JpaRepository<SectionJobTitle, Long>,
        JpaSpecificationExecutor<SectionJobTitle> {

    boolean existsByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);
}
