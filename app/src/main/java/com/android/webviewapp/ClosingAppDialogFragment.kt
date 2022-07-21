package com.android.webviewapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ClosingAppDialogFragment(private val onConfirmAction: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
            .setMessage("Are you sure want to exit?")
            .setPositiveButton("Yes") { dialog, which ->
                onConfirmAction()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // nothing
            }
        return builder.create()
    }
}