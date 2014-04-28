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
import com.cab404.libtabun.data.LivestreetKey;
import com.cab404.libtabun.pages.LetterTablePage;
import com.cab404.libtabun.requests.LetterListRequest;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;
import everypony.tabun.mail.util.PartUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author cab404
 */
public class TableActivity extends AbstractMailActivity {

    /**
     * Загружаем список писем.
     */

    int loaded_page_num = 1;
    HashMap<Integer, Integer> id_to_index;
    HashMap<Integer, LetterLabel> id_to_letter;
    HashSet<Integer> selected;
    /* Ключ страницы с таблицей. */
    LivestreetKey table_key;

    {
        id_to_letter = new HashMap<>();
        id_to_index = new HashMap<>();
        selected = new HashSet<>();
    }

    protected void init() {
        super.init();
        new LoadListTask(loaded_page_num++).execute();
        initCommonBar();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    //TODO: Добавить выделение/удаление/прочтение писем.
    protected void onLetterSelected(Letter letter) {
        if (selected.size() > 0) {
            setSelected(letter, !selected.contains(letter.id));
            if (selected.size() == 0)
                initCommonBar();
        } else {
            Intent down = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://tabun.everypony.ru/talk/read/" + letter.id)
            );
            startActivity(down);
        }
    }

    protected void onSelectionMode(Letter letter) {
        if (selected.size() == 0) {
            setSelected(letter, true);
            initSelectionBar();
        } else {
            switchSelectionModeOff();
        }
    }

    protected void setSelected(Letter letter, boolean selected) {
        if (selected)
            this.selected.add(letter.id);
        else
            this.selected.remove(letter.id);

        getList()
                .getChildAt(id_to_index.get(letter.id))
                .findViewById(R.id.letter_label_root)
                .setBackgroundResource(
                        selected ?
                                R.color.Mail_Label_Selected_BG
                                :
                                R.color.Mail_Label_BG
                );

    }

    /**
     * Кнопки работы со списками. Можно считать как onSelectionModeOn.
     */
    protected void initSelectionBar() {
        LinearLayout bar = getBar();
        bar.removeAllViews();
        ImageView icon;

        icon = new ImageView(bar.getContext());
        icon.setImageResource(R.drawable.ic_action_delete);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                new ExecActionsOnLetterList(LetterListRequest.Action.DELETE).execute();
            }
        });
        bar.addView(icon);

        icon = new ImageView(bar.getContext());
        icon.setImageResource(R.drawable.ic_action_done);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                new ExecActionsOnLetterList(LetterListRequest.Action.READ).execute();
            }
        });
        bar.addView(icon);

        icon = new ImageView(bar.getContext());
        icon.setImageResource(R.drawable.ic_action_select_all);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                for (int id : id_to_index.keySet()) {
                    Letter stub = new Letter();
                    stub.id = id;
                    setSelected(stub, true);
                }
            }
        });
        bar.addView(icon);

        switchOverScrollHandling(false);

    }

    /**
     * Стандартые кнопки для бара.
     */
    protected void initCommonBar() {
        LinearLayout bar = getBar();
        bar.removeAllViews();

        ImageView icon = new ImageView(bar.getContext());
        icon.setImageResource(R.drawable.ic_action_mail_add);
        bar.addView(icon);

        switchOverScrollHandling(true);
    }

    protected void switchSelectionModeOff() {
        for (int id : selected.toArray(new Integer[selected.size()])) {
            Letter stub = new Letter();
            stub.id = id;
            setSelected(stub, false);
        }
        initCommonBar();
    }

    /**
     * Отмечает письмо, как прочитанное.
     */
    protected void readLetter(int id) {
        LetterLabel label = id_to_letter.get(id);
        label.is_new = false;

        PartUtils.dumpIntoLetterLabel(getList().getChildAt(id_to_index.get(id)), label);
    }


    /**
     * Удаляет письмо, сдвигая все индексы.
     */
    protected void deleteLetter(int id) {
        int index = id_to_index.get(id);
        for (Map.Entry<Integer, Integer> e : id_to_index.entrySet())
            if (e.getValue() > index)
                e.setValue(e.getValue() - 1);
        getList().removeViewAt(index);
        updateHeights();
        updateFiller();
    }

    @Override protected void onOverscrollTriggered(boolean top) {
        if (top) {
            getList().removeAllViews();
            loaded_page_num = 1;
            updateFiller();
            updateHeights();
        }
        new LoadListTask(loaded_page_num++).execute();
    }

    private class LoadListTask extends AsyncTask<Void, LetterLabel, Void> {
        private int page_num;

        public LoadListTask(int page) {
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
            table_key = page.key;

            if (page.c_inf == null)
                requestToken();
            return null;
        }

        int progress = 0;
        @Override protected void onProgressUpdate(LetterLabel... values) {
            super.onProgressUpdate(values);

            LinearLayout list = getList();
            final LetterLabel letter = values[0];
            LayoutInflater inflater = getLayoutInflater();


            id_to_letter.put(letter.id, letter);
            id_to_index.put(letter.id, list.getChildCount());
            setProgress((float) progress++ / 100f);


            // Создаём заголовок.
            View label = inflater.inflate(R.layout.letter_label, list, false);

            PartUtils.dumpIntoLetterLabel(label, letter);

            label.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    onLetterSelected(letter);
                }
            });

            label.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View view) {
                    onSelectionMode(letter);
                    return true;
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

    private class ExecActionsOnLetterList extends AsyncTask<Void, Void, Boolean> {
        private LetterListRequest.Action action;
        Integer[] selection;
        private ExecActionsOnLetterList(LetterListRequest.Action action) {
            this.action = action;
        }

        @Override protected void onPreExecute() {
            setProgress(-1f);
        }

        @Override protected Boolean doInBackground(Void... voids) {
            selection = selected.toArray(new Integer[selected.size()]);

            LetterListRequest request = new LetterListRequest(action, selection);
            return request.exec(Au.user, table_key).success();
        }

        @Override protected void onPostExecute(Boolean success) {
            if (success)
                for (int id : selection) {
                    if (action == LetterListRequest.Action.DELETE)
                        deleteLetter(id);
                    if (action == LetterListRequest.Action.READ)
                        readLetter(id);
                }

            switchSelectionModeOff();
            hideProgressBar();
            switchOverScrollHandling(true);
        }

    }

}