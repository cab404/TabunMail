package everypony.tabun.mail.util;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.libtabun.util.Tabun;
import com.cab404.moonlight.util.SU;
import everypony.tabun.mail.R;

import java.util.Calendar;

/**
 * @author cab404
 */
public class PartUtils {

    public static class AnLisImpl implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animator) {}
        @Override public void onAnimationEnd(Animator animator) {}
        @Override public void onAnimationCancel(Animator animator) {}
        @Override public void onAnimationRepeat(Animator animator) {}
    }

    public static String buildRecipients(Context context, Letter letter) {
        int cut = context.getResources().getInteger(R.integer.Mail_Label_Cut);

        if (letter.recipients.size() > cut + 1) {
            int i = letter.recipients.size() - cut;
            return (
                    SU.join(letter.recipients.subList(0, cut), ", ")
                            + String.format(context.getString(R.string.Mail_Label_AndOther), i)
            );
        } else
            return (SU.join(letter.recipients, ", "));

    }

    public static int dpToPx(int px, Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics().densityDpi * px;
    }

    public static String convertDate(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH)
                + " "
                + Tabun.months.get(calendar.get(Calendar.MONTH))
                + " "
                + calendar.get(Calendar.YEAR)
                + " года, "
                + calendar.get(Calendar.HOUR)
                + ":"
                + (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE)
                + ":"
                + (calendar.get(Calendar.SECOND) < 10 ? "0" : "") + calendar.get(Calendar.SECOND);
    }

    /**
     * Загружает данные письма в частичный
     */
    public static void dumpIntoLetterLabel(View label, LetterLabel letter) {
        Resources res = label.getResources();

        // Достаём ресурсы.
        TextView title = (TextView) label.findViewById(R.id.title);
        TextView about = (TextView) label.findViewById(R.id.about);
        TextView comments = (TextView) label.findViewById(R.id.comments);
        TextView new_comments = (TextView) label.findViewById(R.id.comments_new);


        // Собираем заголовок.

        //// Текст и описание

        title.setText(letter.title);
        if (letter.is_new)
            title.setTypeface(Typeface.DEFAULT_BOLD);

        about.setText(PartUtils.buildRecipients(label.getContext(), letter));

        //// Индикаторы количества комментариев.
        comments.setText(""
                        + letter.comments
                        + " "
                        + res.getQuantityString(R.plurals.Mail_Label_Comments, letter.comments)
                        + (letter.comments_new > 0 ? ", " : "")
        );

        if (letter.comments_new > 0)
            new_comments.setText(""
                            + letter.comments_new
                            + " "
                            + res.getQuantityString(R.plurals.Mail_Label_NewComments, letter.comments_new)
            );
        else
            new_comments.setText("");

    }


}
