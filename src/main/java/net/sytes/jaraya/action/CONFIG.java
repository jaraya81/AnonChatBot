package net.sytes.jaraya.action;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class CONFIG extends Action implements IAction {
    public static final String CODE = "Config";

    public CONFIG(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(MessageChat message) throws TelegramException {
        if (check(message)) {
            config(message);
        }
        return this;
    }

    public static boolean check(MessageChat message) {
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void config(MessageChat message) throws TelegramException {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user)) {
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.CONFIG, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.config()));
            logResult(CODE, message.getChatId(), sendResponse.isOk());
        }
    }
}
