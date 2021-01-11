package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;

@Slf4j
public class Action {
    private static final String FORBIDDEN_BLOCKED = "Forbidden: bot was blocked by the user";
    private static final String FORBIDDEN_DEACTIVATED = "Forbidden: user is deactivated";

    protected AnonChatService services;
    protected TelegramBot bot;
    protected MsgProcess msg;
    protected long userAdmin;

    protected Action(TelegramBot bot,
                     AnonChatService serviceChat,
                     MsgProcess msg,
                     Long userAdmin) {
        this.services = serviceChat;
        this.bot = bot;
        this.msg = msg;
        this.userAdmin = userAdmin;
    }

    protected void logResult(String code, Long id, boolean isOK) {
        log.info("{} :: {} :: {}", code, id, isOK);
    }

    public Long getUserAdmin() {
        return userAdmin;
    }

    public boolean isInactive(SendResponse response, Long id) {
        if (!response.isOk() && response.description() != null) {
            log.info("NOK " + response.description());
            if (response.description().contentEquals(FORBIDDEN_BLOCKED) ||
                    response.description().contentEquals(FORBIDDEN_DEACTIVATED)) {
                User user = services.user.getByIdUser(id);
                user.setState(State.STOP.name());
                log.info("STOP :: {} :: {}", user.getIdUser(), services.user.save(user));
                return true;
            }
        }
        return false;
    }

    protected boolean isPremium(User me, String action) {
        if (me.isPremium()) {
            return true;
        } else {
            bot.execute(new SendMessage(me.getIdUser(),
                    msg.msg(Msg.PREMIUM_REQUIRED, me.getLang(), action))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false));
            return false;
        }
    }
}
