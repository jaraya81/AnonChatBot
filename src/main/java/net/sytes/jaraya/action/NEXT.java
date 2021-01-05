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
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class NEXT extends Action implements IAction {

    public static final String CODE = "Next!";

    public NEXT(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }


    @Override
    public IAction exec(MessageChat message) throws TelegramException {
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

            Chat chat = serviceChat.assignNewChat(user1.getIdUser(), user1.getLang());
            if (chat != null) {
                User user2 = serviceChat.getUserRepo().getByIdUser(chat.getUser2());
                SendResponse sendResponse1 = bot.execute(new SendMessage(
                        user1.getIdUser(),
                        msg.msg(Msg.USER_NEXT_OK, user1.getLang(), elvis(user1.getDescription(), ""), elvis(user2.getDescription(), "")))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(false));
                logResult(CODE, message.getChatId(), sendResponse1.isOk());
                SendResponse sendResponse2 = bot.execute(new SendMessage(
                        user2.getIdUser(),
                        msg.msg(Msg.USER_NEXT_OK, user2.getLang(), elvis(user2.getDescription(), ""), elvis(user1.getDescription(), "")))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(false));
                logResult(CODE, message.getChatId(), sendResponse2.isOk());
            } else {
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_NEXT_WAITING, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(false));
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
