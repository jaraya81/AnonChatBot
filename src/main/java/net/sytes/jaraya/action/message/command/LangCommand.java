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
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class LangCommand extends SuperAction implements IAction {
    public static final String CODE = "/lang";
    public static final String SET_CODE = "/lang ";


    public LangCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
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
                && (message.getText().contentEquals(CODE) || message.getText().startsWith(SET_CODE));
    }

    private void bio(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());

        if (User.exist(user) && !User.isBanned(user)) {
            if (message.getText().startsWith(SET_CODE)) {
                String langUser = message.getText().replace(SET_CODE, "");
                String lang = msg.langOrDefault(langUser.toLowerCase());
                user.setLang(lang);
                user = services.user.save(user);
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.SET_LANG_OK, user.getLang())
                        + "<i>" + user.getLang() + "</i>")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                logResult(CODE, message.getChatId(), sendResponse.isOk());
            } else if (message.getText().contentEquals(CODE)) {
                SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.LANG, user.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));
                logResult(CODE, message.getChatId(), sendResponse.isOk());
            }

        }
    }
}
