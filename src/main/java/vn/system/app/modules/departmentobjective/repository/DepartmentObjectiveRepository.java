package vn.system.app.modules.departmentobjective.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.departmentobjective.domain.DepartmentObjective;

import java.util.Collection;
import java.util.List;

@Repository
public interface DepartmentObjectiveRepository
        extends JpaRepository<DepartmentObjective, Long>, JpaSpecificationExecutor<DepartmentObjective> {

    void deleteByDepartmentId(Long departmentId);

    List<DepartmentObjective> findByDepartmentId(Long departmentId);

    /**
     * Load ALL departments (admin level - no company scope restriction).
     * Uses Department as the driving table so departments with 0 objectives are included.
     */
    @org.springframework.data.jpa.repository.Query("SELECT new vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionSummaryDTO(" +
            "d.id, d.name, c.id, c.name, " +
            "SUM(CASE WHEN o.type = 'OBJECTIVE' THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN o.type = 'TASK' THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN o.type = 'AUTHORITY' THEN 1L ELSE 0L END), " +
            "COALESCE(m.issueDate, MAX(o.issueDate)), COALESCE(m.lastUpdatedAt, MAX(o.updatedAt)), " +
            "m.status, m.version, m.issuedAt) " +
            "FROM Department d " +
            "LEFT JOIN d.company c " +
            "LEFT JOIN DepartmentObjective o ON o.department.id = d.id " +
            "LEFT JOIN DepartmentMission m ON m.department.id = d.id " +
            "GROUP BY d.id, d.name, c.id, c.name, m.issueDate, m.lastUpdatedAt, m.status, m.version, m.issuedAt " +
            "ORDER BY c.name ASC, d.name ASC")
    List<vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionSummaryDTO> getSummaryAll();

    /**
     * Load departments scoped to specific company IDs.
     * Uses Department as the driving table so departments with 0 objectives are included.
     */
    @org.springframework.data.jpa.repository.Query("SELECT new vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionSummaryDTO(" +
            "d.id, d.name, c.id, c.name, " +
            "SUM(CASE WHEN o.type = 'OBJECTIVE' THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN o.type = 'TASK' THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN o.type = 'AUTHORITY' THEN 1L ELSE 0L END), " +
            "COALESCE(m.issueDate, MAX(o.issueDate)), COALESCE(m.lastUpdatedAt, MAX(o.updatedAt)), " +
            "m.status, m.version, m.issuedAt) " +
            "FROM Department d " +
            "LEFT JOIN d.company c " +
            "LEFT JOIN DepartmentObjective o ON o.department.id = d.id " +
            "LEFT JOIN DepartmentMission m ON m.department.id = d.id " +
            "WHERE c.id IN :companyIds " +
            "GROUP BY d.id, d.name, c.id, c.name, m.issueDate, m.lastUpdatedAt, m.status, m.version, m.issuedAt " +
            "ORDER BY c.name ASC, d.name ASC")
    List<vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionSummaryDTO> getSummaryByCompanyIds(
            @org.springframework.data.repository.query.Param("companyIds") Collection<Long> companyIds);
}
