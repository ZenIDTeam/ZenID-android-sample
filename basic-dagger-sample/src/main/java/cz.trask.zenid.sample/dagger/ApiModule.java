package cz.trask.zenid.sample.dagger;

import javax.inject.Singleton;

import cz.trask.zenid.sample.BuildConfig;
import cz.trask.zenid.sdk.api.ApiConfig;
import cz.trask.zenid.sdk.api.ApiService;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

@Module
public class ApiModule {

    @Singleton
    @Provides
    public ApiConfig provideApiConfig() {
        return new ApiConfig.Builder()
                .baseUrl("http://your.frauds.zenid.cz/api/")
                .apiKey("your_api_key")
                .build();
    }

    @Singleton
    @Provides
    public HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }
        return interceptor;
    }

    @Singleton
    @Provides
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor httpLoggingInterceptor) {
        return new OkHttpClient().newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }

    @Singleton
    @Provides
    public ApiService provideApiService(ApiConfig apiConfig, OkHttpClient okHttpClient) {
        return new ApiService.Builder()
                .apiConfig(apiConfig)
                .okHttpClient(okHttpClient)
                .build();
    }

}