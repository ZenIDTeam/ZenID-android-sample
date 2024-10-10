package cz.trask.zenid.sample;

import android.content.Context;
import android.widget.Toast;

import timber.log.Timber;

public class LogUtils {

    public static void logInfo(Context context, String msg) {
        Timber.i(msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
