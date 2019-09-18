package cz.trask.zenid.sample.dagger;

import android.app.Application;

import javax.inject.Singleton;

import cz.trask.zenid.sample.MyApplication;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(
        modules = {
                ActivityBindingModule.class,
                AndroidSupportInjectionModule.class,
                ApiModule.class,
                ContextModule.class,
        }
)
@Singleton
public interface MyApplicationComponent extends AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        MyApplicationComponent build();
    }
}