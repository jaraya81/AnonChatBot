package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
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
        actions.add(new PLAY(bot, serviceChat, msg, userAdmin));
        actions.add(new PAUSE(bot, serviceChat, msg, userAdmin));
        actions.add(new NEXT(bot, serviceChat, msg, userAdmin));
        actions.add(new BLOCK(bot, serviceChat, msg, userAdmin));
        actions.add(new REPORT(bot, serviceChat, msg, userAdmin));
        actions.add(new CONFIG(bot, serviceChat, msg, userAdmin));
        actions.add(new BIO(bot, serviceChat, msg, userAdmin));
        actions.add(new LANG(bot, serviceChat, msg, userAdmin));
        actions.add(new ABOUT(bot, serviceChat, msg, userAdmin));
        actions.add(new CHAT(bot, serviceChat, msg, userAdmin));

        final PeriodicalTasks periodicalTasks = new PeriodicalTasks(bot, serviceChat, msg, userAdmin);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(periodicalTasks::exec, 0, 2, TimeUnit.MINUTES);

    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Update update = BotUtils.parseUpdate(request.body());
        if (update == null) {
            return "UPDATE NOK";
        }
        MessageChat message = MessageChat.to(update.message());

        if (message == null) {
            return "NULL PARSING MESSAGE";
        }
        for (IAction action : actions) {
            action.exec(message);
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
