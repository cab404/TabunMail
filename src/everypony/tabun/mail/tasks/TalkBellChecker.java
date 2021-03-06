package everypony.tabun.mail.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.requests.TalkBellRequest;
import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.util.exceptions.RequestFail;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TalkBellChecker extends Thread {
    private final Context context;
    private AccessProfile user;
    /* Работает ли еще поток. */
    private boolean cancelled;

    /* Время между обновлениями, в миллисекундах. */
    private final static int UPDATE_RATE = 15000;

    public TalkBellChecker(Context context, String token) {
        this.context = context.getApplicationContext();
        user = AccessProfile.parseString(token);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run() {

        TabunPage page = new TabunPage();
        long last_launched = 0;

        while (!cancelled) {
            if (page.key == null) page.fetch(user);

            if (System.currentTimeMillis() - last_launched > UPDATE_RATE)
                last_launched = System.currentTimeMillis();
            else continue;

            if (Au.isNetAvailable(context)) {
                TalkBellRequest bell = new TalkBellRequest();

                try {
                    bell.exec(user, page);
                } catch (RequestFail rf) {

                    Au.e(this, "Проблемы с сетью.", rf);
                    continue;

                } catch (Exception e) {

                    page = new TabunPage();

                    try {
                        page.fetch(user);
                    } catch (RequestFail rf) {
                        Au.e(this, "Проблемы с сетью.", rf);
                        continue;
                    }

                    if (page.c_inf == null) {
                        Au.e(this, "Токен сломан, отключаюсь.");
                        break;
                    } else {
                        Au.e(this, "Странная ошибка, токен работает но bell.exec() возвратил ошибку.", e);
                    }

                }

                /* Если у нас есть хоть что-нибудь - отправляем в обработку. */
                if (bell.new_letters.size() > 0 || bell.responses.size() > 0)
                    processNotifications(bell);

            } else {
                Au.e(this, "У нас проблемы с сетью...");
            }

        }
        Au.i(this, "Меня выключили.");

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
        builder.setVibrate(new long[]{300, 300});
        builder.setAutoCancel(true);

        return builder;
    }

    /**
     * Смотрит, что пришло от сервера и выводит уведомления.
     */
    public void processNotifications(TalkBellRequest req) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (Letter letter : req.responses) {
            Au.i(this, "Я получила письмо о том, что у тебя ответ на комментарий!");
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
            Au.i(this, "Я получила письмо о том, что у тебя новое письмо!");
            Notification.Builder builder = notificationTemplate(letter);

            builder.setContentTitle(context.getResources().getString(R.string.Mail_Notification_Letter));
            builder.setTicker(context.getResources().getString(R.string.Mail_Notification_Letter));

            manager.notify(
                    letter.id,
                    builder.getNotification()
            );
        }

    }
}
