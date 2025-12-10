package vn.system.app.modules.sourcelink.service;

import org.springframework.stereotype.Service;
import vn.system.app.common.util.error.DuplicateException;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupDTO;
import vn.system.app.modules.sourcelink.repository.SourceGroupRepository;
import vn.system.app.modules.sourcelink.repository.SourceLinkRepository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@Service
public class SourceGroupService {

    private final SourceGroupRepository groupRepo;
    private final SourceLinkRepository linkRepo;

    public SourceGroupService(SourceGroupRepository groupRepo, SourceLinkRepository linkRepo) {
        this.groupRepo = groupRepo;
        this.linkRepo = linkRepo;
    }

    // ============================================================
    // 1. TÌM NHÓM THEO ID
    // ============================================================
    public SourceGroup findById(Long id) {
        return groupRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy group ID = " + id));
    }

    // ============================================================
    // 2. CẬP NHẬT NHÓM (CÁCH LÀM GIỐNG NHÓM CHÍNH)
    // ============================================================
    public SourceGroup handleUpdate(SourceGroup req) {
        Optional<SourceGroup> dbOpt = groupRepo.findById(req.getId());
        if (dbOpt.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy nhóm với ID = " + req.getId());
        }

        Optional<SourceGroup> dup = groupRepo.findByName(req.getName());
        if (dup.isPresent() && !dup.get().getId().equals(req.getId())) {
            throw new DuplicateException("Tên nhóm đã tồn tại: " + req.getName());
        }

        SourceGroup db = dbOpt.get();
        db.setName(req.getName().trim());
        return groupRepo.save(db);
    }

    // ============================================================
    // 3. XÓA NHÓM (VÀ TOÀN BỘ LINK BÊN TRONG)
    // ============================================================
    public void handleDelete(Long id) {
        SourceGroup group = findById(id);
        if (group.getLinks() != null && !group.getLinks().isEmpty()) {
            linkRepo.deleteAll(group.getLinks());
        }
        groupRepo.delete(group);
    }

    // ============================================================
    // 4. THÊM NHIỀU LINK MỚI VÀO NHÓM (HỖ TRỢ DÁN NHIỀU DÒNG)
    // ============================================================
    public SourceGroup handleAddLink(Long groupId, String rawUrls) {
        SourceGroup group = findById(groupId);

        // Loại bỏ khoảng trắng đầu-cuối và tách theo dòng hoặc khoảng trắng
        String[] urlLines = rawUrls.split("\\r?\\n|\\s+");

        List<SourceLink> validLinks = new ArrayList<>();

        for (String url : urlLines) {
            String cleaned = url.trim();
            if (!cleaned.isEmpty() && cleaned.startsWith("http")) {
                SourceLink link = new SourceLink();
                link.setUrl(cleaned);
                link.setGroup(group);
                validLinks.add(link);
            }
        }

        if (validLinks.isEmpty()) {
            throw new IdInvalidException("Không có link hợp lệ nào để thêm vào nhóm.");
        }

        // Lưu toàn bộ link hợp lệ
        linkRepo.saveAll(validLinks);

        return group;
    }

    // ============================================================
    // 5. XÓA 1 LINK KHỎI NHÓM
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
    // 6. CHUYỂN ENTITY -> DTO
    // ============================================================
    public ResSourceGroupDTO convertToDTO(SourceGroup group) {
        ResSourceGroupDTO dto = new ResSourceGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        dto.setTotalLinks(group.getLinks() != null ? group.getLinks().size() : 0);

        if (group.getMainGroup() != null) {
            dto.setMainGroupId(group.getMainGroup().getId());
            dto.setMainGroupName(group.getMainGroup().getName());
        }

        return dto;
    }
}
