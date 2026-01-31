package vn.system.app.modules.positionlevel.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.positionlevel.domain.PositionLevel;
import vn.system.app.modules.positionlevel.domain.request.ReqCreatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.request.ReqUpdatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.response.ResCreatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.response.ResPositionLevelDTO;
import vn.system.app.modules.positionlevel.repository.PositionLevelRepository;

@Service
public class PositionLevelService {

    private final PositionLevelRepository repo;

    public PositionLevelService(PositionLevelRepository repo) {
        this.repo = repo;
    }

    // ================================
    // Helper Methods
    // ================================
    private String extractBand(String code) {
        return code.replaceAll("[0-9]", "");
    }

    private Integer extractLevel(String code) {
        return Integer.parseInt(code.replaceAll("[^0-9]", ""));
    }

    private Integer findBandOrder(String band) {
        return repo.findAll().stream()
                .filter(x -> extractBand(x.getCode()).equals(band))
                .map(PositionLevel::getBandOrder)
                .findFirst()
                .orElse(null);
    }

    // ================================
    // So sánh cấp bậc
    // return < 0 : p1 cao hơn p2
    // return > 0 : p1 thấp hơn p2
    // return = 0 : bằng nhau
    // ================================
    public int compareLevels(PositionLevel p1, PositionLevel p2) {

        // 1) So sánh theo bandOrder
        int bandCompare = p1.getBandOrder().compareTo(p2.getBandOrder());
        if (bandCompare != 0) {
            return bandCompare; // bandOrder nhỏ hơn = cấp cao hơn
        }

        // 2) Nếu band giống nhau → so sánh levelNumber
        int level1 = extractLevel(p1.getCode());
        int level2 = extractLevel(p2.getCode());

        return Integer.compare(level1, level2);
    }

    // ================================
    // Exists
    // ================================
    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    // ================================
    // DELETE
    // ================================
    public void handleDelete(Long id) {
        PositionLevel pl = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tồn tại ID để xoá."));
        repo.delete(pl);
    }

    // ================================
    // CREATE
    // ================================
    public ResCreatePositionLevelDTO handleCreate(ReqCreatePositionLevelDTO req) {

        if (repo.existsByCode(req.getCode())) {
            throw new IdInvalidException("Code đã tồn tại.");
        }

        String band = extractBand(req.getCode());
        Integer level = extractLevel(req.getCode());

        boolean bandExists = repo.findAll().stream()
                .anyMatch(x -> extractBand(x.getCode()).equals(band));

        PositionLevel pl = new PositionLevel();
        pl.setCode(req.getCode());

        if (!bandExists) {

            if (level != 1) {
                throw new IdInvalidException("Band mới phải bắt đầu từ cấp 1 (S1, M1...).");
            }

            if (req.getBandOrder() == null) {
                throw new IdInvalidException("BandOrder bắt buộc cho cấp đầu tiên.");
            }

            if (repo.existsByBandOrder(req.getBandOrder())) {
                throw new IdInvalidException("BandOrder đã tồn tại.");
            }

            pl.setBandOrder(req.getBandOrder());
        } else {
            Integer existingOrder = findBandOrder(band);
            pl.setBandOrder(existingOrder);
        }

        pl = repo.save(pl);

        return convertToResCreateDTO(pl);
    }

    // ================================
    // UPDATE
    // ================================
    public ResPositionLevelDTO handleUpdate(ReqUpdatePositionLevelDTO req) {

        PositionLevel current = repo.findById(req.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID."));

        if (req.getCode() != null) {

            if (repo.existsByCode(req.getCode()) && !req.getCode().equals(current.getCode())) {
                throw new IdInvalidException("Code đã tồn tại.");
            }

            String newBand = extractBand(req.getCode());
            String oldBand = extractBand(current.getCode());

            if (!newBand.equals(oldBand)) {
                throw new IdInvalidException("Không được đổi band.");
            }

            current.setCode(req.getCode());
        }

        if (req.getBandOrder() != null) {
            String band = extractBand(current.getCode());
            Integer existingOrder = findBandOrder(band);

            if (existingOrder != null && !existingOrder.equals(req.getBandOrder())) {
                throw new IdInvalidException("Không được đổi bandOrder của band đã tồn tại.");
            }

            current.setBandOrder(req.getBandOrder());
        }

        current = repo.save(current);
        return convertToResDTO(current);
    }

    // ================================
    // FETCH ONE
    // ================================
    public ResPositionLevelDTO fetchById(Long id) {
        PositionLevel pl = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tồn tại."));
        return convertToResDTO(pl);
    }

    // ================================
    // FETCH ALL
    // ================================
    public ResultPaginationDTO fetchAll(Specification<PositionLevel> spec, Pageable pageable) {

        Page<PositionLevel> pagePL = repo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePL.getTotalPages());
        mt.setTotal(pagePL.getTotalElements());
        rs.setMeta(mt);

        List<ResPositionLevelDTO> list = pagePL.getContent()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());

        rs.setResult(list);
        return rs;
    }

    // ================================
    // CONVERT METHODS
    // ================================
    private ResCreatePositionLevelDTO convertToResCreateDTO(PositionLevel pl) {
        ResCreatePositionLevelDTO res = new ResCreatePositionLevelDTO();

        res.setId(pl.getId());
        res.setCode(pl.getCode());
        res.setBand(extractBand(pl.getCode()));
        res.setLevelNumber(extractLevel(pl.getCode()));
        res.setBandOrder(pl.getBandOrder());
        res.setCreatedAt(pl.getCreatedAt());
        res.setCreatedBy(pl.getCreatedBy());

        return res;
    }

    private ResPositionLevelDTO convertToResDTO(PositionLevel pl) {
        ResPositionLevelDTO res = new ResPositionLevelDTO();

        res.setId(pl.getId());
        res.setCode(pl.getCode());
        res.setBand(extractBand(pl.getCode()));
        res.setLevelNumber(extractLevel(pl.getCode()));
        res.setBandOrder(pl.getBandOrder());
        res.setCreatedAt(pl.getCreatedAt());
        res.setUpdatedAt(pl.getUpdatedAt());
        res.setCreatedBy(pl.getCreatedBy());
        res.setUpdatedBy(pl.getUpdatedBy());

        return res;
    }
}
