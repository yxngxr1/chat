package com.ssau.chat.dto;

import com.ssau.chat.entity.enums.ChatType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class CreateChatRequest {

    private List<@Positive Long> userIds;

    @NotNull(message = "Chat type is required")
    private ChatType chatType;
}

