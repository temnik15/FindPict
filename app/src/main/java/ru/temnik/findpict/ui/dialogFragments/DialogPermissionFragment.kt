package ru.temnik.findpict.ui.dialogFragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.temnik.findpict.AppData
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment

class DialogPermissionFragment : DialogFragment() {
    companion object {
        val tag = DialogPermissionFragment::class.java.simpleName
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = activity?.getString(R.string.dialog_permission_default_message)
            ?: "No memory access permission was granted. Permission can be given in the section \"About the application\""
        val cancelBtn = activity?.getString(R.string.dialog_permission_btn_cancel)?:"CANCEL"
        val settingBtn =  activity?.getString(R.string.dialog_permission_btn_about_app)?:"ABOUT THE APP"
        val builder =
            AlertDialog.Builder(ContextThemeWrapper(activity!!, R.style.Theme_AppCompat_Dialog))
        builder.setMessage(message)

        builder.setPositiveButton(
            settingBtn
        ) { dialog, id ->
            openApplicationSettings()
        }
        builder.setNegativeButton(
            cancelBtn
        ) { dialog, id ->
            dialog.cancel()
        }
        builder.setCancelable(true)
        return builder.create()
    }

    private fun openApplicationSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context?.packageName)
        )
        val fragment = fragmentManager?.findFragmentByTag(DetailsImageFragment.tag)
        fragment?.startActivityForResult(appSettingsIntent, AppData.REQUEST_CODE_PERMISSIONS)
    }


}