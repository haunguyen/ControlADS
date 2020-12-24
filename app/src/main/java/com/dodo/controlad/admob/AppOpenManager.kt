package com.dodo.controlad.admob

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*


class AppOpenManager(
    context: Application
) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    private var isShowingAd = false
    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallBack: AppOpenAd.AppOpenAdLoadCallback
    private var myApplication: Application = context
    private lateinit var currentActivity: Activity
    private var loadTime: Long = 0
    lateinit var fullScreenContentCallback: FullScreenContentCallback
    private var showTime = 0

    init {
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun fetchAdFirstTime(
        admobIdOpenApp: String,
        showOpenAdsAdmobListener: ShowOpenAdsAdmobListener
    ){
        resetFetchTime()
        fetchAd(admobIdOpenApp, showOpenAdsAdmobListener)
    }


    fun fetchAd(
        admobIdOpenApp: String,
        showOpenAdsAdmobListener: ShowOpenAdsAdmobListener
    ) {
        Log.e("Control Ads ","fetchAd Open App$showTime")
        showTime++
        if (isAdAvailable()) {
            return
        }
        loadCallBack = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAppOpenAdLoaded(ad: AppOpenAd) {
                super.onAppOpenAdLoaded(ad)
                appOpenAd = ad
                loadTime = Date().time
                Log.e("Control Ads ", "load ad open app done")
                showAdIfAvailable(admobIdOpenApp, showOpenAdsAdmobListener)

            }

            override fun onAppOpenAdFailedToLoad(p0: LoadAdError) {
                super.onAppOpenAdFailedToLoad(p0)
                showOpenAdsAdmobListener.onLoadFailAdsOpenApp()
                Log.e("Control Ads ", "load ad open app FAIL")
            }
        }

        val request: AdRequest = getAdRequest()
        AppOpenAd.load(
            myApplication, admobIdOpenApp, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallBack
        )
    }

    /** Shows the ad if one isn't already showing.  */
    fun showAdIfAvailable(
        admobIdOpenApp: String,
        showOpenAdsAdmobListener: ShowOpenAdsAdmobListener
    ) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        Log.e("Control Ads ", "showAdIfAvailable " + isShowingAd + "////" + isAdAvailable())
        if (!isShowingAd && isAdAvailable()) {
            if (showTime > 1) {
                return
            }
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd(admobIdOpenApp, showOpenAdsAdmobListener)
                        showOpenAdsAdmobListener.onLoadedAdsOpenApp()
                        Log.e("Control Ads ", "onAdDismissedFullScreenContent")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("Control Ads ", "onAdFailedToShowFullScreenContent")
                        showOpenAdsAdmobListener.onLoadFailAdsOpenApp()
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.e("Control Ads ", "onAdShowedFullScreenContent")
                        isShowingAd = true
                    }
                }
            currentActivity?.apply {
                showTime++
                appOpenAd?.show(this, fullScreenContentCallback)
            }
        } else {
            fetchAd(admobIdOpenApp, showOpenAdsAdmobListener)
        }
    }

    private fun resetFetchTime() {
        showTime = 0
    }

    /** Utility method to check if ad was loaded more than n hours ago.  */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    // implement Application.ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}