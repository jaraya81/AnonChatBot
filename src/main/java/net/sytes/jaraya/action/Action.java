package net.sytes.jaraya.action;

import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.vo.MessageChat;

public interface Action {

    Action exec(MessageChat message) throws TelegramException;

}
