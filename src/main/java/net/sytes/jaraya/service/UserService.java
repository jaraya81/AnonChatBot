package net.sytes.jaraya.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.UserRepo;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.vo.MessageChat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class UserService {

    private final UserRepo userRepo = new UserRepo();

    public UserService() throws TelegramException {
        // only for exception
    }

    public List<User> getByState(State state) {
        return userRepo.getByState(state);
    }

    public List<User> getByLang(String lang) {
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

    public List<User> getAll() {
        return userRepo.getAll();
    }

    public Map<String, Long> countByState(String lang) {
        List<User> users;
        if (lang == null) {
            users = userRepo.getAll();
        } else {
            users = userRepo.getByLang(lang);
        }
        return users.stream().collect(Collectors.groupingBy(User::getState, Collectors.counting()));

    }

    public Map<String, Long> incorporation(String lang, String days) {
        List<User> users;
        if (lang == null) {
            users = userRepo.getAll();
        } else {
            users = userRepo.getByLang(lang);
        }
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
        return new TreeMap<>(result);

    }

    private static String to(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public User get(MessageChat message) {
        if (message == null || message.getFromId() == null) {
            return null;
        }
        User user = getByIdUser(message.getFromId().longValue());
        boolean isUpdatable = true;
        if (user == null ||
                (user.getUsername() == null && message.getFromUsername() == null) ||
                (user.getUsername() != null && message.getFromUsername() != null &&
                        user.getUsername().contentEquals(message.getFromUsername()))) {
            isUpdatable = false;
        }
        if (isUpdatable) {
            log.info("Updating user {}", user.getIdUser());
            user.setUsername(message.getFromUsername());
            return save(user);
        }
        return user;
    }
}
