package net.sytes.jaraya.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.action.callback.ConfigCB;
import net.sytes.jaraya.action.callback.InterestsCB;
import net.sytes.jaraya.action.message.Bio;
import net.sytes.jaraya.action.message.CHAT;
import net.sytes.jaraya.action.message.ForceBio;
import net.sytes.jaraya.action.message.IAction;
import net.sytes.jaraya.action.message.button.*;
import net.sytes.jaraya.action.message.command.*;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.component.PeriodicalTasks;
import net.sytes.jaraya.enums.Msg;
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
    private final MsgProcess msg;
    private final PeriodicalTasks periodicalTasks;

    public AnonChatBot(Long userAdmin) throws CoreException {

        AnonChatService service = new AnonChatService();
        msg = new MsgProcess();

        token = Properties.get(Property.TOKEN_BOT.name());
        log.info("TOKEN: ..." + token.substring(0, 5));
        bot = new TelegramBot.Builder(token).build();
        messageActions.add(new StartCommand(bot, service, msg, userAdmin));
        messageActions.add(new ForceBio(bot, service, msg, userAdmin));
        messageActions.add(new PlayButton(bot, service, msg, userAdmin));
        messageActions.add(new PauseButton(bot, service, msg, userAdmin));
        messageActions.add(new NextButton(bot, service, msg, userAdmin));
        messageActions.add(new BlockButton(bot, service, msg, userAdmin));
        messageActions.add(new ReportButton(bot, service, msg, userAdmin));
        messageActions.add(new ConfigButton(bot, service, msg, userAdmin));
        messageActions.add(new Bio(bot, service, msg, userAdmin));
        messageActions.add(new LangCommand(bot, service, msg, userAdmin));
        messageActions.add(new AboutCommand(bot, service, msg, userAdmin));
        messageActions.add(new RegisterPremiumCommand(bot, service, msg, userAdmin));
        messageActions.add(new TagsCommand(bot, service, msg, userAdmin));
        messageActions.add(new AdminCommand(bot, service, msg, userAdmin));
        messageActions.add(new CHAT(bot, service, msg, userAdmin));

        callbackActions.add(new InterestsCB(bot, service, msg, userAdmin));
        callbackActions.add(new ConfigCB(bot, service, msg, userAdmin));

        periodicalTasks = new PeriodicalTasks(bot, service, msg, userAdmin);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(periodicalTasks::exec, 0, 2, TimeUnit.MINUTES);

    }

    @Override
    public Object handle(Request request, Response response) {
        Update update = BotUtils.parseUpdate(request.body());
        if (update == null) {
            return "UPDATE NOK";
        }
        if (update.message() != null &&
                update.message().chat() != null
                && update.message().chat().type() != null
                && update.message().chat().type() != Chat.Type.Private) {
            bot.execute(new SendMessage(update.message().chat().id(), msg.msg(Msg.ONLY_PRIVATE, "es"))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false));
            return "Not private update chat";
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

    public PeriodicalTasks getPeriodicalTasks() {
        return periodicalTasks;
    }
}
