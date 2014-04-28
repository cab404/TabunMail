package everypony.tabun.mail.tasks;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import com.cab404.libtabun.requests.TalkBellRequest;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TalkBellService extends Service {
    AsyncTask<Void, TalkBellRequest, Void> checker;
    public static final String TOKEN = "token";

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Au.i(this, "onStart()");

        if (intent == null) {
            Au.i(this, "Не могу запустить обновления с сервера, передан пустой intent.");
            if (checker != null && !(checker.getStatus() == AsyncTask.Status.FINISHED || checker.isCancelled()))
                Au.i(this, "Тем не менее, по всей видимости он всё ещё работает.");
        }

        if (checker == null || checker.getStatus() == AsyncTask.Status.FINISHED || checker.isCancelled())
            checker = new TalkBellChecker(this, intent.getStringExtra(TOKEN)).execute();

        return START_NOT_STICKY;

    }

    @Override public void onDestroy() {
        Au.i(this, "onDestroy()");

        checker.cancel(false);
    }

    @Override public IBinder onBind(Intent intent) {
        Au.i(this, "onBind()");

        return null;
    }
}
