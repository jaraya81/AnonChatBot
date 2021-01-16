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
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.List;
import java.util.Objects;

@Slf4j
public class BlockButton extends SuperAction implements IAction {

    public BlockButton(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        block(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User user1 = services.user.getByIdUser(message.getFromId().longValue());
        return Objects.nonNull(message.getText())
                && message.getText().contentEquals(msg.commandButton(Msg.BLOCK, user1.getLang()));
    }

    private void block(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user) && !User.isBanned(user) && User.isPlayed(user)) {
            List<Chat> chats = services.chat.getByIdUserAndState(user.getIdUser(), ChatState.ACTIVE);
            bot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                chat.setState(ChatState.BLOCKED.name());
                services.chat.save(chat);
                SendResponse sendResponse = bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.USER_BLOCK, user.getLang(),
                        msg.commandButton(Msg.NEXT, user.getLang())))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyMarkup(keyboard.getByUserStatus(user))
                );
                logResult(Msg.BLOCK.name(), message.getChatId(), sendResponse.isOk());
            }

        }
    }


}
