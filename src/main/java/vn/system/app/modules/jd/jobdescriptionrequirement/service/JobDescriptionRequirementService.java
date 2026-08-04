package vn.system.app.modules.jd.jobdescriptionrequirement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirement;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirementItem;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.request.ReqRequirementDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.request.ReqRequirementItemDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.response.ResRequirementDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.response.ResRequirementItemDTO;
import vn.system.app.modules.jd.jobdescriptionrequirement.repository.JobDescriptionRequirementItemRepository;
import vn.system.app.modules.jd.jobdescriptionrequirement.repository.JobDescriptionRequirementRepository;

@Service
@RequiredArgsConstructor
public class JobDescriptionRequirementService {

    private final JobDescriptionRequirementRepository repository;
    private final JobDescriptionRequirementItemRepository itemRepository;

    /*
     * CREATE FROM JD POST
     */
    @Transactional
    public void createFromDTO(JobDescription jd, ReqRequirementDTO req) {

        if (req == null)
            return;

        JobDescriptionRequirement entity = new JobDescriptionRequirement();

        entity.setJobDescription(jd);

        entity = repository.save(entity);

        saveItems(entity, req.getItems());
    }

    /*
     * GET BY JD
     */
    public ResRequirementDTO getByJobDescription(Long jdId) {

        JobDescriptionRequirement entity = repository
                .findByJobDescription_Id(jdId)
                .orElse(null);

        return convertToDTO(entity);
    }

    public ResRequirementDTO convertToDTO(JobDescriptionRequirement entity) {
        if (entity == null)
            return null;

        ResRequirementDTO res = new ResRequirementDTO();

        List<JobDescriptionRequirementItem> items = itemRepository
                .findByJobDescriptionRequirement_IdOrderByCategoryAscOrderNoAsc(entity.getId());

        res.setItems(items.stream().map(this::convertItemToDTO).collect(Collectors.toList()));

        return res;
    }

    private ResRequirementItemDTO convertItemToDTO(JobDescriptionRequirementItem item) {
        ResRequirementItemDTO dto = new ResRequirementItemDTO();
        dto.setId(item.getId());
        dto.setCategory(item.getCategory());
        dto.setOrderNo(item.getOrderNo());
        dto.setContent(item.getContent());
        return dto;
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

        itemRepository.deleteByJobDescriptionRequirement_Id(current.getId());
        saveItems(current, req.getItems());

        return current;
    }

    private void saveItems(JobDescriptionRequirement requirement, List<ReqRequirementItemDTO> items) {

        if (items == null || items.isEmpty())
            return;

        List<JobDescriptionRequirementItem> entities = items.stream().map(itemReq -> {
            JobDescriptionRequirementItem item = new JobDescriptionRequirementItem();
            item.setJobDescriptionRequirement(requirement);
            item.setCategory(itemReq.getCategory());
            item.setOrderNo(itemReq.getOrderNo());
            item.setContent(itemReq.getContent());
            return item;
        }).collect(Collectors.toList());

        itemRepository.saveAll(entities);
    }

    /*
     * DELETE
     */
    @Transactional
    public void delete(Long id) {

        itemRepository.deleteByJobDescriptionRequirement_Id(id);
        repository.deleteById(id);
    }
}
