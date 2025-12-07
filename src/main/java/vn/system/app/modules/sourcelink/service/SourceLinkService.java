package vn.system.app.modules.sourcelink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.dowload.domain.reponse.DownloadResponse;
import vn.system.app.modules.dowload.service.DownloadService;
import vn.system.app.modules.sourcelink.repository.SourceLinkRepository;

import java.util.List;

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
    // 1️⃣ Tải toàn bộ link trong 1 group
    // ============================================================
    public void processGroup(Long groupId) {
        var group = groupService.getGroup(groupId);
        log.info("Bắt đầu tải group '{}' ({} link)", group.getName(), group.getLinks().size());
        group.getLinks().forEach(this::processAsync);
    }

    // ============================================================
    // 2️⃣ Tải từng link (bất đồng bộ)
    // ============================================================
    @Async
    public void processAsync(SourceLink link) {
        try {
            String folder = (link.getGroup() != null)
                    ? link.getGroup().getName() + "/" + link.getId()
                    : "threads-video";

            log.info("Tải video ID={} | {}", link.getId(), link.getUrl());
            DownloadResponse result = downloadService.processDownload(link.getUrl(), folder);

            if (result.isSuccess()) {
                link.setStatus(SourceLink.ProcessingStatus.SUCCESS);
                link.setName(result.getName());
                link.setUserId(result.getUserId());
                link.setCaption(result.getCaption());
                link.setContentGenerated(result.getFolder());
                link.setErrorMessage(null);
                link.setType(SourceLink.ContentType.VIDEO);
            } else {
                link.setStatus(SourceLink.ProcessingStatus.FAILED);
                link.setErrorMessage(result.getError());
            }

        } catch (Exception e) {
            link.setStatus(SourceLink.ProcessingStatus.FAILED);
            link.setErrorMessage("Lỗi tải: " + e.getMessage());
        } finally {
            linkRepo.save(link);
        }
    }

    // ============================================================
    // 3️⃣ Lấy danh sách link SUCCESS (để đăng bài)
    // ============================================================
    public List<SourceLink> getSuccessLinksByGroup(Long groupId) {
        var group = groupService.getGroup(groupId);
        return group.getLinks().stream()
                .filter(link -> link.getStatus() == SourceLink.ProcessingStatus.SUCCESS)
                .toList();
    }
}
