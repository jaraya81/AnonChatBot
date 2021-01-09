package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;

@Slf4j
public class Action {
    private static final String FORBIDDEN_BLOCKED = "Forbidden: bot was blocked by the user";
    private static final String FORBIDDEN_DEACTIVATED = "Forbidden: user is deactivated";

    protected ServiceChat serviceChat;
    protected TelegramBot bot;
    protected MsgProcess msg;
    protected long userAdmin;

    protected Action(TelegramBot bot,
                     ServiceChat serviceChat,
                     MsgProcess msg,
                     Long userAdmin) {
        this.serviceChat = serviceChat;
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

    public boolean isInactive(SendResponse response, Long id) throws TelegramException {
        if (!response.isOk() && response.description() != null) {
            log.info("NOK " + response.description());
            if (response.description().contentEquals(FORBIDDEN_BLOCKED) ||
                    response.description().contentEquals(FORBIDDEN_DEACTIVATED)) {
                User user = serviceChat.getUserByIdUser(id);
                user.setState(State.STOP.name());
                log.info("STOP :: {} :: {}", user.getIdUser(), serviceChat.saveUser(user));
                return true;
            }
        }
        return false;
    }

}
