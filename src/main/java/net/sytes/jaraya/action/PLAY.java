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
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class PLAY extends Action implements IAction {
    public static final String CODE = "▶ Play";

    public PLAY(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) {
        play(message);
        return this;
    }

    @Override
    public boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void play(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        play(user);
    }

    public void play(User user) {
        if (User.exist(user) && !User.isBanned(user) && !User.isPlayed(user)) {
            user.setState(State.PLAY.name());
            services.user.save(user);
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.USER_PLAY, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.play()));
            logResult(Msg.USER_PLAY.name(), user.getIdUser(), sendResponse.isOk());
            new NEXT(bot, services, msg, userAdmin).next(user);
        }
    }
}
