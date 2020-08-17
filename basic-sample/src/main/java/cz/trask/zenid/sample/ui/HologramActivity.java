package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentResult;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.HologramState;
import cz.trask.zenid.sdk.HologramView;
import cz.trask.zenid.sdk.Language;
import timber.log.Timber;

public class HologramActivity extends AppCompatActivity {

    private HologramView hologramView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hologram);

        hologramView = findViewById(R.id.hologramView);
        hologramView.setDocumentType(DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
        hologramView.setLifecycleOwner(this);
        hologramView.enableDefaultVizualization(Language.ENGLISH);
        hologramView.setCallback(new HologramView.Callback() {

            @Override
            public void onStateChanged(HologramState state) {
                Timber.i("onStateChanged: %s", state);
            }

            @Override
            public void onVideoTaken(DocumentResult result) {
                LogUtils.logInfo(getApplicationContext(), "onVideoTaken... " + result.getFilePath());
            }
        });
    }
}
