package net.sytes.jaraya.action.callback;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.message.Action;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.model.UserTag;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.CBQuery;

import java.util.*;

@Slf4j
public class Interests extends Action implements IAction {
    public static final String CODE = "/about";

    public Interests(TelegramBot bot, AnonChatService serviceChat, MsgProcess msg, Long userAdmin) {
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
        CallbackQuery callBackQuery = ((CBQuery) baseUpdate).getQuery();
        return Objects.nonNull(callBackQuery) && Objects.nonNull(callBackQuery.data());
    }

    private void action(CallbackQuery callbackQuery) {
        User user = services.user.getByIdUser(callbackQuery.message().from().id().longValue());
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
            }
            AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id()).showAlert(true);
            BaseResponse responseAnswer = bot.execute(answer);
            logResult(this.getClass().getSimpleName(), user.getIdUser(), responseAnswer.isOk());

            EditMessageText edit = new EditMessageText(
                    callbackQuery.message().chat().id(),
                    callbackQuery.message().messageId(),
                    callbackQuery.message().text())
                    .replyMarkup(getInlineKeyboardPref(services.tag.getByUserId(user))).parseMode(ParseMode.HTML);

            BaseResponse responseEdit = bot.execute(edit);
            logResult(this.getClass().getSimpleName(), user.getIdUser(), responseEdit.isOk());
        }
    }

    private InlineKeyboardMarkup getInlineKeyboardPref(List<UserTag> tags) {
        List<List<InlineKeyboardButton>> grilla = new ArrayList<>();
        List<InlineKeyboardButton> inlines = new ArrayList<>();

        int i = 0;
        for (Tag x : Tag.values()) {
            Optional<UserTag> opt = tags.stream()
                    .filter(userTag -> userTag.getTag().contentEquals(x.name()))
                    .findFirst();
            inlines.add(new InlineKeyboardButton(
                    String.format("%s %s", opt.isPresent() ? "✅" : "❌", x.reverse()))
                    .callbackData(x.name()));
            if (i++ == 1 || i >= Tag.values().length) {
                grilla.add(inlines);
                inlines = new ArrayList<>();
            }
        }
        grilla.add(inlines);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        for (List<InlineKeyboardButton> linea : grilla) {
            inlineKeyboard = inlineKeyboard.addRow(linea.toArray(new InlineKeyboardButton[0]));
        }
        return inlineKeyboard;

    }

}
