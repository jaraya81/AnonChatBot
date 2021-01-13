package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.*;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.CoreException;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.util.Properties;
import net.sytes.jaraya.vo.MessageChat;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AnonChatBot implements Route {

    private final String token;
    private final TelegramBot bot;
    private final List<IAction> actions = new ArrayList<>();

    public AnonChatBot(Long userAdmin) throws CoreException {

        AnonChatService serviceChat = new AnonChatService();
        MsgProcess msg = new MsgProcess();

        token = Properties.get(Property.TOKEN_BOT.name());
        log.info("TOKEN: ..." + token.substring(0, 5));
        bot = new TelegramBot.Builder(token).build();
        actions.add(new START(bot, serviceChat, msg, userAdmin));
        actions.add(new FORCE_BIO(bot, serviceChat, msg, userAdmin));
        actions.add(new PLAY(bot, serviceChat, msg, userAdmin));
        actions.add(new PAUSE(bot, serviceChat, msg, userAdmin));
        actions.add(new NEXT(bot, serviceChat, msg, userAdmin));
        actions.add(new BLOCK(bot, serviceChat, msg, userAdmin));
        actions.add(new REPORT(bot, serviceChat, msg, userAdmin));
        actions.add(new CONFIG(bot, serviceChat, msg, userAdmin));
        actions.add(new BIO(bot, serviceChat, msg, userAdmin));
        actions.add(new LANG(bot, serviceChat, msg, userAdmin));
        actions.add(new ABOUT(bot, serviceChat, msg, userAdmin));
        actions.add(new REGISTERPREMIUM(bot, serviceChat, msg, userAdmin));
        actions.add(new CHAT(bot, serviceChat, msg, userAdmin));

        final PeriodicalTasks periodicalTasks = new PeriodicalTasks(bot, serviceChat, msg, userAdmin);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(periodicalTasks::exec, 0, 2, TimeUnit.MINUTES);

    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info(">> " + request.body());
        Update update = BotUtils.parseUpdate(request.body());
        if (update == null) {
            return "UPDATE NOK";
        }
        if (update.message() != null &&
                update.message().chat() != null
                && update.message().chat().type() != null
                && update.message().chat().type() != Chat.Type.Private) {
            bot.execute(new SendMessage(update.message().chat().id(), "Soy sólo un bot para privados sufro miedo escénico, sácame de aquí \uD83D\uDE12")
                    .parseMode(ParseMode.MarkdownV2)
                    .disableWebPagePreview(false)
                    .disableNotification(false));
            return "Not private update chat";
        }
        MessageChat message = MessageChat.to(update.message());

        if (message == null) {
            return "NULL PARSING MESSAGE";
        }

        for (IAction action : actions) {
            if (action.check(message)) {
                action.exec(message);
                break;
            }
        }
        return "";

    }

    public TelegramBot getBot() {
        return bot;
    }

    public String getToken() {
        return token;
    }
}
