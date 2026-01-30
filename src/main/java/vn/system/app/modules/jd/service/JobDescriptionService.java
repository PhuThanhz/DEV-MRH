package vn.system.app.modules.jd.service;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.domain.JobDescription;
import vn.system.app.modules.jd.domain.request.ReqCreateJobDescription;
import vn.system.app.modules.jd.domain.request.ReqUpdateJobDescription;
import vn.system.app.modules.jd.domain.response.ResJobDescriptionDTO;
import vn.system.app.modules.jd.domain.response.ResJobDescriptionListDTO;
import vn.system.app.modules.jd.repository.JobDescriptionRepository;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository repo;

    public JobDescriptionService(JobDescriptionRepository repo) {
        this.repo = repo;
    }

    /*
     * =====================================================
     * CREATE – TẠO JD (DRAFT)
     * =====================================================
     */
    public JobDescription create(ReqCreateJobDescription req) {

        JobDescription jd = new JobDescription();
        jd.setTitle(req.getTitle());
        jd.setContent(req.getContent());
        jd.setStatus("DRAFT");

        return repo.save(jd);
    }

    /*
     * =====================================================
     * UPDATE – CHỈ CHO DRAFT
     * =====================================================
     */
    public JobDescription update(ReqUpdateJobDescription req)
            throws IdInvalidException {

        JobDescription jd = fetchById(req.getId());

        if (!"DRAFT".equals(jd.getStatus())) {
            throw new IdInvalidException(
                    "JD đang xử lý hoặc đã ban hành, không thể chỉnh sửa");
        }

        jd.setTitle(req.getTitle());
        jd.setContent(req.getContent());

        return repo.save(jd);
    }

    /*
     * =====================================================
     * DELETE – CHỈ XOÁ KHI DRAFT
     * =====================================================
     */
    public void delete(Long id)
            throws IdInvalidException {

        JobDescription jd = fetchById(id);

        if (!"DRAFT".equals(jd.getStatus())) {
            throw new IdInvalidException(
                    "Chỉ được xoá JD ở trạng thái DRAFT");
        }

        repo.delete(jd);
    }

    /*
     * =====================================================
     * SUBMIT – GỬI JD ĐI DUYỆT
     * (thực tế sẽ do JD Flow xử lý)
     * =====================================================
     */
    public JobDescription submit(Long id)
            throws IdInvalidException {

        JobDescription jd = fetchById(id);

        if (!"DRAFT".equals(jd.getStatus())) {
            throw new IdInvalidException(
                    "Chỉ JD ở trạng thái DRAFT mới được gửi duyệt");
        }

        jd.setStatus("IN_REVIEW");
        return repo.save(jd);
    }

    /*
     * =====================================================
     * ISSUE – BAN HÀNH JD
     * (thực tế do CEO duyệt ở JD Flow)
     * =====================================================
     */
    public JobDescription issue(Long id)
            throws IdInvalidException {

        JobDescription jd = fetchById(id);

        if (!"IN_REVIEW".equals(jd.getStatus())) {
            throw new IdInvalidException(
                    "Chỉ JD đang duyệt mới được ban hành");
        }

        jd.setStatus("PUBLIC");
        return repo.save(jd);
    }

    /*
     * =====================================================
     * FETCH BY ID
     * =====================================================
     */
    public JobDescription fetchById(Long id)
            throws IdInvalidException {

        return repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));
    }

    /*
     * =====================================================
     * LIST – KHO JD
     * =====================================================
     */
    public ResultPaginationDTO fetchAll(
            Specification<JobDescription> spec,
            Pageable pageable) {

        Page<JobDescription> page = repo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        rs.setResult(
                page.getContent()
                        .stream()
                        .map(jd -> {
                            ResJobDescriptionListDTO dto = new ResJobDescriptionListDTO();
                            dto.setId(jd.getId());
                            dto.setTitle(jd.getTitle());
                            dto.setStatus(jd.getStatus());
                            return dto;
                        })
                        .collect(Collectors.toList()));

        return rs;
    }

    /*
     * =====================================================
     * CONVERT DETAIL
     * =====================================================
     */
    public ResJobDescriptionDTO toRes(JobDescription jd) {

        ResJobDescriptionDTO res = new ResJobDescriptionDTO();
        res.setId(jd.getId());
        res.setTitle(jd.getTitle());
        res.setContent(jd.getContent());
        res.setStatus(jd.getStatus());
        res.setCreatedAt(jd.getCreatedAt());
        res.setUpdatedAt(jd.getUpdatedAt());

        return res;
    }
}
