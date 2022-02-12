package com.t3ddyss.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object SettingsUtils {
    fun openApplicationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
}