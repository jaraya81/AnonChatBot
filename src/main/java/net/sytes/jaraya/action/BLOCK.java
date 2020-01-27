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
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.ChatState;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class BLOCK implements Action {

    public static final String CODE = "Block";

    private TelegramBot bot;
    private MsgProcess msg;

    private ServiceChat serviceChat;

    @Override
    public Action exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            block(message);
        }
        return this;
    }

    private void block(MessageChat message) throws TelegramException {
        User user1 = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        if (User.exist(user1) && !User.isBanned(user1) && User.isPlayed(user1)) {
            List<Chat> chats = serviceChat.find(user1.getIdUser()).stream().filter(x -> x.getState().contentEquals(ChatState.ACTIVE.name())).collect(Collectors.toList());
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                chat.setState(ChatState.BLOCKED.name());
                serviceChat.getChatRepo().save(chat);

                SendResponse sendResponse = bot.execute(new SendMessage(user1.getIdUser(), msg.msg(Msg.USER_BLOCK, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
            }
        }
    }

    private boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

}
