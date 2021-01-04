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
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class ABOUT extends Action implements IAction {
    public static final String CODE = "/about";

    public ABOUT(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            action(message);
        }
        return this;
    }

    private boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().startsWith(CODE);
    }

    private void action(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user) && message.getText().startsWith(CODE)) {
            long size = serviceChat.getUserRepo().getAllByLang(user.getLang()).parallelStream().filter(x -> x.getState().contentEquals(State.PLAY.name())).count();
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(),
                    msg.msg(Msg.ABOUT, user.getLang(), size))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
        }
    }

}
