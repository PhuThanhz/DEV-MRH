package vn.system.app.modules.sourcelink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.dowload.domain.response.DownloadResponse;
import vn.system.app.modules.dowload.service.DownloadService;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.repository.SourceLinkRepository;

@Slf4j
@Service
public class SourceLinkService {

    private final SourceLinkRepository linkRepo;
    private final DownloadService downloadService;
    private final SourceGroupService groupService;

    public SourceLinkService(SourceLinkRepository linkRepo,
            DownloadService downloadService,
            SourceGroupService groupService) {
        this.linkRepo = linkRepo;
        this.downloadService = downloadService;
        this.groupService = groupService;
    }

    // ============================================================
    // 1. Xử lý tải toàn bộ link trong group
    // ============================================================
    public void handleProcessGroup(Long groupId) {
        SourceGroup group = groupService.findById(groupId);
        log.info("Bắt đầu xử lý group '{}' ({} link)", group.getName(), group.getLinks().size());

        group.getLinks().stream()
                .filter(SourceLink::isPendingOrFailed)
                .forEach(this::processAsync);

        log.info("Đã gửi yêu cầu tải các link chưa xử lý hoặc lỗi trong group '{}'", group.getName());
    }

    // ============================================================
    // 2. Tải từng link (bất đồng bộ)
    // ============================================================
    @Async
    public void processAsync(SourceLink link) {
        final String DEFAULT_FOLDER = "threads_video";

        try {
            log.info("Bắt đầu tải video ID={} | {}", link.getId(), link.getUrl());
            DownloadResponse result = downloadService.processDownload(link.getUrl(), DEFAULT_FOLDER);

            if (result.isSuccess()) {
                link.setStatus(SourceLink.ProcessingStatus.SUCCESS);
                link.setName(result.getName());
                link.setUserId(result.getUserId());
                link.setCaption(result.getCaption());
                link.setContentGenerated(result.getFolder());
                link.setErrorMessage(null);
                link.setType(SourceLink.ContentType.VIDEO);
                log.info(" Tải thành công video ID={} | {}", link.getId(), link.getUrl());
            } else {
                link.setStatus(SourceLink.ProcessingStatus.FAILED);
                link.setErrorMessage(result.getError());
                log.warn(" Tải thất bại video ID={} | Lỗi: {}", link.getId(), result.getError());
            }

        } catch (Exception e) {
            log.error(" Lỗi tải link ID={} - {}", link.getId(), e.getMessage());
            link.setStatus(SourceLink.ProcessingStatus.FAILED);
            link.setErrorMessage("Lỗi tải: " + e.getMessage());
        } finally {
            linkRepo.save(link);
        }
    }

    // ============================================================
    // 3. Lấy danh sách link theo group (phân trang + filter SUCCESS/FAILED)
    // ============================================================
    public ResultPaginationDTO handleGetLinksByGroup(Long groupId, Pageable pageable, String statusFilter) {
        SourceGroup group = groupService.findById(groupId);
        Page<SourceLink> page;

        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                SourceLink.ProcessingStatus statusEnum = SourceLink.ProcessingStatus
                        .valueOf(statusFilter.toUpperCase());
                page = linkRepo.findAllByGroupAndStatus(group, statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                throw new IdInvalidException(
                        "Trạng thái không hợp lệ: " + statusFilter + " (chỉ nhận SUCCESS hoặc FAILED)");
            }
        } else {
            page = linkRepo.findAllByGroup(group, pageable);
        }

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(page.getContent());
        return rs;
    }

    // ============================================================
    // 4. Cập nhật caption cho link
    // ============================================================
    public SourceLink handleUpdateCaption(Long linkId, String caption) {
        SourceLink link = linkRepo.findById(linkId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy link ID = " + linkId));

        link.setCaption(caption.trim());
        linkRepo.save(link);
        log.info("Đã cập nhật caption cho link ID={} -> {}", linkId, caption);
        return link;
    }

    // ============================================================
    // 5. Tìm link theo ID
    // ============================================================
    public SourceLink findById(Long linkId) {
        return linkRepo.findById(linkId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy link ID = " + linkId));
    }
}
