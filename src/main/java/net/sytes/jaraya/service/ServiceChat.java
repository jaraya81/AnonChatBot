package net.sytes.jaraya.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.Report;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.ChatRepo;
import net.sytes.jaraya.repo.ReportRepo;
import net.sytes.jaraya.repo.UserRepo;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ServiceChat {

    private final ChatRepo chatRepo = new ChatRepo();
    private final UserRepo userRepo = new UserRepo();
    private final ReportRepo reportRepo = new ReportRepo();


    public ServiceChat() throws TelegramException {
        // only for exception
    }

    public void deleteChats(List<Chat> chats) {
        chatRepo.delete(chats);
    }

    public List<Chat> getChatsByStatusMinusMinute(ChatState chatState, int minutes) throws TelegramException {
        return chatRepo.getByStatusAndMinusMinute(chatState, minutes);
    }

    public List<User> getUsersByState(State state) {
        return userRepo.getByState(state);
    }

    public List<User> getUsersByLang(String lang) throws TelegramException {
        return userRepo.getByLang(lang);
    }

    public List<Chat> getChatsByIdUserAndState(Long user, ChatState state) {
        return chatRepo.getByIdUserAndState(user, state);
    }

    public Integer saveUser(User user) throws TelegramException {
        return userRepo.save(user);
    }

    public Integer saveChat(Chat user) throws TelegramException {
        return chatRepo.save(user);
    }

    public User getUserByIdUser(Long idUser) throws TelegramException {
        return userRepo.getByIdUser(idUser);
    }

    public void reportUser(Long userId) throws TelegramException {
        reportRepo.save(Report.builder()
                .user(userId)
                .build());
        if (reportRepo.getByIdUser(userId).size() > 5) {
            User user = getUserByIdUser(userId);
            if (user != null) {
                user.setState(State.BANNED.name());
                userRepo.save(user);
            }
        }
    }

    public Chat assignNewChat(Long idUser) throws TelegramException {
        List<User> users = userRepo.getByState(State.PLAY);
        List<User> ids = users.parallelStream()
                .filter(User::isPlayed)
                .sorted(Comparator.comparing(User::getDateupdate).reversed())
                .filter(user -> !isRepetido(user.getIdUser(), idUser))
                .filter(user -> isAsignable(user.getIdUser(), idUser))
                .collect(Collectors.toList());
        log.info("{}/{}", ids.size(), users.size());
        if (ids.isEmpty()) {
            ids = users.parallelStream()
                    .filter(User::isPlayed)
                    .filter(user -> isAsignable(user.getIdUser(), idUser))
                    .collect(Collectors.toList());
            log.info("{}/{}", ids.size(), users.size());
            Collections.shuffle(ids);
        }
        if (!ids.isEmpty()) {
            Chat chat = Chat.builder()
                    .user1(idUser)
                    .user2(ids.get(0).getIdUser())
                    .state(ChatState.ACTIVE.name())
                    .build();
            chatRepo.save(chat);
            log.info("{}", chat);
            return chat;
        }
        return null;
    }

    private boolean isRepetido(long idUser, long yourId) {
        if (idUser == yourId) {
            return false;
        }
        return chatRepo.getByIdUser(idUser).stream().anyMatch(x -> x.getUser1() == yourId || x.getUser2() == yourId);

    }

    private boolean isAsignable(long idUser, long yourId) {
        if (idUser == yourId) {
            return false;
        }
        List<Chat> chats = chatRepo.getByIdUser(idUser);
        if (chats.parallelStream()
                .filter(x -> x.getState().contentEquals(ChatState.BLOCKED.name()) ||
                        x.getState().contentEquals(ChatState.REPORT.name()))
                .anyMatch(x -> x.getUser1() == yourId || x.getUser2() == yourId)) {
            return false;
        }

        return chats.parallelStream().noneMatch(x -> x.getState().contentEquals(ChatState.ACTIVE.name()));
    }

    public Set<Long> cleanerChat() throws TelegramException {
        List<Chat> chats = chatRepo.getByStatusAndMinusMinute(ChatState.ACTIVE, 4);
        for (Chat chat : chats) {
            chat.setState(ChatState.SKIPPED.name());
            chatRepo.save(chat);
        }
        Set<Long> ids = chats.stream().map(Chat::getUser1).collect(Collectors.toSet());
        ids.addAll(chats.stream().map(Chat::getUser2).collect(Collectors.toSet()));
        return ids;
    }

    public List<User> getByInactiveUsers(State state, int minutes) throws TelegramException {
        return userRepo.getByInactiveMinutes(minutes)
                .parallelStream()
                .filter(x -> x.getState().contentEquals(state.name()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
         public List<User> getByLangAndActivesUsers(String lang, Integer minutes) {
        return userRepo.getByLang(lang)
                .parallelStream()
                .filter(x -> x.getDateupdate().toLocalDateTime().plusMinutes(minutes).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

    }
}
