package com.jesson.belle.event;

/**
 * Created by zhangdi on 14-3-7.
 */
public class NetworkErrorEvent {

    public Exception e;

    public NetworkErrorEvent() {
    }

    public NetworkErrorEvent(Exception e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "NetworkErrorEvent{" +
                "e=" + e +
                '}';
    }
}
