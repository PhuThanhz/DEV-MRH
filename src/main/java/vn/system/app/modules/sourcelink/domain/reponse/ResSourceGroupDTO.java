package vn.system.app.modules.sourcelink.domain.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResSourceGroupDTO {

    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private int totalLinks;

    private List<LinkInfo> links;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinkInfo {
        private Long id;
        private String url;
        private String caption;
        private String status;
        private String contentGenerated;
        private String errorMessage;
    }
}
