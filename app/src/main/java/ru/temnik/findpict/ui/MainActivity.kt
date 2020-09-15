package ru.temnik.findpict.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import kotlinx.android.synthetic.main.activity_main.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.homeFragment.HomeFragment
import java.io.File

class MainActivity : AppCompatActivity(), ProfileView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if(savedInstanceState==null){
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(HomeFragment.tag)
                .add(
                    R.id.activity_main_frame,
                    HomeFragment(),
                    HomeFragment.tag
                )
                .commit()
        }
    }

    override fun onBackPressed() {
        val adapter = HomeFragment.contentPresenter?.contentAdapter
        if(adapter!=null && !adapter.isLoading && adapter.itemCount>0){
            adapter.updateItems(emptyList())
            tv_appName?.visibility=View.VISIBLE
            iv_error?.visibility=View.GONE
            tv_loading?.visibility=View.GONE
        }else{
            finish()
        }
    }

    private fun clearApplicationData() {
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

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
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

    override fun visibilityError(visible: Boolean) {
        if(visible){
            tv_appName?.visibility=View.GONE
            iv_error?.visibility=View.VISIBLE
        }else{
            iv_error?.visibility=View.GONE
        }
    }

    override fun visibilityLoading(visible: Boolean) {
        if(visible){
            tv_appName?.visibility=View.GONE
            iv_error?.visibility=View.GONE
            //Todo показать загрузку
            tv_loading?.visibility=View.VISIBLE
        }else{
            //Todo спрятать загрузку
            tv_loading?.visibility=View.GONE
        }
    }
}