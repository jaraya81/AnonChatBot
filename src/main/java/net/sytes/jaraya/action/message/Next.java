package net.sytes.jaraya.action.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.model.UserTag;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Next extends Action implements IAction {

    public static final String CODE = "⏩ Next!";
    public static final String CODE_ALT = "⏩";

    public Next(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
    }


    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        next(message);
        return this;
    }

    private void next(MessageChat message) {
        User me = services.user.getByIdUser(message.getFromId().longValue());
        services.user.save(me);
        next(me);
    }

    public void next(User me) {
        if (User.exist(me) && User.isPlayed(me)) {
            boolean isOK;
            do {
                isOK = true;
                skippedChats(me);
                User other = selectNewNext(me);
                Chat chat = assignNew(me, other);
                if (chat != null) {
                    List<String> commonsTags = commonsTags(me, other);
                    SendResponse sendResponse1 = bot.execute(new SendMessage(
                            me.getIdUser(),
                            msg.msg(Msg.USER_NEXT_OK, me.getLang(),
                                    me.isPremium() ? me.getDescription() + " \uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F"
                                            : me.getDescription(),
                                    other.isPremium() ? other.getDescription() + " \uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F"
                                            : other.getDescription(),
                                    formatTags(commonsTags)))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    if (isInactive(sendResponse1, me.getIdUser())) {
                        break;
                    }
                    SendResponse sendResponse2 = bot.execute(new SendMessage(
                            other.getIdUser(),
                            msg.msg(Msg.USER_NEXT_OK,
                                    other.getLang(),
                                    other.isPremium() ? other.getDescription() + " \uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F"
                                            : other.getDescription(),
                                    me.isPremium() ? me.getDescription() + " \uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F"
                                            : me.getDescription(),
                                    formatTags(commonsTags)))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    log.info("{} :: {} {} -> {} {}", CODE, me.getIdUser(), sendResponse1.isOk(), other.getIdUser(), sendResponse2.isOk());
                    if (isInactive(sendResponse2, other.getIdUser())) {
                        isOK = false;
                    }
                } else {
                    SendResponse sendResponse = bot.execute(new SendMessage(me.getIdUser(), msg.msg(Msg.USER_NEXT_WAITING, me.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(false));
                    logResult(Msg.USER_NEXT_WAITING.name(), me.getIdUser(), sendResponse.isOk());
                }

            } while (!isOK);
        }
    }

    private String formatTags(List<String> commonsTags) {
        return commonsTags.stream()
                .map(x -> "#" + x).collect(Collectors.joining(" "));
    }

    private List<String> commonsTags(User me, User other) {
        List<String> meTags = services.tag.getByUserId(me.getIdUser())
                .stream()
                .map(UserTag::getTag)
                .collect(Collectors.toList());
        if (meTags.isEmpty()) {
            services.tag.add(me, Tag.GENERAL.name());
            meTags.add(Tag.GENERAL.name());
        }
        List<String> otherTags = services.tag.getByUserId(other.getIdUser())
                .stream()
                .map(UserTag::getTag)
                .collect(Collectors.toList());
        if (otherTags.isEmpty()) {
            otherTags.add(Tag.GENERAL.reverse());
        }
        return otherTags
                .parallelStream()
                .filter(x -> meTags.parallelStream()
                        .anyMatch(y -> y.contentEquals(x)))
                .collect(Collectors.toList());
    }

    public User selectNewNext(User me) {
        List<User> users = services.user.getByState(State.PLAY);
        List<User> filter = users.parallelStream()
                .filter(User::isPlayed)
                .sorted(Comparator.comparing(User::getDateupdate).reversed())
                .filter(user -> !isRepetido(me, user))
                .filter(user -> isAsignable(me, user))
                .filter(user -> matchTags(me, user))
                .collect(Collectors.toList());
        log.info("{}/{}", filter.size(), users.size());
        if (filter.isEmpty()) {
            filter = users.parallelStream()
                    .filter(User::isPlayed)
                    .filter(user -> isAsignable(me, user))
                    .filter(user -> matchTags(me, user))
                    .collect(Collectors.toList());
            log.info("{}/{}", filter.size(), users.size());
            Collections.shuffle(filter);
        }
        return filter.isEmpty() ? null : filter.get(0);
    }

    public Chat assignNew(User me, User other) {
        if (me == null || other == null) {
            return null;
        }
        Chat chat = Chat.builder()
                .user1(me.getIdUser())
                .user2(other.getIdUser())
                .state(ChatState.ACTIVE.name())
                .build();
        services.chat.save(chat);
        return chat;
    }

    private boolean matchTags(User me, User other) {
        return !commonsTags(me, other).isEmpty();
    }


    private boolean isRepetido(User me, User other) {
        if (me.getIdUser().longValue() == other.getIdUser()) {
            return false;
        }
        return services.chat.getByIdUser(other).stream().anyMatch(x ->
                x.getUser1() == me.getIdUser().longValue() || x.getUser2() == me.getIdUser().longValue());

    }

    private boolean isAsignable(User me, User other) {
        if (me.getIdUser().longValue() == other.getIdUser()) {
            return false;
        }
        List<Chat> chats = services.chat.getByIdUser(other);
        if (chats.parallelStream()
                .filter(x -> x.getState().contentEquals(ChatState.BLOCKED.name()) ||
                        x.getState().contentEquals(ChatState.REPORT.name()))
                .anyMatch(x -> x.getUser1() == me.getIdUser().longValue() || x.getUser2() == me.getIdUser().longValue())) {
            return false;
        }

        return chats.parallelStream().noneMatch(x -> x.getState().contentEquals(ChatState.ACTIVE.name()));
    }


    private void skippedChats(User myUser) {
        List<Chat> chats = services.chat.getByIdUserAndState(myUser.getIdUser(), ChatState.ACTIVE);
        for (Chat chat : chats) {
            skippedChat(chat, myUser.getIdUser());
        }
    }

    private void skippedChat(Chat chat, long myUserId) {
        chat.setState(ChatState.SKIPPED.name());
        services.chat.save(chat);
        User otherUser = services.user.getByIdUser(chat.getUser1().compareTo(myUserId) != 0 ? chat.getUser1() : chat.getUser2());
        isInactive(bot.execute(new SendMessage(otherUser.getIdUser(), msg.msg(Msg.NEXT_YOU, otherUser.getLang()))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)), otherUser.getIdUser());
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && (message.getText().contentEquals(CODE) || message.getText().contentEquals(CODE_ALT));
    }

}