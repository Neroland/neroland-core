package za.co.neroland.nerolandcore.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Alert-cap eviction order and the module an alert's event is published under. */
class LinkAlertsCapTest {

    private static LinkAlert alert(String id, long createdAt, boolean acked) {
        return new LinkAlert(id, "testmod", LinkAlert.Severity.INFO, "text", createdAt, acked, 0L);
    }

    @Test
    void underTheCapNothingIsEvicted() {
        assertTrue(LinkAlerts.selectEvictions(List.of(alert("a", 1, false)), 5, "a").isEmpty());
    }

    @Test
    void oldestAcknowledgedGoFirstThenOldest() {
        List<LinkAlert> alerts = List.of(
                alert("old-unacked", 1, false),
                alert("mid-acked", 5, true),
                alert("new-acked", 8, true),
                alert("recent", 9, false),
                alert("raised", 10, false));
        List<String> evicted = LinkAlerts.selectEvictions(alerts, 2, "raised").stream().map(LinkAlert::id).toList();
        assertEquals(List.of("mid-acked", "new-acked", "old-unacked"), evicted);
    }

    @Test
    void theJustRaisedAlertIsNeverEvicted() {
        List<LinkAlert> alerts = List.of(alert("raised", 0, true), alert("b", 5, false));
        assertEquals(List.of("b"),
                LinkAlerts.selectEvictions(alerts, 1, "raised").stream().map(LinkAlert::id).toList());
    }

    @Test
    void eventsArePublishedUnderTheAlertsModuleWithCoreFallback() {
        assertEquals("nerologistics", LinkAlerts.publishingModule(
                new LinkAlert("x", "nerologistics", LinkAlert.Severity.WARN, "t", 0, false, 0)));
        assertEquals("core", LinkAlerts.publishingModule(
                new LinkAlert("x", " ", LinkAlert.Severity.WARN, "t", 0, false, 0)));
        assertEquals("core", LinkAlerts.publishingModule(
                new LinkAlert("x", null, LinkAlert.Severity.WARN, "t", 0, false, 0)));
    }
}
