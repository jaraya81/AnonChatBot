package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;

@Slf4j
public class SuperAction {
    private static final String FORBIDDEN_BLOCKED = "Forbidden: bot was blocked by the user";
    private static final String FORBIDDEN_DEACTIVATED = "Forbidden: user is deactivated";

    protected AnonChatService services;
    protected TelegramBot bot;
    protected MsgProcess msg;
    protected long userAdmin;
    protected final Keyboard keyboard;
    protected SuperAction(TelegramBot bot,
                          AnonChatService serviceChat,
                          MsgProcess msg,
                          Long userAdmin) {
        this.services = serviceChat;
        this.bot = bot;
        this.msg = msg;
        this.userAdmin = userAdmin;
        this.keyboard = new Keyboard(msg);
    }

    protected void sendYouMyBio(User userFrom, User userTo) {
        SendResponse sr;
        if (userFrom.getDescriptionPhoto() != null && !userFrom.getDescriptionPhoto().isEmpty()) {
            sr = bot.execute(new SendPhoto(userTo.getIdUser(), userFrom.getDescriptionPhoto())
                    .parseMode(ParseMode.MarkdownV2)
                    .caption(userFrom.isPremium() ? userFrom.bioPremium()
                            : userFrom.getDescriptionText())
                    .disableNotification(false));
        } else {
            sr = bot.execute(new SendMessage(userTo.getIdUser(),
                    userFrom.isPremium() ? userFrom.bioPremium()
                            : userFrom.getDescriptionText())
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true));
        }
        logResult("sendYouMyBio " + userFrom.getIdUser() + " -> " + userTo.getIdUser(), 0L, sr.isOk());
    }

    protected void sendMyBio(User user) {
        sendYouMyBio(user, user);
    }

    protected void logResult(String code, Long id, boolean isOK) {
        log.info("{} :: {} :: {}", code, id, isOK);
    }

    public Long getUserAdmin() {
        return userAdmin;
    }

    public boolean isInactive(SendResponse response, Long id) {
        if (!response.isOk() && response.description() != null) {
            log.error("NOK " + response.description());
            if (response.description().contentEquals(FORBIDDEN_BLOCKED) ||
                    response.description().contentEquals(FORBIDDEN_DEACTIVATED)) {
                User user = services.user.getByIdUser(id);
                user.setState(State.STOP.name());
                log.info("STOP :: {} :: {}", user.getIdUser(), services.user.save(user));
                return true;
            } else {
                log.error("{}", response);
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
