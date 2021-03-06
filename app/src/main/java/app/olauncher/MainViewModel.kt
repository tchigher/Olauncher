package app.olauncher

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import app.olauncher.data.AppModel
import app.olauncher.data.Constants
import app.olauncher.data.Prefs
import app.olauncher.helper.WallpaperWorker
import app.olauncher.helper.getAppsList
import app.olauncher.helper.getDefaultLauncherPackage
import app.olauncher.helper.resetDefaultLauncher
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefs = Prefs(appContext)

    //  private val selectedApp = MutableLiveData<AppModelWithFlag>()
    val firstOpen = MutableLiveData<Boolean>()
    val refreshHome = MutableLiveData<Boolean>()
    val updateSwipeApps = MutableLiveData<Any>()
    val appList = MutableLiveData<List<AppModel>>()
    val isOlauncherDefault = MutableLiveData<Boolean>()
    val launcherResetFailed = MutableLiveData<Boolean>()
    val isDarkModeOn = MutableLiveData<Boolean>()
    val homeAppAlignment = MutableLiveData<Int>()

    fun selectedApp(appModel: AppModel, flag: Int) {
        when (flag) {
            Constants.FLAG_LAUNCH_APP -> {
                launchApp(appModel.appPackage)
            }
            Constants.FLAG_SET_HOME_APP_1 -> {
                prefs.appName1 = appModel.appLabel
                prefs.appPackage1 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_2 -> {
                prefs.appName2 = appModel.appLabel
                prefs.appPackage2 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_3 -> {
                prefs.appName3 = appModel.appLabel
                prefs.appPackage3 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_4 -> {
                prefs.appName4 = appModel.appLabel
                prefs.appPackage4 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_5 -> {
                prefs.appName5 = appModel.appLabel
                prefs.appPackage5 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_HOME_APP_6 -> {
                prefs.appName6 = appModel.appLabel
                prefs.appPackage6 = appModel.appPackage
                refreshHome(false)
            }
            Constants.FLAG_SET_SWIPE_LEFT_APP -> {
                prefs.appNameSwipeLeft = appModel.appLabel
                prefs.appPackageSwipeLeft = appModel.appPackage
                updateSwipeApps()
            }
            Constants.FLAG_SET_SWIPE_RIGHT_APP -> {
                prefs.appNameSwipeRight = appModel.appLabel
                prefs.appPackageSwipeRight = appModel.appPackage
                updateSwipeApps()
            }
        }
//        selectedApp.value = AppModelWithFlag(appModel, flag)
    }

    fun firstOpen(value: Boolean) {
        firstOpen.postValue(value)
    }

    fun refreshHome(appCountUpdated: Boolean) {
        refreshHome.value = appCountUpdated
    }

    fun updateSwipeApps() {
        updateSwipeApps.postValue(Unit)
    }

    private fun launchApp(packageName: String) {
        try {
            val intent: Intent? = appContext.packageManager.getLaunchIntentForPackage(packageName)
            intent?.addCategory(Intent.CATEGORY_LAUNCHER)
            appContext.startActivity(intent)
        } catch (e: Exception) {
            refreshHome(false)
        }
    }

    fun getAppList() {
        viewModelScope.launch {
            appList.value = getAppsList(appContext)
        }
    }

    fun isOlauncherDefault() {
        isOlauncherDefault.value =
            app.olauncher.helper.isOlauncherDefault(appContext)
    }

    fun resetDefaultLauncherApp(context: Context) {
        resetDefaultLauncher(context)
        launcherResetFailed.value = getDefaultLauncherPackage(
            appContext
        ).contains(".")
    }

    fun switchTheme() {
        prefs.darkModeOn = !prefs.darkModeOn
        setTheme(prefs.darkModeOn)
    }

    private fun setTheme(darkMode: Boolean) {
        prefs.darkModeOn = darkMode
        if (darkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        isDarkModeOn.value = prefs.darkModeOn
    }

    fun setWallpaperWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.DAYS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager
            .getInstance(appContext)
            .enqueueUniquePeriodicWork(Constants.WALLPAPER_WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, uploadWorkRequest)
    }

    fun cancelWallpaperWorker() {
        WorkManager.getInstance(appContext).cancelUniqueWork(Constants.WALLPAPER_WORKER_NAME)
    }

    fun updateHomeAlignment() {
        when (prefs.homeAlignment) {
            Gravity.START -> prefs.homeAlignment = Gravity.END
            Gravity.END -> prefs.homeAlignment = Gravity.CENTER
            Gravity.CENTER -> prefs.homeAlignment = Gravity.START
        }
        homeAppAlignment.value = prefs.homeAlignment
    }
}

data class AppModelWithFlag(
    val appModel: AppModel,
    val flag: Int
)