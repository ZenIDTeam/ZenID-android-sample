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
import cz.trask.zenid.sdk.models.ZenidModels;
import java.util.ArrayList;
import java.util.List;

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

    private List<String> optionsForType(String type, List<String> profiles, String country, String role) {
        if ("country".equals(type)) {
            List<String> result = new ArrayList<>();
            for (ZenidModels.ZenidModel m : ZenidModels.getModels()) {
                String name = m.getCountry().name();
                if (!result.contains(name)) result.add(name);
            }
            if (result.size() > 1) result.add(0, ANY_COUNTRY);
            return result;
        } else if ("role".equals(type)) {
            List<String> result = new ArrayList<>();
            for (ZenidModels.ZenidModel m : ZenidModels.getModels()) {
                if (!ANY_COUNTRY.equals(country) && !m.getCountry().name().equals(country)) continue;
                String name = m.getDocumentRole().name();
                if (!result.contains(name)) result.add(name);
            }
            return result;
        } else if ("page".equals(type)) {
            List<String> result = new ArrayList<>();
            for (ZenidModels.ZenidModel m : ZenidModels.getModels()) {
                if (!ANY_COUNTRY.equals(country) && !m.getCountry().name().equals(country)) continue;
                if (!m.getDocumentRole().name().equals(role)) continue;
                String name = m.getPageCode().name();
                if (!result.contains(name)) result.add(name);
            }
            return result;
        } else if ("profile".equals(type) && profiles != null) {
            return profiles;
        }
        return new ArrayList<>();
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
