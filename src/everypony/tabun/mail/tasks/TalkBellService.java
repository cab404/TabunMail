package everypony.tabun.mail.tasks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TalkBellService extends Service {
    public static final String TOKEN = "token";

    TalkBellChecker checker;

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Au.i(this, "onStart()");

        if (intent == null) {
            Au.i(this, "Не могу запустить обновления с сервера, передан пустой intent.");
            if (checker != null && checker.isAlive())
                Au.i(this, "Тем не менее, по всей видимости он всё ещё работает.");
            return START_NOT_STICKY;
        }


        /** В intent-е что-то есть, запустим поток, если его нет. */

        if (checker == null || !checker.isAlive() || checker.isCancelled()) {
            checker = new TalkBellChecker(this, intent.getStringExtra(TOKEN));
            checker.start();
        }

        return START_NOT_STICKY;

    }

    @Override public void onDestroy() {
        Au.i(this, "onDestroy()");

        checker.cancel();
    }

    @Override public IBinder onBind(Intent intent) {
        Au.i(this, "onBind()");

        return null;
    }
}
