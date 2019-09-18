package cz.trask.zenid.sample;

import javax.inject.Inject;

import cz.trask.zenid.sample.dagger.DaggerMyApplicationComponent;
import cz.trask.zenid.sdk.ZenId;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class MyApplication extends DaggerApplication {

    @Inject
    ZenId zenId;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        ZenId.setSingletonInstance(zenId);

        zenId.initialize();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerMyApplicationComponent.builder().application(this).build();
    }
}