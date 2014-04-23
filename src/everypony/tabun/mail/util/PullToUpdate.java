package everypony.tabun.mail.util;

import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.moonlight.util.U;

/**
* @author cab404
*/
public class PullToUpdate extends AsyncTask<Void, Void, Void> {
    ViewGroup p2upd;
    View header;
    boolean isTouched = false;

    public PullToUpdate(ViewGroup p2upd) {
        this.p2upd = p2upd;
        header = p2upd.getChildAt(0);

        p2upd.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                view.onTouchEvent(motionEvent);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    U.v("DOWN");
                    isTouched = true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    U.v("UP");
                    isTouched = false;
                }
                return true;
            }
        });
    }

    @Override protected Void doInBackground(Void... scrollViews) {
        while (p2upd.isEnabled()) {
            try {
                Thread.sleep(1000 / 30);
                publishProgress();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        int top = header.getTop();
        U.v(top);
        int height = header.getHeight();

        if (top < height & !isTouched) {
            int off = (int) ((height - top) / 5f);
            Au.setY(p2upd, off + top);
        }


        if (top == 0) {
            U.v("Yup!");
            isTouched = false;
        }

    }

}
