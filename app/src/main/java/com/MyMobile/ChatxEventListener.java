package com.MyMobile;

import java.util.EventListener;
import java.util.Vector;

/**
 * Created by zhutong on 2016/5/26.
 */
public interface ChatxEventListener extends EventListener {
    public void OnChatEvtNewMsg(String msg);
    public void OnChatEvtConnected();
    public void OnChatEvtDisConnected();
    public void OnNoAgents(String reason);
}
