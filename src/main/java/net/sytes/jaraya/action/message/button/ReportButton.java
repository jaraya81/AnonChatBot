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
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ReportButton extends SuperAction implements IAction {

    private final PeriodicalTasks periodicalTasks;

    public ReportButton(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        report(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User user = services.user.getByIdUser(message.getFromId().longValue());
        return Objects.nonNull(message.getText())
                && message.getText().contentEquals(msg.commandButton(Msg.REPORT, user.getLang()));
    }

    private void report(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User me = services.user.getByIdUser(message.getFromId().longValue());
        bot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
        if (User.exist(me) && !User.isBanned(me) && User.isPlayed(me)) {
            List<Chat> chats = services.chat.getByIdUserAndState(me.getIdUser(), ChatState.ACTIVE);
            if (chats.isEmpty()) {
                return;
            }
            chats.get(0).setState(ChatState.REPORT.name());
            services.chat.save(chats.get(0));

            Long otherUser = chats.get(0).getUser1().compareTo(me.getIdUser()) != 0 ? chats.get(0).getUser1() : chats.get(0).getUser2();

            services.report.report(otherUser);
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.USER_REPORT, me.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getByUserStatus(me))
            );
            logResult(Msg.REPORT.name(), message.getChatId(), sendResponse.isOk());
            periodicalTasks.addDeleteMessage(sendResponse);
        }

    }
}

