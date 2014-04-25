package everypony.tabun.mail.util;

import android.content.Context;
import android.view.View;

/**
 * Сообщает о том, что его увидели.
 *
 * @author cab404
 */
public class SpyView extends View {

    public SpyView(Context context) {
        super(context);
//        this.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
    }

    @Override public void refreshDrawableState() {
        Au.v(this, "refreshDrawableState()");
        super.refreshDrawableState();
    }

    @Override protected void onDisplayHint(int hint) {
        Au.v(this, "onDisplayHint(" + hint + ")");
        super.onDisplayHint(hint);
    }

    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Au.v(this, "onScrollChanged()");
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
