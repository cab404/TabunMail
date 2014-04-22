package everypony.tabun.mail.activities;

import android.app.Application;
import android.content.Intent;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class MailApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        Au.v(this, "onCreate()");
        startService(new Intent(this, TalkBellService.class));
    }


}
