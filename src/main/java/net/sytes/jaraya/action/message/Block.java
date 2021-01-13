package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
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
public class Block extends Action implements IAction {

    public static final String CODE = "✖ Block";

    public Block(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
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
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().contentEquals(CODE);
    }

    private void block(MessageChat message) {
        User user1 = services.user.getByIdUser(message.getFromId().longValue());
        if (User.exist(user1) && !User.isBanned(user1) && User.isPlayed(user1)) {
            List<Chat> chats = services.chat.getByIdUserAndState(user1.getIdUser(), ChatState.ACTIVE);
            if (!chats.isEmpty()) {
                Chat chat = chats.get(0);
                chat.setState(ChatState.BLOCKED.name());
                services.chat.save(chat);
                SendResponse sendResponse = bot.execute(new SendMessage(user1.getIdUser(), msg.msg(Msg.USER_BLOCK, user1.getLang()))
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true));

                logResult(CODE, message.getChatId(), sendResponse.isOk());
            }
        }
    }


}
