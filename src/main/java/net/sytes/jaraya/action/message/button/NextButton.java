package net.sytes.jaraya.action.message.button;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class NextButton extends SuperAction implements IAction {

    private final PeriodicalTasks periodicalTasks;

    public NextButton(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        User me = services.user.getByIdUser(message.getFromId().longValue());
        return Objects.nonNull(message.getText())
                && message.getText().contentEquals(msg.commandButton(Msg.NEXT, me.getLang()));
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        next(message);
        return this;
    }

    private void next(MessageChat message) {
        User me = services.user.get(message);
        me = services.user.save(me);
        next(me, message.getChatId(), message.getMessageId());
    }

    public void next(User me) {
        next(me, null, null);
    }

    public void next(User me, Long chatId, Integer messageId) {
        if (User.exist(me) && User.isPlayed(me)) {
            if (chatId != null && messageId != null) {
                bot.execute(new DeleteMessage(chatId, messageId));
            }
            boolean isOK;
            do {
                isOK = true;
                skippedChats(me);
                User other = selectNewNext(me);
                Chat chat = assignNew(me, other);
                if (chat != null) {
                    List<Tag> commonsTags = commonsTags(me, other);
                    SendResponse sendResponse2 = bot.execute(new SendMessage(
                            other.getIdUser(),
                            msg.msg(Msg.USER_NEXT_OK,
                                    other.getLang(),
                                    formatReverseTags(commonsTags, other.getLang())))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(true));
                    periodicalTasks.addDeleteMessage(sendResponse2);
                    if (isInactive(sendResponse2, other.getIdUser())) {
                        chat.setState(ChatState.SKIPPED.name());
                        services.chat.save(chat);
                        isOK = false;
                    } else {
                        sendYouMyBio(me, other);
                        SendResponse sendResponse1 = bot.execute(new SendMessage(
                                me.getIdUser(),
                                msg.msg(Msg.USER_NEXT_OK, me.getLang(),
                                        formatTags(commonsTags, me.getLang())))
                                .parseMode(ParseMode.HTML)
                                .disableWebPagePreview(false)
                                .disableNotification(true)
                                .replyMarkup(keyboard.getByUserStatus(me))
                        );
                        periodicalTasks.addDeleteMessage(sendResponse1);
                        if (isInactive(sendResponse1, me.getIdUser())) {
                            chat.setState(ChatState.SKIPPED.name());
                            services.chat.save(chat);
                            break;
                        }
                        sendYouMyBio(other, me);
                        log.info("{} :: {} {} -> {} {}", Msg.NEXT.name(), me.getIdUser(), sendResponse1.isOk(), other.getIdUser(), sendResponse2.isOk());
                    }
                } else {
                    SendResponse sendResponse = bot.execute(new SendMessage(me.getIdUser(), msg.msg(Msg.USER_NEXT_WAITING, me.getLang()))
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(false)
                            .disableNotification(true));
                    logResult(Msg.USER_NEXT_WAITING.name(), me.getIdUser(), sendResponse.isOk());
                    periodicalTasks.addDeleteMessage(sendResponse);
                }

            } while (!isOK);
        }
    }

    private String formatReverseTags(List<Tag> commonsTags, String lang) {
        return commonsTags.stream()
                .map(x -> "#" + msg.reverseTag(x, lang)).collect(Collectors.joining(", "));
    }

    private String formatTags(List<Tag> commonsTags, String lang) {
        return commonsTags.stream()
                .map(x -> "#" + msg.tag(x, lang)).collect(Collectors.joining(", "));
    }

    private List<Tag> commonsTags(User me, User other) {
        List<String> meUTags = services.tag.getByUserId(me)
                .stream()
                .map(UserTag::getTag)
                .collect(Collectors.toList());
        if (meUTags.isEmpty()) {
            services.tag.add(me, Tag.GENERAL.name());
            meUTags.add(Tag.GENERAL.name());
        }
        List<String> otherUTags = services.tag.getByUserId(other)
                .stream()
                .map(UserTag::getTag)
                .collect(Collectors.toList());
        if (otherUTags.isEmpty()) {
            otherUTags.add(Tag.GENERAL.reverse());
        }
        List<Tag> meTags = meUTags.parallelStream()
                .map(x -> Arrays.stream(Tag.values())
                        .filter(y -> y.name().contentEquals(x)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Tag> otherTags = otherUTags.parallelStream()
                .map(x -> Arrays.stream(Tag.values())
                        .filter(y -> y.name().contentEquals(x)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return otherTags
                .parallelStream()
                .filter(x -> meTags.parallelStream()
                        .anyMatch(y -> y.name().contentEquals(x.reverse())))
                .collect(Collectors.toList());
    }

    public User selectNewNext(User me) {
        List<User> users = services.user.getByState(State.PLAY);
        List<User> preFilter = users.parallelStream()
                .sorted(Comparator.comparing(User::getDateupdate).reversed())
                .filter(user -> isAsignable(me, user))
                .filter(user -> matchTags(me, user))
                .collect(Collectors.toList());
        List<User> filter = preFilter.stream()
                .filter(user -> equalsLang(me, user))
                .filter(user -> !isRepetido(me, user))
                .collect(Collectors.toList());
        if (!filter.isEmpty()) {
            return filter.get(0);
        }
        filter = preFilter.stream()
                .filter(user -> !isRepetido(me, user))
                .collect(Collectors.toList());
        if (!filter.isEmpty()) {
            return filter.get(0);
        }
        return null;
        //Collections.shuffle(preFilter);
        //return preFilter.isEmpty() ? null : preFilter.get(0);
    }

    private boolean equalsLang(User me, User user) {
        if (me.getLang() == null || user.getLang() == null) return true;
        return me.getLang().contentEquals(user.getLang());
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
        SendResponse sendResponse = bot.execute(new SendMessage(otherUser.getIdUser(), msg.msg(Msg.NEXT_YOU, otherUser.getLang(),
                msg.commandButton(Msg.NEXT, otherUser.getLang()), msg.commandButton(Msg.NEXT, otherUser.getLang())))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true));
        isInactive(sendResponse, otherUser.getIdUser());
        periodicalTasks.addDeleteMessage(sendResponse);

    }


}
