package cz.trask.zenid.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.enums.Country;
import cz.trask.zenid.sdk.enums.DocumentRole;
import cz.trask.zenid.sdk.enums.PageCodes;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class PickerActivity extends AppCompatActivity {

    public static final String ANY_COUNTRY = "Any";

    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_PROFILES = "profiles";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_COUNTRY = "country";
    public static final String EXTRA_ROLE = "role";

    private static final int MENU_RESET = 1;

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        type = getIntent().getStringExtra(EXTRA_TYPE);
        String selected = getIntent().getStringExtra(EXTRA_SELECTED);
        List<String> profiles = getIntent().getStringArrayListExtra(EXTRA_PROFILES);
        String country = getIntent().getStringExtra(EXTRA_COUNTRY);
        String role = getIntent().getStringExtra(EXTRA_ROLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(titleForType(type));

        List<String> options = optionsForType(type, profiles, country, role);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new PickerAdapter(options, selected, option -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_TYPE, type);
            result.putExtra(EXTRA_VALUE, option);
            setResult(RESULT_OK, result);
            finish();
        }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String resetLabel = resetLabelForType(type);
        if (resetLabel != null) {
            menu.add(0, MENU_RESET, 0, resetLabel)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_RESET) {
            Intent result = new Intent();
            result.putExtra(EXTRA_TYPE, type);
            result.putExtra(EXTRA_VALUE, (String) null);
            setResult(RESULT_OK, result);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String titleForType(String type) {
        if (type == null) return "";
        switch (type) {
            case "country": return "Country";
            case "role": return "Role";
            case "page": return "Page";
            case "profile": return "Profile";
            default: return type;
        }
    }

    private String resetLabelForType(String type) {
        if ("role".equals(type) || "page".equals(type)) return "None";
        if ("profile".equals(type)) return "Default";
        return null;
    }

    static Country toCountry(String name) {
        return name == null || name.isEmpty() || ANY_COUNTRY.equals(name) ? null : Country.valueOf(name);
    }

    private List<String> optionsForType(String type, List<String> profiles, String country, String role) {
        if ("profile".equals(type)) {
            return profiles != null ? profiles : new ArrayList<>();
        }
        // Offer only what the license allows.
        List<String> result = new ArrayList<>();
        try {
            ZenId zenId = ZenId.get();
            if ("country".equals(type)) {
                for (Country c : zenId.getSupportedCountries()) result.add(c.name());
                if (result.size() > 1) result.add(0, ANY_COUNTRY);
            } else if ("role".equals(type)) {
                for (DocumentRole r : zenId.getSupportedDocuments(toCountry(country))) result.add(r.name());
            } else if ("page".equals(type)) {
                DocumentRole roleFilter = role != null ? DocumentRole.valueOf(role) : null;
                for (PageCodes p : zenId.getSupportedDocumentPageCodes(toCountry(country), roleFilter)) result.add(p.name());
            }
        } catch (Exception e) {
            Timber.e(e);
            return new ArrayList<>();
        }
        return result;
    }

    interface OnOptionSelected {
        void onSelected(String option);
    }

    static class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.ViewHolder> {
        private final List<String> options;
        private final String selected;
        private final OnOptionSelected listener;

        PickerAdapter(List<String> options, String selected, OnOptionSelected listener) {
            this.options = options;
            this.selected = selected;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String option = options.get(position);
            holder.optionText.setText(option);
            holder.radioButton.setChecked(option.equals(selected));
            holder.itemView.setOnClickListener(v -> listener.onSelected(option));
        }

        @Override
        public int getItemCount() { return options.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView optionText;
            final RadioButton radioButton;

            ViewHolder(View view) {
                super(view);
                optionText = view.findViewById(R.id.optionText);
                radioButton = view.findViewById(R.id.radioButton);
            }
        }
    }
}
