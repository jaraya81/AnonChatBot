package net.sytes.jaraya.action.message.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.action.message.button.PlayButton;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.state.ChatState;
import net.sytes.jaraya.state.State;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.MessageChat;

import java.util.Objects;

@Slf4j
public class StartCommand extends SuperAction implements IAction {
    public static final String CODE = "/start";

    private final PeriodicalTasks periodicalTasks;

    public StartCommand(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        start(message);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        MessageChat message = (MessageChat) baseUpdate;
        return Objects.nonNull(message)
                && Objects.nonNull(message.getText())
                && message.getText().startsWith(CODE);
    }

    public void start(MessageChat message) {
        User user = services.user.getByIdUser(message.getFromId().longValue());
        if (user == null) {
            String lang = msg.langOrDefault(message.getLanguageCode());
            user = User.builder()
                    .idUser(message.getFromId().longValue())
                    .username(message.getFromUsername())
                    .state(State.EMPTY_BIO.name())
                    .premiumType(PremiumType.TEMPORAL.name())
                    .lang(lang)
                    .description(msg.takeADescription(lang, message.getFromId().longValue()))
                    .build();
            user = services.user.save(user);
            log.info("NEW USER: {}", user);
        }
        if (User.isBanned(user)) {
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_BANNED_USER, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getByUserStatus(user)));
            logResult(Msg.START_BANNED_USER.name(), message.getChatId(), sendResponse.isOk());
        } else {
            user.setDescription(msg.takeADescription(user.getLang(), user.getIdUser()));
            user.setState(State.PAUSE.name());
            user = services.user.save(user);

            services.chat.getByIdUserAndState(user.getIdUser(), ChatState.ACTIVE)
                    .parallelStream()
                    .forEach(x -> {
                        x.setState(ChatState.SKIPPED.name());
                        services.chat.save(x);
                    });
            services.tag.deleteAll(user);
            services.tag.add(user, Tag.GENERAL.name());
            SendResponse sendResponse = bot.execute(new SendMessage(message.getChatId(), msg.msg(Msg.START_OK, user.getLang()))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyMarkup(keyboard.getByUserStatus(user))
            );
            logResult(Msg.START_OK.name(), message.getChatId(), sendResponse.isOk());
            new PlayButton(bot, services, msg, userAdmin, periodicalTasks).play(user);
        }
    }
}
