package me.rei_m.hbfavmaterial.extension

import android.content.Context
import android.support.v4.app.Fragment

/**
 * Application Contextを取得する.
 */
fun Fragment.getAppContext(): Context {
    return activity.applicationContext
}
