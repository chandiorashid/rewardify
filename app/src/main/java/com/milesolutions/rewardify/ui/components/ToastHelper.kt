package com.milesolutions.rewardify.ui.components

import android.content.Context
import android.widget.Toast

/** Short toast helper used across screens. */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
