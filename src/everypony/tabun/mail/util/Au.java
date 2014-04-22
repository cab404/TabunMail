package everypony.tabun.mail.util;

import android.util.Log;
import com.cab404.moonlight.framework.AccessProfile;

/**
 * Android utils и другие.
 *
 * @author cab404
 */
public class Au {

    public static AccessProfile user;

    private static String getClassName(Object obj) {
        return obj == null ? "NULL" : obj.getClass().getSimpleName();
    }

    /**
     * Труба к Log.v
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void v(Object object, String text) {
        Log.v(getClassName(object), text);
    }

    /**
     * Труба к Log.v
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void v(Object object, String text, Throwable t) {
        Log.v(getClassName(object), text, t);
    }


    /**
     * Труба к Log.w
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void w(Object object, String text) {
        Log.w(getClassName(object), text);
    }

    /**
     * Труба к Log.w
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void w(Object object, String text, Throwable t) {
        Log.w(getClassName(object), text, t);
    }


    /**
     * Труба к Log.w
     * Записывает данные в лог, используя простое имя объекта, как тег.
     */
    public static void w(Object object, Throwable t) {
        Log.w(getClassName(object), t);
    }

}
