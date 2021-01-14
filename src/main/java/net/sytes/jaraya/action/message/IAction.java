package net.sytes.jaraya.action.message;

import net.sytes.jaraya.vo.BaseUpdate;

public interface IAction {

    IAction exec(BaseUpdate message);

    boolean check(BaseUpdate message);

}
