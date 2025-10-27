package com.smile.colorballs_main.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.smile.colorballs_main.tools.LogUtil

class MyBillingClient(context: Context) {

    private var billingClient: BillingClient
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                // Process the purchase.
                // Verify the purchase with your backend server (highly recommended).
                // Acknowledge the purchase if it's a non-consumable or a subscription.
                // Consume the purchase if it's a consumable product.
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    init {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts() // For one-time products
            // .enableSubscriptions() // If you also want to enable pending subscriptions
            .build()
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            // Required for purchases that involve a delayed payment method
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    LogUtil.d(TAG, "BillingClient setup successful.")
                    // Query for existing purchases, products, etc.
                } else {
                    LogUtil.e(TAG, "BillingClient setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                LogUtil.w(TAG, "BillingClient disconnected. Retrying...")
                // You might want to implement a retry mechanism with backoff.
            }
        })
    }

    // --- Methods to handle purchases, query products, etc. will go here ---

    private fun handlePurchase(purchase: Purchase) {
        // TODO: Implement purchase handling logic
        // 1. Verify the purchase on your backend server.
        // 2. Grant entitlement to the user.
        // 3. Acknowledge or consume the purchase.
        LogUtil.i(TAG, "Purchase successful: ${purchase.orderId}")

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        LogUtil.d(TAG, "Purchase acknowledged.")
                    } else {
                        LogUtil.e(TAG, "Error acknowledging purchase: ${billingResult.debugMessage}")
                    }
                }
            }
        }
    }

    // Example of launching the purchase flow
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        val productDetailsParamsList = mutableListOf<BillingFlowParams.ProductDetailsParams>()

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        // If it's a subscription, you might need to specify an offer token
        offerToken?.let {
            productDetailsParamsBuilder.setOfferToken(it)
        }

        productDetailsParamsList.add(productDetailsParamsBuilder.build())

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            LogUtil.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
        }
    }


    // Make sure to end the connection when it's no longer needed, e.g., in onDestroy() of your Activity/Fragment
    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
            LogUtil.i(TAG, "BillingClient connection ended.")
        }
    }

    companion object {
        private const val TAG = "MyBillingClient"
    }
}
