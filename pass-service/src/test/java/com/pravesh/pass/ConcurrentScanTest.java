package com.pravesh.pass;

import com.pravesh.pass.dto.response.PassValidationResponse;
import com.pravesh.pass.entity.VisitorPass;
import com.pravesh.pass.entity.enums.PassStatus;
import com.pravesh.pass.entity.enums.PassType;
import com.pravesh.pass.repository.VisitorPassRepository;
import com.pravesh.pass.service.PassService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrentScanTest {

    @Autowired
    private PassService passService;

    @Autowired
    private VisitorPassRepository passRepository;

    @Test
    void tenSimultaneousScans_exactlyOneGranted() throws InterruptedException {
        // Arrange: create a fresh ONE_TIME pass directly via the repository
        String uuid = UUID.randomUUID().toString();
        VisitorPass pass = VisitorPass.builder()
                .residentId(1L)
                .societyId(1L)
                .uuid(uuid)
                .visitorName("Concurrent Test Visitor")
                .visitorPhone("9800000000")
                .passType(PassType.ONE_TIME)
                .validFrom(LocalDateTime.now().minusMinutes(5))
                .validUntil(LocalDateTime.now().plusHours(1))
                .status(PassStatus.ACTIVE)
                .build();
        passRepository.save(pass);
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger grantedCount = new AtomicInteger(0);
        AtomicInteger deniedCount = new AtomicInteger(0);

        List<Future<?>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // all threads wait here, then release together
                    PassValidationResponse result = passService.validateAndConsume(uuid, 1L);
                    if (result.granted()) {
                        grantedCount.incrementAndGet();
                    } else {
                        deniedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown(); // release all 10 threads at once
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("GRANTED: " + grantedCount.get() + ", DENIED: " + deniedCount.get());

        assertEquals(1, grantedCount.get(), "Exactly one scan must be GRANTED");
        assertEquals(9, deniedCount.get(), "Exactly nine scans must be DENIED");
    }
}