package vn.system.app.modules.dowload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.system.app.modules.dowload.domain.reponse.DownloadResponse;
import vn.system.app.modules.dowload.service.DownloadService;

@RestController
@RequestMapping("/api/v1/download")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping
    public DownloadResponse download(
            @RequestParam("url") String url,
            @RequestParam(name = "folder", defaultValue = "downloads") String folder) {
        return downloadService.processDownload(url, folder);
    }

    @GetMapping("/test")
    public String test() {
        return "✅ Download API đang hoạt động!";
    }
}
