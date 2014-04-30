package everypony.tabun.mail;

import android.app.Application;
import android.util.Log;
import com.cab404.moonlight.util.logging.Logger;

/**
 * @author cab404
 */
public class MailApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        // Устанавливаем логгер для вывода в Android.
        com.cab404.moonlight.util.logging.Log.setLogger(new Logger() {
            @Override public void error(String str) {
                Log.w("libTabun", str);
            }
            @Override public void verbose(String str) {
                Log.v("libTabun", str);
            }
        });

    }

}
