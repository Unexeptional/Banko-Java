package com.unexceptional.beast.banko.newVersion.ui.activity

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.unexceptional.beast.banko.R
import com.unexceptional.beast.banko.newVersion.ui.adapter.ProductsAdapter

import kotlinx.android.synthetic.main.activity_billingtest.*

class billingtest : AppCompatActivity(), PurchasesUpdatedListener {



    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        println("responseCode: działa")
    }


    private lateinit var productsAdapter: ProductsAdapter

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billingtest)
        setupBillingClient()



    }

/* private fun setupBillingClient() {
     billingClient = BillingClient
             .newBuilder(this)
             .setListener(this)
             .build()

     billingClient.startConnection(object : BillingClientStateListener {
         override fun onBillingSetupFinished(billingResult: BillingResult?) {
             if (billingResult.responseCode == BillingResponse.OK) {
                 println("BILLING | startConnection | RESULT OK")
             } else {
                 println("BILLING | startConnection | RESULT: $billingResponseCode")
             }
         }

         override fun onBillingServiceDisconnected() {
             println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
         }
     })
 }*/

 private fun setupBillingClient() {
     billingClient = BillingClient.newBuilder(this)
             .enablePendingPurchases()
             .setListener(this).build()
     billingClient.startConnection(object : BillingClientStateListener {
         override fun onBillingSetupFinished(billingResult: BillingResult) {
             if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                 onLoadProductsClicked()
                 // The BillingClient is ready. You can query purchases here.
             }
         }
         override fun onBillingServiceDisconnected() {
             // Try to restart the connection on the next request to
             // Google Play by calling the startConnection() method.
         }
     })
 }

 private fun initProductAdapter(skuDetailsList: List<SkuDetails>) {
     productsAdapter = ProductsAdapter(skuDetailsList) {
         val billingFlowParams = BillingFlowParams
                 .newBuilder()
                 .setSkuDetails(it)
                 .build()
         billingClient.launchBillingFlow(this, billingFlowParams)
     }

     products.adapter = productsAdapter
 }

    fun onLoadProductsClicked() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("querySkuDetailsAsync, responseCode: $responseCode")
                    initProductAdapter(skuDetailsList)
                } else {
                    println("Can't querySkuDetailsAsync, responseCode: $responseCode")
                }
            }
        } else {
            println("Billing Client not ready")
        }
    }

    companion object {
        private val skuList = listOf("no_ads_month")
    }

}
