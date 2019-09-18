package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.annotation.LayoutRes;

import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

public abstract class BaseActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        ButterKnife.bind(this);
    }

    @LayoutRes
    protected abstract int layoutRes();
}
