package net.sytes.jaraya;

import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.enums.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

@Slf4j
public class BotTesting {
    private TelegramBot bot;

    @Before
    public void before() {
        bot = new TelegramBot("938814287:AAFt82f80HIYXu4hs_rHZwJcqoFuxaZbM9U");
        bot.execute(new DeleteWebhook());
    }

    @Test
    public void execute() {
        Map<String, Boolean> estados = new HashMap<>();
        estados.put(Tag.GENERAL.name(), true);

        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);

        int lastUpdateId = 0;
        List<Update> updates = updatesResponse.updates();
        for (Update update : updates) {
            log.info("{}", new GsonBuilder().setPrettyPrinting().create().toJson(update));
            if (update.callbackQuery() != null) {
                if (update.callbackQuery().data() != null) {
                    Optional<Tag> tag = Arrays.stream(Tag.values()).filter(x -> x.name().contentEquals(update.callbackQuery().data())).findFirst();
                    tag.ifPresent(value -> estados.put(value.name(),
                            estados.get(value.name()) == null || !estados.get(value.name())));
                    AnswerCallbackQuery answer = new AnswerCallbackQuery(update.callbackQuery().id()).showAlert(true);
                    log.info("{}", bot.execute(answer));

                    EditMessageText edit = new EditMessageText(
                            update.callbackQuery().message().chat().id(),
                            update.callbackQuery().message().messageId(),
                            update.callbackQuery().message().text() + " XD")
                            .replyMarkup(getInlineKeyboardPref(estados)).parseMode(ParseMode.HTML);

                    log.info("{}", bot.execute(edit));
                }
            }
            if (update.message() != null
                    && update.message().chat() != null
                    && update.message().text() != null
                    && update.message().text().startsWith("p")) {
                SendMessage request = new SendMessage(update.message().chat().id(), "Marca tus intereses")
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyMarkup(getInlineKeyboardPref(estados));
                BaseResponse response = bot.execute(request);
                log.info("{}", response);
            }
            lastUpdateId = update.updateId();
        }
        GetUpdates getUpdates2 = new GetUpdates().limit(0).offset(lastUpdateId + 1).timeout(0);
        bot.execute(getUpdates2);

    }

    private InlineKeyboardMarkup getInlineKeyboardPref(Map<String, Boolean> estados) {
        List<List<InlineKeyboardButton>> grilla = new ArrayList<>();
        List<InlineKeyboardButton> inlines = new ArrayList<>();

        int i = 0;
        for (Tag x : Tag.values()) {
            inlines.add(new InlineKeyboardButton(
                    (estados.get(x.name()) != null && estados.get(x.name()) ? "✅ " : "❌ ") + x.reverse())
                    .callbackData(x.name()));
            if (i++ == 2 || i >= Tag.values().length) {
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

    @After
    public void after() {
    }
}
