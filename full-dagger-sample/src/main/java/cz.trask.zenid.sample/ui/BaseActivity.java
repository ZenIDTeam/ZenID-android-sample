package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import cz.trask.zenid.sample.MyApplication;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        ButterKnife.bind(this);
    }

    @LayoutRes
    protected abstract int layoutRes();

    protected MyApplication getMyApplication() {
        return (MyApplication) getApplication();
    }
}
