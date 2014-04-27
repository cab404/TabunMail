package everypony.tabun.mail.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.libtabun.pages.LetterTablePage;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author cab404
 */
public class TableActivity extends AbstractMailActivity {

    /**
     * Загружаем список писем.
     */

    int loaded_page_num = 1;
    HashMap<Integer, Integer> id_to_index;
    HashSet<Integer> selected;

    {
        id_to_index = new HashMap<>();
        selected = new HashSet<>();
    }

    protected void init() {
        super.init();
        new InitTask(loaded_page_num++).execute();
        initBar();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    //TODO: Добавить выделение/удаление/прочтение писем.
    protected void onLetterSelected(Letter letter) {
        Intent down = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://tabun.everypony.ru/talk/read/" + letter.id)
        );
        startActivity(down);
    }

    protected void onSelectionMode(Letter letter) {

    }

    protected void initBar() {
        LinearLayout bar = getBar();
        for (int i = 0; i < 5; i++) {
            ImageView icon = new ImageView(bar.getContext());
            icon.setImageResource(R.drawable.ic_action_mail_add);
            bar.addView(icon);
        }
    }

    protected void switchSelectionModeOff() {
        LinearLayout list = getList();
        selected.clear();

        for (int i = 0; i < list.getChildCount(); i++) {
            list
                    .getChildAt(i)
                    .findViewById(R.id.letter_label_root)
                    .setBackgroundResource(R.color.Mail_Label_BG);
        }
    }

    protected class InitTask extends AsyncTask<Void, LetterLabel, Void> {
        private int page_num;

        public InitTask(int page) {
            page_num = page;
        }

        @Override protected void onPreExecute() {
            setProgress(-1f);
            showProgressBar();
            switchOverScrollHandling(false);
        }

        @Override protected Void doInBackground(Void... voids) {

            LetterTablePage page;
            page = new LetterTablePage(page_num) {
                @Override public void handle(Object object, int key) {
                    super.handle(object, key);

                    switch (key) {
                        case BLOCK_LETTER_LABEL:
                            publishProgress((LetterLabel) object);
                            break;
                    }

                }

            };

            page.fetch(Au.user);
            if (page.c_inf == null)
                requestToken();
            return null;
        }

        int progress = 0;
        @Override protected void onProgressUpdate(LetterLabel... values) {
            super.onProgressUpdate(values);


            LinearLayout list = getList();
            LayoutInflater inflater = getLayoutInflater();
            setProgress((float) progress++ / 100f);
            final LetterLabel letter = values[0];
            id_to_index.put(letter.id, list.getChildCount());


            // Создаём заголовок.
            View label = inflater.inflate(R.layout.letter_label, list, false);

            PartUtils.dumpIntoLetterLabel(label, letter);

            label.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    onLetterSelected(letter);
                }
            });

            list.addView(label);

        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setProgress(1f);
            hideProgressBar();

            switchOverScrollHandling(true);
            setBottomOverscrool(true);
            setTopOverscrool(true);

            getList().forceLayout();
            updateFiller();
            updateHeights();

        }
    }

    @Override protected void onOverscrollTriggered(boolean top) {
        if (top) {
            getList().removeAllViews();
            loaded_page_num = 1;
            updateFiller();
            updateHeights();
        }
        new InitTask(loaded_page_num++).execute();
    }

}