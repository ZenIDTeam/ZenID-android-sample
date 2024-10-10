package cz.trask.zenid.sample;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import cz.trask.zenid.sdk.DocumentModule;
import cz.trask.zenid.sdk.LoggerCallback;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.ApiConfig;
import cz.trask.zenid.sdk.api.ApiService;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessModule;
import cz.trask.zenid.sdk.selfie.SelfieModule;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class MyApplication extends Application {

    public static ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        initZenId();

        initApiService();

        LogUtils.logInfo(getApplicationContext(), "Build type: " + BuildConfig.BUILD_TYPE);
    }

    private void initZenId() {
        if (ZenId.isSingletonInstanceExists()) {
            Timber.i("Skip building an instance of ZenId");
        } else {
            ZenId zenId = new ZenId.Builder()
                    .applicationContext(getApplicationContext())
                    .modules(new DocumentModule(), new SelfieModule(), new FaceLivenessModule())
                    .build();

            ZenId.setSingletonInstance(zenId);

            // This may take a few seconds. Please do it as soon as possible.
            zenId.initialize(new ZenId.InitCallback() {
                @Override
                public void onInitialized() {
                    LogUtils.logInfo(getApplicationContext(), "Initialized.");
                }

                @Override
                public void onInitializationFailed(ZenIdException e) {
                    Timber.e(e);
                }
            });

            zenId.getSecurity().setLoggerCallback(new LoggerCallback() {
                @Override
                public void logMessage(String module, String method, String message) {

                }
            });
        }
    }

    private void initApiService() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();

        ApiConfig apiConfig = new ApiConfig.Builder()
                .baseUrl(BuildConfig.ZENID_URL)
                .apiKey(BuildConfig.ZENID_APIKEY)
                .build();

        apiService = new ApiService.Builder()
                .apiConfig(apiConfig)
                .okHttpClient(okHttpClient)
                .build();
    }

}