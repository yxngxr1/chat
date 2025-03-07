package com.ssau.chat.dto;

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
    private LocalDateTime createdAt;
}
