package vn.system.app.modules.jd.duty.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.duty.domain.JDDuty;
import vn.system.app.modules.jd.duty.domain.request.ReqSaveJDDutyDTO;
import vn.system.app.modules.jd.duty.domain.response.ResJDDutyDTO;
import vn.system.app.modules.jd.duty.repository.JDDutyRepository;

@Service
public class JDDutyService {

    private final JDDutyRepository repository;

    public JDDutyService(JDDutyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ResJDDutyDTO save(ReqSaveJDDutyDTO req) {

        JDDuty duty = repository
                .findByJobDescriptionIdAndActiveTrue(req.getJobDescriptionId())
                .orElse(new JDDuty());

        duty.setJobDescriptionId(req.getJobDescriptionId());
        duty.setContent(req.getContent());
        duty.setActive(true);

        duty = repository.save(duty);

        return convert(duty);
    }

    @Transactional(readOnly = true)
    public ResJDDutyDTO fetchByJD(Long jdId) {

        JDDuty duty = repository
                .findByJobDescriptionIdAndActiveTrue(jdId)
                .orElseThrow(() -> new IdInvalidException("JD chưa có mô tả công việc."));

        return convert(duty);
    }

    private ResJDDutyDTO convert(JDDuty entity) {

        ResJDDutyDTO dto = new ResJDDutyDTO();
        dto.setId(entity.getId());
        dto.setJobDescriptionId(entity.getJobDescriptionId());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }
}