# RuStore SDK payments implementation

## Requirements

For the SDK to work correctly, the following requirements must be met:

- consoleApplicationId in create() is set correctly:
```
 private static RuStoreBillingClient ruStoreBillingClient = RuStoreBillingClientFactory.INSTANCE.create(
                app,
                "184050",
                "rustoresdkexamplescheme",
                null,
                null,
                null,
                true
        );
```

- ApplicationId specified in build.gradle must be the same as applicationId in the apk file that you published in RuStore Console:
```
android {
    defaultConfig {
        applicationId = "ru.rustore.sdk.billingexample" // In buildTypes .debug is often added
    }
}
```

- The keystore signature must be the same as the signature used to sign the app published in RuStore Console. Make sure that the buildType used (e.g. debug) use the same signature as the published app (e.g. release).

## Consumption and cancellation
RuStore Billing SDK requires correct purchase state processing to provide for the best use case scenario.
Purchased consumable products must be consumed, while unfinished purchases have to be canceled—to make a new purchase possible.

Processing of unfinished purchases must be done by the app developer.

Use deletePurchase method if:

* The `getPurchases` method returned an error with the following status: 
 * `PurchaseState.CREATED`. 
 * `PurchaseState.INVOICE_CREATED`. 
* The `purchaseProduct` method returned `PaymentResult.Failure`.

Use confirmPurchase if `getPurchases` returned a CONSUMABLE type error with PurchaseState.PAID status.

On opening your products screen get the purchases list with getPurchases() and process these purchases in the following manner:
```
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
                if (purchase.getPurchaseState() != null) {
                    if (purchase.getPurchaseState() == PurchaseState.CREATED ||
                            purchase.getPurchaseState() == PurchaseState.INVOICE_CREATED )
                    {
                        deletePurchase(purchaseId);
                    } else if (purchase.getPurchaseState() == PurchaseState.PAID) {
                        confirmPurchase(purchaseId);
                    }
                } else {
                    Log.e("RuStoreBillingClient", "PurchaseState is null")
                }
            }
        });
    }).addOnFailureListener(throwable ->
        Log.e("RuStoreBillingClient", "Error calling getPurchases cause: " + throwable)
    );
}
```
The example is taken from [StartFragment.java](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=app/src/main/java/ru/rustore/example/rustorebillingsample/StartFragment.java&branch=master).
> Synchronous await() methods are optional.

Process the purchase result in the following manner:
```
public void purchaseProduct(String productId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.purchaseProduct(productId, null, 1, developerPayload)
                .addOnSuccessListener(this::handlePaymentResult)
                .addOnFailureListener(throwable ->
            // Process error
        );
}

private void handlePaymentResult(PaymentResult paymentResult) {
        if (paymentResult instanceof PaymentResult.Success) {
            confirmPurchase(((PaymentResult.Success) paymentResult).getPurchaseId());
        } else if (paymentResult instanceof PaymentResult.Failure) {
            deletePurchase(((PaymentResult.Failure) paymentResult).getPurchaseId());
        }
}

```
The example is taken from [StartFragment.java](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=app/src/main/java/ru/rustore/example/rustorebillingsample/StartFragment.java&branch=master).

## Still have questions left?

If there are any questions about SDK payments integration, please, write to support@rustore.ru.