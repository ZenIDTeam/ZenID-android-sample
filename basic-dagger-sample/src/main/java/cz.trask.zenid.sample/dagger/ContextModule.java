package cz.trask.zenid.sample.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import cz.trask.zenid.sdk.ZenId;
import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    @Provides
    public Context provideApplicationContext(Application application) {
        return application.getApplicationContext();
    }

    @Singleton
    @Provides
    public ZenId provideZenId(Context context) {
        return new ZenId.Builder()
                .applicationContext(context)
                .build();
    }
}
