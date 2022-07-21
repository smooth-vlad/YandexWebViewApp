package com.android.webviewapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ClosingAppDialogFragment(private val onConfirmAction: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
            .setMessage(getString(R.string.exit_confirmation_text))
            .setPositiveButton(getString(R.string.exit_confirmation_confirm)) { dialog, which ->
                onConfirmAction()
            }
            .setNegativeButton(getString(R.string.exit_confirmation_cancel)) { dialog, which ->
                // nothing
            }
        return builder.create()
    }
}