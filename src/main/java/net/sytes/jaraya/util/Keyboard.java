package net.sytes.jaraya.util;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import net.sytes.jaraya.action.*;

public class Keyboard {
    private Keyboard() {
    }

    public static com.pengrad.telegrambot.model.request.Keyboard play() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(PAUSE.CODE),
                new KeyboardButton(NEXT.CODE),
                new KeyboardButton(BLOCK.CODE),
                new KeyboardButton(REPORT.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard banned() {
        return new ReplyKeyboardRemove();
    }

    public static com.pengrad.telegrambot.model.request.Keyboard start() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(PLAY.CODE),
                new KeyboardButton(CONFIG.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard pause() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(PLAY.CODE),
                new KeyboardButton(CONFIG.CODE)).resizeKeyboard(true).selective(true);
    }

    public static com.pengrad.telegrambot.model.request.Keyboard config() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(BIO.CODE_1),
                new KeyboardButton(PLAY.CODE)).resizeKeyboard(true).selective(true);
    }
}
