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

import java.util.Objects;

@Slf4j
@Builder
public class BIO implements Action {
    public static final String CODE_1 = "Bio";
    public static final String CODE_2 = "/bio";
    public static final String SET_CODE = "/bio ";

    private ServiceChat serviceChat;
    private TelegramBot bot;
    private MsgProcess msg;

    @Override
    public Action exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            bio(message);
        }
        return this;
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && (message.getText().contentEquals(CODE_1) || message.getText().contentEquals(CODE_2) || message.getText().startsWith(SET_CODE));
    }

    private void bio(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());

        if (User.exist(user) && !User.isBanned(user)) {
            if (message.getText().startsWith(SET_CODE)) {
                String bio = message.getText().replace(SET_CODE, "");
                user.setDescription(bio.length() <= 140 ? bio : bio.substring(0, 139));
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.SET_BIO_OK, user.getLang())
                        + "<i>" + (user.getDescription() != null ? user.getDescription() : "") + "</i>")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                serviceChat.getUserRepo().save(user);
                log.info(SET_CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
            } else if (message.getText().contentEquals(CODE_1) || message.getText().contentEquals(CODE_2)) {
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.BIO, user.getLang())
                        + "\n\n<i>" + (user.getDescription() != null ? user.getDescription() : "") + "</i>")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                log.info(CODE_1 + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
            }

        }

    }

}
