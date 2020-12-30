package com.dodo.controlad.admob


import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.dodo.controlad.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd



object InterstitialAdAdmob {

    private lateinit var mInterstitialAds: PublisherInterstitialAd

    fun initAdAdmob(context: Context, idAdmobInterstitial: String) {
        mInterstitialAds = PublisherInterstitialAd(context)
        mInterstitialAds.adUnitId = idAdmobInterstitial
        loadAdAdmob()
    }


     fun loadAdAdmob() {
        mInterstitialAds.loadAd(PublisherAdRequest.Builder().build())
    }

    fun showAdAdmob(context: Context, showInterstitialAdsAdmobListener: ShowInterstitialAdsAdmobListener) {

        val dialog = Dialog(context, R.style.DialogFragmentTheme)
        dialog.setContentView(R.layout.dialog_loading_ads_fullscreen)
        dialog.show()


        Handler(Looper.getMainLooper()).postDelayed({
            if (mInterstitialAds.isLoaded) {
                mInterstitialAds.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        dialog.dismiss()
                        showInterstitialAdsAdmobListener.onLoadFailInterstitialAdsAdmob()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        dialog.dismiss()
                        showInterstitialAdsAdmobListener.onInterstitialAdsAdmobClose()
                        loadAdAdmob()
                    }

                }
                mInterstitialAds.show()
            }else{
                dialog.dismiss()
                showInterstitialAdsAdmobListener.onLoadFailInterstitialAdsAdmob()
            }
        },600)


    }


}