package net.sytes.jaraya.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeleteMessage {
    private long chatId;
    private int messageId;
    private LocalDateTime time;
}
