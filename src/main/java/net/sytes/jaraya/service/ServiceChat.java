package net.sytes.jaraya.service;

import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.Report;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.ChatRepo;
import net.sytes.jaraya.repo.ReportRepo;
import net.sytes.jaraya.repo.UserRepo;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ServiceChat {

    private static final String DEFAULT_LANG = MsgProcess.ES;

    private ChatRepo chatRepo = new ChatRepo();
    private UserRepo userRepo = new UserRepo();
    private ReportRepo reportRepo = new ReportRepo();


    public ServiceChat() throws TelegramException {
    }

    public List<Chat> find(Long user) {
        return chatRepo.getByIdUser(user);
    }

    public UserRepo getUserRepo() {
        return userRepo;
    }

    public ChatRepo getChatRepo() {
        return chatRepo;
    }

    public void addReport(Long userId) throws TelegramException {
        reportRepo.save(Report.builder()
                .user(userId)
                .build());
        if (reportRepo.getByIdUser(userId).size() > 5) {
            User user = userRepo.getByIdUser(userId);
            if (user != null) {
                user.setState(State.BANNED.name());
                userRepo.save(user);
            }
        }
    }

    public Chat assignNewChat(Long idUser, String lang) throws TelegramException {
        List<User> users = userRepo.getAllByLang(lang == null || lang.isEmpty() ? DEFAULT_LANG : lang);
        List<Long> ids = users.parallelStream()
                .filter(User::isPlayed)
                .filter(x -> x.getIdUser().compareTo(idUser) != 0)
                .filter(x -> isAsignable(x.getIdUser(), idUser))
                .map(User::getIdUser)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            // Collections.shuffle(ids);
            Chat chat = Chat.builder()
                    .user1(idUser)
                    .user2(ids.get(0))
                    .state(ChatState.ACTIVE.name())
                    .build();
            chatRepo.save(chat);
            return chat;
        } else {
            List<Long> reintentos = users.parallelStream()
                    .filter(User::isPlayed)
                    .filter(x -> x.getIdUser().compareTo(idUser) != 0)
                    .filter(x -> isReintentable(x.getIdUser(), idUser))
                    .map(User::getIdUser)
                    .collect(Collectors.toList());
            if (!reintentos.isEmpty()) {
                Collections.shuffle(reintentos);
                Chat chat = Chat.builder()
                        .user1(idUser)
                        .user2(reintentos.get(0))
                        .state(ChatState.ACTIVE.name())
                        .build();
                chatRepo.save(chat);
                return chat;
            } else {
                return null;
            }
        }

    }

    private boolean isReintentable(Long idUser, Long yourId) {
        List<Chat> chats = chatRepo.getByIdUser(idUser);

        boolean isBlocked = chats
                .parallelStream()
                .filter(x -> x.getState().contentEquals(ChatState.BLOCKED.name()))
                .anyMatch(x -> (x.getUser1().compareTo(idUser) == 0 && x.getUser2().compareTo(yourId) == 0)
                        || (x.getUser1().compareTo(yourId) == 0 && x.getUser2().compareTo(idUser) == 0));
        if (isBlocked) {
            return false;
        }

        boolean isActive = chats
                .parallelStream()
                .anyMatch(x -> x.getState().contentEquals(ChatState.ACTIVE.name()));
        return !isActive;

    }

    private boolean isAsignable(Long idUser, Long yourId) {
        List<Chat> chats = chatRepo.getByIdUser(idUser);
        boolean isActive = chats
                .parallelStream()
                .anyMatch(x -> x.getState().contentEquals(ChatState.ACTIVE.name()));
        if (isActive) {
            return false;
        }

        boolean isSkipped = chats
                .parallelStream()
                .anyMatch(x -> (x.getUser1().compareTo(idUser) == 0 && x.getUser2().compareTo(yourId) == 0)
                        || (x.getUser1().compareTo(yourId) == 0 && x.getUser2().compareTo(idUser) == 0));
        return !isSkipped;
    }

    public Set<Long> cleanerChat() throws TelegramException {
        List<Chat> chats = chatRepo.getAllStatusMinusMinute(ChatState.ACTIVE, 5);
        for (Chat chat : chats) {
            chat.setState(ChatState.SKIPPED.name());
            chatRepo.save(chat);
        }
        Set<Long> ids = chats.stream().map(Chat::getUser1).collect(Collectors.toSet());
        ids.addAll(chats.stream().map(Chat::getUser2).collect(Collectors.toSet()));
        return ids;
    }

    public List<User> getUsersInactive() throws TelegramException {
        return getUsersInactive(State.PLAY, 60 * 12);
    }

    public List<User> getUsersInactive(State state, int minutes) throws TelegramException {
        return userRepo.getAllInactiveMinutes(minutes)
                .parallelStream()
                .filter(x -> x.getState().contentEquals(state.name()))
                .collect(Collectors.toList());
    }

}
