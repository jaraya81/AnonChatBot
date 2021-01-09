package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class PAUSE extends Action implements IAction {
    public static final String CODE = "⏸ Pause";

    public PAUSE(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            pause(message);
        }
        return this;
    }

    private void pause(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user) && (User.isPlayed(user) || User.isPausedOrStop(user))) {
            user.setState(State.PAUSE.name());
            serviceChat.saveUser(user);
            List<Chat> chats = serviceChat.getChatsByIdUserAndState(user.getIdUser(), ChatState.ACTIVE);
            for (Chat chat : chats) {
                chat.setState(ChatState.SKIPPED.name());
                serviceChat.saveChat(chat);
            }
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_PAUSE, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.pause()));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
        }
    }
}
