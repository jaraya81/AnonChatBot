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

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class NEXT extends Action implements IAction {

    public static final String CODE = "⏩ Next!";
    public static final String CODE_ALT = "⏩";

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
        next(message.getFromId().longValue());
    }

    public void next(long userId) throws TelegramException {
        User myUser = serviceChat.getUserByIdUser(userId);
        serviceChat.saveUser(myUser);
        if (User.exist(myUser) && User.isPlayed(myUser)) {
            boolean isOK;
            do {
                isOK = true;
                skippedChats(myUser);
                Chat chat = serviceChat.assignNewChat(myUser.getIdUser());
                if (chat != null) {
                    User user2 = serviceChat.getUserByIdUser(chat.getUser2());
                    SendResponse sendResponse1 = bot.execute(new SendMessage(
                            myUser.getIdUser(),
                            msg.msg(Msg.USER_NEXT_OK, myUser.getLang(), elvis(myUser.getDescription(), ""), elvis(user2.getDescription(), "")))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    if (isInactive(sendResponse1, myUser.getIdUser())) {
                        isOK = false;
                    }
                    SendResponse sendResponse2 = bot.execute(new SendMessage(
                            user2.getIdUser(),
                            msg.msg(Msg.USER_NEXT_OK, user2.getLang(), elvis(user2.getDescription(), ""), elvis(myUser.getDescription(), "")))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    log.info("{} :: {} {} -> {} {}", CODE, myUser.getIdUser(), sendResponse1.isOk(), user2.getIdUser(), sendResponse2.isOk());
                    if (isInactive(sendResponse2, user2.getIdUser())) {
                        isOK = false;
                    }
                } else {
                    SendResponse sendResponse = bot.execute(new SendMessage(myUser.getIdUser(), msg.msg(Msg.USER_NEXT_WAITING, myUser.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    logResult(CODE, myUser.getIdUser(), sendResponse.isOk());
                }

            } while (!isOK);
        }
    }

    private void skippedChats(User myUser) throws TelegramException {
        List<Chat> chats = serviceChat.getChatsByIdUserAndState(myUser.getIdUser(), ChatState.ACTIVE);
        for (Chat chat : chats) {
            skippedChat(chat, myUser.getIdUser());
        }
    }

    private void skippedChat(Chat chat, long myUserId) throws TelegramException {
        chat.setState(ChatState.SKIPPED.name());
        serviceChat.saveChat(chat);
        User otherUser = serviceChat.getUserByIdUser(chat.getUser1().compareTo(myUserId) != 0 ? chat.getUser1() : chat.getUser2());
        isInactive(bot.execute(new SendMessage(otherUser.getIdUser(), msg.msg(Msg.NEXT_YOU, otherUser.getLang()))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)), otherUser.getIdUser());
    }

    private boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && (message.getText().contentEquals(CODE) || message.getText().contentEquals(CODE_ALT));
    }

}
