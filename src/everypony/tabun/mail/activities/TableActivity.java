package everypony.tabun.mail.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.pages.LetterTablePage;
import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.util.SU;
import everypony.tabun.auth.TabunAccount;
import everypony.tabun.auth.TabunTokenGetterActivity;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;

/**
 * @author cab404
 */
public class TableActivity extends Activity {

    private static final int TOKEN_REQUEST_CODE = 42;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(this, TabunTokenGetterActivity.class), TOKEN_REQUEST_CODE);
    }

    public void init() {
        setContentView(R.layout.main);
        new InitTask().execute();

    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TOKEN_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
                return;
            }
            Au.user = AccessProfile.parseString(data.getStringExtra(TabunAccount.COOKIE_TOKEN_TYPE));
            init();
        }

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
            LayoutInflater inflater = getLayoutInflater();

            LinearLayout list = (LinearLayout) findViewById(R.id.list);
            list.removeAllViews();
            Resources res = getResources();

            for (Letter letter : page.letters) {
                View label = inflater.inflate(R.layout.mail, list, false);
                assert label != null;

                TextView title = (TextView) label.findViewById(R.id.title);
                TextView about = (TextView) label.findViewById(R.id.about);
                TextView comments = (TextView) label.findViewById(R.id.comments);
                TextView new_comments = (TextView) label.findViewById(R.id.comments_new);


                title.setText(letter.title);
                int cut = res.getInteger(R.integer.Mail_Label_Cut);
                if (letter.recipients.size() > cut + 1) {
                    int i = letter.recipients.size() - cut;
                    about.setText(
                            SU.join(letter.recipients.subList(0, cut), ", ")
                                    + String.format(res.getString(R.string.Mail_Label_AndOther), i)
                    );
                } else
                    about.setText(SU.join(letter.recipients, ", "));


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


                list.addView(label);
            }


        }
    }

}