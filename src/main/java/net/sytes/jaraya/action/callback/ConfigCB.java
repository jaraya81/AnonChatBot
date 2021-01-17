package net.sytes.jaraya.action.callback;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.ForceBio;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.SuperAction;
import net.sytes.jaraya.action.message.command.TagsCommand;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.CBQuery;

import java.util.Objects;

@Slf4j
public class ConfigCB extends SuperAction implements IAction {

    public ConfigCB(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
        super(bot, serviceChat, msg, userAdmin);
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
                && (callbackQuery.data().contentEquals(Msg.CB_BIO.name())
                || callbackQuery.data().contentEquals(Msg.CB_TAGS.name()))
                ;
    }

    private void action(CallbackQuery callbackQuery) {
        User user = services.user.getByIdUser(callbackQuery.message().chat().id());
        if (User.exist(user) && !User.isBanned(user)) {
            if (callbackQuery.data().contentEquals(Msg.CB_BIO.name())) {
                new ForceBio(bot, services, msg, userAdmin).sendMessage(user);
                AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id()).showAlert(true);
                BaseResponse responseAnswer = bot.execute(answer);
                logResult(this.getClass().getSimpleName(), user.getIdUser(), responseAnswer.isOk());
            } else if (callbackQuery.data().contentEquals(Msg.CB_TAGS.name())) {
                AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id()).showAlert(true);
                BaseResponse responseAnswer = bot.execute(answer);
                new TagsCommand(bot, services, msg, userAdmin).action(user);
                logResult(this.getClass().getSimpleName(), user.getIdUser(), responseAnswer.isOk());
            }
        }
    }
}


