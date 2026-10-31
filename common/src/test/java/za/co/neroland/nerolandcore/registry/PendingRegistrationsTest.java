package za.co.neroland.nerolandcore.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PendingRegistrationsTest {
    @Test
    void attachesExistingAndLaterRegistrationsExactlyOnce() {
        var pending = new PendingRegistrations<String>();
        List<String> attached = new ArrayList<>();
        pending.add("blocks");
        pending.add("items");
        pending.attachAll(attached::add);
        pending.attachAll(attached::add);
        pending.add("tabs");
        pending.attachAll(attached::add);
        assertEquals(List.of("blocks", "items", "tabs"), attached);
    }

    @Test
    void failedAttachmentRemainsPendingWithoutRepeatingSuccessfulOnes() {
        var pending = new PendingRegistrations<String>();
        List<String> attached = new ArrayList<>();
        pending.add("blocks");
        pending.add("items");
        pending.add("tabs");
        assertThrows(IllegalStateException.class, () -> pending.attachAll(register -> {
            if (register.equals("items")) {
                throw new IllegalStateException("attachment failed");
            }
            attached.add(register);
        }));
        pending.attachAll(attached::add);
        assertEquals(List.of("blocks", "items", "tabs"), attached);
    }

    @Test
    @Timeout(20)
    void parallelModConstructionAndAttachmentLoseNoRegistrations() throws Exception {
        var pending = new PendingRegistrations<Integer>();
        List<Integer> attached = new ArrayList<>();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int mod = 0; mod < 8; mod++) {
                int base = mod * 1000;
                tasks.add(executor.submit(() -> {
                    start.await();
                    for (int entry = 0; entry < 1000; entry++) {
                        pending.add(base + entry);
                        if (entry % 10 == 0) {
                            pending.attachAll(attached::add);
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(15, TimeUnit.SECONDS);
            }
        }
        pending.attachAll(attached::add);
        assertEquals(8000, attached.size());
        assertEquals(8000, new HashSet<>(attached).size());
    }
}
