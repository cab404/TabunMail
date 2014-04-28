package everypony.tabun.mail.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import com.cab404.moonlight.framework.AccessProfile;

/**
 * Android utils и другие.
 *
 * @author cab404
 */
public class Au {

    public static AccessProfile user;

    public static boolean isNetAvailable(Context context) {
        ConnectivityManager activeNetworkInfo = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return !(activeNetworkInfo.getActiveNetworkInfo() == null || !activeNetworkInfo.getActiveNetworkInfo().isAvailable());
    }

    public static void setX(View view, int value) {
        if (Build.VERSION.SDK_INT >= 11)
            view.setX(value);
        else
            view.offsetLeftAndRight(value - view.getScrollX());
    }

    public static void setY(View view, int value) {
        if (Build.VERSION.SDK_INT >= 11)
            view.setY(value);
        else
            view.offsetTopAndBottom(value - view.getScrollY());
    }

    private static String getClassName(Object obj) {
        return obj == null ? "NULL" : obj.getClass().getSimpleName();
    }

    /**
     * Труба к Log.i
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void i(Object object, String text) {
        Log.i(getClassName(object), text);
    }

    /**
     * Труба к Log.i
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void i(Object object, String text, Throwable t) {
        Log.i(getClassName(object), text, t);
    }


    /**
     * Труба к Log.e
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void e(Object object, String text) {
        Log.e(getClassName(object), text);
    }

    /**
     * Труба к Log.e
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void e(Object object, String text, Throwable t) {
        Log.e(getClassName(object), text, t);
    }


}
