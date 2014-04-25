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
        Au.v(this, "Открываю письмо " + letter_id);

        // Запускаем загрузку.

        new InitTask().execute();
        showProgressBar();
    }


    HashMap<Integer, Integer> levels;

    protected void addComment(Comment comment) {
        int level = 0;
        if (comment.parent != 0) {
            level = levels.get(comment.parent) + 1;
        }
        levels.put(comment.id, level);

    }

    private class InitTask extends AsyncTask<Void, Object, Void> {

        @Override protected Void doInBackground(Void... voids) {
            new LetterPage(letter_id) {

                @Override protected void bindParsers(ModularBlockParser base) {
                    super.bindParsers(base);
                    base.bind(new CommentNumModule(), BLOCK_COMMENT_NUM);
                }

                @Override public void handle(Object object, int key) {
                    super.handle(object, key);
                    publishProgress(key, object);

                }
            }.fetch(Au.user);
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
//                    label.setAlpha(0);
//                    label.animate().alpha(1).setDuration(100);

                    setBarTitle(letter.title);

                    PartUtils.dumpIntoLetterLabel(label, letter);

//                    ViewGroup viewGroup = (ViewGroup) label.findViewById(R.id.content);
//                    TextView view = new TextView(TalkActivity.this);
//                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.Mail_Label_Primary_Size));
//                    view.setTextColor(getResources().getColor(R.color.Mail_Label_Primary_Color));
//                    view.setText(letter.text);
//                    viewGroup.addView(view);


//                    ((TextView) label.findViewById(R.id.starter_nick)).setText(letter.starter.login);
//                    ((TextView) label.findViewById(R.id.recipients)).setText(SU.join(letter.recipients, ", "));

                    getList().addView(label);

                }
                break;

                case TabunPage.BLOCK_COMMENT: {
                    Comment comment = (Comment) obj;

                    View label = inflater.inflate(R.layout.comment, getList(), false);
//                    label.setAlpha(0);
//                    label.animate().alpha(1).setDuration(100);

                    label.findViewById(R.id.new_mark).setVisibility(comment.is_new ? View.VISIBLE : View.INVISIBLE);

                    ViewGroup viewGroup = (ViewGroup) label.findViewById(R.id.content);
                    TextView view = new TextView(TalkActivity.this);
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.Mail_Label_Primary_Size));
                    view.setTextColor(getResources().getColor(R.color.Mail_Label_Primary_Color));
                    view.setText(comment.text);
                    viewGroup.addView(view);

                    ((TextView) label.findViewById(R.id.starter_nick)).setText(comment.author.login);
                    ((TextView) label.findViewById(R.id.recipients)).setText(PartUtils.convertDate(comment.date));

                    getList().addView(label);
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
            hideProgressBar();
        }
    }
}