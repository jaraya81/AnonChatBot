package net.sytes.jaraya.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.ForceBio;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.exception.UtilException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Properties;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class PeriodicalTasks extends SuperAction {
    private static final String MSG_LOG = "{} :: {}";

    private final Map<LocalDateTime, Map> notifications = new HashMap<>();
    private final Map<String, net.sytes.jaraya.dto.DeleteMessage> deleteMessages = new HashMap<>();

    private Map<String, String> parameters;

    public PeriodicalTasks(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) throws UtilException {
        super(bot, serviceChat, msg, userAdmin);
        parameters = Properties.gets();
    }

    public void exec() {
        try {
            cleanChat();
            deleteOldsSkips();
            pauseUsersInactive();
            reminderInactiveUsers();
            updateEmptyBio();
//            expirePremium();
//            renoveAll();
            removeSuspension();
            sendNotification();
        } catch (TelegramException e) {
            log.error("", e);
        }
    }


    private void deleteMessages() {
        List<Map.Entry<String, net.sytes.jaraya.dto.DeleteMessage>> running = deleteMessages.entrySet()
                .parallelStream()
                .filter(x -> x.getValue().getTime().plusHours(3).isBefore(LocalDateTime.now(ZoneId.systemDefault())))
                .collect(Collectors.toList());
        for (Map.Entry<String, net.sytes.jaraya.dto.DeleteMessage> entry : running) {
            bot.execute(
                    new DeleteMessage(
                            entry.getValue().getChatId(),
                            entry.getValue().getMessageId()
                    )
            );
            deleteMessages.remove(entry.getKey());
        }
    }

    private void sendNotification() {
        List<Map.Entry<LocalDateTime, Map>> running = notifications.entrySet()
                .stream()
                .filter(x -> x.getKey().isBefore(LocalDateTime.now(ZoneId.systemDefault())))
                .collect(Collectors.toList());
        if (!running.isEmpty()) {
            log.info("Notifications pending: {}", running.size());
            deleteMessages();
        }
        for (Map.Entry<LocalDateTime, Map> entry : running) {
            notifications.remove(entry.getKey());
            Map params = entry.getValue();
            Boolean test = (Boolean) params.get("test");
            String lang = (String) params.get("lang");
            String type = (String) params.get("type");
            String msg = (String) params.get("msg");
            Double lastMinutes = (Double) params.get("last_minutes");
            Map<String, String> buttons = (Map<String, String>) params.get("buttons");

            String text = (type != null ? String.format("<b>%s</b>%n%n", type) : "") + msg;

            List<User> userList = new ArrayList<>();
            if (elvis(test, true)) {
                User user = services.user.getByIdUser(userAdmin);
                SendResponse response = bot.execute(new SendMessage(user.getIdUser(), "T:\n" + text)
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(buttons != null && !buttons.isEmpty()
                                ? keyboard.getInlineKeyboardUrls(buttons)
                                : keyboard.getByUserStatus(user))
                );
                if (!response.isOk()) {
                    log.error(response.description());
                }
                addDeleteMessage(response);
            } else {
                if (lastMinutes == null) {
                    userList = services.user.getByLang(lang);
                } else {
                    userList = services.user.getByLangAndActives(lang, lastMinutes.intValue());
                }
                userList = userList.parallelStream()
                        .filter(x -> !x.getState().contentEquals(State.BANNED.name()))
                        .filter(x -> !x.getState().contentEquals(State.STOP.name()))
                        .collect(Collectors.toList());

                userList.forEach(x -> {
                    SendResponse sendResponse = bot.execute(new SendMessage(x.getIdUser(), text)
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(buttons != null
                                    ? keyboard.getInlineKeyboardUrls(buttons)
                                    : keyboard.getByUserStatus(x)));
                    isInactive(sendResponse, x.getIdUser());
                    addDeleteMessage(sendResponse);

                });
            }
            log.info("Notifications sending: {}", userList.size());
        }
    }

    private void removeSuspension() {
        services.user.getByInactives(State.BANNED, 60)
                .parallelStream()
                .filter(User::isPremium)
                .forEach(user -> {
                    user.setState(State.STOP.name());
                    user = services.user.save(user);
                    log.info(MSG_LOG, "removeSuspension", user.getIdUser());
                });
    }

    private void renoveAll() {
        //log.info("renoveAll");
        services.user.getAll().parallelStream()
            //    .peek(user -> log.info("{} :: {} :: {} :: {}", user, LocalDateTime.now(ZoneId.systemDefault()), user.getDatePremium().toLocalDateTime(), LocalDateTime.now(ZoneId.systemDefault()).isAfter(user.getDatePremium().toLocalDateTime())))
                .filter(user -> LocalDateTime.now(ZoneId.systemDefault()).isAfter(user.getDatePremium().toLocalDateTime()))
                .forEach(user -> {
                    user.setPremium(PremiumType.TEMPORAL.name());
                    user.setDatePremium(User.calcDatePremium(PremiumType.TEMPORAL, new Date()));
                    services.user.save(user);
                });
    }

    private void expirePremium() {
        services.user.getByState(State.PLAY)
                .parallelStream()
                .filter(x -> PremiumType.valueOf(x.getPremiumType()) != PremiumType.NO)
                .filter(x -> !x.isPremium())
                .forEach(user -> {
                    user.setPremium(PremiumType.NO.name());
                    user = services.user.save(user);
                    bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.PREMIUM_EXPIRED, user.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(false)
                    );
                    log.info(MSG_LOG, Msg.PREMIUM_EXPIRED.name(), user.getIdUser());
                });
    }

    private void updateEmptyBio() {
        ForceBio forceBio = new ForceBio(bot, services, msg, userAdmin, this);
        List<User> users = services.user.getByInactives(State.EMPTY_BIO, 2);
        for (User user : users) {
            log.info(MSG_LOG, "updateEmptyBio", user.getIdUser());
            forceBio.forceBio(user, null, msg.takeADescription(user.getLang(), user.getIdUser()));
        }
    }

    private void deleteOldsSkips() {
        services.chat.deletes(
                services.chat.getByStatusMinusMinute(ChatState.SKIPPED, 60)
        );
    }

    private void reminderInactiveUsers() {
        List<User> users = services.user.getByInactives(State.PAUSE, 720);
        for (User user : users) {
            user = services.user.save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.REMINDER_PAUSED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(keyboard.getByUserStatus(user)));
            log.info(MSG_LOG, Msg.REMINDER_PAUSED_USER.name(), user.getIdUser());
        }
    }

    private void cleanChat() throws TelegramException {
        Set<Long> ids = services.chat.cleaner();
        for (Long id : ids) {
            User user = services.user.getByIdUser(id);
            log.info(MSG_LOG, Msg.CHAT_TIMEOUT.name(), user.getIdUser());
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang(),
                    msg.commandButton(Msg.NEXT, user.getLang())))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false));
        }
    }


    private void pauseUsersInactive() {
        String pauseMinutes = parameters.get(Property.PAUSE_USERS_INACTIVE.name());
        pauseMinutes = elvis(pauseMinutes, "1000");
        List<User> users = services.user.getByInactives(
                State.PLAY,
                Integer.parseInt(pauseMinutes)
        );
        for (User user : users) {
            log.info(MSG_LOG, Msg.INACTIVITY_USER.name(), user.getIdUser());
            user.setState(State.PAUSE.name());
            user = services.user.save(user);
            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.INACTIVITY_USER, user.getLang(),
                    msg.commandButton(Msg.PLAY, user.getLang())))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(false)
                    .replyMarkup(keyboard.getByUserStatus(user)));
        }
    }

    public String addNotification(Map params) {
        notifications.put(LocalDateTime.now(ZoneId.systemDefault()), params);
        return String.format("%s", notifications.size());
    }

    public void addDeleteMessage(long chatId, int messageId) {
        deleteMessages.put(UUID.randomUUID().toString(), net.sytes.jaraya.dto.DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .time(LocalDateTime.now(ZoneId.systemDefault()))
                .build());
    }

    public void addDeleteMessage(BaseUpdate update) {
        if (update instanceof MessageChat) {
            MessageChat messageChat = (MessageChat) update;
            if (messageChat.getChatId() != null && messageChat.getMessageId() != null) {
                addDeleteMessage(((MessageChat) update).getChatId(), ((MessageChat) update).getMessageId());
            }
        }
    }

    public void addDeleteMessage(SendResponse response) {
        if (response != null && response.isOk() && response.message() != null &&
                response.message().chat() != null && response.message().chat().id() != null &&
                response.message().messageId() != null) {
            addDeleteMessage(response.message().chat().id(), response.message().messageId());
        }
    }
}
