package vn.system.app.modules.processaction.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.processaction.domain.ProcessAction;
import vn.system.app.modules.processaction.domain.response.ResProcessActionDTO;
import vn.system.app.modules.processaction.repository.ProcessActionRepository;

@Service
public class ProcessActionService {

    private final ProcessActionRepository repo;

    public ProcessActionService(ProcessActionRepository repo) {
        this.repo = repo;
    }

    // ================= CREATE =================
    public ProcessAction handleCreate(ProcessAction action) {
        if (repo.existsByCode(action.getCode())) {
            throw new IdInvalidException("ProcessAction code đã tồn tại");
        }
        return repo.save(action);
    }

    // ================= UPDATE =================
    public ProcessAction handleUpdate(ProcessAction req) {
        ProcessAction current = repo.findById(req.getId()).orElse(null);
        if (current == null)
            return null;

        current.setName(req.getName());
        current.setShortDescription(req.getShortDescription());
        current.setDescription(req.getDescription());
        current.setActive(req.isActive());

        return repo.save(current);
    }

    // ================= DELETE =================
    public void handleDelete(long id) {
        repo.deleteById(id);
    }

    // ================= FETCH BY ID (DÙNG RIÊNG) =================
    public ProcessAction fetchById(long id) {
        return repo.findById(id).orElse(null);
    }

    // ================= FETCH ENTITY (DÙNG CHO MODULE KHÁC) =================
    public ProcessAction fetchEntityById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "ProcessAction với id = " + id + " không tồn tại"));
    }

    // ================= FETCH ALL =================
    public ResultPaginationDTO fetchAll(
            Specification<ProcessAction> spec,
            Pageable pageable) {

        Page<ProcessAction> page = repo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);

        List<ResProcessActionDTO> list = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    // ================= MAPPER =================
    private ResProcessActionDTO convertToDTO(ProcessAction action) {
        ResProcessActionDTO res = new ResProcessActionDTO();
        res.setId(action.getId());
        res.setCode(action.getCode());
        res.setName(action.getName());
        res.setShortDescription(action.getShortDescription());
        res.setDescription(action.getDescription());
        res.setActive(action.isActive());
        return res;
    }
}
