package vn.system.app.modules.facebook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.modules.facebook.domain.FacebookPage;
import vn.system.app.modules.facebook.repository.FacebookPageRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/facebook-pages")
public class FacebookPageController {

    private final FacebookPageRepository facebookPageRepo;

    public FacebookPageController(FacebookPageRepository facebookPageRepo) {
        this.facebookPageRepo = facebookPageRepo;
    }

    // ============================================================
    // 1 Lấy danh sách tất cả các fanpage
    // ============================================================
    @GetMapping
    public ResponseEntity<List<FacebookPage>> getAll() {
        return ResponseEntity.ok(facebookPageRepo.findAll());
    }

    // ============================================================
    // 2 Tạo mới fanpage (thêm token và thông tin)
    // ============================================================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody FacebookPage page) {
        if (facebookPageRepo.existsByPageId(page.getPageId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Fanpage với pageId này đã tồn tại!");
        }
        FacebookPage saved = facebookPageRepo.save(page);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ============================================================
    // 3 Cập nhật access token hoặc tên page (khi token thay đổi)
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePage(@PathVariable Long id, @RequestBody FacebookPage updatedPage) {
        Optional<FacebookPage> existing = facebookPageRepo.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy fanpage ID = " + id);
        }

        FacebookPage page = existing.get();

        if (updatedPage.getName() != null && !updatedPage.getName().isBlank()) {
            page.setName(updatedPage.getName());
        }
        if (updatedPage.getAccessToken() != null && !updatedPage.getAccessToken().isBlank()) {
            page.setAccessToken(updatedPage.getAccessToken());
        }
        if (updatedPage.getPageId() != null && !updatedPage.getPageId().isBlank()) {
            page.setPageId(updatedPage.getPageId());
        }

        facebookPageRepo.save(page);
        return ResponseEntity.ok(page);
    }

    // ============================================================
    // 4 Xóa page khỏi hệ thống
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!facebookPageRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        facebookPageRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
