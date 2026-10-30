package com.example.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build

object AppLockHelper {

    fun isDeviceSecure(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            km.isDeviceSecure
        } else {
            @Suppress("DEPRECATION")
            km.isKeyguardSecure
        }
    }

    fun createConfirmDeviceCredentialIntent(context: Context): Intent? {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            km.createConfirmDeviceCredentialIntent(
                "SFR Droid Terkunci",
                "Verifikasi identitas dengan Sidik Jari, Wajah, atau PIN/Pola HP untuk membuka aplikasi"
            )
        } else {
            null
        }
    }
}
