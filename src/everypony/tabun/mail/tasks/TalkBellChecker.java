package everypony.tabun.mail.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.requests.TalkBellRequest;
import com.cab404.moonlight.framework.AccessProfile;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TalkBellChecker extends AsyncTask<Void, TalkBellRequest, Void> {
    private AccessProfile user;

    private final static int UPDATE_RATE = 10000;

    private Context context;

    public TalkBellChecker(Context context, String token) {
        user = AccessProfile.parseString(token);
        this.context = context.getApplicationContext();
    }

    @Override protected Void doInBackground(Void... voids) {
        Au.v(this, "doInBackground()");

        TabunPage page = new TabunPage();
        page.fetch(user);

        long last_launched = 0;

        while (!isCancelled()) {

            if (System.currentTimeMillis() - last_launched > 10000)
                last_launched = System.currentTimeMillis();
            else continue;

            if (Au.isNetAvailable(context) && user != null) {
                TalkBellRequest bell = new TalkBellRequest();

                try {
                    bell.exec(user, page);
                } catch (Exception e) {

                    page = new TabunPage();
                    page.fetch(user);

                    if (page.c_inf == null) {
                        Au.v(this, "Токен сломан, отключаюсь.");
                        cancel(false);
                    }

                }

                if (bell.new_letters.size() > 0 || bell.responses.size() > 0)
                    publishProgress(bell);

            } else {
                Au.v(this, "problems...");
            }

        }
        Au.v(this, "Меня выключили.");
        return null;
    }

    protected Notification.Builder notificationTemplate(Letter letter) {
        Notification.Builder builder = new Notification.Builder(context);

        PendingIntent link =
                PendingIntent.getActivity(
                        context,
                        0,
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://tabun.everypony.ru/talk/read/" + letter.id)
                        ),
                        0
                );


        builder.setSmallIcon(R.drawable.ic_mail);
        builder.setContentText(letter.title);
        builder.setContentIntent(link);
        builder.setLights(Color.WHITE, 1000, 1000);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{300});


        return builder;
    }

    @Override protected void onProgressUpdate(TalkBellRequest... values) {
        super.onProgressUpdate(values);
        TalkBellRequest req = values[0];
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (Letter letter : req.responses) {
            Au.v(this, "Я получила письмо о том, что у тебя ответ на комментарий!");
            Notification.Builder builder = notificationTemplate(letter);

            builder.setContentTitle(context.getResources().getString(R.string.Mail_Notification_Comment));
            builder.setTicker(context.getResources().getString(R.string.Mail_Notification_Comment));
            builder.setNumber(letter.comments_new);

            manager.notify(
                    letter.id,
                    builder.getNotification()
            );
        }


        for (Letter letter : req.new_letters) {
            Au.v(this, "Я получила письмо о том, что у тебя новое письмо!");
            Notification.Builder builder = notificationTemplate(letter);

            builder.setContentTitle(context.getResources().getString(R.string.Mail_Notification_Letter));
            builder.setTicker(context.getResources().getString(R.string.Mail_Notification_Letter));
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            manager.notify(
                    letter.id,
                    builder.getNotification()
            );
        }

    }

}
