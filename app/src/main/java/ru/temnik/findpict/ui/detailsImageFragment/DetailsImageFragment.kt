package ru.temnik.findpict.ui.detailsImageFragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
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
import ru.temnik.findpict.ui.dialogFragments.DialogNotificationFragment
import ru.temnik.findpict.ui.dialogFragments.DialogPermissionFragment
import ru.temnik.findpict.ui.dialogFragments.DialogShareFragment
import java.util.concurrent.Callable


class DetailsImageFragment : Fragment(), ProfileView {
    companion object {
        val tag = DetailsImageFragment::class.java.simpleName
        val presenter: DetailsImagePresenter = DetailsImagePresenter()
        lateinit var item: ImageDTO
            private set

        fun newInstance(_item: ImageDTO): DetailsImageFragment {
            item = _item
            presenter.onAttachImageItem(_item)
            return DetailsImageFragment()
        }
    }

    private lateinit var loadingToast: Toast
    private lateinit var errorLoadToast: Toast
    private lateinit var saveToast: Toast
    private var isLoading:Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.details_image_fragment, container, false)
        val imageView = view.view_image
        initToast(view.context)
        setImage(imageView)
        setInfo(view)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view_arrowBack.setOnClickListener {
            activity?.onBackPressed()
        }
        view_btn_internet.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.pageURL))
            startActivity(intent)
        }
        view_btn_share.setOnClickListener {
            showShareDialog()
        }
        view_btn_save.setOnClickListener {
            saveImage()
        }
        view_error_btn.setOnClickListener {
            val imageView = view?.view_image
            imageView?.let {
               if(!isLoading){
                   setLargeImage(it)
               }
            }
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

    private fun initToast(context: Context) {
        loadingToast =
            Toast.makeText(context, R.string.details_default_loading_image_info, Toast.LENGTH_SHORT)
        errorLoadToast = Toast.makeText(
            context,
            R.string.details_default_failed_loading_image_info,
            Toast.LENGTH_SHORT
        )
        saveToast = Toast.makeText(
            context,
            R.string.details_default_success_loading_image_info,
            Toast.LENGTH_SHORT
        )
    }

    private fun setImage(imageView: SubsamplingScaleImageView) {
        loadingToast.show()
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
        isLoading = true
        val imageLargeUrl = item.largeImageURL
        val callable = Callable<Bitmap> {
            Glide.with(imageView.context)
                .asBitmap()
                .load(imageLargeUrl)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
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
        }
        val observer = object : Observer<Bitmap> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: Bitmap) {
                visibilityLoadError(false)
                view_image?.setImage(ImageSource.cachedBitmap(t))
                isLoading=false
            }

            override fun onError(e: Throwable) {
                isLoading=false
                visibilityLoadError(true)
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
        showNotificationDialog(getString(R.string.details_default_failed_save_image))
    }

    private fun visibilityLoadError(visible: Boolean) {
        loadingToast.cancel()
        if (visible) {
            showNotificationDialog(getString(R.string.details_default_failed_load_image))
            view_error?.visibility = View.VISIBLE
        } else {
            view_error?.visibility = View.GONE
        }
    }

    private fun showNotificationDialog(
        title: String,
        message: String = getString(R.string.default_check_internet)
    ) {
       try {
           val context = context
           if(context!=null){
               val fragment = DialogNotificationFragment.newInstance(title, message)
               fragment.show(childFragmentManager, fragment.tag)
           }

       }catch (ex:Exception){
           val context = context
           if(context!=null){
               Toast.makeText(context,context.getString(R.string.default_check_internet),Toast.LENGTH_SHORT).show()
           }
       }
    }

    override fun visibilityLoading(visible: Boolean) {
        if (visible) {
            if (loadingToast.view.isShown) loadingToast.cancel()
            loadingToast.show()
        } else {
            loadingToast.cancel()
        }
    }

    override fun visibilitySave(visible: Boolean) {
        if (visible) {
            if (loadingToast.view.isShown) loadingToast.cancel()
            if (saveToast.view.isShown) saveToast.cancel()
            saveToast.show()
        } else {
            saveToast.cancel()
        }
    }

    override fun onBackPressed(): Boolean {
        visibilityLoading(false)
        visibilityLoadError(false)
        visibilityError(false)
        visibilitySave(false)
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
                presenter.saveImage()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    AppData.REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (AppData.REQUEST_CODE_PERMISSIONS == requestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        when (requestCode) {
            AppData.REQUEST_CODE_PERMISSIONS -> saveImage()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }




    private fun showShareDialog() {
        val fragment = DialogShareFragment()
        DialogShareFragment.parentFragment = this
        fragment.show(childFragmentManager, DialogShareFragment.tag)
    }
}

