package everypony.tabun.mail.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.cab404.libtabun.pages.LetterPage;
import everypony.tabun.mail.R;

import java.util.List;

/**
 * @author cab404
 */
public class TalkActivity extends AbstractMailActivity {


    int id;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Intent intent = getIntent();
        Uri data = intent.getData();
        assert data != null;
        List<String> path = data.getPathSegments();
        id = Integer.parseInt(path.get(path.size() - 1));

    }

    @Override protected void init() {
        super.init();

    }

    private class InitTask extends AsyncTask<Void, Object, Void> {

        @Override protected Void doInBackground(Void... voids) {

            LetterPage page = new LetterPage(id) {
                @Override public void handle(Object object, int key) {
                    super.handle(object, key);


                }
            };

            return null;


        }


    }
}