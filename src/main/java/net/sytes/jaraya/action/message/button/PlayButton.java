package net.sytes.jaraya.action.message.button;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
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
public class PlayButton extends SuperAction implements IAction {

    public static final String COMMAND = "/play";

    public PlayButton(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        play(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (user == null) {
            new StartCommand(bot, services, msg, userAdmin).start(message);
            user = services.user.getByIdUser(message.getFromId().longValue());
        }
        return Objects.nonNull(message.getText())
                && (message.getText().contentEquals(msg.commandButton(Msg.PLAY, user.getLang()))
                || message.getText().toLowerCase().contentEquals(COMMAND));
    }

    private void play(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        play(user, message.getChatId(), message.getMessageId());
    }

    public void play(User user) {
        play(user, null, null);
    }

    public void play(User user, Long chatId, Integer messageId) {
        if (User.exist(user) && !User.isBanned(user) && !User.isPlayed(user)) {
            user.setState(State.PLAY.name());
            user = services.user.save(user);
            if (chatId != null && messageId != null) {
                bot.execute(new DeleteMessage(chatId, messageId));
            }
            SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.USER_PLAY, user.getLang(),
                    msg.commandButton(Msg.NEXT, user.getLang())))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getByUserStatus(user))
            );
            logResult(Msg.USER_PLAY.name(), user.getIdUser(), sendResponse.isOk());
            new NextButton(bot, services, msg, userAdmin).next(user);
        }
    }
}
