package net.sytes.jaraya.component;

import net.sytes.jaraya.enums.Msg;
import net.sytes.jaraya.exception.UtilException;
import net.sytes.jaraya.properties.Properties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MsgProcess {

    public static final String EN = "en";
    public static final String ES = "es";

    private Map<String, Map<Msg, String>> languages = new HashMap<>();

    public MsgProcess() {
        super();
        languages();
    }

    private void languages() {
        languages.put(EN, getMap(EN));
        languages.put(ES, getMap(ES));
    }

    private Map<Msg, String> getMap(String lang) {
        Map<Msg, String> map = new HashMap<>();

        File fileLang = new File("lang/" + lang);
        if (!fileLang.exists()) {
            return map;
        }
        try {
            for (Msg msg : Msg.values()) {
                map.put(msg, Properties.get(msg.name(), fileLang.getAbsolutePath()));
            }
        } catch (UtilException e) {
            e.printStackTrace();
        }
        return map;
    }

    public String msg(Msg msg, String lang) {
        if (Objects.isNull(msg)) {
            return null;
        }

        String translate = languages.get(langAvailable(lang) ? lang : EN).get(msg);
        return translate != null ? translate : msg.code();
    }

    public boolean langAvailable(String lang) {
        if (Objects.isNull(lang) || lang.isEmpty()) {
            return false;
        }
        return languages.get(lang) != null;
    }

    public String langOrDefault(String lang) {
        return langAvailable(lang) ? lang : EN;
    }
}
