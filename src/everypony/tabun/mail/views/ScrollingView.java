package everypony.tabun.mail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Немного видоизменённый ScrollView, форкающий скроллинг в обработчик.
 *
 * @author cab404
 */
public class ScrollingView extends ScrollView {

    public ScrollingView(Context context) {
        super(context);
    }
    public ScrollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ScrollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private ScrollHandler handler;

    public void setHandler(ScrollHandler handler) {
        this.handler = handler;
    }

    public interface ScrollHandler {
        /**
         * Сюда напрямую шлются форкнутые данные из onScrollChanged.
         */
        void onScrolled(int y, int old_y);
        /**
         * Сюда напрямую шлются форкнутые данные из onOverScroll.
         * Если в onTouchEvent появился ACTION_UP, то сюда шлется (-100, false)
         */
        void onOverScrolled(float y, boolean clamped);
    }

    @Override public boolean onTouchEvent(MotionEvent ev) {
        boolean b = super.onTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (handler != null)
                handler.onOverScrolled(-100, false); // Небольшой хак для отключения обновлялки.
        } else {

            if (ev.getHistorySize() > 0) {

                MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                MotionEvent.PointerCoords old_coords = new MotionEvent.PointerCoords();

                ev.getPointerCoords(0, coords);
                ev.getHistoricalPointerCoords(0, ev.getHistorySize() - 1, old_coords);

                delta = coords.y - old_coords.y;
            }
        }


        return b;
    }

    private float delta = 0; // Хранит delta Y.
    @Override protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (handler != null)
            handler.onOverScrolled(delta, clampedY);
    }

    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (handler != null)
            handler.onScrolled(t, oldt);
    }
}


