package vn.system.app.modules.jd.jobdescriptionrequirement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirement;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.request.ReqRequirementDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.response.ResRequirementDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.repository.JobDescriptionRequirementRepository;

@Service
@RequiredArgsConstructor
public class JobDescriptionRequirementService {

    private final JobDescriptionRequirementRepository repository;

    /*
     * CREATE FROM JD POST
     */
    @Transactional
    public void createFromDTO(JobDescription jd, ReqRequirementDTO req) {

        if (req == null)
            return;

        JobDescriptionRequirement entity = new JobDescriptionRequirement();

        entity.setJobDescription(jd);

        entity.setKnowledge(req.getKnowledge());
        entity.setExperience(req.getExperience());
        entity.setSkills(req.getSkills());
        entity.setQualities(req.getQualities());
        entity.setOtherRequirements(req.getOtherRequirements());

        repository.save(entity);
    }

    /*
     * GET BY JD
     */
    public ResRequirementDTO getByJobDescription(Long jdId) {

        JobDescriptionRequirement entity = repository
                .findByJobDescription_Id(jdId)
                .orElse(null);

        if (entity == null)
            return null;

        ResRequirementDTO res = new ResRequirementDTO();

        res.setKnowledge(entity.getKnowledge());
        res.setExperience(entity.getExperience());
        res.setSkills(entity.getSkills());
        res.setQualities(entity.getQualities());
        res.setOtherRequirements(entity.getOtherRequirements());

        return res;
    }

    /*
     * UPDATE
     */
    @Transactional
    public JobDescriptionRequirement update(Long jdId, ReqRequirementDTO req) {

        JobDescriptionRequirement current = repository
                .findByJobDescription_Id(jdId)
                .orElseThrow(() -> new IdInvalidException(
                        "Requirement không tồn tại cho JD id = " + jdId));

        current.setKnowledge(req.getKnowledge());
        current.setExperience(req.getExperience());
        current.setSkills(req.getSkills());
        current.setQualities(req.getQualities());
        current.setOtherRequirements(req.getOtherRequirements());

        return repository.save(current);
    }

    /*
     * DELETE
     */
    @Transactional
    public void delete(Long id) {

        repository.deleteById(id);
    }
}