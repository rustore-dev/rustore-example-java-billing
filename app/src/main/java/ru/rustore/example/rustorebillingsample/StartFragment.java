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
import java.util.List;

import ru.rustore.example.rustorebillingsample.di.PaymentsModule;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
import ru.rustore.sdk.billingclient.usecase.ProductsUseCase;
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase;
import ru.rustore.sdk.billingclient.utils.BillingRuStoreExceptionExtKt;
import ru.rustore.sdk.billingclient.utils.pub.RuStoreBillingClientExtKt;
import ru.rustore.sdk.core.exception.RuStoreException;

public class StartFragment extends Fragment {

    Button productButton;
    Button purchaseButton;
    RecyclerView productsList;
    RecyclerView purchasesList;

    private static final String TAG = "RuStoreBillingClient";

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

        checkPurchaseAvailability();

        productButton.setOnClickListener(v -> getProducts());

        purchaseButton.setOnClickListener(v -> getPurchases());
    }

    public void checkPurchaseAvailability() {
        RuStoreBillingClientExtKt.checkPurchasesAvailability(RuStoreBillingClient.Companion)
                .addOnSuccessListener(result -> {
                    if (result instanceof PurchaseAvailabilityResult.Available) {
                        Log.w(TAG, "Success calling checkPurchaseAvailability - Available: " + result);
                    } else if (result instanceof PurchaseAvailabilityResult.Unavailable) {
                        Log.w(TAG, "Success calling checkPurchaseAvailability - Unavailable: " + result);
                    } else {
                        RuStoreException exception = ((PurchaseAvailabilityResult.Unavailable) result).getCause();
                        BillingRuStoreExceptionExtKt.resolveForBilling(exception, getContext());
                        Log.w(TAG, "Success calling checkPurchaseAvailability - Unavailable: " + exception);
                    }
                }).addOnFailureListener(error -> Log.e(TAG, "Error calling checkPurchaseAvailability: " + error));
    }

    public void getProducts() {
        ProductsUseCase productsUseCase = billingClient.getProducts();
        List<String> productsId = Arrays.asList(
                "productId1",
                "productId2",
                "productId3"
        );

        productsUseCase.getProducts(productsId)
                .addOnSuccessListener(products -> {
                    ProductsAdapter productsAdapter = new ProductsAdapter(products);

                    productsList.setAdapter(productsAdapter);

                    productsList.setLayoutManager(new LinearLayoutManager(getContext()));

                    ItemClickSupport.addTo(productsList).setOnItemClickListener((recyclerView, position, v) -> {
                        purchaseProduct(products.get(position).getProductId());
                        Toast.makeText(getContext(), "Clicked: " + position, Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(throwable -> Log.e(TAG, "Error calling getProducts cause: " + throwable));
    }

    public void getPurchases() {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.getPurchases()
                .addOnSuccessListener(purchases -> {
                    PurchaseAdapter purchaseAdapter = new PurchaseAdapter(purchases);
                    purchasesList.setAdapter(purchaseAdapter);
                    purchasesList.setLayoutManager(new LinearLayoutManager(getContext()));

                    proceedUnfinishedPurchases(purchases);
                })
                .addOnFailureListener(throwable -> Log.e("RuStoreBillingClient", "Error calling getPurchases cause: " + throwable));
    }

    public void proceedUnfinishedPurchases(List<Purchase> purchases) {
        purchases.forEach(purchase -> {
            String purchaseId = purchase.getPurchaseId();
            PurchaseState purchaseState = purchase.getPurchaseState();

            if (purchaseId == null) {
                return;
            } else if (purchaseState == null) {
                Log.e(TAG, "PurchaseState is null");
                return;
            }

            boolean needDeletePurchase = purchaseState == PurchaseState.CREATED || purchaseState == PurchaseState.INVOICE_CREATED;
            boolean needConfirmPurchase = purchaseState == PurchaseState.PAID;

            if (needDeletePurchase) {
                deletePurchase(purchaseId);
            } else if (needConfirmPurchase) {
                confirmPurchase(purchaseId);
            }
        });
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
                .addOnSuccessListener(unit -> {
                })
                .addOnFailureListener(throwable -> {
                    Log.e("RuStoreBillingClient", "Error calling deletePurchase cause: " + throwable);
                });
    }
}
