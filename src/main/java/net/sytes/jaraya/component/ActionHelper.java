package net.sytes.jaraya.component;

import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;

@Slf4j
public class ActionHelper {

    private static final String DESCRIPTION_BLOCKED = "Forbidden: bot was blocked by the user";

    private ServiceChat serviceChat;

    public ActionHelper(ServiceChat serviceChat) {
        this.serviceChat = serviceChat;
    }

    public boolean isInactive(SendResponse response, Long id) throws TelegramException {
        if (!response.isOk() && response.description() != null) {
            log.info("NOK " + response.description());
            if (response.description().contentEquals(DESCRIPTION_BLOCKED)) {
                log.info("STOP :: " + id);
                User user = serviceChat.getUserRepo().getByIdUser(id);
                user.setState(State.STOP.name());
                serviceChat.getUserRepo().save(user);
                return true;
            }
        }
        return false;
    }
}
