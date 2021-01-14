package net.sytes.jaraya.vo;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Data
@Builder
@ToString
public class MessageChat extends BaseUpdate {

    private Integer fromId;
    private Integer messageId;
    private String text;
    private Long chatId;
    private String languageCode;
    private String fromUsername;
    private String stickerFileId;
    private String voiceFileId;
    private String caption;
    private String photo;

    public static MessageChat to(Message message) {
        if (message == null) return null;
        Optional<PhotoSize> photoSize = message.photo() != null ? Arrays.asList(message.photo()).stream().findFirst() : new ArrayList<PhotoSize>().stream().findFirst();
        return MessageChat.builder()
                .messageId(message.messageId())
                .fromId(message.from().id())
                .text(message.text())
                .chatId(message.chat().id())
                .languageCode(message.from().languageCode())
                .fromUsername(message.from().username())
                .stickerFileId(message.sticker() != null ? message.sticker().fileId() : null)
                .voiceFileId(message.voice() != null ? message.voice().fileId() : null)
                .caption(message.caption())
                .photo(photoSize.isPresent() ? photoSize.get().fileId() : null)
                .build();
    }

}
