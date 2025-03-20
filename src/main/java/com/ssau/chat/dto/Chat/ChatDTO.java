package com.ssau.chat.dto.Chat;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
