package ru.temnik.findpict.ui.detailsImageFragment

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.temnik.findpict.AppData
import ru.temnik.findpict.ProfilePresenter
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.entityDTO.ImageDTO
import ru.temnik.findpict.ui.dialogFragments.DialogNotificationFragment

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

    fun shareImage(observer: Observer<Bitmap>) {
        val view = profileView
        val itemSave = item
        if (itemSave != null && view != null && view is DetailsImageFragment) {
            val context = view.context
            if (context != null) {
                Observable.fromCallable {
                    Glide.with(context)
                        .asBitmap()
                        .load(itemSave.largeImageURL)
                        .submit()
                        .get()

                }.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(observer)

            }
        }
    }


    fun saveImage() {
        val view = profileView
        val itemSave = item
        if (itemSave != null && view != null && view is DetailsImageFragment) {
            val context = view.context
            context?.let {
                saveImageStart(it, getDefaultImageObserver(itemSave), itemSave)
            }
        } else {
            profileView?.visibilityError(true)
            Log.e(
                AppData.DEBUG_TAG,
                "$logName Ошибка загрузки файла! Отсутствует context или item!"
            )
        }
    }

    private fun saveImageStart(
        context: Context,
        observer: Observer<Boolean>,
        item: ImageDTO
    ) {
        Observable.fromCallable { saveImageToStorage(context, item) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }


     fun getBitmapCompressFormat(imageFormat: String): Bitmap.CompressFormat {
        return when (imageFormat) {
            "jpg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.WEBP
        }
    }

    private fun createImageUri(
        context: Context,
        item: ImageDTO,
        imageFormat: String
    ): Uri? {
        val resolver = context.contentResolver
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

    private fun saveImageToStorage(context: Context, itemSave: ImageDTO): Boolean {
        val bitmapImage =
            Glide.with(context)
                .asBitmap()
                .load(itemSave.largeImageURL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val view = profileView as DetailsImageFragment

                        val manager = view.childFragmentManager

                        val fragment = DialogNotificationFragment.newInstance(
                            view.getString(R.string.saveimage_helper_failed_load),
                            view.getString(R.string.default_check_internet)
                        )
                        fragment.show(manager, fragment.tag)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .submit()
                .get()
        val imageFormat = itemSave.largeImageURL?.substringAfterLast(".") ?: "jpg"
        val resolver = context.contentResolver
        val uri = createImageUri(context, itemSave, imageFormat)
        val stream =
            uri?.let { uriNotNull -> resolver.openOutputStream(uriNotNull) }
        var saved = false
        if (stream != null) {
            val bitmapFormat = getBitmapCompressFormat(imageFormat)
            saved = bitmapImage.compress(bitmapFormat, 100, stream)
            stream.close()
        }
        return saved
    }

    private fun getDefaultImageObserver(item: ImageDTO): Observer<Boolean> {
        return object : Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: Boolean) {
                if (t) {
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

