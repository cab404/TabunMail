package everypony.tabun.mail.activities;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.cab404.libtabun.data.LivestreetKey;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.requests.UserAutocompleteRequest;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.framework.AccessProfile;
import everypony.tabun.mail.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class MailCreationActivity extends AbstractMailActivity {

    @Override protected void init() {
        super.init();

        LinearLayout list = getList();
        LayoutInflater inflater = getLayoutInflater();
        View creation = inflater.inflate(R.layout.creation_layout, list, false);

        AutoCompleteTextView recipients = (AutoCompleteTextView) creation.findViewById(R.id.recipients);
        recipients.setAdapter(new UserAutoComplete());



        recipients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                UserAutoComplete uac = (UserAutoComplete) adapterView.getAdapter();

            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        list.addView(creation);
    }

    private class UserAutoComplete implements ListAdapter, Filterable {
        List<String> names = new ArrayList<>();

        @Override public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }
        @Override public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override public int getCount() {
            return names.size();
        }
        @Override public Object getItem(int i) {
            return names.get(i);
        }
        @Override public long getItemId(int i) {
            return i;
        }
        @Override public boolean hasStableIds() {
            return false;
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

        @Override public int getItemViewType(int i) {
            return 1;
        }
        @Override public int getViewTypeCount() {
            return 1;
        }
        @Override public boolean isEmpty() {
            return names.isEmpty();
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
                                if (filterResults.values != null)
                                    names = (List<String>) filterResults.values;
                    }

                };
            return staticFilter;
        }

        @Override public boolean areAllItemsEnabled() {
            return true;
        }
        @Override public boolean isEnabled(int i) {
            return true;
        }
    }

}