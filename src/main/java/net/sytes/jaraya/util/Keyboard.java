package net.sytes.jaraya.util;

import com.pengrad.telegrambot.model.request.*;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.model.UserTag;
import net.sytes.jaraya.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Keyboard {
    private final MsgProcess msg;

    public Keyboard(MsgProcess msg) {
        this.msg = msg;
    }

    private com.pengrad.telegrambot.model.request.Keyboard play(String lang) {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(msg.commandButton(Msg.PAUSE, lang)),
                new KeyboardButton(msg.commandButton(Msg.NEXT, lang)),
                new KeyboardButton(msg.commandButton(Msg.BLOCK, lang)),
                new KeyboardButton(msg.commandButton(Msg.REPORT, lang)),
                new KeyboardButton(msg.commandButton(Msg.CONFIG, lang))
        )
                .resizeKeyboard(true)
                .selective(true);
    }

    private com.pengrad.telegrambot.model.request.Keyboard banned() {
        return new ReplyKeyboardRemove();
    }

    private com.pengrad.telegrambot.model.request.Keyboard remove() {
        return new ReplyKeyboardRemove();
    }

    private com.pengrad.telegrambot.model.request.Keyboard pause(String lang) {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(msg.commandButton(Msg.PLAY, lang)),
                new KeyboardButton(msg.commandButton(Msg.CONFIG, lang))
        )
                .resizeKeyboard(true).selective(true);
    }

    public InlineKeyboardMarkup getInlineKeyboardConfig(String lang) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.addRow(new InlineKeyboardButton(msg.commandButton(Msg.CB_BIO, lang)).callbackData(Msg.CB_BIO.name()));
        inlineKeyboard.addRow(new InlineKeyboardButton(msg.commandButton(Msg.CB_TAGS, lang)).callbackData(Msg.CB_TAGS.name()));
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup getInlineKeyboardPref(List<UserTag> tags, String lang) {
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

    public com.pengrad.telegrambot.model.request.Keyboard getByUserStatus(User user) {
        if (user.getState().contentEquals(State.BANNED.name())) {
            return banned();
        }
        if (user.getState().contentEquals(State.EMPTY_BIO.name())) {
            return remove();
        }
        if (user.getState().contentEquals(State.PLAY.name())) {
            return play(user.getLang());
        }
        if (user.getState().contentEquals(State.PAUSE.name())) {
            return pause(user.getLang());
        }
        if (user.getState().contentEquals(State.STOP.name())) {
            return pause(user.getLang());
        }
        return null;
    }
}
