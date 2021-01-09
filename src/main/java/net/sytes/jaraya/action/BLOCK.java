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
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class BLOCK extends Action implements IAction {

    public static final String CODE = "✖ Block";

    public BLOCK(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            block(message);
        }
        return this;
    }

    private void block(MessageChat message) throws TelegramException {
        User user1 = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user1) && !User.isBanned(user1) && User.isPlayed(user1)) {
            List<Chat> chats = services.chat.getByIdUserAndState(user1.getIdUser(), ChatState.ACTIVE);
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                chat.setState(ChatState.BLOCKED.name());
                services.chat.save(chat);
                SendResponse sendResponse = bot.execute(new SendMessage(user1.getIdUser(), msg.msg(Msg.USER_BLOCK, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));

                logResult(CODE, message.getChatId(), sendResponse.isOk());
            }
        }
    }

    private boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

}
