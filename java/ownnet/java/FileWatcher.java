package ownnet.java;

import java.nio.file.*;
import java.util.ArrayList;

public class FileWatcher {
  
  public static void watchDir(String dir, final WatchEventReceiver receiver) throws Exception {
    final WatchService watcher = FileSystems.getDefault().newWatchService();
    Path path = Paths.get(dir);
    WatchKey key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                 StandardWatchEventKinds.ENTRY_DELETE,
                                 StandardWatchEventKinds.ENTRY_MODIFY);
    (new Thread() {
	@Override
	public void run() {
	  for(;;) {
	    WatchKey key;
	    try {
	      key = watcher.take();
	    } catch (Exception e) {
	      return;
	    }

	    for(WatchEvent<?> event : key.pollEvents()) {
	      receiver.receive(event);
	    }
	    
	    if(!key.reset()) {
	      break;
	    }
	  }
	}
      }).run();
  }
}
