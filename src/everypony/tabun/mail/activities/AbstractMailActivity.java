package everypony.tabun.mail.activities;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cab404.moonlight.framework.AccessProfile;
import everypony.tabun.mail.R;
import everypony.tabun.mail.tasks.TalkBellService;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;
import everypony.tabun.mail.views.ScrollingView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class AbstractMailActivity extends Activity {
    private static final int TOKEN_REQUEST_CODE = 42;

    protected LinearLayout getList() {
        return (LinearLayout) findViewById(R.id.list);
    }
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Au.user == null)
            requestToken();
        else
            init();

        max_counter = getResources().getDisplayMetrics().density * 100f;

    }

    public LinearLayout getBar() {
        return (LinearLayout) findViewById(R.id.bar_contents);
    }

    public void requestToken() {
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
        if (Au.user != null)
            startTalkBellService();
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


    /**
     * Выполняется после того, как пользователь был получен или уже существует.
     */
    protected void init() {
        Au.i(this, "init()");
        setContentView(R.layout.main);
    }

      /*========================*/
     /* Код, отвечающий за бар */
    /*========================*/


    /**
     * Управляет значением progressBar-а в шапке. Значение от 0 до 1.
     * Если передано отрицательное значение, progressBar перейдёт в циклический режим.
     */
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

    protected void hideProgressBar() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.animate().alpha(0).setListener(new PartUtils.AnLisImpl() {
            @Override public void onAnimationEnd(Animator animator) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Fade-in-ает progressBar в шапке.
     */
    protected void showProgressBar() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.animate().alpha(1).setListener(new PartUtils.AnLisImpl() {
            @Override public void onAnimationStart(Animator animator) {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }


      /*===========================*/
     /* Код, отвечающий за скролл */
    /*===========================*/

    private int current_scroll = 0;

    // Кэшированные высоты.
    private List<Integer> heights;
    {heights = new ArrayList<>();}

    protected void onScrolled(int y, int old_y) {}
    protected int getCurrentScroll() { return current_scroll;}

    protected void smoothScrollTo(int index) { smoothScrollToPixel(heights.get(index));}
    protected void smoothScrollToPixel(int y) { ((ScrollView) findViewById(R.id.root)).smoothScrollTo(0, y);}

    protected void scrollTo(int index) { scrollToPixel(heights.get(index));}
    protected void scrollToPixel(int y) { findViewById(R.id.root).scrollTo(0, y);}

    protected void updateHeights() {
        LinearLayout list = getList();
        heights.clear();
        int h = 0;
        for (int i = 0; i < list.getChildCount(); i++) {
            heights.add(h);
            h += list.getChildAt(i).getHeight();
        }
    }

      /*=============================================================================================*/
     /* Дальше идёт код, контролирующий overscroll. Копытами и руками не трогать, громко не чихать. */
    /*=============================================================================================*/

    // Отвечает за включение/выключения overscroll-а.
    private boolean top_overscroll_working = false;
    private boolean bottom_overscroll_working = false;
    // Не слушаем обновления до тех пор, пока animate в hide не закончит скрывать бар.
    private boolean top_overscroll_switching = false;
    private boolean bottom_overscroll_switching = false;
    // Контролирует необходимую прокрутку в стену для обновления.
    // Вычисляется в создании в соответствии с DPI.
    private float max_counter;
    // Счетчик "длины касания края". Столько, сколько юзер прокручивал в стену.
    // Один на оба края. Для верка положительный, для низа отрицательный.
    // Нужен для обработки overscroll-ов.
    private float edge_counter = 0;
    // Пользователь должен отпустить экран перед тем, как снова запускать обновление.
    // Этот параметр будет в true после триггера и до ACTION_UP.
    private void onOverScrolled(float y, boolean clamped) {
        updateFiller();
        if (clamped) {

            if (y > 0 && top_overscroll_working && !top_overscroll_switching) {
                edge_counter = edge_counter < 0 ? 0 : edge_counter;
                edge_counter += y;
                setTopOverscrollBar((float) Math.sqrt(Math.abs(edge_counter) / max_counter));
            }

            if (y < 0 && bottom_overscroll_working && !top_overscroll_switching) {
                edge_counter = edge_counter > 0 ? 0 : edge_counter;
                edge_counter += y;
                setBottomOverscrollBar((float) Math.sqrt(Math.abs(edge_counter) / max_counter));
            }

        } else {
            // -100 указано в ScrollingView, magic number в данном случае. Активируется при ACTION_UP (отпустили экран).
            // А нам это и нужно. Если edge counter достиг нужной отметки к этому времени, то можно отправлять эвент в
            // onOverscrollTriggered.
            if (y == -100) {

                if (edge_counter >= +max_counter)
                    onOverscrollTriggered(true);

                if (edge_counter <= -max_counter)
                    onOverscrollTriggered(false);
            }

            hideTopOverscrollBar();
            hideBottomOverscrollBar();
            edge_counter = 0;
        }

    }

    protected void onOverscrollTriggered(boolean top) {
        Au.i(this, "Overscroll triggered " + (top ? "top" : "bottom"));
    }

    /**
     * Обновляет заполнитель до полного экрана в root-е.
     * Нужен для работы прокрутки даже если контент меньше экрана.
     * Вызывать при обновлениях контента в list и обновлении размеров экрана (при повороте).
     */
    protected void updateFiller() {
        int delta = findViewById(R.id.root).getHeight() - getList().getHeight() + 10; // Оставляем пиксели для скролла.

        if (delta > 0) {
            findViewById(R.id.root_filler).setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            delta
                    )
            );
            findViewById(R.id.root).setVerticalScrollBarEnabled(false);
        } else {
            findViewById(R.id.root_filler).setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0
                    )
            );
            findViewById(R.id.root).setVerticalScrollBarEnabled(true);
        }
    }

    /**
     * С помощью этой штуки можно полностью отключить весь хэндлинг и обработку scroll-а и overscroll-а.
     */
    protected void switchOverScrollHandling(boolean state) {
        if (state)
            ((ScrollingView) findViewById(R.id.root)).setHandler(new ScrollingView.ScrollHandler() {
                @Override public void onScrolled(int y, int old_y) {
                    AbstractMailActivity.this.onScrolled(y, old_y);
                    current_scroll = y;
                }
                @Override public void onOverScrolled(float y, boolean clamped) {
                    AbstractMailActivity.this.onOverScrolled(y, clamped);
                }
            });
        else
            ((ScrollingView) findViewById(R.id.root)).setHandler(null);
    }

    /**
     * Изменяет размер бара в шапке.
     */
    private void setTopOverscrollBar(float size) {
        if (!top_overscroll_switching)
            findViewById(R.id.top_overscroll_indicator)
                    .setScaleX(size);
    }

    /**
     * Изменяет размер бара на дне.
     */
    private void setBottomOverscrollBar(float size) {
        if (!bottom_overscroll_switching)
            findViewById(R.id.bottom_overscroll_indicator)
                    .setScaleX(size);
    }

    /**
     * Скрывает overscroll-бар в шапке.
     */
    private void hideTopOverscrollBar() {
        if (!top_overscroll_switching) {
            findViewById(R.id.top_overscroll_indicator)
                    .animate()
                    .scaleX(0)
                    .setListener(new PartUtils.AnLisImpl() {
                        @Override public void onAnimationStart(Animator animator) { top_overscroll_switching = true;}
                        @Override public void onAnimationEnd(Animator animator) { top_overscroll_switching = false; }
                    });
        }
    }
    /**
     * Скрывает бар на дне.
     */
    private void hideBottomOverscrollBar() {
        if (!bottom_overscroll_switching) {
            findViewById(R.id.bottom_overscroll_indicator)
                    .animate()
                    .scaleX(0)
                    .setListener(new PartUtils.AnLisImpl() {
                        @Override public void onAnimationStart(Animator animator) { bottom_overscroll_switching = true;}
                        @Override public void onAnimationEnd(Animator animator) {
                            bottom_overscroll_switching = false;
                        }
                    });
        }
    }

    /**
     * Включает или выключает бар прокрутки в топе.
     */
    protected void setTopOverscrool(boolean is_working) {
        this.top_overscroll_working = is_working;
    }

    /**
     * Включает или выключает бар прокрутки на дне.
     */
    protected void setBottomOverscrool(boolean is_working) {
        this.bottom_overscroll_working = is_working;
    }


}
