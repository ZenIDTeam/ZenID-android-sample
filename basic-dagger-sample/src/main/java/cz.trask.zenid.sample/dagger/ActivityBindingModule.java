package cz.trask.zenid.sample.dagger;

import cz.trask.zenid.sample.MyActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    @ContributesAndroidInjector()
    public abstract MyActivity myActivity();

}
