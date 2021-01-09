package net.sytes.jaraya.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.dto.StatsDto;
import net.sytes.jaraya.dto.UsersDto;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.UserRepo;
import net.sytes.jaraya.state.State;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Stats implements Route {

    private final UserRepo userRepo = new UserRepo();

    public Stats() throws TelegramException {
    }


    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info(request.url());
        String lang = request.queryParams("lang");
        String type = request.queryParams("type");
        response.type("application/json");

        if (type == null || type.contentEquals("stats")) {
            List<User> users = userRepo.getByLang(lang);
            return new GsonBuilder().setPrettyPrinting().create().toJson(StatsDto.builder()
                    .active(users.parallelStream().filter(x -> x.getState().contentEquals(State.PLAY.name())).count())
                    .paused(users.parallelStream().filter(x -> x.getState().contentEquals(State.PAUSE.name())).count())
                    .banned(users.parallelStream().filter(x -> x.getState().contentEquals(State.BANNED.name())).count())
                    .total((long) users.size())
                    .build());
        } else if (type.contentEquals("users")) {
            String state = request.queryParams("state");
            List<User> users = userRepo.getByLang(lang)
                    .stream()
                    .filter(x -> state == null || x.getState().contentEquals(state)).collect(Collectors.toList());
            return new GsonBuilder().setPrettyPrinting().create().toJson(UsersDto.builder().size(users.size()).user(users).build());
        }
        return "{}";

    }
}
