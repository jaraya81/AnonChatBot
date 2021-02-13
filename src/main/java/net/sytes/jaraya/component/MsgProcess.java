package net.sytes.jaraya.component;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.enums.Tag;
import net.sytes.jaraya.exception.TelegramException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.sytes.jaraya.util.Operator.elvis;

@Slf4j
public class MsgProcess {

    public static final String EN = "en";
    public static final String ES = "es";
    public static final String PT = "pt";
    public static final String RU = "ru";

    private final Map<String, Map<String, String>> languages = new HashMap<>();

    public MsgProcess() {
        super();
        languages();
    }

    private void languages() {
        languages.put(ES, getMap(ES));
        languages.put(EN, getMap(EN));
        languages.put(PT, getMap(PT));
        languages.put(RU, getMap(RU));
    }

    private Map<String, String> getMap(String lang) {

        File fileLang = new File("lang/" + lang + ".json");
        try {
            return fileLang.exists()
                    ? (Map<String, String>) new Gson().fromJson(new String(Files.readAllBytes(fileLang.toPath()), StandardCharsets.UTF_8), Map.class)
                    : new HashMap<>();
        } catch (IOException e) {
            log.error("", e);
        }
        return new HashMap<>();
    }

    private String get(String msg, String lang, Object... objects) {
        if (Objects.isNull(msg)) {
            return "";
        }
        String format = languages.get(langOrDefault(lang)).get(msg);
        if (format != null) {
            return String.format(format, objects);
        } else {
            return msg;
        }
    }

    public String msg(Msg msg, String lang, Object... objects) {
        if (Objects.isNull(msg)) {
            return "";
        }
        return get(msg.name(), lang, objects);
    }

    private boolean langAvailable(String lang) {
        if (Objects.isNull(lang) || lang.isEmpty()) {
            return false;
        }
        return languages.get(lang) != null;
    }

    public String takeADescription(String lang, Long idUser) {
        int i = ThreadLocalRandom.current().nextInt(1, idUser.intValue());
        return msg(Msg.DEFAULT_BIO, lang, idUser % i);
    }

    public String langOrDefault(String lang) {
        return langAvailable(lang) ? lang : ES;
    }

    public String reverseTag(Tag tag, String lang, Object... objects) {
        if (Objects.isNull(tag)) {
            return "";
        }
        return get(tag.reverse(), lang, objects);
    }

    public String tag(Tag tag, String lang, Object... objects) {
        if (Objects.isNull(tag)) {
            return "";
        }
        return get(tag.name(), lang, objects);
    }

    @SneakyThrows
    public String commandButton(Msg command, String lang) {
        final String finalLang = elvis(lang, EN);
        return msg(
                Arrays.stream(Msg.values()).filter(x -> x.name().contentEquals(command.name())).findFirst()
                        .orElseThrow(() -> new TelegramException(String.format("'%s' not found in file lang '%s'", command.name(), finalLang))),
                lang
        );
    }

}
