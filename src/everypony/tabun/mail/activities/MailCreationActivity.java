package everypony.tabun.mail.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.LivestreetKey;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.requests.LetterAddRequest;
import com.cab404.libtabun.requests.UserAutocompleteRequest;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.util.SU;
import everypony.tabun.mail.R;
import everypony.tabun.mail.util.Au;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author cab404
 */
public class MailCreationActivity extends AbstractMailActivity {

    HashSet<String> subjects = new HashSet<>();

    @Override protected void init() {
        super.init();

        LinearLayout list = getList();
        LayoutInflater inflater = getLayoutInflater();
        View creation = inflater.inflate(R.layout.creation_layout, list, false);

        final AutoCompleteTextView recipients = (AutoCompleteTextView) creation.findViewById(R.id.recipients);
        recipients.setAdapter(new UserAutoComplete());


        recipients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String username = recipients.getText().toString();

                final ViewGroup users = (ViewGroup) findViewById(R.id.users);
                final View label = getLayoutInflater().inflate(R.layout.subject_layout, users, false);
                ((TextView) label.findViewById(R.id.title)).setText(username);
                subjects.add(username);
                label.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        users.removeView(label);
                        subjects.remove(username);
                    }
                });

                users.addView(label);
                recipients.setText("");
            }
        });


        creation.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                send();
            }
        });

        list.addView(creation);
    }

    private class UserAutoComplete extends BaseAdapter implements Filterable {
        List<String> names = new ArrayList<>();

        @Override public int getCount() {
            return names.size();
        }
        @Override public Object getItem(int i) {
            return names.get(i);
        }
        @Override public long getItemId(int i) {
            return i;
        }

        @Override public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                view = inflater.inflate(R.layout.string_item, viewGroup, false);
            }

            TextView text = (TextView) view.findViewById(R.id.title);
            text.setText(names.get(i));

            return view;
        }

        private Filter staticFilter;
        @Override public Filter getFilter() {
            if (staticFilter == null)
                staticFilter = new Filter() {
                    LivestreetKey key = null;
                    AccessProfile profile;
                    @Override protected FilterResults performFiltering(CharSequence charSequence) {
                        if (key == null) {
                            profile = new TabunAccessProfile();
                            TabunPage page = new TabunPage();
                            page.fetch(profile);
                            key = page.key;
                        }

                        if (charSequence == null) return null;

                        UserAutocompleteRequest request = new UserAutocompleteRequest(charSequence.toString());
                        request.exec(profile, key);

                        FilterResults results = new FilterResults();
                        results.count = request.names.size();
                        results.values = request.names;
                        return results;
                    }

                    @Override protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                        if (charSequence != null)
                            if (filterResults != null)
                                if (filterResults.values != null) {
                                    names = (List<String>) filterResults.values;
                                    notifyDataSetChanged();
                                }
                    }

                };
            return staticFilter;
        }

    }

    private void send() {
        new SendLetter().execute();
    }

    private class SendLetter extends AsyncTask<Void, Void, Void> {
        Letter letter;

        private SendLetter() {
            letter = new Letter();
            letter.text = String.valueOf(((TextView) findViewById(R.id.text)).getText());
            letter.title = String.valueOf(((TextView) findViewById(R.id.text)).getText());
            Au.i(this, SU.join(subjects, ", "));
            letter.recipients = Arrays.asList(subjects.toArray(new String[subjects.size()]));
        }

        @Override protected void onPreExecute() {
            showProgressBar();
            setProgress(-1f);
        }

        @Override protected Void doInBackground(Void... voids) {
            TabunPage tabunPage = new TabunPage() {
                @Override public String getURL() {
                    return "/talk/add";
                }
            };
            tabunPage.fetch(Au.user);
            new LetterAddRequest(letter).exec(Au.user, tabunPage);
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressBar();
            startActivity(
                    new Intent(
                            MailCreationActivity.this,
                            TableActivity.class
                    )
            );
            finish();
        }

    }

}