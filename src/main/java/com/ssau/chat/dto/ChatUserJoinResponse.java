package com.ssau.chat.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserJoinResponse {
    private Long chatId;
    private Long userId;
    private LocalDateTime joinedAt;
}