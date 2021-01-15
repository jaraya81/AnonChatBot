package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.button.PauseButton;
import net.sytes.jaraya.action.message.button.PlayButton;
import net.sytes.jaraya.action.message.button.ReportButton;
import net.sytes.jaraya.action.message.command.AboutCommand;
import net.sytes.jaraya.action.message.button.BlockButton;
import net.sytes.jaraya.action.message.command.LangCommand;
import net.sytes.jaraya.action.message.button.NextButton;
import net.sytes.jaraya.action.message.command.StartCommand;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.util.StringUtil;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class CHAT extends SuperAction implements IAction {

    public CHAT(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        chat(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && (message.getText() == null ||
                (!message.getText().contentEquals(NextButton.CODE)
                        && !message.getText().contentEquals(NextButton.CODE_ALT)
                        && !message.getText().contentEquals(PauseButton.CODE)
                        && !message.getText().contentEquals(PlayButton.CODE)
                        && !message.getText().contentEquals(BlockButton.CODE)
                        && !message.getText().contentEquals(ReportButton.CODE)
                        && !message.getText().contentEquals(StartCommand.CODE)
                        && !message.getText().contentEquals(Bio.CODE_1)
                        && !message.getText().contentEquals(Bio.CODE_2)
                        && !message.getText().startsWith(Bio.SET_CODE)
                        && !message.getText().startsWith(AboutCommand.CODE)
                        && !message.getText().contentEquals(LangCommand.CODE)
                        && !message.getText().startsWith(LangCommand.SET_CODE)))
                ;
    }

    private void chat(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        services.user.save(user);
        if (User.exist(user) && !User.isBanned(user) && User.isPlayed(user)) {
            List<Chat> chats = services.chat.getByIdUserAndState(user.getIdUser(), ChatState.ACTIVE);
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                Long id = chat.otherId(user.getIdUser());
                if (User.isPlayed(services.user.getByIdUser(id))) {
                    ifIsStickerSend(message, user, id, chat);
                    ifIsPhotoSend(message, user, id, chat);
                    ifIsVoiceSend(message, user, id, chat);
                    ifIsTextSend(message, user, id, chat);
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

    private void ifIsTextSend(MessageChat message, User user, Long id, Chat chat) {
        if (message.getText() != null) {
            String msgText = cleanText(message.getText(), user);
            if (msgText != null) {
                log.info("{} -> {}: {}", user.getIdUser(), id, msgText.replaceAll("[\\d\\D]+", "*"));
                if (user.getIdUser().longValue() == getUserAdmin()) {
                    bot.execute(new SendMessage(user.getIdUser(), msgText)
                            .parseMode(ParseMode.MarkdownV2)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                }
                if (isInactive(bot.execute(new SendMessage(id, msgText)
                                .parseMode(ParseMode.MarkdownV2)
                                .disableWebPagePreview(!user.isPremium())
                                .disableNotification(false))
                        , id)) {
                    sendNextU(user, chat);
                }
            }
        }

    }

    private String cleanText(String text, User user) {
        if (containsHttp(text) && !isPremium(user, "HTTP")) {
            return null;
        }
        return String.format("%s", StringUtil.clean("» " + text));
    }

    private boolean containsHttp(String text) {
        return text.toLowerCase().contains("http:")
                || text.toLowerCase().contains("https:")
                || text.matches("[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)");
    }

    private void ifIsVoiceSend(MessageChat message, User me, Long otherId, Chat chat) {
        if (message.getVoiceFileId() != null
                && isPremium(me, "Voice")
                && isInactive(bot.execute(new SendVoice(otherId, message.getVoiceFileId())
                .parseMode(ParseMode.MarkdownV2)
                .disableNotification(false)), otherId)) {
            sendNextU(me, chat);
        }

    }

    private void ifIsPhotoSend(MessageChat message, User me, Long otherId, Chat chat) {
        if (message.getPhoto() != null
                && isPremium(me, "Photo")
                && isInactive(bot.execute(new SendPhoto(otherId, message.getPhoto())
                .parseMode(ParseMode.MarkdownV2)
                .caption(message.getCaption() != null ? message.getCaption() : "")
                .disableNotification(false)), otherId)) {
            sendNextU(me, chat);
        }

    }

    private void ifIsStickerSend(MessageChat message, User me, Long otherId, Chat chat) {
        if (message.getStickerFileId() != null
                && isPremium(me, "Stickers")
                && isInactive(bot.execute(new SendSticker(otherId, message.getStickerFileId())
                .disableNotification(false)), otherId)) {
            sendNextU(me, chat);
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



}
