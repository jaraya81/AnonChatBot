package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.vo.MessageChat;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.util.Keyboard;

import java.util.Objects;

@Builder
@Slf4j
public class CONFIG implements Action {
    public static final String CODE = "Config";

    private ServiceChat serviceChat;
    private TelegramBot bot;
    private MsgProcess msg;

    @Override
    public Action exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            config(message);
        }
        return this;
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void config(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user)) {
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.CONFIG, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.config()));
            log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
        }
    }
}
