package everypony.tabun.mail.activities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TalkBellService extends Service {
    public IBinder onBind(Intent intent) {
        Au.v(this, "onBind()");
        return null;
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {

        Au.v(this, "onStartCommand()");

        return START_STICKY_COMPATIBILITY;
    }

    private class TalkBell extends AsyncTask<Void, Void, Void> {

        @Override protected Void doInBackground(Void... voids) {


            return null;
        }

    }
}
