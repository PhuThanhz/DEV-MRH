package vn.system.app.modules.jd.requirement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.requirement.domain.JDRequirement;
import vn.system.app.modules.jd.requirement.domain.request.ReqSaveJDRequirementDTO;
import vn.system.app.modules.jd.requirement.domain.response.ResJDRequirementDTO;
import vn.system.app.modules.jd.requirement.repository.JDRequirementRepository;

@Service
public class JDRequirementService {

    private final JDRequirementRepository repository;

    public JDRequirementService(JDRequirementRepository repository) {
        this.repository = repository;
    }

    /*
     * ==========================================
     * SAVE / UPDATE REQUIREMENT
     * ==========================================
     */
    @Transactional
    public ResJDRequirementDTO save(ReqSaveJDRequirementDTO req) {

        JDRequirement entity = repository
                .findByJobDescriptionIdAndActiveTrue(req.getJobDescriptionId())
                .orElse(new JDRequirement());

        entity.setJobDescriptionId(req.getJobDescriptionId());

        entity.setKnowledge(req.getKnowledge());
        entity.setExperience(req.getExperience());
        entity.setSkills(req.getSkills());
        entity.setQualities(req.getQualities());
        entity.setOtherRequirements(req.getOtherRequirements());

        entity.setActive(true);

        entity = repository.save(entity);

        return convert(entity);
    }

    /*
     * ==========================================
     * FETCH REQUIREMENT BY JD
     * ==========================================
     */
    @Transactional(readOnly = true)
    public ResJDRequirementDTO fetchByJD(Long jdId) {

        JDRequirement entity = repository
                .findByJobDescriptionIdAndActiveTrue(jdId)
                .orElseThrow(() -> new IdInvalidException("JD chưa có yêu cầu vị trí."));

        return convert(entity);
    }

    /*
     * ==========================================
     * CONVERT
     * ==========================================
     */
    private ResJDRequirementDTO convert(JDRequirement entity) {

        ResJDRequirementDTO dto = new ResJDRequirementDTO();

        dto.setId(entity.getId());
        dto.setJobDescriptionId(entity.getJobDescriptionId());

        dto.setKnowledge(entity.getKnowledge());
        dto.setExperience(entity.getExperience());
        dto.setSkills(entity.getSkills());
        dto.setQualities(entity.getQualities());
        dto.setOtherRequirements(entity.getOtherRequirements());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }
}