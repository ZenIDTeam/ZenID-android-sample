package cz.trask.zenid.sample;

import android.app.Application;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.ApiConfig;
import cz.trask.zenid.sdk.api.ApiService;
import cz.trask.zenid.sdk.api.model.InvestigationResponseJson;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.internal.verifier.DocumentVerifier;
import cz.trask.zenid.sdk.internal.verifier.FaceLivenessVerifier;
import cz.trask.zenid.sdk.internal.verifier.HologramVerifier;
import cz.trask.zenid.sdk.internal.verifier.LicensePlateVerifier;
import cz.trask.zenid.sdk.internal.verifier.MsLivenessVerifier;
import cz.trask.zenid.sdk.internal.verifier.SelfieVerifier;
import cz.trask.zenid.sdk.models.UploadReadyData;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MyApplication extends Application {

    public interface ResultCallback {
        void onResult(String json);
    }

    public static AppSettings appSettings;
    @Nullable
    public static ApiService apiService;
    public static byte[] pendingSignedPackage;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        appSettings = new AppSettings(this);
        initZenId();
        rebuildApiService();
    }

    private void initZenId() {
        if (ZenId.singletonInstanceExists()) {
            Timber.i("Skip building an instance of ZenId");
            return;
        }
        ZenId zenId = new ZenId.Builder()
                .applicationContext(getApplicationContext())
                .verifiers(new DocumentVerifier(), new HologramVerifier(), new FaceLivenessVerifier(), new SelfieVerifier(), new MsLivenessVerifier(), new LicensePlateVerifier())
                .build();
        ZenId.setSingletonInstance(zenId);
        zenId.initialize();
        zenId.setLoggerCallback((module, method, message) -> Timber.tag(module).d("%s - %s", method, message));
    }

    public void rebuildApiService() {
        String url = appSettings.getServerUrl();
        if (url == null || url.isEmpty()) {
            url = BuildConfig.ZENID_URL;
        }
        if (url == null || url.isEmpty()) return;

        String key = appSettings.getApiKey();
        if (key == null || key.isEmpty()) {
            key = BuildConfig.ZENID_APIKEY;
        }
        if (key == null) key = "";

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        apiService = null;
        try {
            ApiConfig apiConfig = new ApiConfig.Builder()
                    .baseUrl(url)
                    .apiKey(key)
                    .build();
            apiService = new ApiService.Builder()
                    .apiConfig(apiConfig)
                    .okHttpClient(okHttpClient)
                    .build();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static void postSampleAndInvestigate(UploadReadyData uploadReadyData, ResultCallback callback) {
        postSampleAndInvestigate(uploadReadyData.getSignedSamplePackage(), callback);
    }

    public static void postSampleAndInvestigate(byte[] signedPackage, ResultCallback callback) {
        if (apiService == null) {
            callback.onResult("No API service configured");
            return;
        }
        apiService.postSample(signedPackage).enqueue(new Callback<SampleJson>() {
            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                SampleJson sample = response.body();
                String sampleId = sample != null ? sample.getSampleId() : null;
                if (sampleId != null) {
                    investigate(sampleId, callback);
                } else {
                    callback.onResult("Upload failed: " + (sample != null ? sample.getErrorCode() : "empty response"));
                }
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
                callback.onResult("Upload failed: " + t.getMessage());
            }
        });
    }

    private static void investigate(String sampleId, ResultCallback callback) {
        if (apiService == null) {
            callback.onResult("No API service configured");
            return;
        }
        String profile = appSettings.getProfile();
        String profileParam = (profile != null && !profile.isEmpty()) ? profile : null;
        apiService.investigateSamples(Collections.singletonList(sampleId), profileParam).enqueue(new Callback<InvestigationResponseJson>() {
            @Override
            public void onResponse(Call<InvestigationResponseJson> call, Response<InvestigationResponseJson> response) {
                callback.onResult(new Gson().toJson(response.body()));
            }

            @Override
            public void onFailure(Call<InvestigationResponseJson> call, Throwable t) {
                Timber.e(t);
                callback.onResult("Investigation failed: " + t.getMessage());
            }
        });
    }
}
