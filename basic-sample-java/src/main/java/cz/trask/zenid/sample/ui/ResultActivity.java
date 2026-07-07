package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_JSON = "json";

    public static Intent newIntent(Context context, String json) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_JSON, json);
        return intent;
    }

    public static Intent newIntent(Context context, byte[] signedPackage) {
        MyApplication.pendingSignedPackage = signedPackage;
        return new Intent(context, ResultActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Result");

        ProgressBar loadingIndicator = findViewById(R.id.loadingIndicator);
        ScrollView scrollView = findViewById(R.id.scrollView);
        TextView textView = findViewById(R.id.resultText);

        byte[] signedPackage = MyApplication.pendingSignedPackage;
        MyApplication.pendingSignedPackage = null;
        if (signedPackage != null) {
            MyApplication.postSampleAndInvestigate(signedPackage, json -> runOnUiThread(() -> {
                loadingIndicator.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                textView.setText(json);
            }));
        } else {
            loadingIndicator.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            String json = getIntent().getStringExtra(EXTRA_JSON);
            textView.setText(json != null ? json : "No result");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
