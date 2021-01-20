package net.sytes.jaraya.controller;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.service.AnonChatService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

@Slf4j
public class Notification extends SuperAction implements Route {

    private PeriodicalTasks periodicalTasks;

    public Notification(TelegramBot bot, Long userAdmin, PeriodicalTasks periodicalTasks) throws TelegramException {
        super(bot, new AnonChatService(), new MsgProcess(), userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public Object handle(Request request, Response response) {
        if (request == null) {
            return "NOK";
        }
        Map params = new Gson().fromJson(request.body(), Map.class);
        return periodicalTasks.addNotification(params);
    }
}
