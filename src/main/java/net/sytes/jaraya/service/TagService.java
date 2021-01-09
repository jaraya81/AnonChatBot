package net.sytes.jaraya.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.UserTag;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.repo.TagUserRepo;

import java.util.Arrays;
import java.util.List;

import static net.sytes.jaraya.util.Validation.check;

@Slf4j
public class TagService {

    private final TagUserRepo repo = new TagUserRepo();

    public TagService() throws TelegramException {
        // only for exception
    }

    public List<UserTag> getByUserId(long userId) {
        return repo.getByUserId(userId);
    }

    @SneakyThrows
    public void add(User user, String... tags) {
        check(user != null, "User is null");
        check(user.getIdUser() != null, "idUser is null");
        Arrays.stream(tags).forEach(tag -> repo.save(user.getIdUser(), tag));
    }

    public void removeAll(User user) {
        getByUserId(user.getIdUser())
                .parallelStream()
                .forEach(repo::delete);
    }
}
