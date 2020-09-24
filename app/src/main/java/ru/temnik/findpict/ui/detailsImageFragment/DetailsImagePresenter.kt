package ru.temnik.findpict.ui.detailsImageFragment

import android.content.ContentValues
import android.graphics.Bitmap
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
import ru.temnik.findpict.ProfilePresenter
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.entityDTO.ImageDTO
import java.util.concurrent.Callable

class DetailsImagePresenter : ProfilePresenter {
    private val logName = "[DetailsImagePresenter]"
    private var profileView: ProfileView? = null
    private var item: ImageDTO? = null


    override fun onAttach(view: ProfileView) {
        profileView = view
    }

    override fun onDetach() {
        profileView = null
    }

    fun onAttachImageItem(_item: ImageDTO) {
        item = _item
    }

    fun onDetachImageItem() {
        item = null
    }


    fun saveImageToGallery() {
        val view = profileView
        val itemSave = item
        if (itemSave != null && view != null && view is DetailsImageFragment) {
            val context = view.context
            context?.let {
                val imageLargeUrl = itemSave.largeImageURL
                val callable = Callable<Boolean> {
                    val bitmapImage =
                        Glide.with(it)
                            .asBitmap()
                            .load(imageLargeUrl)
                            .submit()
                            .get()

                    val imageFormat = itemSave.largeImageURL?.substringAfterLast(".") ?: "jpg"
                    val resolver = it.contentResolver
                    val values = ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "picture${itemSave.id}.$imageFormat"
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/$imageFormat")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                "DCIM/${AppData.DEFAULT_SAVE_IMAGES_DIRECTORY}"
                            )
                        }
                    }
                    val uri =
                        resolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                    val stream =
                        uri?.let { uriNotNull -> resolver.openOutputStream(uriNotNull) }
                    var saved = false
                    if (stream != null) {
                        val bitmapFormat =
                            when (imageFormat) {
                                "jpg" -> Bitmap.CompressFormat.JPEG
                                "png" -> Bitmap.CompressFormat.PNG
                                else -> Bitmap.CompressFormat.WEBP
                            }
                        saved = bitmapImage.compress(bitmapFormat, 100, stream)
                        stream.close()
                    }
                    saved
                }

                val observer = object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            profileView?.visibilityLoading(true)
                            Log.i(
                                AppData.DEBUG_TAG,
                                "$logName Файл picture${itemSave.id} успешно сохранен!"
                            )
                        } else {
                            profileView?.visibilityError(true)
                            Log.e(
                                AppData.DEBUG_TAG,
                                "$logName Файл picture${itemSave.id} - сохранение не удалось!"
                            )
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.wtf(
                            AppData.DEBUG_TAG,
                            "$logName Фатальная ошибка загрузки!"
                        )
                    }

                    override fun onComplete() {

                    }
                }
                Observable.fromCallable(callable)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer)
            }
        } else {
            profileView?.visibilityError(true)
            Log.e(
                AppData.DEBUG_TAG,
                "$logName Ошибка загрузки файла! Отсутствует context или item!"
            )
        }
    }
}

