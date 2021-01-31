package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.button.PlayButton;
import net.sytes.jaraya.action.message.command.StartCommand;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class ForceBio extends SuperAction implements IAction {

    public ForceBio(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        forceBio(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        if (Objects.nonNull(message)
                && ((Objects.nonNull(message.getText())
                && !message.getText().contentEquals(StartCommand.CODE))
                || (message.getPhoto() != null))
        ) {
            User user = services.user.getByIdUser(message.getFromId().longValue());
            return user != null && (user.getState().contentEquals(State.EMPTY_BIO.name()) || user.getDescriptionText() == null || user.getDescriptionText().isEmpty());
        }
        return false;
    }

    public void forceBio(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        String text = message.getCaption() != null ? message.getCaption() : message.getText();
        forceBio(user, message.getPhoto(), text);
    }

    public void forceBio(User user, String idPhoto, String text) {
        if (User.exist(user) && User.isEmptyBio(user)) {
            text = text != null && !text.isEmpty() ? text : msg.anyDescription(user.getLang());
            user.setDescription(idPhoto, text.length() <= 240 ?
                    text
                    : text.substring(0, 239));
            user = services.user.save(user);
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(),
                    msg.msg(Msg.SET_BIO_OK, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true));
            logResult(Msg.SET_BIO_OK.name(), user.getIdUser(), sendResponse.isOk());
            if (user.getDescriptionPhoto() != null && !user.getDescriptionPhoto().isEmpty()) {
                SendResponse sendResponse2 = bot.execute(new SendPhoto(user.getIdUser(), user.getDescriptionPhoto())
                        .parseMode(ParseMode.MarkdownV2)
                        .caption(user.getDescriptionText())
                        .disableNotification(false));
                logResult(Msg.SET_BIO_OK.name(), user.getIdUser(), sendResponse2.isOk());
            } else {
                SendResponse sendResponse2 = bot.execute(new SendMessage(user.getIdUser(),
                        user.getDescriptionText())
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(false)
                        .disableNotification(true));
                logResult(Msg.SET_BIO_OK.name(), user.getIdUser(), sendResponse2.isOk());
            }
            new PlayButton(bot, services, msg, userAdmin).play(user);
        } else if (User.exist(user) && !User.isBanned(user) && user.getDescriptionText() == null || user.getDescriptionText().isEmpty()) {
            sendMessage(user);
        }

    }

    public void sendMessage(User user) {
        if (!User.isEmptyBio(user) || user.getDescriptionText() == null || user.getDescriptionText().isEmpty()) {
            user.setState(State.EMPTY_BIO.name());
            user.setDescription("");
            user = services.user.save(user);
        }

        SendResponse sendResponse2 = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.EMPTY_BIO, user.getLang()))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .disableNotification(false)
                .replyMarkup(keyboard.getByUserStatus(user))
        );
        logResult(Msg.EMPTY_BIO.name(), user.getIdUser(), sendResponse2.isOk());
    }
}
