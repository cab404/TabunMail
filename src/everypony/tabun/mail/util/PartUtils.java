package everypony.tabun.mail.util;

import android.content.Context;
import android.content.res.Resources;
import com.cab404.libtabun.data.Letter;
import com.cab404.moonlight.util.SU;
import everypony.tabun.mail.R;

/**
 * @author cab404
 */
public class PartUtils {

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

}
