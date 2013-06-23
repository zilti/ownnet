package ownnet.java;

import java.nio.file.*;

public interface WatchEventReceiver {

    public void receive(WatchEvent<?> event);

}
