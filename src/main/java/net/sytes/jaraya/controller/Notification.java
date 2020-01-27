package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.ActionHelper;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.state.State;
import spark.Request;
import spark.Response;
import spark.Route;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class Notification implements Route {

    private TelegramBot bot;
    private ServiceChat serviceChat = new ServiceChat();
    private MsgProcess msg = new MsgProcess();
    private ActionHelper helperBot = new ActionHelper(serviceChat);

    public Notification(TelegramBot bot) throws TelegramException {
        this.bot = bot;
    }

    @Override
    public Object handle(Request request, Response response) throws TelegramException {
        if (request == null) {
            return "NOK";
        }
        String msg = new String(Base64.getDecoder().decode(request.queryParams("msg")), StandardCharsets.UTF_8);
        String title = request.queryParams("title");
        String lang = request.queryParams("lang");

        serviceChat.getUserRepo().getAllByLang(lang).parallelStream()
                .filter(x -> !x.getState().contentEquals(State.BANNED.name()) && !x.getState().contentEquals(State.STOP.name()))
                .forEach(x -> {
                    try {
                        helperBot.isInactive(bot.execute(new SendMessage(x.getIdUser(), (title != null && !title.isEmpty()
                                ? ("<b>" + title + "</b>" + "\n\n")
                                : "")
                                + msg)
                                .parseMode(ParseMode.HTML)), x.getIdUser());
                    } catch (TelegramException e) {
                        log.error("", e);
                    }
                });
        return msg;
    }
}
