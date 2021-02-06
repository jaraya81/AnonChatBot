package net.sytes.jaraya.action.callback;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.model.UserTag;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.CBQuery;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class InterestsCB extends SuperAction implements IAction {

    private final PeriodicalTasks periodicalTasks;

    public InterestsCB(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin, PeriodicalTasks periodicalTasks) {
        super(bot, serviceChat, msg, userAdmin);
        this.periodicalTasks = periodicalTasks;
    }

    @Override
    public IAction exec(BaseUpdate baseUpdate) {
        CallbackQuery callBackQuery = ((CBQuery) baseUpdate).getQuery();
        action(callBackQuery);
        return this;
    }

    @Override
    public boolean check(BaseUpdate baseUpdate) {
        CallbackQuery callbackQuery = ((CBQuery) baseUpdate).getQuery();
        return Objects.nonNull(callbackQuery)
                && Objects.nonNull(callbackQuery.data())
                && Objects.nonNull(callbackQuery.message())
                && Objects.nonNull(callbackQuery.message().chat())
                && Objects.nonNull(callbackQuery.message().chat().id())
                && Arrays.stream(Tag.values()).anyMatch(x -> x.name().contentEquals(callbackQuery.data()));

    }

    private void action(CallbackQuery callbackQuery) {
        User user = services.user.getByIdUser(callbackQuery.message().chat().id());
        if (User.exist(user) && !User.isBanned(user)) {
            List<UserTag> userTags = services.tag.getByUserId(user);

            Optional<Tag> tag = Arrays.stream(Tag.values())
                    .filter(x -> x.name().contentEquals(callbackQuery.data()))
                    .findFirst();
            if (tag.isPresent()) {
                Optional<UserTag> userTag = userTags.stream()
                        .filter(x -> x.getTag().contentEquals(tag.get().name()))
                        .findFirst();
                if (userTag.isPresent()) {
                    services.tag.delete(userTag.get());
                } else {
                    services.tag.add(user, tag.get().name());
                }
                AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id()).showAlert(true);
                BaseResponse responseAnswer = bot.execute(answer);
                logResult(this.getClass().getSimpleName(), user.getIdUser(), responseAnswer.isOk());

                EditMessageText edit = new EditMessageText(
                        callbackQuery.message().chat().id(),
                        callbackQuery.message().messageId(),
                        callbackQuery.message().text())
                        .replyMarkup(keyboard.getInlineKeyboardPref(services.tag.getByUserId(user), user.getLang()))
                        .parseMode(ParseMode.MarkdownV2);

                BaseResponse responseEdit = bot.execute(edit);
                logResult(this.getClass().getSimpleName(), user.getIdUser(), responseEdit.isOk());
            }
        }
    }

}
