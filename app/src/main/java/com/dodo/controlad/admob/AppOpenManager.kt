package com.dodo.controlad.admob

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
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
    var mActivities = HashMap<String, Activity>()
    var localClassName: String? = ""

    init {
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun fetchAdFirstTime(
        admobIdOpenApp: String,
        localClassName: String?,
        showOpenAdsAdmobListener: ShowOpenAdsAdmobListener
    ) {
        this.localClassName = localClassName
        resetFetchTime()
        fetchAd(admobIdOpenApp, showOpenAdsAdmobListener)
    }


    fun fetchAd(
        admobIdOpenApp: String,
        showOpenAdsAdmobListener: ShowOpenAdsAdmobListener
    ) {
        Log.e("Control Ads ", "fetchAd Open App$showTime")
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
            getCurrentActivityVisible()?.apply {
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
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Log.e("Control Ads ", "onActivityCreated" + activity.localClassName)
        if (!mActivities.containsKey(activity.localClassName)) {
            mActivities[activity.localClassName] = activity
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!mActivities.containsKey(activity.localClassName)) {
            mActivities[activity.localClassName] = activity
        }
        Log.e("Control Ads ", "onActivityStarted" + activity.localClassName)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.e("Control Ads ", "onActivityResumed" + activity.localClassName)
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e("Control Ads ", "onActivityPaused" + activity.localClassName)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.e("Control Ads ", "onActivityStopped " + activity.localClassName)
        mActivities.remove(activity.localClassName)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun getCurrentActivityVisible(): Activity? {
        if (mActivities.size > 0) {
            for (key in mActivities.keys) {
                mActivities[key]?.let {
                    if (it is AppCompatActivity) {
                        if ((it as AppCompatActivity?)!!.lifecycle.currentState.isAtLeast(
                                Lifecycle.State.RESUMED
                            )
                            && key == localClassName
                        ) {
                            return it
                        }
                    } else if (key == localClassName && !it.isFinishing) {
                        return it
                    }
                }

            }
        }
        return null
    }
}