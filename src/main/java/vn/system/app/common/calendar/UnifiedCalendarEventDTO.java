package vn.system.app.common.calendar;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCalendarEventDTO {

    private String id;
    private String module;      // e.g. "TASK", "JOB_DESCRIPTION", "ACCOUNTING", "EVALUATION", "LEAVE", "MEETING"
    private String label;       // e.g. "Tác vụ hằng ngày"
    private String title;       // Task title or Event title
    private String description;
    private Instant eventDate;  // Event Date (ISO)
    private Instant dueDate;    // Deadline / Execution date
    private String actionLink;  // Deep-link to navigate in UI
    private String colorCode;   // Hex color code, e.g. "#1890ff"
    private String priority;    // e.g. "URGENT", "HIGH", "MEDIUM", "LOW"
    private String tagColor;
    private String tagLabel;
    private Long targetId;
    private String targetUrl;
}
