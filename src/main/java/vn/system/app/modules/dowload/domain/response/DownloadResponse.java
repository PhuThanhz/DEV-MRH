package vn.system.app.modules.dowload.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponse {
    private boolean success;

    private String name;
    private String userId;

    private String caption;
    private String folder;
    private String error;
}
