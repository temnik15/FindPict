package ru.temnik.findpict.ui.detailsImageFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.details_image_fragment.*
import kotlinx.android.synthetic.main.details_image_fragment.view.*
import ru.temnik.findpict.AppData
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.entityDTO.ImageDTO
import ru.temnik.findpict.ui.dialogPermissionFragment.DialogPermissionFragment
import java.util.concurrent.Callable


class DetailsImageFragment : Fragment(), ProfileView {
    companion object {
        val tag = DetailsImageFragment::class.java.simpleName
        private val presenter: DetailsImagePresenter = DetailsImagePresenter()
        private lateinit var item: ImageDTO


        fun newInstance(_item: ImageDTO): DetailsImageFragment {
            item = _item
            presenter.onAttachImageItem(_item)
            return DetailsImageFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.details_image_fragment, container, false)
        val imageView = view.view_image
        setImage(imageView)
        setInfo(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view_arrowBack.setOnClickListener {
            activity?.onBackPressed()
        }
        view_btn_save.setOnClickListener {
            saveImage()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.onAttach(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.onDetach()
    }


    private fun setImage(imageView: SubsamplingScaleImageView) {
        Toast.makeText(imageView.context, R.string.default_loading_image_info, Toast.LENGTH_SHORT)
            .show()
        setPreviewImage(imageView)
        setLargeImage(imageView)
    }

    private fun setInfo(rootView: View) {
        val imageTags = "Tags: ${item.tags ?: "no tags"}"
        val imageSize = "${item.imageWidth}x${item.imageHeight}"
        rootView.view_tags.text = imageTags
        rootView.view_size.text = imageSize
    }

    private fun setPreviewImage(imageView: SubsamplingScaleImageView) {
        val imagePreviewUrl = item.previewURL
        Glide.with(imageView.context)
            .asBitmap()
            .load(imagePreviewUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImage(ImageSource.cachedBitmap(resource))
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    private fun setLargeImage(imageView: SubsamplingScaleImageView) {
        val imageLargeUrl = item.largeImageURL
        val callable = Callable<Bitmap> {
            Glide.with(imageView.context)
                .asBitmap()
                .load(imageLargeUrl)
                .submit()
                .get()
        }
        val observer = object : Observer<Bitmap> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: Bitmap) {
                view_image.setImage(ImageSource.cachedBitmap(t))
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        }

        Observable.fromCallable(callable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    override fun visibilityError(visible: Boolean) {
        if (visible) {
            Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
        }
    }

    override fun visibilityLoading(visible: Boolean) {
        if (visible) {
            Toast.makeText(context, "Успешно сохранено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onDetachImageItem()
        return false
    }

    private fun saveImage() {
        val context = context
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                presenter.onAttach(this)
                presenter.saveImageToGallery()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    AppData.REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (AppData.REQUEST_CODE == requestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage()
        } else {
            val manager = fragmentManager
            manager?.let {
                val fragment = DialogPermissionFragment()
                fragment.show(manager, DialogPermissionFragment.tag)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppData.REQUEST_CODE) {
            saveImage()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

