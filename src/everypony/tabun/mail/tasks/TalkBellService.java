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

        Au.v(this, "onStart()");
        if (checker == null || checker.isCancelled())
            checker = new TalkBellChecker(this, intent.getStringExtra(TOKEN)).execute();

        return START_REDELIVER_INTENT;

    }

    @Override public void onDestroy() {
        Au.v(this, "onDestroy()");
        super.onDestroy();
        checker.cancel(false);
    }

    @Override public IBinder onBind(Intent intent) {
        Au.v(this, "onBind()");
        return null;
    }
}
