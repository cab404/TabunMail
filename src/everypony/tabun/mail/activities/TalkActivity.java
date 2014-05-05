package everypony.tabun.mail.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.modules.CommentNumModule;
import com.cab404.libtabun.pages.LetterPage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;
import everypony.tabun.mail.util.TextEscaper;

import java.util.HashMap;
import java.util.List;

/**
 * @author cab404
 */
public class TalkActivity extends AbstractMailActivity {
    int letter_id;

    @Override protected void init() {
        super.init();

        // Достаём id письма.

        Intent intent = getIntent();
        Uri data = intent.getData();
        assert data != null;
        List<String> path = data.getPathSegments();
        letter_id = Integer.parseInt(path.get(path.size() - 1));
        Au.i(this, "Открываю письмо " + letter_id);

        // Запускаем загрузку.

        new InitTask().execute();
        showProgressBar();
    }


    HashMap<Integer, Integer> levels;
    {levels = new HashMap<>();}

    protected void addComment(Comment comment) {
        int level = 0;
        if (comment.parent != 0)
            level = levels.get(comment.parent) + 1;
        levels.put(comment.id, level);

        View label = getLayoutInflater().inflate(R.layout.comment, getList(), false);

        label.findViewById(R.id.new_mark).setVisibility(comment.is_new ? View.VISIBLE : View.INVISIBLE);

        ViewGroup viewGroup = (ViewGroup) label.findViewById(R.id.content);
        TextView view = new TextView(TalkActivity.this);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.Mail_Label_Primary_Size));
        view.setTextColor(getResources().getColor(R.color.Mail_Label_Primary_Color));
        view.setText(TextEscaper.simpleEscape(comment.text, label.getContext()));
        view.setLinksClickable(true);
        viewGroup.addView(view);

        ((TextView) label.findViewById(R.id.starter_nick)).setText(comment.author.login);
        ((TextView) label.findViewById(R.id.recipients)).setText(PartUtils.convertDate(comment.date));

        label.setPadding(
                label.getPaddingRight() + label.getPaddingRight() * level * 5,
                label.getPaddingTop(),
                label.getPaddingRight(),
                label.getPaddingLeft()
        );

        getList().addView(label);

    }

    private class InitTask extends AsyncTask<Void, Object, Void> {
        LetterPage page;

        @Override protected Void doInBackground(Void... voids) {
            page = new LetterPage(letter_id) {

                @Override protected void bindParsers(ModularBlockParser base) {
                    super.bindParsers(base);
                    base.bind(new CommentNumModule(), BLOCK_COMMENT_NUM);
                }

                @Override public void handle(Object object, int key) {
                    super.handle(object, key);
                    publishProgress(key, object);

                }
            };
            page.fetch(Au.user);
            return null;
        }

        int num = 0, loaded = 0;
        @Override protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            int key = (int) values[0];
            Object obj = values[1];

            LayoutInflater inflater = getLayoutInflater();

            switch (key) {
                case TabunPage.BLOCK_LETTER_HEADER: {
                    Letter letter = (Letter) obj;

                    View label = inflater.inflate(R.layout.letter_label, getList(), false);

                    setBarTitle(letter.title);

//                    PartUtils.dumpLetterLabel(label, letter);

                    getList().addView(label);

                }
                break;

                case TabunPage.BLOCK_COMMENT: {
                    Comment comment = (Comment) obj;

                    addComment(comment);

                    loaded++;
                    setProgress(loaded / (float) num);
                }
                break;

                case TabunPage.BLOCK_COMMENT_NUM:
                    num = (int) obj;
            }

        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (page.c_inf == null)
                requestToken();
            hideProgressBar();
            switchOverScrollHandling(true);

            getList().forceLayout();

        }
    }

    @Override protected void onOverscrollTriggered(boolean top) {
        Au.i(this, "Triggered.");
        smoothScrollTo(3);
    }

}