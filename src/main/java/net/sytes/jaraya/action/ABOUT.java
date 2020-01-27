package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.vo.MessageChat;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.state.State;

import java.util.Objects;

@Slf4j
@Builder
public class ABOUT implements Action {
    public static final String CODE = "/about";

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
                && message.getText().startsWith(CODE);
    }

    private void bio(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user)) {
            if (message.getText().startsWith(CODE)) {
                Long size = serviceChat.getUserRepo().getAllByLang(user.getLang()).parallelStream().filter(x -> x.getState().contentEquals(State.PLAY.name())).count();
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.ABOUT, user.getLang())
                        + "\n\n" + msg.msg(Msg.USER_ACTIVE, user.getLang()) + size)
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
            }
        }
    }
}
