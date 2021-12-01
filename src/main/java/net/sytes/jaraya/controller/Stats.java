package net.sytes.jaraya.controller;

import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.dto.StatsDto;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class Stats extends SuperAction implements Route {

    private PeriodicalTasks periodicalTasks;

    public Stats(TelegramBot bot, Long userAdmin, PeriodicalTasks periodicalTasks) throws TelegramException {
        super(bot, new AnonChatService(), new MsgProcess(), userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    private enum TypeStat {
        COUNT, USERS, INCORPORATIONS;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info(request.url());

        String type = request.queryParams("type");
        response.type("application/json");
        if (type == null || type.isEmpty()) {
            return "type is required";
        }
        if (type.contentEquals(TypeStat.COUNT.name())) {
            return count(request);
        }
        if (type.contentEquals(TypeStat.USERS.name())) {
            return users(request);
        }
        if (type.contentEquals(TypeStat.INCORPORATIONS.name())) {
            return incorportarions(request);
        }
        return "{}";

    }

    private Object incorportarions(Request request) {
        String lang = request.queryParams("lang");
        String days = request.queryParams("days");
        return new GsonBuilder().setPrettyPrinting().create().toJson(services.user.incorporation(lang, days));
    }


    private Object users(Request request) {
        String lang = request.queryParams("lang");
        List<User> users;
        if (lang == null) {
            users = services.user.getAll();
        } else {
            users = services.user.getByLang(lang);
        }
        String state = request.queryParams("state");
        if (state != null && !state.isEmpty()) {
            users = users.stream()
                    .filter(x -> x.getState().contentEquals(state))
                    .collect(Collectors.toList());
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(
                users.stream().collect(Collectors.groupingBy(User::getState))
        );
    }

    private Object count(Request request) {
        List<StatsDto> result = services.user.countByState(request.queryParams("lang"))
                .entrySet().stream()
                .map(entry -> StatsDto.builder()
                        .key(UUID.randomUUID().toString())
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(result);
    }
}
