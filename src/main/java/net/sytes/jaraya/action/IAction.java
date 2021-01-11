package net.sytes.jaraya.action;

import net.sytes.jaraya.vo.MessageChat;

public interface IAction {

    IAction exec(MessageChat message);

    boolean check(MessageChat message);

}
