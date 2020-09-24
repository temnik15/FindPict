package ru.temnik.findpict.ui


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.home_fragment.*
import ru.temnik.findpict.AppData
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment
import ru.temnik.findpict.ui.homeFragment.HomeFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(HomeFragment.tag)
                .add(R.id.activity_main_frame, HomeFragment.newInstance(), HomeFragment.tag)
                .commit()
        }
        checkingLastCacheDelete()
    }

    override fun onBackPressed() {
        val detailsFragment = supportFragmentManager.findFragmentByTag(DetailsImageFragment.tag)
        val homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment.tag)
        if (detailsFragment == null && homeFragment != null &&
            homeFragment is ProfileView
        ) {
            if (!homeFragment.onBackPressed()) {
                finish()
            } else homeFragment.visibilityAppName(true)
        } else {
            if (detailsFragment is ProfileView) {
                if (!detailsFragment.onBackPressed()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }



   private fun checkingLastCacheDelete(){
       fun checkingCleaningPeriod(lastDateMs:Long):Boolean{
           val currentDate = Date().time
           return currentDate-lastDateMs>AppData.CACHE_FLUSH_PERIOD
       }
       val sharedPref = getSharedPreferences(AppData.APP_SHARED_PREF, MODE_PRIVATE)
       val lastDateMs = sharedPref.getLong(AppData.SHARED_PREF_LAST_CACHE_REMOVAL,0)
       if(checkingCleaningPeriod(lastDateMs)){
           val editor = sharedPref.edit()
           editor.putLong(AppData.SHARED_PREF_LAST_CACHE_REMOVAL,Date().time)
           editor.apply()
           Observable.fromCallable {
               Glide.get(applicationContext).clearDiskCache()
           }.subscribeOn(Schedulers.io()).subscribe()
           Log.d(AppData.DEBUG_TAG,"${AppData.APP_LOG_NAME} Очистка кэша успешно проведена!")
       }


   }


}