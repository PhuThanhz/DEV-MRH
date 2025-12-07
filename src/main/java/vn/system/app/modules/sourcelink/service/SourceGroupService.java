package vn.system.app.modules.sourcelink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.domain.request.ReqCreateGroupDTO;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupDTO;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupSummaryDTO;
import vn.system.app.modules.sourcelink.repository.SourceGroupRepository;
import vn.system.app.modules.sourcelink.repository.SourceLinkRepository;

@Service
public class SourceGroupService {

    private final SourceGroupRepository groupRepo;
    private final SourceLinkRepository linkRepo;

    public SourceGroupService(SourceGroupRepository groupRepo, SourceLinkRepository linkRepo) {
        this.groupRepo = groupRepo;
        this.linkRepo = linkRepo;
    }

    // ============================================================
    // 1 CREATE GROUP (Không có link ban đầu)
    // ============================================================
    public SourceGroup handleCreate(ReqCreateGroupDTO req) {
        SourceGroup group = new SourceGroup();
        group.setName(req.getGroupName().trim());
        return groupRepo.save(group);
    }

    // ============================================================
    // 2 GET ALL (Pagination)
    // ============================================================
    public ResultPaginationDTO handleGetAll(Pageable pageable) {
        Page<SourceGroup> page = groupRepo.findAll(pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);

        // Chỉ map sang SummaryDTO (không bao gồm links)
        rs.setResult(page.getContent().stream().map(this::convertToSummaryDTO).toList());
        return rs;
    }

    // ============================================================
    // 3 FIND BY ID
    // ============================================================
    public SourceGroup findById(Long id) {
        return groupRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy group ID = " + id));
    }

    // ============================================================
    // 4 UPDATE NAME
    // ============================================================
    public SourceGroup handleUpdateName(Long id, String newName) {
        SourceGroup group = findById(id);
        group.setName(newName.trim());
        return groupRepo.save(group);
    }

    // ============================================================
    // 5 DELETE GROUP
    // ============================================================
    public void handleDelete(Long id) {
        SourceGroup group = findById(id);
        linkRepo.deleteAll(group.getLinks());
        groupRepo.delete(group);
    }

    // ============================================================
    // 6 ADD LINK TO GROUP
    // ============================================================
    public SourceGroup handleAddLink(Long groupId, String url) {
        SourceGroup group = findById(groupId);
        SourceLink link = new SourceLink();
        link.setUrl(url.trim());
        link.setGroup(group);
        linkRepo.save(link);
        return group;
    }

    // ============================================================
    // 7 DELETE LINK
    // ============================================================
    public SourceGroup handleDeleteLink(Long groupId, Long linkId) {
        SourceGroup group = findById(groupId);
        SourceLink link = linkRepo.findById(linkId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy link ID = " + linkId));

        if (!link.getGroup().getId().equals(groupId)) {
            throw new IdInvalidException("Link không thuộc group ID = " + groupId);
        }

        linkRepo.delete(link);
        return group;
    }

    // ============================================================
    // 8 CONVERT ENTITY -> DTO
    // ============================================================
    public ResSourceGroupDTO convertToResDTO(SourceGroup group) {
        ResSourceGroupDTO dto = new ResSourceGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        dto.setTotalLinks(group.getLinks() != null ? group.getLinks().size() : 0);

        return dto;
    }

    public ResSourceGroupSummaryDTO convertToSummaryDTO(SourceGroup group) {
        ResSourceGroupSummaryDTO dto = new ResSourceGroupSummaryDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        dto.setTotalLinks(group.getLinks() != null ? group.getLinks().size() : 0);
        return dto;
    }

}
