package com.ssau.chat.controller;

import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.service.test.TestDataGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class MockController {

    private final TestDataGeneratorService testDataGeneratorService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/createMockData")
    public void createMockData() {
        log.info("Create mock data");
        testDataGeneratorService.generateTestData();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/createMockMessages/{chatId}")
    public void createMockMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "10") int messageCount,  // Параметр для задания количества сообщений
            @AuthenticationPrincipal UserEntity userDetails) {
        log.info("Create mock messages for user with ID: {} in chat with ID: {} and message count: {}",
                userDetails.getId(), chatId, messageCount);
        testDataGeneratorService.generateMessagesForChat(chatId, userDetails.getId(), messageCount);
    }
}
