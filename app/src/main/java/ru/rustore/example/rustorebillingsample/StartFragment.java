package ru.rustore.example.rustorebillingsample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import ru.rustore.example.rustorebillingsample.di.PaymentsModule;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
import ru.rustore.sdk.billingclient.usecase.ProductsUseCase;
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase;
import ru.rustore.sdk.billingclient.utils.pub.RuStoreBillingClientExtKt;
import ru.rustore.sdk.core.exception.RuStoreException;
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult;

public class StartFragment extends Fragment {

    Button productButton;
    Button purchaseButton;
    RecyclerView productsList;
    RecyclerView purchasesList;

    private static final RuStoreBillingClient billingClient =
            PaymentsModule.provideRuStorebillingClient();

    public StartFragment() {
        super(R.layout.fragment_start);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productButton = view.findViewById(R.id.productsButton);
        purchaseButton = view.findViewById(R.id.purchasesButton);

        productsList = view.findViewById(R.id.productsList);
        purchasesList = view.findViewById(R.id.purchasesList);

        checkPurchaseAvailiability();

        productButton.setOnClickListener(v -> getProducts());

        purchaseButton.setOnClickListener(v -> getPurchases());
    }

    public void checkPurchaseAvailiability() {
        RuStoreBillingClientExtKt.checkPurchasesAvailability(RuStoreBillingClient.Companion)
                .addOnSuccessListener(result -> {
                    if (result instanceof FeatureAvailabilityResult.Available) {
                        Log.w("RuStoreBillingClient", "Success calling checkPurchaseAvailiability - Available: " + result);
                    } else {
                        RuStoreException exception = ((FeatureAvailabilityResult.Unavailable) result).getCause();
                        Log.w("RuStoreBillingClient", "Success calling checkPurchaseAvailiability - Unavailable: " + exception);
                    }
                }).addOnFailureListener(error -> {
                    Log.e("RuStoreBillingClient", "Error calling checkPurchaseAvailiability: " + error);
                });
    }

    public void getProducts() {
        ProductsUseCase productsUseCase = billingClient.getProducts();

        productsUseCase.getProducts(
                Arrays.asList(
                        "your_product_id1", "your_product_id_2", "your_product_id_3",
                        "your_subscription_id_1"
                )).addOnSuccessListener(products -> {
                    ProductsAdapter productsAdapter = new ProductsAdapter(products);

                    productsList.setAdapter(productsAdapter);

                    productsList.setLayoutManager(new LinearLayoutManager(getContext()));

                    ItemClickSupport.addTo(productsList).setOnItemClickListener((recyclerView, position, v) -> {
                        purchaseProduct(products.get(position).getProductId());
                        Toast.makeText(getContext(), "Clicked: " + position, Toast.LENGTH_LONG).show();
                    });
                }).addOnFailureListener(throwable -> Log.e("RuStoreBillingClient", "Error calling getProducts cause: " + throwable));
    }

    public void getPurchases() {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.getPurchases().addOnSuccessListener(purchases -> {
            PurchaseAdapter purchaseAdapter = new PurchaseAdapter(purchases);

            purchasesList.setAdapter(purchaseAdapter);
            purchasesList.setLayoutManager(new LinearLayoutManager(getContext()));

            purchases.forEach(purchase -> {
                String purchaseId = purchase.getPurchaseId();
                if (purchaseId != null) {
                    assert purchase.getDeveloperPayload() != null;
                    Log.w("HOHOHO", purchase.getDeveloperPayload());
                    if (purchase.getPurchaseState() != null) {
                        if (purchase.getPurchaseState() == PurchaseState.CREATED ||
                                purchase.getPurchaseState() == PurchaseState.INVOICE_CREATED )
                        {
                            deletePurchase(purchaseId);
                        } else if (purchase.getPurchaseState() == PurchaseState.PAID) {
                            confirmPurchase(purchaseId);
                        }
                    } else {
                        Log.e("HOHOHO", "PurchaseState is null");
                    }

                }
            });
        }).addOnFailureListener(throwable ->
            Log.e("RuStoreBillingClient", "Error calling getPurchases cause: " + throwable)
        );
    }

    public void purchaseProduct(String productId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        String developerPayload = "your_developer_payload";

        purchasesUseCase.purchaseProduct(productId, null, 1, developerPayload)
                .addOnSuccessListener(this::handlePaymentResult)
                .addOnFailureListener(throwable ->
            Log.e("RuStoreBillingClient", "Error calling purchaseProduct cause: " + throwable)
        );
    }

    private void handlePaymentResult(PaymentResult paymentResult) {
        if (paymentResult instanceof PaymentResult.Success) {
            confirmPurchase(((PaymentResult.Success) paymentResult).getPurchaseId());
        } else if (paymentResult instanceof PaymentResult.Failure) {
            deletePurchase(((PaymentResult.Failure) paymentResult).getPurchaseId());
        }
    }

    public void confirmPurchase(String purchaseId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.confirmPurchase(purchaseId)
                .addOnSuccessListener(unit -> {
                    Toast.makeText(getContext(), "Purchase confirmed", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(throwable -> {
                    Log.e("RuStoreBillingClient", "Error calling confirmPurchase cause: " + throwable);
                });
    }


    public void deletePurchase(String purchaseId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.deletePurchase(purchaseId)
                .addOnSuccessListener(unit -> {})
                .addOnFailureListener(throwable -> {
                    Log.e("RuStoreBillingClient", "Error calling deletePurchase cause: " + throwable);
                });
    }
}