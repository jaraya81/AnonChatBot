package net.sytes.jaraya.service;

import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.ChatRepo;
import net.sytes.jaraya.state.ChatState;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ChatService {

    private final ChatRepo chatRepo = new ChatRepo();

    public ChatService() throws TelegramException {
// exception
    }

    public void deletes(List<Chat> chats) {
        chatRepo.delete(chats);
    }

    public List<Chat> getByStatusMinusMinute(ChatState chatState, int minutes) {
        return chatRepo.getByStatusAndMinusMinute(chatState, minutes);
    }

    public List<Chat> getByIdUserAndState(Long user, ChatState state) {
        return chatRepo.getByIdUserAndState(user, state);
    }

    public Integer save(Chat user) {
        return chatRepo.save(user);
    }


    public List<Chat> getByIdUser(User user) {
        return chatRepo.getByIdUser(user.getIdUser());
    }


    public Set<Long> cleaner() {
        List<Chat> chats = chatRepo.getByStatusAndMinusMinute(ChatState.ACTIVE, 7);
        for (Chat chat : chats) {
            chat.setState(ChatState.SKIPPED.name());
            chatRepo.save(chat);
        }
        Set<Long> ids = chats.stream().map(Chat::getUser1).collect(Collectors.toSet());
        ids.addAll(chats.stream().map(Chat::getUser2).collect(Collectors.toSet()));

        List<Chat> almostNewChats = chatRepo.getByStatusAndMinusMinute(ChatState.ACTIVE, 5)
                .parallelStream()
                .filter(x -> x.getDatecreation().toLocalDateTime().plusMinutes(4).isAfter(x.getDateupdate().toLocalDateTime()))
                .collect(Collectors.toList());
        for (Chat chat : almostNewChats) {
            chat.setState(ChatState.SKIPPED.name());
            chatRepo.save(chat);
        }
        ids.addAll(almostNewChats.stream().map(Chat::getUser1).collect(Collectors.toSet()));
        ids.addAll(almostNewChats.stream().map(Chat::getUser2).collect(Collectors.toSet()));
        return ids;
    }

}
