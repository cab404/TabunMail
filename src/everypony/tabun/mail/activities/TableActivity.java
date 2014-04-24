package everypony.tabun.mail.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.pages.LetterTablePage;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;

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

    protected class InitTask extends AsyncTask<Void, Void, Void> {
        LetterTablePage page;

        @Override protected Void doInBackground(Void... voids) {
            page = new LetterTablePage(1);
            page.fetch(Au.user);
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressBar();
            LayoutInflater inflater = getLayoutInflater();

            LinearLayout list = getList();
            list.removeAllViews();

            Resources res = getResources();

            for (final Letter letter : page.letters) {
                // Создаём заголовок.
                View label = inflater.inflate(R.layout.letter_label, list, false);
                assert label != null;

                // Достаём ресурсы.
                TextView title = (TextView) label.findViewById(R.id.title);
                TextView about = (TextView) label.findViewById(R.id.about);
                TextView comments = (TextView) label.findViewById(R.id.comments);
                TextView new_comments = (TextView) label.findViewById(R.id.comments_new);


                // Собираем заголовок.


                //// Текст и описание
                title.setText(letter.title);
                about.setText(PartUtils.buildRecipients(TableActivity.this, letter));

                //// Индикаторы количества комментариев.
                comments.setText(""
                                + letter.comments
                                + " "
                                + res.getQuantityString(R.plurals.Mail_Label_Comments, letter.comments)
                                + (letter.comments_new > 0 ? ", " : "")
                );

                if (letter.comments_new > 0)
                    new_comments.setText(""
                                    + letter.comments_new
                                    + " "
                                    + res.getQuantityString(R.plurals.Mail_Label_NewComments, letter.comments_new)
                    );
                else
                    new_comments.setText("");

                label.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Intent down = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://tabun.everypony.ru/talk/read/" + letter.id)
                        );
                        startActivity(down);
                    }
                });

                list.addView(label);
            }

        }
    }

}