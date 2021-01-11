package net.sytes.jaraya.controller;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.Action;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.State;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class Notification extends Action implements Route {

    public Notification(TelegramBot bot, Long userAdmin) throws TelegramException {
        super(bot, new AnonChatService(), null, userAdmin);
    }

    @Override
    public Object handle(Request request, Response response) throws TelegramException {
        if (request == null) {
            return "NOK";
        }
        Map params = new Gson().fromJson(request.body(), Map.class);
        Boolean test = (Boolean) params.get("test");
        String lang = (String) params.get("lang");
        String type = (String) params.get("type");
        String msg = (String) params.get("msg");
        Double lastMinutes = (Double) params.get("last_minutes");

        String text = String.format("<pre>%s</pre>%n%n%s", elvis(type, "Broadcast Message"), msg);

        List<User> userList = new ArrayList<>();
        if (elvis(test, true)) {
            bot.execute(new SendMessage(userAdmin, "T:" + text)
                    .parseMode(ParseMode.HTML));
        } else {
            if (lastMinutes == null) {
                userList = services.user.getByLang(lang);
            } else {
                userList = services.user.getByLangAndActives(lang, lastMinutes.intValue());
            }
            userList.parallelStream()
                    .filter(x -> !x.getState().contentEquals(State.BANNED.name()))
                    .filter(x -> !x.getState().contentEquals(State.STOP.name()))
                    .forEach(x -> isInactive(bot.execute(new SendMessage(x.getIdUser(), text)
                            .parseMode(ParseMode.HTML)), x.getIdUser()));
        }
        return userList.size();
    }
}
