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

import java.sql.Timestamp;
import java.util.Date;

@Slf4j
public class RegisterPremiumCommand extends SuperAction implements IAction {
    public static final String CODE = "/premium";

    private final PeriodicalTasks periodicalTasks;

    public RegisterPremiumCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
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
        if (message != null
                && message.getText() != null
                && message.getText().startsWith(CODE)) {
            User user = services.user.getByIdUser(message.getFromId().longValue());
            return user != null && userAdmin == user.getIdUser();
        }
        return false;
    }

    private void action(MessageChat message) {
        User userA = services.user.getByIdUser(message.getFromId().longValue());
        if (userA != null && userAdmin == userA.getIdUser()) {
            String[] params = message.getText().split(" ");
            if (params.length == 3) {
                User user = services.user.getByIdUser(Long.valueOf(params[1]));
                if (user != null) {
                    PremiumType type = PremiumType.valueOf(params[2]);
                    user.setPremium(type.name());
                    user.setDatePremium(new Timestamp(new Date().getTime()));
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
}