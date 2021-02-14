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
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Slf4j
public class PremiumCommand extends SuperAction implements IAction {
    public static final String CODE = "/premium";

    private final PeriodicalTasks periodicalTasks;

    public PremiumCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        action(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return message != null
                && message.getText() != null
                && message.getText().startsWith(CODE);
    }

    private void action(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (user != null) {
            if (message.getText().startsWith(CODE + "_")) {
                showBankOptions(user, message);
            } else {
                if (userAdmin == user.getIdUser() && !message.getText().contentEquals(CODE)) {
                    register(message);
                } else {
                    if (user.isPremium()) {
                        userPremiumMessage(user);
                    } else {
                        showOptionsPremium(user);
                    }
                }
            }
        }
    }

    private void showBankOptions(User user, MessageChat message) {
        String value = message.getText().replace(CODE + "_", "");
        String[] params = value.split("_");
        log.info("{}|{}", value, Arrays.asList(params));
        if (params[0].matches("[\\d]+") && Long.parseLong(params[0]) == user.getIdUser()) {
            if (params.length >= 2) {
                PremiumType type = Arrays.stream(PremiumType.values()).filter(x -> x.name().contentEquals(params[1])).findFirst().orElse(null);
                if (type != null) {
                    SendResponse sendResponseAdmin = bot.execute(
                            new SendMessage(user.getIdUser(), msg.msg(Msg.PREMIUM_PETITION, user.getLang(),
                                    String.format("RandomNextBot %s %s", type, params[0])
                            ))
                                    .parseMode(ParseMode.HTML)
                                    .disableWebPagePreview(true)
                                    .disableNotification(true));
                    logResult(Msg.PREMIUM_PETITION.name(), user.getIdUser(), sendResponseAdmin.isOk());
                }
            }
        }
    }

    private void showOptionsPremium(User user) {

        SendResponse sendResponseAdmin = bot.execute(
                new SendMessage(user.getIdUser(), msg.msg(Msg.PREMIUM_OPTIONS, user.getLang(),
                        String.format("%s_%s_%s", CODE, user.getIdUser(), PremiumType.MONTHLY.name()),
                        String.format("%s_%s_%s", CODE, user.getIdUser(), PremiumType.ANNUAL.name()),
                        String.format("%s_%s_%s", CODE, user.getIdUser(), PremiumType.PERMANENT.name())
                ))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
        logResult(Msg.PREMIUM_OPTIONS.name(), user.getIdUser(), sendResponseAdmin.isOk());
    }

    private void userPremiumMessage(User user) {
        SendResponse sendResponseAdmin = bot.execute(
                new SendMessage(user.getIdUser(), msg.msg(Msg.PREMIUM_HAS, user.getLang(),
                        user.getIdUser(), expiration(user)))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
        logResult(Msg.PREMIUM_HAS.name(), user.getIdUser(), sendResponseAdmin.isOk());

    }

    private LocalDateTime expiration(User user) {
        if (user != null && user.isPremium()) {
            return user.getDatePremium().toLocalDateTime();
        }
        return LocalDateTime.now(ZoneId.systemDefault());
    }

    private void register(MessageChat message) {
        String[] params = message.getText().split(" ");
        if (params.length == 3) {
            User user = services.user.getByIdUser(Long.valueOf(params[1]));
            if (user != null) {
                PremiumType type = PremiumType.valueOf(params[2]);
                user.setPremium(type.name());
                user.setDatePremium(User.calcDatePremium(type, user.getDatePremium()));
                user = services.user.save(user);
                SendResponse sendResponseAdmin = bot.execute(
                        new SendMessage(userAdmin, msg.msg(Msg.PREMIUM_REGISTER_ADMIN, user.getLang(),
                                user.getIdUser(), user.getPremiumType()))
                                .parseMode(ParseMode.HTML)
                                .disableWebPagePreview(true)
                                .disableNotification(true));
                logResult(Msg.PREMIUM_REGISTER_ADMIN.name(), userAdmin, sendResponseAdmin.isOk());
                SendResponse sendResponseUser = bot.execute(new SendMessage(
                        user.getIdUser(), msg.msg(Msg.PREMIUM_REGISTER_USER, user.getLang(),
                        user.getIdUser(), user.getPremiumType()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                logResult(Msg.PREMIUM_REGISTER_USER.name(), user.getIdUser(), sendResponseUser.isOk());
            }
        }

    }
}