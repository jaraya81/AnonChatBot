package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.util.StringUtil;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class CHAT extends Action implements IAction {

    public CHAT(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            chat(message);
        }
        return this;
    }

    private void chat(MessageChat message) throws TelegramException {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        services.user.save(user);
        if (User.exist(user) && !User.isBanned(user) && User.isPlayed(user)) {
            List<Chat> chats = services.chat.getByIdUserAndState(user.getIdUser(), ChatState.ACTIVE);
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                Long id = chat.otherId(user.getIdUser());
                if (User.isPlayed(services.user.getByIdUser(id))) {
                    if (message.getStickerFileId() != null &&
                            isInactive(bot.execute(new SendSticker(id, message.getStickerFileId())
                                    .disableNotification(false)), id)) {
                        sendNextU(user, chat);
                    }
                    if (message.getPhoto() != null &&
                            isInactive(bot.execute(new SendPhoto(id, message.getPhoto())
                                    .parseMode(ParseMode.MarkdownV2)
                                    .caption(message.getCaption() != null ? message.getCaption() : "")
                                    .disableNotification(false)), id)) {
                        sendNextU(user, chat);
                    }

                    if (message.getVoiceFileId() != null &&
                            isInactive(bot.execute(new SendVoice(id, message.getVoiceFileId())
                                    .parseMode(ParseMode.MarkdownV2)
                                    .disableNotification(false)), id)) {
                        sendNextU(user, chat);
                    }

                    if (message.getText() != null) {
                        String msgText = String.format("%s", StringUtil.clean("» " + message.getText()));
                        log.info("{} -> {}: {}", user.getIdUser(), id, msgText.replaceAll("[\\d\\D]+", "*"));
                        if (user.getIdUser().longValue() == getUserAdmin()) {
                            bot.execute(new SendMessage(user.getIdUser(), msgText)
                                    .parseMode(ParseMode.MarkdownV2)
                                    .disableWebPagePreview(false)
                                    .disableNotification(false));
                        }
                        if (isInactive(bot.execute(new SendMessage(id, msgText)
                                        .parseMode(ParseMode.MarkdownV2)
                                        .disableWebPagePreview(false)
                                        .disableNotification(false))
                                , id)) {
                            sendNextU(user, chat);
                        }
                    }
                    services.chat.save(chat);
                }
            } else {
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_NO_CHAT, user.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyMarkup(Keyboard.play()));
                logResult(Msg.USER_NO_CHAT.name(), message.getChatId(), sendResponse.isOk());
            }
        }
    }

    private void sendNextU(User user, Chat chat) {
        log.info("{} :: {} :: {}", Msg.NEXT_YOU, user.getIdUser(),
                bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.NEXT_YOU, user.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)).isOk());
        chat.setState(ChatState.SKIPPED.name());
    }


    public boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && (message.getText() == null ||
                (!message.getText().contentEquals(NEXT.CODE)
                        && !message.getText().contentEquals(NEXT.CODE_ALT)
                        && !message.getText().contentEquals(PAUSE.CODE)
                        && !message.getText().contentEquals(PLAY.CODE)
                        && !message.getText().contentEquals(BLOCK.CODE)
                        && !message.getText().contentEquals(REPORT.CODE)
                        && !message.getText().contentEquals(START.CODE)
                        && !message.getText().contentEquals(BIO.CODE_1)
                        && !message.getText().contentEquals(BIO.CODE_2)
                        && !message.getText().startsWith(BIO.SET_CODE)
                        && !message.getText().startsWith(ABOUT.CODE)
                        && !message.getText().contentEquals(LANG.CODE)
                        && !message.getText().startsWith(LANG.SET_CODE)))
                ;
    }

}
