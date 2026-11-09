package application.util;
import javafx.application.Platform;
import java.util.concurrent.CopyOnWriteArrayList;
public final class DataChanges {
 private static final CopyOnWriteArrayList<Runnable> listeners=new CopyOnWriteArrayList<>();
 private DataChanges(){}
 public static AutoCloseable subscribe(Runnable listener){listeners.add(listener);return ()->listeners.remove(listener);}
 public static void publish(){for(Runnable listener:listeners)try{if(Platform.isFxApplicationThread())listener.run();else Platform.runLater(listener);}catch(IllegalStateException ignored){}}
}
