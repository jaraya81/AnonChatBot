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
import net.sytes.jaraya.properties.Properties;
import net.sytes.jaraya.security.Base64;
import net.sytes.jaraya.security.Cypher;

import java.nio.charset.StandardCharsets;

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

        Authorization auth = new Authorization();
        AnonChatBot anonChatBot = new AnonChatBot();
        Notification notification = new Notification(anonChatBot.getBot());
        Stats stats = new Stats();

        port(Integer.parseInt(Properties.get(Property.PORT.name(), fileProperties)));

        path("/" + nameBot + "/", () -> {
            before("/*", auth);
            get("/notification", notification);
            get("/stats", stats);
        });

        path("/url", () -> {
            post("/x", (request, response) -> {
                String url = request.queryParams("url");
                if (url == null || url.isEmpty()) {
                    throw new TelegramException("null url");
                }
                return Base64.encodeUrl(
                        Cypher.encrypt(
                                url.getBytes(StandardCharsets.UTF_8),
                                Properties.get(Property.CYPHER_KEY.name()).getBytes(StandardCharsets.UTF_8)));
            });
            get("/x/:base", (request, response) -> {
                String base = request.params(":base");
                if (base == null || base.isEmpty()) {
                    throw new TelegramException("null action");
                }
                response.status(301);
                String url = new String(
                        Cypher.decrypt(
                                Base64.decodeUrl(base.getBytes(StandardCharsets.UTF_8)),
                                Properties.get(Property.CYPHER_KEY.name()).getBytes(StandardCharsets.UTF_8)));
                log.info(url);
                response.redirect(url);
                return null;
            });
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
