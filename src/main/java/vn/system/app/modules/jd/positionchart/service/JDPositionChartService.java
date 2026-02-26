package vn.system.app.modules.jd.positionchart.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.jd.positionchart.domain.JDPositionChart;
import vn.system.app.modules.jd.positionchart.domain.request.ReqCreateJDPositionChartDTO;
import vn.system.app.modules.jd.positionchart.domain.response.ResJDPositionChartGraphDTO;
import vn.system.app.modules.jd.positionchart.domain.response.ResJDPositionChartNodeDTO;
import vn.system.app.modules.jd.positionchart.repository.JDPositionChartRepository;

@Service
public class JDPositionChartService {

    private final JDPositionChartRepository repository;

    public JDPositionChartService(JDPositionChartRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ResJDPositionChartNodeDTO create(ReqCreateJDPositionChartDTO req) {

        JDPositionChart entity = new JDPositionChart();
        entity.setJobDescriptionId(req.getJobDescriptionId());
        entity.setNodeKey(req.getNodeKey());
        entity.setTitle(req.getTitle());
        entity.setParentId(req.getParentId());
        entity.setSortOrder(req.getSortOrder());
        entity.setActive(true);

        entity = repository.save(entity);

        return convert(entity);
    }

    @Transactional(readOnly = true)
    public ResJDPositionChartGraphDTO getGraph(Long jdId) {

        List<JDPositionChart> list = repository
                .findByJobDescriptionIdAndActiveTrueOrderBySortOrderAsc(jdId);

        ResJDPositionChartGraphDTO res = new ResJDPositionChartGraphDTO();
        res.setNodes(list.stream().map(this::convert).collect(Collectors.toList()));

        return res;
    }

    private ResJDPositionChartNodeDTO convert(JDPositionChart entity) {

        ResJDPositionChartNodeDTO dto = new ResJDPositionChartNodeDTO();

        dto.setId(entity.getId());
        dto.setNodeKey(entity.getNodeKey());
        dto.setTitle(entity.getTitle());
        dto.setParentId(entity.getParentId());
        dto.setSortOrder(entity.getSortOrder());

        return dto;
    }
}