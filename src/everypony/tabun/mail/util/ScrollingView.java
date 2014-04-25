package everypony.tabun.mail.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
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

    @Override protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        Au.v(this, "onOverScrolled(" + scrollY + ", " + clampedY + ")");
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }
    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Au.v(this, "onScrollChanged(" + t + ")");
        super.onScrollChanged(l, t, oldl, oldt);
    }
}


