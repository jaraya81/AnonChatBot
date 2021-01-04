package net.sytes.jaraya.component;

import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;

@Slf4j
public class ActionHelper {

    private static final String FORBIDDEN_BLOCKED = "Forbidden: bot was blocked by the user";
    private static final String FORBIDDEN_DEACTIVATED = "Forbidden: user is deactivated";

    private final ServiceChat serviceChat;
    private final Long userAdmin;

    public ActionHelper(ServiceChat serviceChat, Long userAdmin) {
        this.userAdmin = userAdmin;
        this.serviceChat = serviceChat;
    }

    public boolean isInactive(SendResponse response, Long id) throws TelegramException {
        if (!response.isOk() && response.description() != null) {
            log.info("NOK " + response.description());
            if (response.description().contentEquals(FORBIDDEN_BLOCKED) ||
                    response.description().contentEquals(FORBIDDEN_DEACTIVATED)) {
                log.info("STOP :: " + id);
                User user = serviceChat.getUserRepo().getByIdUser(id);
                user.setState(State.STOP.name());
                serviceChat.getUserRepo().save(user);
                return true;
            }
        }
        return false;
    }

    public Long getUserAdmin() {
        return userAdmin;
    }
}
