package vn.system.app.modules.positionlevel.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.positionlevel.domain.PositionLevel;
import vn.system.app.modules.positionlevel.domain.request.ReqCreatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.request.ReqUpdatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.response.ResCreatePositionLevelDTO;
import vn.system.app.modules.positionlevel.domain.response.ResPositionLevelDTO;
import vn.system.app.modules.positionlevel.repository.PositionLevelRepository;

@Service
public class PositionLevelService {

    private final PositionLevelRepository repo;
    private final CompanyRepository companyRepo; // ⭐ THÊM MỚI

    public PositionLevelService(PositionLevelRepository repo, CompanyRepository companyRepo) {
        this.repo = repo;
        this.companyRepo = companyRepo;
    }

    // ================= HELPERS ============================
    private String extractBand(String code) {
        return code.replaceAll("[0-9]", "");
    }

    private Integer extractLevel(String code) {
        return Integer.parseInt(code.replaceAll("[^0-9]", ""));
    }

    // ⭐ THAY — filter theo companyId thay vì findAll()
    private Integer findBandOrder(String band, Long companyId) {
        return repo.findByCompanyId(companyId).stream()
                .filter(x -> extractBand(x.getCode()).equals(band))
                .map(PositionLevel::getBandOrder)
                .findFirst()
                .orElse(null);
    }

    // ================= ACTIVE / INACTIVE ====================
    public void inactive(Long id) {
        PositionLevel pl = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tồn tại ID để vô hiệu hóa."));

        pl.setStatus(0);
        repo.save(pl);
    }

    public void active(Long id) {
        PositionLevel pl = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tồn tại ID để kích hoạt."));

        pl.setStatus(1);
        repo.save(pl);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    // ================= CREATE ==============================
    public ResCreatePositionLevelDTO handleCreate(ReqCreatePositionLevelDTO req) {

        // ⭐ THÊM — validate & lấy company
        Company company = companyRepo.findById(req.getCompanyId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy công ty."));

        // ⭐ THAY — check code trùng trong phạm vi công ty
        if (repo.existsByCodeAndCompanyId(req.getCode(), req.getCompanyId())) {
            throw new IdInvalidException("Code đã tồn tại trong công ty này.");
        }

        String band = extractBand(req.getCode());
        Integer level = extractLevel(req.getCode());

        // ⭐ THAY — check band tồn tại trong phạm vi công ty
        boolean bandExists = repo.findByCompanyId(req.getCompanyId())
                .stream()
                .anyMatch(x -> extractBand(x.getCode()).equals(band));

        PositionLevel pl = new PositionLevel();
        pl.setCode(req.getCode());
        pl.setStatus(1);
        pl.setCompany(company); // ⭐ THÊM — gán company

        if (!bandExists) {
            if (level != 1) {
                throw new IdInvalidException("Band mới phải bắt đầu bằng cấp 1 (S1, M1...).");
            }

            if (req.getBandOrder() == null) {
                throw new IdInvalidException("BandOrder bắt buộc cho cấp đầu tiên.");
            }

            // ⭐ THAY — check bandOrder trùng trong phạm vi công ty
            if (repo.existsByBandOrderAndCompanyId(req.getBandOrder(), req.getCompanyId())) {
                throw new IdInvalidException("BandOrder đã tồn tại trong công ty này.");
            }

            pl.setBandOrder(req.getBandOrder());
        } else {
            // ⭐ THAY — truyền companyId vào helper
            pl.setBandOrder(findBandOrder(band, req.getCompanyId()));
        }

        pl = repo.save(pl);
        return convertToResCreate(pl);
    }

    // ================= UPDATE =============================
    public ResPositionLevelDTO handleUpdate(ReqUpdatePositionLevelDTO req) {

        PositionLevel current = repo.findById(req.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ID."));

        // ⭐ Lấy companyId từ bản ghi hiện tại — không cho đổi công ty
        Long companyId = current.getCompany().getId();

        // update code
        if (req.getCode() != null) {

            // ⭐ THAY — check code trùng trong phạm vi công ty
            if (repo.existsByCodeAndCompanyId(req.getCode(), companyId)
                    && !req.getCode().equals(current.getCode())) {
                throw new IdInvalidException("Code đã tồn tại trong công ty này.");
            }

            String newBand = extractBand(req.getCode());
            String oldBand = extractBand(current.getCode());

            if (!newBand.equals(oldBand)) {
                throw new IdInvalidException("Không thể thay đổi band.");
            }

            current.setCode(req.getCode());
        }

        // update bandOrder
        if (req.getBandOrder() != null) {
            String band = extractBand(current.getCode());

            // ⭐ THAY — truyền companyId vào helper
            Integer existingOrder = findBandOrder(band, companyId);

            if (existingOrder != null && !existingOrder.equals(req.getBandOrder())) {
                throw new IdInvalidException("Không thể thay đổi bandOrder của band này.");
            }

            current.setBandOrder(req.getBandOrder());
        }

        // update status
        if (req.getStatus() != null) {
            current.setStatus(req.getStatus());
        }

        current = repo.save(current);
        return convertToRes(current);
    }

    // ================= FETCH ONE ===========================
    public ResPositionLevelDTO fetchById(Long id) {
        PositionLevel pl = repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tồn tại."));
        return convertToRes(pl);
    }

    // ================= FETCH ALL ===========================
    public ResultPaginationDTO fetchAll(Specification<PositionLevel> spec, Pageable pageable) {

        Page<PositionLevel> pagePL = repo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePL.getTotalPages());
        mt.setTotal(pagePL.getTotalElements());

        rs.setMeta(mt);

        rs.setResult(
                pagePL.getContent()
                        .stream()
                        .map(this::convertToRes)
                        .collect(Collectors.toList()));

        return rs;
    }

    // ================= CONVERTERS ==========================
    private ResCreatePositionLevelDTO convertToResCreate(PositionLevel pl) {
        ResCreatePositionLevelDTO res = new ResCreatePositionLevelDTO();

        res.setId(pl.getId());
        res.setCode(pl.getCode());
        res.setBand(extractBand(pl.getCode()));
        res.setLevelNumber(extractLevel(pl.getCode()));
        res.setBandOrder(pl.getBandOrder());

        // ⭐ THÊM — thông tin công ty
        res.setCompanyId(pl.getCompany().getId());
        res.setCompanyName(pl.getCompany().getName());

        res.setCreatedAt(pl.getCreatedAt());
        res.setCreatedBy(pl.getCreatedBy());

        return res;
    }

    private ResPositionLevelDTO convertToRes(PositionLevel pl) {
        ResPositionLevelDTO res = new ResPositionLevelDTO();

        res.setId(pl.getId());
        res.setCode(pl.getCode());
        res.setBand(extractBand(pl.getCode()));
        res.setLevelNumber(extractLevel(pl.getCode()));
        res.setBandOrder(pl.getBandOrder());

        res.setStatus(pl.getStatus());
        res.setActive(pl.getStatus() != null && pl.getStatus() == 1);

        // ⭐ THÊM — thông tin công ty
        res.setCompanyId(pl.getCompany().getId());
        res.setCompanyName(pl.getCompany().getName());

        res.setCreatedAt(pl.getCreatedAt());
        res.setUpdatedAt(pl.getUpdatedAt());
        res.setCreatedBy(pl.getCreatedBy());
        res.setUpdatedBy(pl.getUpdatedBy());

        return res;
    }
}