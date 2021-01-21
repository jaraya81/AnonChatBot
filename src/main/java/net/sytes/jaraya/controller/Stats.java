package net.sytes.jaraya.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.UserRepo;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class Stats implements Route {

    private final UserRepo userRepo = new UserRepo();

    public Stats() throws TelegramException {
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
        List<User> users;
        if (lang == null) {
            users = userRepo.getAll();
        } else {
            users = userRepo.getByLang(lang);
        }
        String days = request.queryParams("days");
        Map<String, Long> result = users.stream()
                .filter(x -> x
                        .getDatecreation()
                        .toLocalDateTime()
                        .toLocalDate().isAfter(
                                LocalDate.now().minusDays(days != null
                                        ? Long.parseLong(days)
                                        : 7L)
                        )
                )
                .collect(Collectors
                        .groupingBy(x -> to(x
                                .getDatecreation()
                                .toLocalDateTime()
                                .toLocalDate()), Collectors.counting()));
        return new GsonBuilder().setPrettyPrinting().create().toJson(new TreeMap<>(result));
    }

    private static String to(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    private Object users(Request request) {
        String lang = request.queryParams("lang");
        List<User> users;
        if (lang == null) {
            users = userRepo.getAll();
        } else {
            users = userRepo.getByLang(lang);
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
        List<User> users;
        String lang = request.queryParams("lang");
        if (lang == null) {
            users = userRepo.getAll();
        } else {
            users = userRepo.getByLang(lang);
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(
                users.stream().collect(Collectors.groupingBy(User::getState, Collectors.counting()))
        );
    }
}
