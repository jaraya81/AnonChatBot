package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class FORCE_BIO extends Action implements IAction {

    public FORCE_BIO(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) {
        forceBio(message);
        return this;
    }

    @Override
    public boolean check(MessageChat message) {
        if (Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && !message.getText().contentEquals(START.CODE)) {
            User user = services.user.getByIdUser(message.getFromId().longValue());
            return user != null && (user.getState().contentEquals(State.EMPTY_BIO.name()) || user.getDescription() == null || user.getDescription().isEmpty());
        }
        return false;
    }

    private void forceBio(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        forceBio(user, message.getText());
    }

    public void forceBio(User user, String text) {
        if (User.exist(user) && User.isEmptyBio(user)) {
            user.setDescription(text.length() <= 240 ?
                    text
                    : text.substring(0, 239));
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.SET_BIO_OK, user.getLang())
                    + "<i>" + (user.getDescription() != null ? user.getDescription() : "") + "</i>")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true));
            services.user.save(user);
            logResult(Msg.SET_BIO_OK.name(), user.getIdUser(), sendResponse.isOk());
            new PLAY(bot, services, msg, userAdmin).play(user);
        } else if (User.exist(user) && !User.isBanned(user) && user.getDescription() == null || user.getDescription().isEmpty()) {
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.EMPTY_BIO, user.getLang())
                    + "\n\n<i>" + (user.getDescription() != null ? user.getDescription() : "") + "</i>")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true));
            user.setState(State.EMPTY_BIO.name());
            services.user.save(user);
            logResult(Msg.BIO.name(), user.getIdUser(), sendResponse.isOk());
        }

    }


}
