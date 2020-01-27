package net.sytes.jaraya.component;

import net.sytes.jaraya.enums.Msg;

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
        languages.put(EN, en());
        languages.put(ES, es());
    }

    private Map<Msg, String> es() {
        Map<Msg, String> map = new HashMap<>();
        map.put(Msg.START_OK, "<b>¡Bienvenida(o)!</b>\n\n" +
                "Con este Bot puedes conocer nuevas personas en tu mismo idioma manteniendo tu anonimato.\n\n" +
                "- Para empezar, debes presionar el botón <b>Play</b>.\n\n" +
                "- Posteriormente cambiar tu bio (/bio tu-bio).\n\n" +
                "- Si no quieres chatear más con alguien, presiona el botón <b>Next!</b> y cambiaras a otra persona.\n\n" +
                "- Si te cayó mal <b>Block</b> y estará bloqueado para ti.\n\n" +
                "- Si es SPAM, márcalo como <b>SPAM</b> para expulsarlo de la comunidad.\n\n" +
                "- Recuerda que tanto tú como todos son anónimos y la única manera de identificarse es con la bio, por lo tanto, puedes cambiarlo las veces que quieras y no dejar rastros.\n\n" +
                "- Si quieres descansar o dormir, presiona el botón <b>Pause</b>.\n\n" +
                "- Si quieres cambiar a una comunidad en otro idioma escribe <i>/lang idioma</i> (español /lang es, english /lang en).\n\n" +
                "- Para recomendaciones, preguntas, etc., comunícate conmigo por el grupo https://t.me/RandomNextChat \n\n" +
                "- Por último recuerda <b>no entregar datos personales ni contraseñas a desconocidos.</b>\n\n" +
                "¡Que lo disfrutes!,\n\n" +
                "                    <i>Julian</i>");
        map.put(Msg.START_AGAIN, "¡Bienvenida(o) nuevamente! :D\n\n" +
                "Con este Bot puedes conocer nuevas personas en tu mismo idioma manteniendo tu anonimato.\n\n" +
                "- Para empezar, debes presionar el botón <b>Play</b>.\n\n" +
                "- Posteriormente cambiar tu bio (/bio tu-bio).\n\n" +
                "- Si no quieres chatear más con alguien, presiona el botón <b>Next!</b> y cambiaras a otra persona.\n\n" +
                "- Si te cayó mal <b>Block</b> y estará bloqueado para ti.\n\n" +
                "- Si es SPAM, márcalo como <b>SPAM</b> para expulsarlo de la comunidad.\n\n" +
                "- Recuerda que tanto tú como todos son anónimos y la única manera de identificarse es con la bio, por lo tanto, puedes cambiarlo las veces que quieras y no dejar rastros.\n\n" +
                "- Si quieres descansar o dormir, presiona el botón <b>Pause</b>.\n\n" +
                "- Si quieres cambiar a una comunidad en otro idioma escribe <i>/lang idioma</i> (español /lang es, english /lang en).\n\n" +
                "- Para recomendaciones, preguntas, etc., comunícate conmigo por el grupo https://t.me/RandomNextChat \n\n" +
                "- Por último recuerda <b>no entregar datos personales ni contraseñas a desconocidos.</b>\n\n" +
                "¡Que lo disfrutes!,\n\n" +
                "                    <i>Julian</i>");
        map.put(Msg.START_BANNED_USER, "<pre>Lo siento, has sido bloqueado de la comunidad por acumulación de quejas en tu contra :(</pre>");
        map.put(Msg.USER_PLAY, "<pre>Estás activa(o), presiona <b>Next!</b> para buscar a tu nuevo Random.</pre>");
        map.put(Msg.USER_PAUSE, "<pre>Estás en pausa, no recibirás chats ni notificaciones por este chat.</pre>");
        map.put(Msg.USER_REPORT, "<pre>Tu Random ha sido reportado como SPAM, ya no lo verás nuevamente por acá!.</pre>");
        map.put(Msg.USER_1_NEXT_OK, "<pre>¡NEXT! Saluda a tu nuevo Random.\n" +
                "Su bio es: </pre>");
        map.put(Msg.USER_2_NEXT_OK, "<pre>¡NEXT! Saluda a tu nuevo Random.\n" +
                "Su bio es: </pre>");
        map.put(Msg.USER_NEXT_WAITING, "<pre>¡NEXT! pero no hay nadie disponible para chatear, espera que te encuentren :D</pre>");
        map.put(Msg.USER_NO_CHAT, "<pre>No estás en ningún chat, espera que te encuentren o presiona <b>¡NEXT!</b>.</pre>");
        map.put(Msg.SET_BIO_OK, "<pre>Has modificado tu bio: </pre>");
        map.put(Msg.BIO, "<pre>Escribe una /bio que te describa no más de 140 caracteres.\n"
                + "Ejemplo: /bio Soy mujer de Valencia, España, tengo 24 años. Me encanta correr y pasarla bien!</pre>");
        map.put(Msg.CONFIG, "<pre>Ingresa a alguna de las opciones a configurar o <b>Play</b> para iniciar.</pre>");
        map.put(Msg.CHAT_TIMEOUT, "<pre>Tiempo de chat agotado, presiona <b>Next!</b> para un nuevo Random.</pre>");
        map.put(Msg.USER_BLOCK, "<pre>Tu Random ha sido bloqueado, ya no lo verás nuevamente por acá!\n"
                + "Presiona nuevamente <b>Next!</b> para un Random</pre>");
        map.put(Msg.ABOUT, "<b>¿Qué es RandomNext?</b>\n\n"
                + "Es un chat donde puedes conversar con otras personas de manera anónima. Cada vez que hagas <b>Next!</b> " +
                "te conectará con una persona distinta.\n\n" +
                "Si tienes dudas o sugerencias, puedes ingresar al chat de la aplicación:\n\n" +
                "https://t.me/RandomNextChat \n\n" +
                "<b>¿Te sobran Bitcoins?</b>\n Dónalo al proyecto.\n Wallet: <pre>1JC6Y1XGqcMZyXjKtMxvomWsCuBRT2KXM5</pre>");
        map.put(Msg.NEXT_YOU, "<pre>Te han hecho <b>Next!</b> Tú haz <b>Next!</b> para encontrar tu Random.</pre>");
        map.put(Msg.INACTIVITY_USER, "<pre>Se ha pausado tu bot por inactividad, presiona <b>PLAY</b> para continuar chateando.</pre>");
        map.put(Msg.SET_LANG_OK, "<pre>Idioma definido: </pre>");
        map.put(Msg.LANG, "<pre>Escribe /lang seguido del idioma de tu preferencia. Idiomas disponibles actualmente 'es' (Español) y 'en' (English)\n\n" +
                "Ejemplo: /lang es</pre>");

        map.put(Msg.USER_ACTIVE, "Usuarios activos: ");
        map.put(Msg.NEW_BIO, "No he cambiado mi bio, soy ");

        return map;
    }

    private Map<Msg, String> en() {
        Map<Msg, String> map = new HashMap<>();
        map.put(Msg.START_OK, "<b>Welcome!</b>\n\n" +
                "With this Bot you can meet new people in your same language while maintaining your anonymity.\n\n" +
                "- To start, you must press the <b>Play</b>.\n\n" +
                "- Subsequently change your bio (/bio your-bio).\n\n" +
                "- If you don't want to chat with someone else, press the <b>Next!</b> button and you'll change someone else.\n\n" +
                "- If you liked <b>Block</b> and it will be locked for you.\n\n" +
                "- If it is SPAM, mark it as <b>SPAM</b> to eject it from the community.\n\n" +
                "- Remember that both you and everyone are anonymous and the only way to identify yourself is with the bio, therefore, you can change it as many times as you want and leave no traces.\n\n" +
                "- If you want to rest or sleep, press the <b> Pause </b> button.\n\n" +
                "- If you want to change to a community in another language, type <i>/lang language</i> (Español /lang es, english /lang en).\n\n" +
                "- For recommendations, questions, etc., contact me by the group https://t.me/RandomNextChat \n\n" +
                "- Finally remember <b>not to give personal data or passwords to strangers.</b>\n\n" +
                "Enjoy it!, \n\n" +
                "                    <i>Julian</i>");
        map.put(Msg.START_AGAIN, "Welcome again! :D \n\n" +
                "With this Bot you can meet new people in your same language while maintaining your anonymity.\n\n" +
                "- To start, you must press the <b>Play</b>.\n\n" +
                "- Subsequently change your bio (/bio your-bio).\n\n" +
                "- If you don't want to chat with someone else, press the <b>Next!</b> button and you'll change someone else.\n\n" +
                "- If you liked <b>Block</b> and it will be locked for you.\n\n" +
                "- If it is SPAM, mark it as <b>SPAM</b> to eject it from the community.\n\n" +
                "- Remember that both you and everyone are anonymous and the only way to identify yourself is with the bio, therefore, you can change it as many times as you want and leave no traces.\n\n" +
                "- If you want to rest or sleep, press the <b> Pause </b> button.\n\n" +
                "- If you want to change to a community in another language, type <i>/lang language</i> (Español /lang es, english /lang en)..\n\n" +
                "- For recommendations, questions, etc., contact me by the group https://t.me/RandomNextChat \n\n" +
                "- Finally remember <b>not to give personal data or passwords to strangers.</b>\n\n" +
                "Enjoy it!, \n\n" +
                "                    <i>Julian</i>");
        map.put(Msg.START_BANNED_USER, "<pre>Sorry, you have been blocked from the community for accumulating complaints against you :(</pre>");
        map.put(Msg.USER_PLAY, "<pre>You are active, press <b>Next!</b> to search for your new Random.</pre>");
        map.put(Msg.USER_PAUSE, "<pre>You are paused, you will not receive chats or notifications for this chat.</pre>");
        map.put(Msg.USER_REPORT, "<pre>Your Random has been reported as SPAM, you will not see it again here!</pre>");
        map.put(Msg.USER_1_NEXT_OK, "<pre>NEXT! Say hello to your new Random.\n" +
                "His bio is: </pre>");
        map.put(Msg.USER_2_NEXT_OK, "<pre>NEXT! Say hello to your new Random.\n" +
                "His bio is: </pre>");
        map.put(Msg.USER_NEXT_WAITING, "<pre>NEXT! But no one is available to chat, expect them to find you :D</pre>");
        map.put(Msg.USER_NO_CHAT, "<pre>You are not in any chat, expect to be found or press <b>NEXT!</b>.</pre>");
        map.put(Msg.SET_BIO_OK, "<pre>You have modified your bio: </pre>");
        map.put(Msg.BIO, "<pre>Write a /bio that describes you no more than 140 characters.\n"
                + "Example: /bio I am a woman from Valencia, Spain, I am 24 years old. I love to run and have fun!</pre>");
        map.put(Msg.CONFIG, "<pre>Enter one of the options to be configured or <b>Play</b> to start.</pre>");
        map.put(Msg.CHAT_TIMEOUT, "<pre>Chat time is up, press <b>Next!</b> for a new Random.</pre>");
        map.put(Msg.USER_BLOCK, "<pre>Your Random has been blocked, you will not see it again here!\n"
                + "Press <b>Next!</b> again for a Random.</pre>");
        map.put(Msg.ABOUT, "<b>What is RandomNext?</b>\n\n"
                + "It's a chat where you can chat with other people anonymously. Every time you do <b>Next!</b> " +
                "will connect you with a different person. \n\n" +
                "If you have questions or suggestions, you can enter the application chat: \n\n" +
                "https://t.me/RandomNextChat \n\n" +
                "<b>Do you have enough Bitcoins?</b>\n Give it to the project.\n Wallet: <pre>1JC6Y1XGqcMZyXjKtMxvomWsCuBRT2KXM5</pre>");
        map.put(Msg.NEXT_YOU, "<pre>It has made you <b>Next!</b> You do <b>Next!</b> to find your Random.</pre>");
        map.put(Msg.INACTIVITY_USER, "<pre>Your bot has been paused due to inactivity, press <b>PLAY</b> to continue chatting.</pre>");
        map.put(Msg.SET_LANG_OK, "<pre>Language defined: </pre>");
        map.put(Msg.LANG, "<pre>Write /lang followed by the language of your choice. Currently available languages 'es' (Español) and 'en' (English).\n\n" +
                "Example: /lang es</pre>");
        map.put(Msg.USER_ACTIVE, "Active users: ");
        map.put(Msg.NEW_BIO, "I don't changed my bio, I'm ");

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
