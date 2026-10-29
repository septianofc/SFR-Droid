package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.model.InstalledAppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppListRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val appList = mutableListOf<InstalledAppItem>()
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in installedApps) {
                if (appInfo.packageName == selfPkg) continue

                val appName = try {
                    pm.getApplicationLabel(appInfo).toString().ifBlank { appInfo.packageName }
                } catch (_: Exception) {
                    appInfo.packageName
                }

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val icon = try {
                    pm.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    null
                }

                appList.add(
                    InstalledAppItem(
                        packageName = appInfo.packageName,
                        appName = appName,
                        isSystemApp = isSystem,
                        icon = icon
                    )
                )
            }

            // Sort: non-system (user apps) first, then alphabetical by name
            appList.sortWith(
                compareBy<InstalledAppItem> { it.isSystemApp }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.appName }
            )
        } catch (_: Exception) {}

        appList
    }
}
