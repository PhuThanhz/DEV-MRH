package vn.system.app.common.calendar;

import java.time.Instant;
import java.util.List;

public interface CalendarEventProvider {

    List<UnifiedCalendarEventDTO> getMyPendingEventsInRange(String userId, Instant from, Instant to);
}
