package dalbers.com.noise.shared

import android.content.Context

class VersionProvider(
    private val context: Context
) {
    fun getVersion() = context.packageManager.getPackageInfo(context.packageName, 0).versionName
}