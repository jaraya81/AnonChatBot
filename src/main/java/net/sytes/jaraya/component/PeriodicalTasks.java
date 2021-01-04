package net.sytes.jaraya.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;

import java.util.List;
import java.util.Set;

@Slf4j
public class PeriodicalTasks {
    private final TelegramBot bot;
    private final ServiceChat serviceChat;
    private final MsgProcess msg;

    public PeriodicalTasks(TelegramBot bot, ServiceChat serviceChat, MsgProcess msg) {
        this.bot = bot;
        this.serviceChat = serviceChat;
        this.msg = msg;
    }


    public void exec() {
        log.info("Starting periodicals jobs");
        try {
            cleanChat();
            pauseUsersInactive();
            reminderInactiveUsers();
        } catch (TelegramException e) {
            e.printStackTrace();
        }
    }

    private void reminderInactiveUsers() throws TelegramException {
        List<User> users = serviceChat.getUsersInactive(State.PAUSE, 60 * 24);
        for (User user : users) {
            log.info("{} :: {}", Msg.REMINDER_PAUSED_USER.code(), user.getIdUser());
            serviceChat.getUserRepo().save(user);
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
            User user = serviceChat.getUserRepo().getByIdUser(id);
            log.info("{} :: {}", Msg.CHAT_TIMEOUT.code(), user.getIdUser());
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false));
        }
    }


    private void pauseUsersInactive() throws TelegramException {
        List<User> users = serviceChat.getUsersInactive(State.PLAY, 60 * 24 * 7);
        for (User user : users) {
            log.info("{} :: {}", Msg.INACTIVITY_USER.code(), user.getIdUser());
            user.setState(State.PAUSE.name());
            serviceChat.getUserRepo().save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.INACTIVITY_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
        }
    }

}
