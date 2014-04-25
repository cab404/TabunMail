package everypony.tabun.mail;

import android.app.Application;
import com.cab404.moonlight.util.Logger;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class MailApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        com.cab404.moonlight.util.U.setLogger(new Logger() {
            @Override public void error(String str) {
                Au.w("libTabun", str);
            }
            @Override public void verbose(String str) {
                Au.v("libTabun", str);
            }
        });

    }

}
