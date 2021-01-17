package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class Bio extends SuperAction implements IAction {

    public static final String CODE = "/bio";

    public Bio(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        bio(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && (message.getText().contentEquals(CODE)
                || message.getText().startsWith(CODE));
    }

    private void bio(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user)) {
            if (message.getText().contentEquals(CODE)) {
                String mensaje = msg.msg(Msg.BIO,
                        user.getLang(),
                        msg.commandButton(Msg.CONFIG, user.getLang()),
                        user.getDescription());
                log.info(mensaje);
                SendResponse sendResponse = bot.execute(new SendMessage(
                        user.getIdUser(), mensaje)
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                logResult(CODE, message.getChatId(), sendResponse.isOk());
            } else if (message.getText().startsWith(CODE)) {
                String bio = message.getText().replace(CODE, "");
                user.setDescription(bio.length() <= 240 ? bio : bio.substring(0, 239));
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(),
                        msg.msg(Msg.SET_BIO_OK, user.getLang(), user.getDescription()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                services.user.save(user);
                logResult(CODE, message.getChatId(), sendResponse.isOk());
            }


        }

    }

}
