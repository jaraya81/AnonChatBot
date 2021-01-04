package net.sytes.jaraya.controller;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.Action;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.service.ServiceChat;
import net.sytes.jaraya.state.State;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class Notification extends Action implements Route {

    public Notification(TelegramBot bot, Long userAdmin) throws TelegramException {
        super(bot, new ServiceChat(), null, userAdmin);
    }

    @Override
    public Object handle(Request request, Response response) throws TelegramException {
        if (request == null) {
            return "NOK";
        }
        Map params = new Gson().fromJson(request.body(), Map.class);
        Boolean test = (Boolean) params.get("test");
        String lang = (String) params.get("lang");
        log.info(lang);
        String type = (String) params.get("type");
        log.info(type);

        String msg = (String) params.get("msg");
        log.info(msg);

        String text = String.format("<pre>%s</pre>%n%n%s", elvis(type, "Broadcast Message"), msg);
        if (elvis(test, true)) {
                bot.execute(new SendMessage(userAdmin, "T:" + text)
                        .parseMode(ParseMode.HTML));
        } else {
            serviceChat.getUserRepo().getAllByLang(lang).parallelStream()
                    .filter(x -> !x.getState().contentEquals(State.BANNED.name()) && !x.getState().contentEquals(State.STOP.name()))
                    .forEach(x -> {
                        try {
                            isInactive(bot.execute(new SendMessage(x.getIdUser(), text)
                                    .parseMode(ParseMode.HTML)), x.getIdUser());
                        } catch (TelegramException e) {
                            log.error("", e);
                        }
                    });
        }
        return text;
    }
}
