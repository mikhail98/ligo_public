package com.ligo.common.ui.alert

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.ligo.common.R
import com.ligo.data.model.ConfigStringKey
import com.ligo.tools.api.ILocalizationManager

fun Context.showNotEnoughDriversDialog(localizationManager: ILocalizationManager) {
    val alert = getBuilder()
        .setTitle(localizationManager.getLocalized(ConfigStringKey.IMPORTANT_INFORMATION))
        .setMessage(localizationManager.getLocalized(ConfigStringKey.BETA_ALERT_MESSAGE))
        .setPositiveButton(
            localizationManager.getLocalized(ConfigStringKey.GOT_IT)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        .create()

    alert.show()
}

fun Context.showDialog(
    title: String,
    message: String,
    okText: String,
    notOkText: String?,
    okBtnAction: () -> Unit,
) {
    val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
        dialog.dismiss()
        okBtnAction()
    }

    val alertBuilder = getBuilder()
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(okText, positiveButtonClickListener)

    if (notOkText != null) {
        val negativeButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        }
        alertBuilder.setNegativeButton(notOkText, negativeButtonClickListener)
    }

    alertBuilder.create().show()
}

fun Context.showUpdateAppDialog(
    configManager: ILocalizationManager,
    showSkip: Boolean,
    okBtnAction: () -> Unit,
) {
    val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
        dialog.dismiss()
        okBtnAction()
    }

    val negativeButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
        dialog.dismiss()
    }

    val alertBuilder = getBuilder()
        .setTitle(configManager.getLocalized(ConfigStringKey.UPDATE_APP_DIALOG_TITLE))
        .setMessage(configManager.getLocalized(ConfigStringKey.UPDATE_APP_DIALOG_MESSAGE))
        .setCancelable(false)

    alertBuilder.setPositiveButton(
        configManager.getLocalized(ConfigStringKey.UPDATE_APP_DIALOG_OK),
        positiveButtonClickListener
    )
    if (showSkip) {
        alertBuilder.setNegativeButton(
            configManager.getLocalized(ConfigStringKey.UPDATE_APP_DIALOG_SKIP),
            negativeButtonClickListener
        )
    }
    alertBuilder.create().show()
}

private fun Context.getBuilder() = AlertDialog.Builder(this, R.style.AlertDialogTheme)
