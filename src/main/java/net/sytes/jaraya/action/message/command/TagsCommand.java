package net.sytes.jaraya.action.message.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.util.Keyboard;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class TagsCommand extends SuperAction implements IAction {
    public static final String CODE = "/tags";

    public TagsCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
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
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().startsWith(CODE);
    }

    private void action(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user) && message.getText().startsWith(CODE)) {
            long size = services.user.getByState(State.PLAY).size();
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(),
                    msg.msg(Msg.TAGS_PREFERENCES, user.getLang(), String.valueOf(size)))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.getInlineKeyboardPref(services.tag.getByUserId(user), msg, user.getLang()))
            );
            logResult(Msg.TAGS_PREFERENCES.name(), message.getChatId(), sendResponse.isOk());
        }
    }

}
