package net.sytes.jaraya.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.ForceBio;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
public class PeriodicalTasks {
    private static final String MSG_LOG = "{} :: {}";
    private final TelegramBot bot;
    private final AnonChatService serviceChat;
    private final MsgProcess msg;
    private final Long userAdmin;

    public PeriodicalTasks(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
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
            updateEmptyBio();
            expirePremium();
            removeSuspension();
        } catch (TelegramException e) {
            log.error("", e);
        }
    }

    private void removeSuspension() {
        serviceChat.user.getByInactives(State.BANNED, 60)
                .parallelStream()
                .filter(User::isPremium)
                .forEach(user -> {
                    user.setState(State.STOP.name());
                    serviceChat.user.save(user);
                    log.info(MSG_LOG, "removeSuspension", user.getIdUser());
                });
    }

    private void expirePremium() {
        serviceChat.user.getByState(State.PLAY)
                .parallelStream()
                .filter(User::isPremium)
                .filter(user -> {
                    if (user.getPremiumType().contentEquals(PremiumType.TEMPORAL.name())
                            && user.getDatePremium().toLocalDateTime().plusDays(10).isBefore(LocalDateTime.now())) {
                        return true;
                    } else
                        return user.getPremiumType().contentEquals(PremiumType.ANNUAL.name())
                                && user.getDatePremium().toLocalDateTime().plusYears(1).isBefore(LocalDateTime.now());
                })
                .forEach(user -> {
                    user.setPremium(PremiumType.NO.name());
                    serviceChat.user.save(user);
                    bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.PREMIUM_EXPIRED, user.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(false)
                    );
                    log.info(MSG_LOG, Msg.PREMIUM_EXPIRED.name(), user.getIdUser());
                });
    }

    private void updateEmptyBio() {
        ForceBio forceBio = new ForceBio(bot, serviceChat, msg, userAdmin);
        List<User> users = serviceChat.user.getByInactives(State.EMPTY_BIO, 2);
        for (User user : users) {
            log.info(MSG_LOG, "updateEmptyBio", user.getIdUser());
            forceBio.forceBio(user, msg.anyDescription(user.getLang()));
        }
    }

    private void deleteOldsSkips() {
        serviceChat.chat.deletes(
                serviceChat.chat.getByStatusMinusMinute(ChatState.SKIPPED, 60 * 6)
        );
    }

    private void reminderInactiveUsers() {
        List<User> users = serviceChat.user.getByInactives(State.PAUSE, 60 * 24);
        for (User user : users) {
            serviceChat.user.save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.REMINDER_PAUSED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
            log.info(MSG_LOG, Msg.REMINDER_PAUSED_USER.name(), user.getIdUser());
        }

    }

    private void cleanChat() throws TelegramException {
        Set<Long> ids = serviceChat.chat.cleaner();
        for (Long id : ids) {
            User user = serviceChat.user.getByIdUser(id);
            log.info(MSG_LOG, Msg.CHAT_TIMEOUT.name(), user.getIdUser());
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false));
        }
    }


    private void pauseUsersInactive() {
        List<User> users = serviceChat.user.getByInactives(State.PLAY, 60 * 24 * 7);
        for (User user : users) {
            log.info(MSG_LOG, Msg.INACTIVITY_USER.name(), user.getIdUser());
            user.setState(State.PAUSE.name());
            serviceChat.user.save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.INACTIVITY_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(Keyboard.pause()));
        }
    }

}
