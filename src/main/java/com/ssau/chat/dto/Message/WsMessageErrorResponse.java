package com.ssau.chat.dto.Message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WsMessageErrorResponse {
    private String error;
}
