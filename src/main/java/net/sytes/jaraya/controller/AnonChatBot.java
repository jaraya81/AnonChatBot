package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.callback.Interests;
import net.sytes.jaraya.action.message.*;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.CoreException;
import net.sytes.jaraya.service.AnonChatService;
import net.sytes.jaraya.util.Properties;
import net.sytes.jaraya.vo.BaseUpdate;
import net.sytes.jaraya.vo.CBQuery;
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
    private final List<IAction> messageActions = new ArrayList<>();
    private final List<IAction> callbackActions = new ArrayList<>();

    public AnonChatBot(Long userAdmin) throws CoreException {

        AnonChatService serviceChat = new AnonChatService();
        MsgProcess msg = new MsgProcess();

        token = Properties.get(Property.TOKEN_BOT.name());
        log.info("TOKEN: ..." + token.substring(0, 5));
        bot = new TelegramBot.Builder(token).build();
        messageActions.add(new Start(bot, serviceChat, msg, userAdmin));
        messageActions.add(new ForceBio(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Play(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Pause(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Next(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Block(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Report(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Config(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Bio(bot, serviceChat, msg, userAdmin));
        messageActions.add(new Lang(bot, serviceChat, msg, userAdmin));
        messageActions.add(new About(bot, serviceChat, msg, userAdmin));
        messageActions.add(new RegisterPremium(bot, serviceChat, msg, userAdmin));
        messageActions.add(new CHAT(bot, serviceChat, msg, userAdmin));

        callbackActions.add(new Interests(bot, serviceChat, msg, userAdmin));

        final PeriodicalTasks periodicalTasks = new PeriodicalTasks(bot, serviceChat, msg, userAdmin);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(periodicalTasks::exec, 0, 2, TimeUnit.MINUTES);

    }

    @Override
    public Object handle(Request request, Response response) {
        Update update = BotUtils.parseUpdate(request.body());
        if (update == null) {
            return "UPDATE NOK";
        }
        MessageChat message = MessageChat.to(update.message());
        execActions(message, messageActions);

        CBQuery callBackQuery = CBQuery.to(update.callbackQuery());
        execActions(callBackQuery, callbackActions);

        return "";

    }

    private void execActions(BaseUpdate update, List<IAction> actions) {
        if (update != null) {
            for (IAction action : actions) {
                if (action.check(update)) {
                    action.exec(update);
                    break;
                }
            }
        }
    }

    public TelegramBot getBot() {
        return bot;
    }

    public String getToken() {
        return token;
    }
}
