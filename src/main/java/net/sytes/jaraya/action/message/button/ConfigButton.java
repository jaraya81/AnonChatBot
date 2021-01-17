package net.sytes.jaraya.action.message.button;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class ConfigButton extends SuperAction implements IAction {

    public ConfigButton(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        config(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User user = services.user.getByIdUser(message.getFromId().longValue());

        return Objects.nonNull(message.getText())
                && message.getText().contentEquals(msg.commandButton(Msg.CONFIG, user.getLang()));
    }

    private void config(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user)) {
            bot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.CONFIG_MSG, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getInlineKeyboardConfig(user.getLang()))
            );
            logResult(Msg.CONFIG.name(), message.getChatId(), sendResponse.isOk());
        }
    }
}
