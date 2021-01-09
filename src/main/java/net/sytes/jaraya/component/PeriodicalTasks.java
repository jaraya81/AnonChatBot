package net.sytes.jaraya.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.NEXT;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class PeriodicalTasks {
    private static final String MSG_LOG = "{} :: {}";
    private final TelegramBot bot;
    private final ServiceChat serviceChat;
    private final MsgProcess msg;
    private final Long userAdmin;

    public PeriodicalTasks(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg, Long userAdmin) {
        this.bot = bot;
        this.serviceChat = serviceChat;
        this.msg = msg;
        this.userAdmin = userAdmin;
    }


    public void exec() {
        log.info("Starting periodicals jobs");
        try {
            cleanChat();
            deleteOldsSkips();
            pauseUsersInactive();
            reminderInactiveUsers();
            // autoNext();
        } catch (TelegramException e) {
            log.error("", e);
        }
    }

    private void autoNext() {
        NEXT next = new NEXT(bot, serviceChat, msg, userAdmin);

        List<User> users = serviceChat.getUsersByState(State.PLAY);
        Collections.shuffle(users);
        for (User user : users.subList(0, users.size() > 2 ? 1 : 0)) {
            if (serviceChat.getChatsByIdUserAndState(user.getIdUser(), ChatState.ACTIVE).isEmpty()) {
                log.info("AutoNext {}", user.getIdUser());
                try {
                    next.next(user.getIdUser());
                } catch (TelegramException e) {
                    log.error("", e);
                }
            }
        }

    }

    private void deleteOldsSkips() throws TelegramException {
        List<Chat> chats = serviceChat.getChatsByStatusMinusMinute(ChatState.SKIPPED, 60 * 6);
        log.info(MSG_LOG, "deleteOldsSkips", chats.size());
        serviceChat.deleteChats(chats);
    }

    private void reminderInactiveUsers() throws TelegramException {
        List<User> users = serviceChat.getByInactiveUsers(State.PAUSE, 60 * 24);
        for (User user : users) {
            log.info(MSG_LOG, Msg.REMINDER_PAUSED_USER.name(), user.getIdUser());
            serviceChat.saveUser(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.REMINDER_PAUSED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
        }

    }

    private void cleanChat() throws TelegramException {
        Set<Long> ids = serviceChat.cleanerChat();
        for (Long id : ids) {
            User user = serviceChat.getUserByIdUser(id);
            log.info(MSG_LOG, Msg.CHAT_TIMEOUT.name(), user.getIdUser());
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false));
        }
    }


    private void pauseUsersInactive() throws TelegramException {
        List<User> users = serviceChat.getByInactiveUsers(State.PLAY, 60 * 24 * 7);
        for (User user : users) {
            log.info(MSG_LOG, Msg.INACTIVITY_USER.name(), user.getIdUser());
            user.setState(State.PAUSE.name());
            serviceChat.saveUser(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.INACTIVITY_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
        }
    }

}
