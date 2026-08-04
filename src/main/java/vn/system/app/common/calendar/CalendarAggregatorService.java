package vn.system.app.common.calendar;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

@Service
public class CalendarAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(CalendarAggregatorService.class);

    private final List<CalendarEventProvider> providers;

    public CalendarAggregatorService(Optional<List<CalendarEventProvider>> optionalProviders) {
        this.providers = optionalProviders.orElse(List.of());
    }

    public List<UnifiedCalendarEventDTO> getMyActions(Instant from, Instant to) {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Bạn chưa đăng nhập"));

        if (providers.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<List<UnifiedCalendarEventDTO>>> futures = providers.stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> {
                    try {
                        List<UnifiedCalendarEventDTO> events = provider.getMyPendingEventsInRange(currentUserId, from, to);
                        return events != null ? events : List.<UnifiedCalendarEventDTO>of();
                    } catch (Exception e) {
                        log.warn("[CalendarAggregatorService] Provider {} threw exception: {}", provider.getClass().getSimpleName(), e.getMessage());
                        return List.<UnifiedCalendarEventDTO>of();
                    }
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<UnifiedCalendarEventDTO> aggregatedEvents = new ArrayList<>();
        for (var future : futures) {
            try {
                aggregatedEvents.addAll(future.get());
            } catch (Exception ignored) {}
        }

        // Sort by dueDate ascending
        aggregatedEvents.sort(Comparator.comparing(UnifiedCalendarEventDTO::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));

        return aggregatedEvents;
    }
}
