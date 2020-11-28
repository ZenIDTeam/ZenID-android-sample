package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessState;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessView;
import cz.trask.zenid.sdk.Language;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    private FaceLivenessView faceLivenessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        faceLivenessView = findViewById(R.id.faceLivenessView);
        faceLivenessView.setLifecycleOwner(this);
        faceLivenessView.enableDefaultVizualization(Language.CZECH);
        faceLivenessView.setCallback(new FaceLivenessView.Callback() {

            @Override
            public void onStateChanged(FaceLivenessState state) {
                Timber.i("onStateChanged %s", state);
            }

            @Override
            public void onPictureTaken(String path) {
                LogUtils.logInfo(getApplicationContext(), "onPictureTaken... " + path);
            }
        });
    }
}
