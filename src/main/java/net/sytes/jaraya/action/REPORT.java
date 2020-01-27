package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.vo.MessageChat;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class REPORT implements Action {
    public static final String CODE = "Spam";

    private TelegramBot bot;
    private MsgProcess msg;

    private ServiceChat serviceChat;

    @Override
    public Action exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            report(message);
        }
        return this;
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void report(MessageChat message) throws TelegramException {
        User user = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user) && User.isPlayed(user)) {
            List<Chat> chats = serviceChat.find(user.getIdUser()).stream().filter(x -> x.getState().contentEquals(ChatState.ACTIVE.name())).collect(Collectors.toList());
            if (chats.isEmpty()) {
                return;
            }
            chats.get(0).setState(ChatState.REPORT.name());
            serviceChat.getChatRepo().save(chats.get(0));

            Long otherUser = chats.get(0).getUser1().compareTo(user.getIdUser()) != 0 ? chats.get(0).getUser1() : chats.get(0).getUser2();

            serviceChat.addReport(otherUser);

            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_REPORT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true));
            log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
        }

    }
}

