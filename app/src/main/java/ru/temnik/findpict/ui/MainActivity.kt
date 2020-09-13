package ru.temnik.findpict.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import java.io.File

class MainActivity : AppCompatActivity(), ProfileView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        supportFragmentManager
            .beginTransaction()
//            .setCustomAnimations(android.R.animator.fade_in,
//                android.R.animator.fade_out)
            .addToBackStack(HomeFragment.tag)
            .add(
                R.id.activity_main_frame,
                HomeFragment(),
                HomeFragment.tag
            )
            .commit()
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    fun clearApplicationData() {
        val cache: File = cacheDir
        val appDir = File(cache.getParent())
        if (appDir.exists()) {
            val children: Array<String> = appDir.list()
            for (s in children) {
                if (s != "lib") {
                    deleteDir(File(appDir, s))
                    Log.i(
                        "TAG",
                        "**************** File /data/data/APP_PACKAGE/$s DELETED *******************"
                    )
                }
            }
        }
    }

    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        if (dir != null) {
            return dir.delete()
        }
        return false
    }

    override fun setErrorFragment() {

    }


}