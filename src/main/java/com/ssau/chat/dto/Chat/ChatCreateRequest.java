package com.ssau.chat.dto.Chat;

import com.ssau.chat.entity.enums.ChatType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ChatCreateRequest {

    private List<@Positive Long> userIds;

    @NotNull(message = "Chat type is required")
    private ChatType chatType;
}

