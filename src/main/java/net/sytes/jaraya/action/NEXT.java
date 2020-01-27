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
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class NEXT implements Action {

    public static final String CODE = "Next!";

    private TelegramBot bot;
    private MsgProcess msg;

    private ServiceChat serviceChat;

    @Override
    public Action exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            next(message);
        }
        return this;
    }

    private void next(MessageChat message) throws TelegramException {
        User user1 = serviceChat.getUserRepo().getByIdUser(message.getFromId().longValue());
        serviceChat.getUserRepo().save(user1);
        if (User.exist(user1) && !User.isBanned(user1) && User.isPlayed(user1)) {
            List<Chat> chats = serviceChat.find(user1.getIdUser()).stream().filter(x -> x.getState().contentEquals(ChatState.ACTIVE.name())).collect(Collectors.toList());
            for (Chat chat : chats) {
                chat.setState(ChatState.SKIPPED.name());
                serviceChat.getChatRepo().save(chat);
                Long otherUser = chat.getUser1().compareTo(user1.getIdUser()) != 0 ? chat.getUser1() : chat.getUser2();
                bot.execute(new SendMessage(otherUser, msg.msg(Msg.NEXT_YOU, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
            }

            cleanChat();
            pauseUsersInactive();
            Chat chat = serviceChat.assignNewChat(user1.getIdUser(), user1.getLang());
            if (chat != null) {
                User user2 = serviceChat.getUserRepo().getByIdUser(chat.getUser2());
                SendResponse sendResponse1 = bot.execute(new SendMessage(user1.getIdUser(), msg.msg(Msg.USER_1_NEXT_OK, user1.getLang())
                        + "<i>" + (user2.getDescription() != null ? user2.getDescription() : "") + "</i>")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse1.isOk() ? "OK" : "NOK"));
                SendResponse sendResponse2 = bot.execute(new SendMessage(user2.getIdUser(), msg.msg(Msg.USER_2_NEXT_OK, user2.getLang())
                        + "<i>" + (user1.getDescription() != null ? user1.getDescription() : "") + "</i>")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse2.isOk() ? "OK" : "NOK"));
            } else {
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_NEXT_WAITING, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                log.info(CODE + " :: " + message.getChatId() + " :: " + (sendResponse.isOk() ? "OK" : "NOK"));
            }


        }
    }

    private void pauseUsersInactive() throws TelegramException {
        List<User> users = serviceChat.getUsersInactive();
        for (User user : users) {
            user.setState(State.PAUSE.name());
            serviceChat.getUserRepo().save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.INACTIVITY_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
        }
    }

    private void cleanChat() throws TelegramException {
        Set<Long> ids = serviceChat.cleanerChat();
        for (Long id : ids) {
            User user = serviceChat.getUserRepo().getByIdUser(id);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true));
        }
    }

    private boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

}
