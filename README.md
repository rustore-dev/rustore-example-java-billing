# Пример внедрения SDK платежей RuStore
## [Документация SDK платежей](https://www.rustore.ru/help/sdk/payments/kotlin-java/)

### [Readme English Version](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=README_EN.md&branch=master)

### Требуемые условия

Для корректной работы SDK необходимо соблюдать следующие условия:

- Задан правильно consoleApplicationId в create():
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

- ApplicationId, указанный в build.gradle, совпадает с applicationId apk-файла, который вы публиковали в консоль RuStore:
```
android {
    defaultConfig {
        applicationId = "ru.rustore.sdk.billingexample" // Зачастую в buildTypes приписывается .debug
    }
}
```

- Подпись keystore должна совпадать с подписью, которой было подписано приложение, опубликованное в консоль RuStore. Убедитесь, что используемый buildType (пр. debug) использует такую же подпись, что и опубликованное приложение (пр. release).

### Потребление и отмена покупки
RuStore Billing SDK требует правильно обрабатывать состояния покупки, чтобы предоставить наилучший сценарий использования.
Так, купленные потребляемые товары необходимо потребить, а незаконченные покупки - отменять, чтобы иметь возможность заново начать новую.
Подробнее о [потреблении](https://www.rustore.ru/help/sdk/payments/kotlin-java/5-0-0/#подтверждение-покупки) и [отмене](https://www.rustore.ru/help/sdk/payments/kotlin-java/5-0-0/#отмена-покупки).

При открытии вашего экрана товаров, необходимо получить список товаров с помощью getPurchases() и обработать товары следующим образом:
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
Пример взят из [StartFragment.java](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=app/src/main/java/ru/rustore/example/rustorebillingsample/StartFragment.java&branch=master).
> Использовать синхронные await() методы не обязательно.

Обработать результат покупки необходимо следующим образом:
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
Пример взят из [StartFragment.java](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=app/src/main/java/ru/rustore/example/rustorebillingsample/StartFragment.java&branch=master).

### Есть вопросы
Если появились вопросы по интеграции SDK платежей, обратитесь по [ссылке](https://www.rustore.ru/help/sdk/payments).