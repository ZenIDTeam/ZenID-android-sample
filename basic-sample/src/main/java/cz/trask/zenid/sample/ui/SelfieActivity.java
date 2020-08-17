package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.SelfieState;
import cz.trask.zenid.sdk.SelfieView;
import timber.log.Timber;

public class SelfieActivity extends AppCompatActivity {

    private SelfieView selfieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        selfieView = findViewById(R.id.selfieView);
        selfieView.setLifecycleOwner(this);
        selfieView.enableDefaultVizualization(Language.ENGLISH);
        selfieView.setCallback(new SelfieView.Callback() {

            @Override
            public void onStateChanged(SelfieState state) {
                Timber.i("onStateChanged %s", state);
            }

            @Override
            public void onPictureTaken(String path) {
                LogUtils.logInfo(getApplicationContext(), "onPictureTaken... " + path);
            }
        });
    }

}
