package ru.temnik.findpict.ui.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.temnik.findpict.R

class DialogNotificationFragment : DialogFragment() {
    companion object {
        val tag = DialogNotificationFragment::class.java.simpleName
        const val ARGS_KEY_MESSAGE = "args:message"
        const val ARGS_KEY_TITLE = "args:title"

        fun newInstance(title:String,message: String): DialogNotificationFragment {
            val bundle = Bundle()
            bundle.apply {
                putString(ARGS_KEY_TITLE, title)
                putString(ARGS_KEY_MESSAGE, message)
            }
            val fragment = DialogNotificationFragment()
            fragment.arguments=bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARGS_KEY_TITLE) ?: ""
        val message = arguments?.getString(ARGS_KEY_MESSAGE) ?: ""
        val builder =
            AlertDialog.Builder(ContextThemeWrapper(activity!!, R.style.Theme_AppCompat_Dialog))
        val okBtnStr = builder.context.getString(R.string.dialog_notification_btn_ok)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(okBtnStr) { dialog, id ->
            dialog.cancel()
        }
        builder.setCancelable(true)
        return builder.create()
    }
}