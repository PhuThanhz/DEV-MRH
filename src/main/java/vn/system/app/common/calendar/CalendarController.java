package vn.system.app.common.calendar;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class CalendarController {

    private final CalendarAggregatorService aggregatorService;

    public CalendarController(CalendarAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @GetMapping("/calendar/my-actions")
    @ApiMessage("Lịch xử lý công việc tập trung của tôi")
    public ResponseEntity<List<UnifiedCalendarEventDTO>> getMyActions(
            @RequestParam(value = "from", required = false) String fromStr,
            @RequestParam(value = "to", required = false) String toStr) {

        Instant from = parseDateParam(fromStr, false);
        Instant to = parseDateParam(toStr, true);

        return ResponseEntity.ok(aggregatorService.getMyActions(from, to));
    }

    private Instant parseDateParam(String input, boolean endOfDay) {
        if (input == null || input.trim().isEmpty()) return null;
        try {
            if (input.contains("T")) {
                return Instant.parse(input);
            }
            java.time.LocalDate date = java.time.LocalDate.parse(input.trim());
            if (endOfDay) {
                return date.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant();
            } else {
                return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
