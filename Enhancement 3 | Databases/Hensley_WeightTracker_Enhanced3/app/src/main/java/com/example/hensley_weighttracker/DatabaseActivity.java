package com.example.hensley_weighttracker;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class DatabaseActivity extends AppCompatActivity {
    // One queue also keeps rapid reads and writes in the order the user requested them.
    private static final ExecutorService DATABASE_WORK = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    protected <T> void databaseTask(Callable<T> work, Consumer<T> success, View... controls) {
        databaseTask(work, success, () -> { }, controls);
    }

    protected <T> void databaseTask(Callable<T> work, Consumer<T> success,
                                    Runnable failure, View... controls) {
        for (View control : controls) control.setEnabled(false);
        DATABASE_WORK.execute(() -> {
            try {
                T result = work.call();
                main.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    for (View control : controls) control.setEnabled(true);
                    success.accept(result);
                });
            } catch (Exception exception) {
                main.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    for (View control : controls) control.setEnabled(true);
                    Toast.makeText(this, R.string.database_access_failed, Toast.LENGTH_LONG).show();
                    failure.run();
                });
            }
        });
    }
}
