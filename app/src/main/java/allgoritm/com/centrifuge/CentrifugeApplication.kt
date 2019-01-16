package allgoritm.com.centrifuge

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import allgoritm.com.centrifuge.di.AppInjector
import allgoritm.com.centrifuge.v1.data.LOG_TAG
import android.util.Log
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class CentrifugeApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        RxJavaPlugins.setErrorHandler {
            Log.e(LOG_TAG, "rx java", it)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}