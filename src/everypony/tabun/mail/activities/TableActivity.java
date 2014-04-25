package everypony.tabun.mail.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.libtabun.pages.LetterTablePage;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;
import everypony.tabun.mail.util.SpyView;

/**
 * @author cab404
 */
public class TableActivity extends AbstractMailActivity {

    /**
     * Загружаем список писем.
     */

    protected void init() {
        super.init();

        setContentView(R.layout.main);
        showProgressBar();
        setProgress(-1);

        new InitTask().execute();
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

    protected class InitTask extends AsyncTask<Void, LetterLabel, Void> {
        LetterTablePage page;

        @Override protected Void doInBackground(Void... voids) {
            page = new LetterTablePage(1) {
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
            return null;
        }

        int progress = 0;
        @Override protected void onProgressUpdate(LetterLabel... values) {
            super.onProgressUpdate(values);

            LinearLayout list = getList();
            LayoutInflater inflater = getLayoutInflater();

            for (final LetterLabel letter : values) {
                setProgress((float) progress++ / 100f);

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


        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setProgress(1f);
            hideProgressBar();

            getList().addView(new SpyView(TableActivity.this));
        }
    }

}