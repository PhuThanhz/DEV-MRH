package vn.system.app.modules.sourcelink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.DuplicateException;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceGroupMain;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupMainDTO;
import vn.system.app.modules.sourcelink.repository.SourceGroupMainRepository;
import vn.system.app.modules.sourcelink.repository.SourceGroupRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SourceGroupMainService {

    private final SourceGroupMainRepository mainRepo;
    private final SourceGroupRepository groupRepo;

    public SourceGroupMainService(SourceGroupMainRepository mainRepo, SourceGroupRepository groupRepo) {
        this.mainRepo = mainRepo;
        this.groupRepo = groupRepo;
    }

    // ============================================================
    // CREATE
    // ============================================================
    public SourceGroupMain handleCreate(SourceGroupMain req) {
        if (mainRepo.findByName(req.getName()).isPresent()) {
            throw new DuplicateException("Tên nhóm chính đã tồn tại: " + req.getName());
        }
        return mainRepo.save(req);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public SourceGroupMain handleUpdate(SourceGroupMain req) {
        Optional<SourceGroupMain> dbOpt = mainRepo.findById(req.getId());
        if (dbOpt.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy nhóm chính với ID = " + req.getId());
        }

        Optional<SourceGroupMain> dup = mainRepo.findByName(req.getName());
        if (dup.isPresent() && !dup.get().getId().equals(req.getId())) {
            throw new DuplicateException("Tên nhóm chính đã tồn tại: " + req.getName());
        }

        SourceGroupMain db = dbOpt.get();
        db.setName(req.getName().trim());
        return mainRepo.save(db);
    }

    // ============================================================
    // DELETE
    // ============================================================
    public void handleDelete(Long id) {
        if (!mainRepo.existsById(id)) {
            throw new IdInvalidException("Không tìm thấy nhóm chính với ID = " + id);
        }
        mainRepo.deleteById(id);
    }

    // ============================================================
    // GET ALL (với tổng số nhóm con)
    // ============================================================
    public ResultPaginationDTO handleGetAll(Specification<SourceGroupMain> spec, Pageable pageable) {
        Page<SourceGroupMain> page = mainRepo.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        List<ResSourceGroupMainDTO> list = page.getContent().stream().map(main -> {
            ResSourceGroupMainDTO dto = new ResSourceGroupMainDTO();
            dto.setId(main.getId());
            dto.setName(main.getName());
            dto.setCreatedAt(main.getCreatedAt());
            dto.setUpdatedAt(main.getUpdatedAt());
            dto.setTotalGroups(main.getGroups() != null ? main.getGroups().size() : 0);
            return dto;
        }).toList();

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(list);
        return result;
    }

    // ============================================================
    // TẠO NHÓM CON TRONG NHÓM CHÍNH
    // ============================================================
    public SourceGroup handleCreateGroupInMain(Long mainId, String groupName) {
        SourceGroupMain main = mainRepo.findById(mainId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy nhóm chính với ID = " + mainId));

        SourceGroup group = new SourceGroup();
        group.setName(groupName.trim());
        group.setMainGroup(main);

        return groupRepo.save(group);
    }

    // ============================================================
    // LẤY DANH SÁCH NHÓM CON
    // ============================================================
    public List<SourceGroup> handleGetGroupsByMainId(Long mainId) {
        SourceGroupMain main = mainRepo.findById(mainId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy nhóm chính với ID = " + mainId));
        return main.getGroups();
    }
}
