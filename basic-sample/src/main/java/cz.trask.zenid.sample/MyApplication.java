package cz.trask.zenid.sample;

import android.app.Application;

import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.ApiConfig;
import cz.trask.zenid.sdk.api.ApiService;
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
    }

    private void initZenId() {
        ZenId zenId = new ZenId.Builder()
                .applicationContext(getApplicationContext())
                .build();

        ZenId.setSingletonInstance(zenId);

        // This may take a few seconds. Please do it as soon as possible.
        zenId.initialize();
    }

    private void initApiService() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        ApiConfig apiConfig = new ApiConfig.Builder()
                 // .baseUrl("http://your.frauds.zenid.cz/api/")
                 // .apiKey("your_api_key")
                .build();

        apiService = new ApiService.Builder()
                .apiConfig(apiConfig)
                .okHttpClient(okHttpClient)
                .build();
    }

}