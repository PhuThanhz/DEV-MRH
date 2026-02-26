package vn.system.app.modules.position.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.position.domain.Position;

@Repository
public interface PositionRepository
        extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

}