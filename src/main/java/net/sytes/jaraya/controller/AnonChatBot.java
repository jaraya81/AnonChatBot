package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.*;
import net.sytes.jaraya.component.ActionHelper;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.CoreException;
import net.sytes.jaraya.properties.Properties;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.vo.MessageChat;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AnonChatBot implements Route {

    private String token;
    private TelegramBot bot;
    private List<Action> actions = new ArrayList<>();

    public AnonChatBot() throws CoreException {

        ServiceChat serviceChat = new ServiceChat();
        MsgProcess msg = new MsgProcess();
        ActionHelper actionHelper = new ActionHelper(serviceChat);

        token = Properties.get(Property.TOKEN_BOT.name());
        log.info("TOKEN: ..." + token.substring(0, 5));
        bot = new TelegramBot.Builder(token).build();
        actions.add(START.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(PLAY.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(PAUSE.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(NEXT.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(BLOCK.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(REPORT.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(CONFIG.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(BIO.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(LANG.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());
        actions.add(ABOUT.builder().bot(bot).msg(msg).serviceChat(serviceChat).build());

        actions.add(CHAT.builder().bot(bot).msg(msg).serviceChat(serviceChat).actionHelper(actionHelper).build());
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Update update = BotUtils.parseUpdate(request.body());
        if (update == null) {
            return "UPDATE NOK";
        }
        MessageChat message = MessageChat.to(update.message());

        if (message == null) {
            return "NULL PARSING MESSAGE";
        }
        log.info("message in " + message.getMessageId());
        for (Action action : actions) {
            action.exec(message);
        }
        return "";

    }

    public TelegramBot getBot() {
        return bot;
    }

    public String getToken() {
        return token;
    }
}
