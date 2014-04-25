package everypony.tabun.mail.activities;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cab404.moonlight.framework.AccessProfile;
import everypony.tabun.mail.R;
import everypony.tabun.mail.tasks.TalkBellService;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class AbstractMailActivity extends Activity {
    private static final int TOKEN_REQUEST_CODE = 42;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Au.user == null)
            try {
                // Запрашиваем токен
                startActivityForResult(new Intent("everypony.tabun.auth.TOKEN_REQUEST"), TOKEN_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // Нет Табун.Auth, предлагаем скачать.
                Intent download = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=everypony.tabun.auth")
                );
                startActivity(download);
                finish();
            }
        else
            init();

    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TOKEN_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
                return;
            }
            Au.user = AccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
            init();
        }

    }

    @Override protected void onRestart() {
        super.onRestart();
        if (Au.user != null) {
            startTalkBellService();
        }
    }

    /**
     * Запускает обновлялку писем.
     */
    protected void startTalkBellService() {
        // Запускаем обновлялку писем.
        Intent intent = new Intent(this, TalkBellService.class);
        intent.putExtra("token", Au.user.serialize());
        startService(intent);
    }

    protected void hideProgressBar() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.animate().alpha(0).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {

            }
            @Override public void onAnimationEnd(Animator animator) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            @Override public void onAnimationCancel(Animator animator) {

            }
            @Override public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    protected void showProgressBar() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.animate().alpha(1).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationEnd(Animator animator) {

            }
            @Override public void onAnimationCancel(Animator animator) {

            }
            @Override public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    protected void setProgress(float progress) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (progress < 0)
            progressBar.setIndeterminate(true);
        else {
            progressBar.setIndeterminate(false);
            progressBar.setProgress((int) (1000f * progress));
        }
    }

    protected void setBarTitle(CharSequence title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }


    /**
     * Выполняется после того, как пользователь был получен или уже существует.
     */
    protected void init() {
        Au.v(this, "init()");
        setContentView(R.layout.main);
    }

    protected LinearLayout getList() {
        return (LinearLayout) findViewById(R.id.list);
    }

}
