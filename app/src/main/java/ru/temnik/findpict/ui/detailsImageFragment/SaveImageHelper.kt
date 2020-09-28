package ru.temnik.findpict.ui.detailsImageFragment

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.temnik.findpict.AppData
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.entityDTO.ImageDTO

class SaveImageHelper {
    private val logName = "[SaveImageHelper]"
    private var profileView: ProfileView? = null
    private var item: ImageDTO? = null

    fun onAttach(view: ProfileView) {
        profileView = view
    }

    fun onDetach() {
        profileView = null

    }
    fun onAttachImageItem(_item: ImageDTO) {
        item = _item
    }
    fun onDetachImageItem() {
        item = null
    }


    fun saveImage(observer: Observer<Pair<Boolean, Uri?>>? = null) {
        val view = profileView
        val itemSave = item
        if (itemSave != null && view != null && view is DetailsImageFragment) {
            val context = view.context
            context?.let {
                if (observer == null) {
                    saveImageStart(it, getDefaultImageObserver(itemSave), itemSave)
                } else {
                    saveImageStart(it, observer, itemSave)
                }
            }
        } else {
            profileView?.visibilityError(true)
            Log.e(
                AppData.DEBUG_TAG,
                "$logName Ошибка загрузки файла! Отсутствует context или item!"
            )
        }
    }

    private fun saveImageStart(context: Context, observer: Observer<Pair<Boolean, Uri?>>, item: ImageDTO) {
        Observable.fromCallable { saveImageToStorage(context, item) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }


    private fun getBitmapCompressFormat(imageFormat: String): Bitmap.CompressFormat {
        return when (imageFormat) {
            "jpg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.WEBP
        }
    }

    private fun createImageUri(resolver: ContentResolver, item: ImageDTO, imageFormat: String): Uri? {
        val values = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "picture${item.id}.$imageFormat"
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/$imageFormat")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "DCIM/${AppData.DEFAULT_SAVE_IMAGES_DIRECTORY}"
                )
            }
        }
        return resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun saveImageToStorage(context: Context, itemSave: ImageDTO): Pair<Boolean, Uri?> {
        val bitmapImage =
            Glide.with(context)
                .asBitmap()
                .load(itemSave.largeImageURL)
                .submit()
                .get()

        val imageFormat = itemSave.largeImageURL?.substringAfterLast(".") ?: "jpg"
        val resolver = context.contentResolver
        val uri = createImageUri(resolver, itemSave, imageFormat)
        val stream =
            uri?.let { uriNotNull -> resolver.openOutputStream(uriNotNull) }
        var saved = false
        if (stream != null) {
            val bitmapFormat = getBitmapCompressFormat(imageFormat)
            saved = bitmapImage.compress(bitmapFormat, 100, stream)
            stream.close()
        }
        return saved to uri
    }

    private fun getDefaultImageObserver(item: ImageDTO): Observer<Pair<Boolean, Uri?>> {
        return object : Observer<Pair<Boolean, Uri?>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: Pair<Boolean, Uri?>) {
                if (t.first) {
                    profileView?.visibilitySave(true)
                    Log.i(
                        AppData.DEBUG_TAG,
                        "$logName Файл picture${item.id} успешно сохранен!"
                    )
                } else {
                    profileView?.visibilityError(true)
                    Log.e(
                        AppData.DEBUG_TAG,
                        "$logName Файл picture${item.id} - сохранение не удалось!"
                    )
                }
            }

            override fun onError(e: Throwable) {
                profileView?.visibilityError(true)
                Log.wtf(
                    AppData.DEBUG_TAG,
                    "$logName Фатальная ошибка загрузки!"
                )
            }

            override fun onComplete() {

            }
        }
    }
}