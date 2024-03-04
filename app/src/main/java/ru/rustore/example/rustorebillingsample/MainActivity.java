package ru.rustore.example.rustorebillingsample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import ru.rustore.example.rustorebillingsample.di.PaymentsModule;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;

public class MainActivity extends AppCompatActivity {

    private static final RuStoreBillingClient billingClient =
            PaymentsModule.provideRuStorebillingClient();


    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            billingClient.onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        billingClient.onNewIntent(intent);
    }
}