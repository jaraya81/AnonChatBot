package net.sytes.jaraya.util;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import net.sytes.jaraya.action.message.*;

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
}
