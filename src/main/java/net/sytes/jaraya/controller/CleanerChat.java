package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.util.Keyboard;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Set;

@Slf4j
@Deprecated
public class CleanerChat implements Route {

    private TelegramBot bot;
    private ServiceChat serviceChat = new ServiceChat();
    private MsgProcess msg = new MsgProcess();

    public CleanerChat(TelegramBot bot) throws TelegramException {
        this.bot = bot;
    }

    @Override
    public Object handle(Request request, Response response) throws TelegramException {
        if (request == null) {
            return "NOK";
        }
        Set<Long> ids = serviceChat.cleanerChat();
        for (Long id : ids) {
            User user = serviceChat.getUserRepo().getByIdUser(id);

            bot.execute(new SendMessage(user.getIdUser(), msg.msg(Msg.CHAT_TIMEOUT, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(Keyboard.play()));
        }
        return "CLEAN " + ids.size();
    }
}
