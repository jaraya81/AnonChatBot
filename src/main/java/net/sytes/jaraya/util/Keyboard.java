package net.sytes.jaraya.util;

import com.pengrad.telegrambot.model.request.*;
import net.sytes.jaraya.action.message.*;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.UserTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Keyboard {
    private Keyboard() {
    }

    public static com.pengrad.telegrambot.model.request.Keyboard play() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(Pause.CODE),
                new KeyboardButton(Next.CODE),
                new KeyboardButton(Block.CODE),
                new KeyboardButton(Report.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard banned() {
        return new ReplyKeyboardRemove();
    }

    public static com.pengrad.telegrambot.model.request.Keyboard remove() {
        return new ReplyKeyboardRemove();
    }

    public static com.pengrad.telegrambot.model.request.Keyboard start() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(Play.CODE),
                new KeyboardButton(Config.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard pause() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(Play.CODE),
                new KeyboardButton(Config.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard config() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(Bio.CODE_1),
                new KeyboardButton(Play.CODE)).resizeKeyboard(true).selective(true);
    }

    public static InlineKeyboardMarkup getInlineKeyboardPref(List<UserTag> tags, MsgProcess msg, String lang) {
        List<List<InlineKeyboardButton>> grilla = new ArrayList<>();
        List<InlineKeyboardButton> inlines = new ArrayList<>();

        int i = 0;
        for (Tag x : Tag.values()) {
            Optional<UserTag> opt = tags.stream()
                    .filter(userTag -> userTag.getTag().contentEquals(x.name()))
                    .findFirst();
            inlines.add(new InlineKeyboardButton(
                    String.format("%s %s", opt.isPresent() ? "✅" : "❌", msg.reverseTag(x, lang)))
                    .callbackData(x.name()));
            if (i == 1) {
                grilla.add(inlines);
                inlines = new ArrayList<>();
                i = 0;
            } else {
                i++;
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
