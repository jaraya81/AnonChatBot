package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class START extends Action implements IAction {
    public static final String CODE = "/start";

    public START(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            start(message);
        }
        return this;
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void start(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserByIdUser(message.getFromId().longValue());
        if (Objects.isNull(user)) {
            String lang = msg.langOrDefault(message.getLanguageCode());
            user = User.builder()
                    .idUser(message.getFromId().longValue())
                    .username(message.getFromUsername())
                    .state(State.PLAY.name())
                    .lang(lang)
                    .description(msg.anyDescription(lang))
                    .build();
            serviceChat.saveUser(user);
            log.info("NEW USER: {}", user);
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_OK, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.start()));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
            SendResponse sendResponse2 = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_PLAY, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.play()));
            logResult(CODE, message.getChatId(), sendResponse2.isOk());
        } else if (User.isBanned(user)) {
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_BANNED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.banned()));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
        } else {
            user.setState(State.PLAY.name());
            serviceChat.saveUser(user);
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_AGAIN, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.start()));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
            SendResponse sendResponse2 = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_PLAY, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.play()));
            logResult(CODE, message.getChatId(), sendResponse2.isOk());
        }
    }
}
