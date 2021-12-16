package cz.trask.zenid.sample.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentPictureResult;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.selfie.SelfieResult;
import cz.trask.zenid.sdk.selfie.SelfieState;
import cz.trask.zenid.sdk.selfie.SelfieView;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/*
 * This class shows how to handle camera permission
 */
public class SelfieActivity extends AppCompatActivity {

    private SelfieView selfieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(Language.ENGLISH)
                .build();

        selfieView = findViewById(R.id.selfieView);
        selfieView.setLifecycleOwner(this);
        selfieView.enableDefaultVisualization(visualizationSettings);
        selfieView.setCallback(new SelfieView.Callback() {

            @Override
            public void onStateChanged(SelfieState state) {
                Timber.i("onStateChanged %s", state);
            }

            @Override
            public void onPictureTaken(SelfieResult result) {
                Timber.i("onPictureTaken... " + result.getFilePath());
                LogUtils.logInfo(getApplicationContext(), "Uploading taken picture...");
                postSelfieSample(result);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isCameraPermissionDenied(grantResults)) {
            if (shouldShowRequestPermissionRationale()) {
                Timber.i("Camera permission denied but selfieView will immediately request camera permission again.");
            } else {
                selfieView.setAutoRequestPermissions(false);
                Timber.i("Camera permission denied with -don't ask again- option.");
            }
        } else {
            Timber.i("Camera permission granted!");
        }
    }

    private boolean isCameraPermissionDenied(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED;
    }

    private boolean shouldShowRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
    }

    private void postSelfieSample(SelfieResult result) {
        MyApplication.apiService.postSelfieSample(result.getFilePath(), result.getSignature()).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...picture has been uploaded!");
                finish();
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }
}
