package vn.system.app.modules.sourcelink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.repository.SourceGroupRepository;
import vn.system.app.modules.sourcelink.repository.SourceLinkRepository;

import java.util.List;

@Slf4j
@Service
public class SourceGroupService {

    private final SourceGroupRepository groupRepo;
    private final SourceLinkRepository linkRepo;

    public SourceGroupService(SourceGroupRepository groupRepo, SourceLinkRepository linkRepo) {
        this.groupRepo = groupRepo;
        this.linkRepo = linkRepo;
    }

    // ============================================================
    // 1️⃣ Tạo group mới với 1 link khởi tạo
    // ============================================================
    public SourceGroup createGroup(String name, String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL không được để trống");
        }

        SourceGroup group = new SourceGroup();
        group.setName((name == null || name.isBlank()) ? "Unnamed Group" : name.trim());
        groupRepo.save(group);

        addLinkToGroup(group.getId(), url);

        log.info("Tạo group '{}' (ID={}) với link đầu tiên", group.getName(), group.getId());
        return group;
    }

    // ============================================================
    // 2️⃣ Thêm 1 link mới vào group
    // ============================================================
    public SourceLink addLinkToGroup(Long groupId, String url) {
        SourceGroup group = getGroup(groupId);

        SourceLink link = new SourceLink();
        link.setUrl(url.trim());
        link.setGroup(group);
        linkRepo.save(link);

        log.info("Thêm link vào group '{}': {}", group.getName(), url);
        return link;
    }

    // ============================================================
    // 3️⃣ Lấy tất cả group
    // ============================================================
    public List<SourceGroup> getAllGroups() {
        return groupRepo.findAll();
    }

    // ============================================================
    // 4️⃣ Lấy chi tiết group (kèm link)
    // ============================================================
    public SourceGroup getGroup(Long id) {
        return groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group ID=" + id));
    }

    // ============================================================
    // 5️⃣ Cập nhật tên group
    // ============================================================
    public SourceGroup rename(Long id, String newName) {
        SourceGroup group = getGroup(id);
        if (newName != null && !newName.isBlank()) {
            group.setName(newName.trim());
            groupRepo.save(group);
        }
        log.info("Đổi tên group ID={} thành '{}'", id, group.getName());
        return group;
    }

    // ============================================================
    // 6️⃣ Xóa group và toàn bộ link
    // ============================================================
    public void deleteGroup(Long id) {
        SourceGroup group = getGroup(id);
        linkRepo.deleteAll(group.getLinks());
        groupRepo.delete(group);
        log.info("Đã xóa group '{}' (ID={})", group.getName(), group.getId());
    }

    // ============================================================
    // 7️⃣ Xóa 1 link trong group
    // ============================================================
    public void deleteLink(Long groupId, Long linkId) {
        SourceGroup group = getGroup(groupId);
        SourceLink link = linkRepo.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy link ID=" + linkId));

        if (!link.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Link không thuộc group ID=" + groupId);
        }

        linkRepo.delete(link);
        log.info("Đã xóa link ID={} khỏi group '{}'", linkId, group.getName());
    }
}
