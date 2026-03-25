package vn.system.app.modules.jd.jobdescriptionposition.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

import vn.system.app.modules.jd.jobdescriptionposition.domain.JobDescriptionPosition;
import vn.system.app.modules.jd.jobdescriptionposition.domain.request.ReqPositionDTO;
import vn.system.app.modules.jd.jobdescriptionposition.domain.response.ResPositionDTO;
import vn.system.app.modules.jd.jobdescriptionposition.repository.JobDescriptionPositionRepository;

import vn.system.app.modules.jobpositionchart.service.JobPositionChartService;
import vn.system.app.modules.jobpositionnode.service.JobPositionNodeService;
import vn.system.app.modules.jobpositionnode.domain.JobPositionNode;

@Service
@RequiredArgsConstructor
public class JobDescriptionPositionService {

    private final JobDescriptionPositionRepository repository;

    private final JobPositionChartService chartService;
    private final JobPositionNodeService nodeService;

    /*
     * CREATE FROM JD
     */
    @Transactional
    public void createFromDTO(JobDescription jd, List<ReqPositionDTO> positions) {

        if (positions == null || positions.isEmpty())
            return;

        for (ReqPositionDTO req : positions) {

            JobDescriptionPosition entity = new JobDescriptionPosition();

            entity.setJobDescription(jd);

            if (req.getChartId() != null) {
                entity.setChart(chartService.fetchChartById(req.getChartId()));
            }

            if (req.getNodeId() != null) {

                JobPositionNode node = nodeService.fetchNodeById(req.getNodeId());

                entity.setNode(node);

                /*
                 * CACHE NODE DATA
                 */
                entity.setNodeName(node.getName());
                entity.setLevelCode(node.getLevel());
            }

            repository.save(entity);
        }
    }

    /*
     * GET BY JD
     */
    public List<ResPositionDTO> getByJobDescription(Long jdId) {

        List<JobDescriptionPosition> positions = repository.findByJobDescription_Id(jdId);

        return positions.stream().map(p -> {

            ResPositionDTO res = new ResPositionDTO();

            if (p.getChart() != null)
                res.setChartId(p.getChart().getId());

            if (p.getNode() != null)
                res.setNodeId(p.getNode().getId());

            res.setNodeName(p.getNodeName());
            res.setLevelCode(p.getLevelCode());

            return res;

        }).collect(Collectors.toList());
    }

    /*
     * DELETE
     */
    @Transactional
    public void delete(Long id) {

        repository.deleteById(id);
    }

    /*
     * FETCH ENTITY
     */
    public JobDescriptionPosition fetchEntity(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "JobDescriptionPosition không tồn tại id = " + id));
    }
}