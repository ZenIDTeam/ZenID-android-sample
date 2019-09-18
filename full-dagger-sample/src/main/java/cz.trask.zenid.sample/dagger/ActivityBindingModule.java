package cz.trask.zenid.sample.dagger;

import cz.trask.zenid.sample.ui.MainActivity;
import cz.trask.zenid.sample.ui.ResultsActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    @ContributesAndroidInjector()
    public abstract MainActivity myActivity();

    @ContributesAndroidInjector()
    public abstract ResultsActivity resultsActivity();
}
