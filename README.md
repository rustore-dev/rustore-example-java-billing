# Пример внедрения SDK платежей RuStore
## [Документация SDK платежей](https://help.rustore.ru/rustore/for_developers/developer-documentation/sdk_payments/SDK-connecting-payments/quick_start)

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
[Подробнее о потреблении и отмене.](https://help.rustore.ru/rustore/for_developers/developer-documentation/sdk_payments/SDK-connecting-payments/%20consumption-and-withdrawal)

При открытии вашего экрана товаров, необходимо получить список товаров с помощью getPurchases() и обработать товары следующим образом:
```
purchasesUseCase.getPurchases().addOnCompleteListener(new OnCompleteListener<List<Purchase>>() {
            @Override
            public void onFailure(@NonNull Throwable throwable) {
                Log.e("RuStoreBillingClient", "Error calling getPurchases cause: " + throwable);
            }

            @Override
            public void onSuccess(List<Purchase> purchases) {
                PurchaseAdapter purchaseAdapter = new PurchaseAdapter(purchases);

                productsList.setAdapter(purchaseAdapter);
                productsList.setLayoutManager(new LinearLayoutManager(getContext()));

                purchases.forEach(purchase -> {
                    String purchaseId = purchase.getPurchaseId();
                    if (purchaseId != null) {
                        if (purchase.getPurchaseState() == PurchaseState.CREATED ||
                                purchase.getPurchaseState() == PurchaseState.INVOICE_CREATED )
                        {
                            deletePurchase(purchaseId);
                        } else if (purchase.getPurchaseState() == PurchaseState.PAID) {
                            confirmPurchase(purchaseId);
                        }
                    }
                });
            }
        });
```
Пример взят из [StartFragment.java](https://gitflic.ru/project/rustore/rustore-example-java-billing/blob?file=app/src/main/java/ru/rustore/example/rustorebillingsample/StartFragment.java&branch=master).
> Использовать синхронные await() методы не обязательно.

Обработать результат покупки необходимо следующим образом:
```
public void purchaseProduct(String productId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.purchaseProduct(productId)
                .addOnCompleteListener(new OnCompleteListener<PaymentResult>() {
            @Override
            public void onFailure(@NonNull Throwable throwable) {

            }

            @Override
            public void onSuccess(PaymentResult paymentResult) {
                handlePaymentResult(paymentResult);
            }
        });
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
Если появились вопросы по интеграции SDK платежей, обратитесь по этой ссылке:
[https://help.rustore.ru/rustore/trouble/user/help_user_email](https://help.rustore.ru/rustore/trouble/user/help_user_email)
или напишите на почту support@rustore.ru.