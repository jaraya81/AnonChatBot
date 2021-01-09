package net.sytes.jaraya.service;

import net.sytes.jaraya.exception.TelegramException;

public class AnonChatService {

    public AnonChatService() throws TelegramException {
        this.user = new UserService();
        this.tag = new TagService();
        this.chat = new ChatService(user);
        this.report = new ReportService(user);
    }

    public final TagService tag;
    public final ChatService chat;
    public final UserService user;
    public final ReportService report;

}
