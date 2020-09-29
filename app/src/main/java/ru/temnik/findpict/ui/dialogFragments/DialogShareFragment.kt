package ru.temnik.findpict.ui.dialogFragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import ru.temnik.findpict.AppData
import ru.temnik.findpict.BuildConfig
import ru.temnik.findpict.R
import ru.temnik.findpict.entityDTO.ImageDTO
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment.Companion.item
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment.Companion.presenter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DialogShareFragment : DialogFragment() {
    companion object {
        val tag = DialogShareFragment::class.java.simpleName
        var parentFragment: Fragment? = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message =
            activity?.getString(R.string.dialog_share_choose_way_share) ?: "Сhoose a way to share: "
        val imageBtn = activity?.getString(R.string.dialog_share_share_picture) ?: "Share picture"
        val linkBtn = activity?.getString(R.string.dialog_share_share_link) ?: "Share link"
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
        presenter.shareImage(getShareObserver())
    }

    private fun shareLink() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, item.largeImageURL)
        startActivity(sharingIntent)
    }

    private fun getShareObserver(item: ImageDTO = DetailsImageFragment.item): Observer<Bitmap> {
        return object : Observer<Bitmap> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: Bitmap) {
                val context = parentFragment?.context
                if (context != null) {
                    val imageFormat = item.largeImageURL?.substringAfterLast(".") ?: "jpg"
                    val file =
                        File(context.filesDir, "${AppData.DEFAULT_SHARED_IMAGE_NAME}.$imageFormat")
                    if (file.exists()) {
                        file.delete()
                    }
                    file.createNewFile()
                    val byteArrayImage = ByteArrayOutputStream()
                    val bitmapFormat = presenter.getBitmapCompressFormat(imageFormat)
                    t.compress(bitmapFormat, 90, byteArrayImage)
                    val outStream = FileOutputStream(file)
                    outStream.write(byteArrayImage.toByteArray())
                    outStream.flush()
                    outStream.close()
                    val uri =
                        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)

                    uri?.let {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, it)
                        }

                        parentFragment?.startActivity(
                            Intent.createChooser(
                                share,
                                parentFragment?.getString(R.string.dialog_share_share_picture)
                                    ?: "Share picture"
                            )
                        )
                        Log.d(
                            AppData.DEBUG_TAG,
                            "Успешная передача uri картинки в другое приложение!"
                        )
                    }
                }
            }

            override fun onError(e: Throwable) {
                val context = parentFragment
                if (context != null && context is DetailsImageFragment) {
                    val fragment =
                        DialogNotificationFragment.newInstance(
                            context.getString(R.string.details_default_failed_loading_image_info),
                            context.getString(R.string.default_check_internet)
                        )
                    fragment.show(context.childFragmentManager, DialogNotificationFragment.tag)
                }

                Log.d(AppData.DEBUG_TAG, "Ошибка передачи картинки в другое приложение!")
            }

            override fun onComplete() {
            }
        }
    }
}