package com.ssau.chat.dto.ChatUser;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserCreateResponse {
    private Long chatId;
    private Long userId;
    private LocalDateTime joinedAt;
}