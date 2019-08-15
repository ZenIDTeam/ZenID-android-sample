package cz.trask.zenid.sample;

import android.app.Application;

import cz.trask.zenid.sdk.RemoteConfig;
import cz.trask.zenid.sdk.ZenId;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        // In order to start integration, you will need address and credentials.
        RemoteConfig remoteConfig = new RemoteConfig.Builder()
                .baseUrl("http://your.frauds.zenid.cz/api/")
                .apiKey("your_api_key")
                .build();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                // .addInterceptor() https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor
                .build();

        ZenId zenId = new ZenId.Builder()
                .applicationContext(getApplicationContext())
                .remoteConfig(remoteConfig)
                .okHttpClient(okHttpClient)
                .build();

        // Make the client globally accessible.
        ZenId.setSingletonInstance(zenId);

        // This may take a few seconds. Please do it as soon as possible.
        zenId.initialize();
    }

}