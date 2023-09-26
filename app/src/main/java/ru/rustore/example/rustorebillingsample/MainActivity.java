package ru.rustore.example.rustorebillingsample;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;
import ru.rustore.example.rustorebillingsample.di.PaymentsModule;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;
import ru.rustore.sdk.billingclient.impl.RuStoreBillingClientImpl;
import ru.rustore.sdk.billingclient.model.product.Product;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
import ru.rustore.sdk.billingclient.usecase.ProductsUseCase;
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase;
import ru.rustore.sdk.billingclient.utils.pub.RuStoreBillingClientExtKt;
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult;
import ru.rustore.sdk.core.tasks.OnCompleteListener;

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
}