package net.sytes.jaraya.action.message.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class AdminCommand extends SuperAction implements IAction {
    public static final String CODE = "/admin";
    PeriodicalTasks periodicalTasks;

    public AdminCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    public AdminCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        action(message);
        return this;
    }

    private void action(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (user != null && userAdmin == user.getIdUser()) {
            String[] params = message.getText().split(" ");
            if (params.length >= 2) {
                switch (params[1]) {
                    case "stats":
                        stats(user, params);
                        break;
                    case "notification":
                        notification(user, params);
                        break;
                    default:
                        help(user);
                }

            } else if (params.length == 1) {
                help(user);
            }
        }

    }

    private void notification(User user, String[] params) {
        Map<String, String> p = Arrays.stream(params).filter(x -> x.contains("="))
                .collect(Collectors.toMap(x -> x.split("=")[0], x -> x.split("=")[1]));
        Map<String, String> buttons = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("test", p.get("test") != null ? Boolean.parseBoolean(p.get("test")) : null);
        map.put("lang", p.get("lang") != null ? p.get("lang") : null);
        map.put("type", p.get("type") != null ? p.get("type") : null);
        map.put("msg", p.get("msg") != null ? p.get("msg") : null);
        map.put("last_minutes", p.get("last_minutes") != null ? p.get("last_minutes") : null);
        map.put("buttons", buttons);

        SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), periodicalTasks.addNotification(map))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyMarkup(keyboard.getByUserStatus(user)));
        logResult(Msg.STATS_COUNT.name(), user.getIdUser(), sendResponse.isOk());
    }

    private void stats(User user, String[] params) {
        if (params.length >= 3) {
            if (params[2].contentEquals("count")) {
                StringBuilder sb = new StringBuilder();
                services.user.countByState(params.length >= 4
                        ? params[3]
                        : null).forEach((x, y) -> sb.append(String.format("<pre>%s: %s%n</pre>", x, y)));

                SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.STATS_COUNT, user.getLang(), sb.toString()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyMarkup(keyboard.getByUserStatus(user)));
                logResult(Msg.STATS_COUNT.name(), user.getIdUser(), sendResponse.isOk());

            } else if (params[2].contentEquals("inc")) {
                StringBuilder sb = new StringBuilder();
                services.user.incorporation(params.length >= 4
                                ? params[3]
                                : null,
                        params.length >= 5
                                ? params[4]
                                : null
                ).forEach((x, y) -> sb.append(String.format("<pre>%s: %s%n</pre>", x, y)));

                SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.STATS_INC, user.getLang(), sb.toString()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyMarkup(keyboard.getByUserStatus(user)));
                logResult(Msg.STATS_INC.name(), user.getIdUser(), sendResponse.isOk());
            } else {
                help(user);
            }
        } else {
            help(user);
        }
    }

    private void help(User user) {
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().startsWith(CODE);
    }
}
