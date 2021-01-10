package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class START extends Action implements IAction {
    public static final String CODE = "/start";

    public START(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        start(message);
        return this;
    }

    @Override
    public boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void start(MessageChat message) throws TelegramException {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (user == null) {
            String lang = msg.langOrDefault(message.getLanguageCode());
            user = User.builder()
                    .idUser(message.getFromId().longValue())
                    .username(message.getFromUsername())
                    .state(State.EMPTY_BIO.name())
                    .lang(lang)
//                    .description(msg.anyDescription(lang))
                    .build();
            services.user.save(user);
            log.info("NEW USER: {}", user);
        }
        if (User.isBanned(user)) {
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_BANNED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.banned()));
            logResult(Msg.START_BANNED_USER.name(), message.getChatId(), sendResponse.isOk());
        } else {
            if (!User.isEmptyBio(user)) {
                user.setState(State.EMPTY_BIO.name());
                user.setDescription("");
                services.user.save(user);
            }
            services.chat.getByIdUserAndState(user.getIdUser(), ChatState.ACTIVE)
                    .parallelStream()
                    .forEach(x -> {
                        x.setState(ChatState.SKIPPED.name());
                        services.chat.save(x);
                    });
            services.tag.removeAll(user);
            services.tag.add(user, Tag.GENERAL.value());
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_OK, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.remove())
            );
            logResult(Msg.START_OK.name(), message.getChatId(), sendResponse.isOk());
            SendResponse sendResponse2 = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.EMPTY_BIO, user.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false)
                    //        .replyMarkup(Keyboard.play())
            );
            logResult(Msg.EMPTY_BIO.name(), message.getChatId(), sendResponse2.isOk());
        }

    }
}
