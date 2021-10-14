package net.sytes.jaraya;

import com.pengrad.telegrambot.request.SetWebhook;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.controller.AnonChatBot;
import net.sytes.jaraya.controller.Authorization;
import net.sytes.jaraya.controller.Notification;
import net.sytes.jaraya.controller.Stats;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.CoreException;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.util.Properties;

import static spark.Spark.*;

/**
 * AnonChatBot
 */
@Slf4j
public class App {

    public static void main(String[] args) throws CoreException {
        log.info("Init microservice...");
        String fileProperties = args.length == 1 ? args[0] : null;

        String nameBot = Properties.get(Property.NAME_BOT.name(), fileProperties);
        if (nameBot == null || nameBot.isEmpty()) {
            throw new TelegramException("appSite not found");
        }
        Long userAdmin = Long.valueOf(Properties.get(Property.USER_ID_ADMIN.name(), fileProperties));

        Authorization auth = new Authorization();
        AnonChatBot anonChatBot = new AnonChatBot(userAdmin);
        Notification notification2 = new Notification(anonChatBot.getBot(), userAdmin, anonChatBot.getPeriodicalTasks());
        Stats stats = new Stats(anonChatBot.getBot(), userAdmin, anonChatBot.getPeriodicalTasks());
staticFileLocation("public");
        port(Integer.parseInt(Properties.get(Property.PORT.name(), fileProperties)));

        path("/" + nameBot + "/", () -> {
            before("/*", auth);
            post("/notification", notification2);
            get("/stats", stats);
        });

        post("/" + anonChatBot.getToken(), anonChatBot);
        String appSite = Properties.get(Property.APP_SITE.name());
        if (appSite == null || appSite.isEmpty()) {
            throw new TelegramException("appSite not found");
        }
        log.info(Property.APP_SITE.name() + ": " + appSite);
        anonChatBot.getBot().execute(new SetWebhook().url(appSite + "/" + anonChatBot.getToken()));


        log.info("Microservice ready!");


    }
}
