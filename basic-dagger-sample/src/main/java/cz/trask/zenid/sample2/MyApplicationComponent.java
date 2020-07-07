package cz.trask.zenid.sample2;

import android.app.Application;

import javax.inject.Singleton;

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

    void inject(MyActivity myActivity);

    @Component.Factory
    interface Factory {
        MyApplicationComponent create(@BindsInstance Application application);
    }
}