package ru.temnik.findpict.ui.dialogShareFragment

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import ru.temnik.findpict.AppData
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment.Companion.item
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment.Companion.presenter
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment.Companion.sharedUri

class DialogShareFragment : DialogFragment() {
    companion object {
        val tag = DialogShareFragment::class.java.simpleName
        var parentFragment: Fragment? = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = "Выберите способ поделиться: "
        val imageBtn = "Поделиться картинкой"
        val linkBtn = "Поделиться ссылкой"
        val builder =
            AlertDialog.Builder(ContextThemeWrapper(activity!!, R.style.Theme_AppCompat_Dialog))

        builder.setMessage(message)
        builder.setNegativeButton(
            linkBtn
        ) { dialog, id ->
            shareLink()
        }
        builder.setPositiveButton(
            imageBtn
        ) { dialog, id ->
            shareImage()
        }
        builder.setCancelable(true)
        return builder.create()
    }

    private fun shareImage() {
        presenter.saveImage(getImageShareObserver())
    }

    private fun shareLink() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, item.largeImageURL)
        startActivity(sharingIntent)
    }
    private fun getImageShareObserver(): Observer<Pair<Boolean, Uri?>> {
        return object : Observer<Pair<Boolean, Uri?>> {
            override fun onSubscribe(d: Disposable) {
                Toast.makeText(parentFragment?.context,"Изображение подготавливается",Toast.LENGTH_SHORT).show()
            }

            override fun onNext(t: Pair<Boolean, Uri?>) {
                Log.d(AppData.DEBUG_TAG,Thread.currentThread().name)
                val uri = t.second
                uri?.let {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, it)
                    }
                    parentFragment?.startActivity(Intent.createChooser(share, "Поделиться изображением"))
                    sharedUri = it
//                    parentFragment?.startActivityForResult(
//                        Intent.createChooser(share, "Поделиться изображением"),
//                        AppData.REQUEST_CODE_SHARE_IMAGE
//                    )

                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        }
    }



}