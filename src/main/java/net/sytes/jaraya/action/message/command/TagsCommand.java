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
        User user = services.user.getByIdUser(message.getFromId().longValue());
        action(user);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().startsWith(CODE);
    }

    public void action(User user) {
        if (User.exist(user) && !User.isBanned(user)) {
            long size = services.user.getByState(State.PLAY).size();
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(),
                    msg.msg(Msg.TAGS_PREFERENCES, user.getLang(), String.valueOf(size)))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getInlineKeyboardPref(services.tag.getByUserId(user), user.getLang()))
            );
            logResult(Msg.TAGS_PREFERENCES.name(), user.getIdUser(), sendResponse.isOk());
        }
    }

}
