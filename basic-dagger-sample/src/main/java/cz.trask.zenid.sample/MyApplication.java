package cz.trask.zenid.sample;

import android.app.Application;

import javax.inject.Inject;

import cz.trask.zenid.sample.dagger.DaggerMyApplicationComponent;
import cz.trask.zenid.sample.dagger.MyApplicationComponent;
import cz.trask.zenid.sdk.ZenId;
import timber.log.Timber;

public class MyApplication extends Application {

    @Inject
    ZenId zenId;
    private MyApplicationComponent myApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        myApplicationComponent = DaggerMyApplicationComponent.factory().create(this);
        myApplicationComponent.inject(this);

        Timber.plant(new Timber.DebugTree());

        ZenId.setSingletonInstance(zenId);

        zenId.initialize();
    }

    public MyApplicationComponent getMyApplicationComponent() {
        return myApplicationComponent;
    }
}