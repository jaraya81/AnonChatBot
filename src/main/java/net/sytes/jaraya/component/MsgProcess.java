package net.sytes.jaraya.component;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.enums.Msg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Slf4j
public class MsgProcess {

    public static final String EN = "en";
    public static final String ES = "es";
    public static final String PT = "pt";

    private final Map<String, Map<String, String>> languages = new HashMap<>();

    private final Map<String, List<String>> descriptions = new HashMap<>();

    public MsgProcess() {
        super();
        languages();
        descriptions();
    }

    private void descriptions() {
        descriptions.put(ES, getDescriptions(ES));
        descriptions.put(EN, getDescriptions(EN));
        descriptions.put(PT, getDescriptions(PT));
    }

    private List<String> getDescriptions(String lang) {
        File fileLang = new File("lang/descriptions_" + lang + ".json");
        try {
            return fileLang.exists()
                    ? (List<String>) new Gson().fromJson(new String(Files.readAllBytes(fileLang.toPath()), StandardCharsets.UTF_8), List.class)
                    : new ArrayList<>();
        } catch (IOException e) {
            log.error("", e);
        }
        return new ArrayList<>();
    }

    private void languages() {
        languages.put(ES, getMap(ES));
        languages.put(EN, getMap(EN));
        languages.put(PT, getMap(PT));
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

    public String msg(Msg msg, String lang, Object... objects) {
        if (Objects.isNull(msg)) {
            return "";
        }
        String format = languages.get(langOrDefault(lang)).get(msg.name());
        if (format != null) {
            return String.format(format, objects);
        } else {
            return msg.name();
        }
    }

    private boolean langAvailable(String lang) {
        if (Objects.isNull(lang) || lang.isEmpty()) {
            return false;
        }
        return languages.get(lang) != null;
    }

    public String anyDescription(String lang) {
        List<String> list = new ArrayList<>(descriptions.get(lang));
        Collections.shuffle(list);
        return list.stream().findFirst().orElse("NO_BIO");
    }

    public String langOrDefault(String lang) {
        return langAvailable(lang) ? lang : ES;
    }
}
