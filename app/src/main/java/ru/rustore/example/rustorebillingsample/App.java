package ru.rustore.example.rustorebillingsample;

import android.app.Application;

import ru.rustore.example.rustorebillingsample.di.PaymentsModule;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PaymentsModule.install(this);
    }
}
