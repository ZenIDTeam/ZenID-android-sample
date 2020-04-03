package cz.trask.zenid.sample.dagger;

import android.app.Application;

import javax.inject.Singleton;

import cz.trask.zenid.sample.MyApplication;
import dagger.BindsInstance;
import dagger.Component;

@Component(
        modules = {
                ApiModule.class,
                ContextModule.class,
        }
)

@Singleton
public interface MyApplicationComponent {
    void inject(MyApplication myApplication);

    @Component.Factory
    interface Factory {
        MyApplicationComponent create(@BindsInstance Application application);
    }
}