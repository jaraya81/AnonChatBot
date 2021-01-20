package net.sytes.jaraya.service;

import lombok.SneakyThrows;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.UserRepo;
import net.sytes.jaraya.state.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepo userRepo = new UserRepo();

    public UserService() throws TelegramException {
        // only for exception
    }

    public List<User> getByState(State state) {
        return userRepo.getByState(state);
    }

    public List<User> getByLang(String lang) throws TelegramException {
        return userRepo.getByLang(lang);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    @SneakyThrows
    public User getByIdUser(Long idUser) {
        return userRepo.getByIdUser(idUser);
    }

    public List<User> getByInactives(State state, int minutes) {
        return userRepo.getByInactiveMinutes(minutes)
                .parallelStream()
                .filter(x -> x.getState().contentEquals(state.name()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<User> getByLangAndActives(String lang, Integer minutes) {
        return userRepo.getByLang(lang)
                .parallelStream()
                .filter(x -> x.getDateupdate().toLocalDateTime().plusMinutes(minutes).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }
}
