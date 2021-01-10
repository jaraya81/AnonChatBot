package net.sytes.jaraya.action;

import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.vo.MessageChat;

public interface IAction {

    IAction exec(MessageChat message) throws TelegramException;

    boolean check(MessageChat message);

}
