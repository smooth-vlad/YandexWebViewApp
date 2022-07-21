package com.android.webviewapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.android.webviewapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG = "MainActivity"
        const val KEY_LAST_URL = "lastUrl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.apply {
            webViewClient = MyWebViewClient()
            // enable js
            settings.javaScriptEnabled = true
            // enable caching
            settings.cacheMode = WebSettings.LOAD_DEFAULT
        }
        // allow cookies and third party cookies
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().acceptThirdPartyCookies(binding.webView)

        restoreLastUrl(binding.webView, "https://yandex.ru/")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveLastUrl()
    }

    private fun saveLastUrl() {
        val prefs: SharedPreferences = this.applicationContext
            .getSharedPreferences(this.packageName, MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_LAST_URL, binding.webView.url)
            .commit()

        Log.d(TAG, "saved")
    }

    private fun restoreLastUrl(into: WebView, defaultUrl: String) {
        val prefs: SharedPreferences = this.applicationContext
            .getSharedPreferences(this.packageName, MODE_PRIVATE)

        val lastUrl = prefs.getString(KEY_LAST_URL, "")
        if (lastUrl != null && lastUrl != "") {
            into.loadUrl(lastUrl)
            clearLastUrl(prefs)
        } else {
            into.loadUrl(defaultUrl)
        }

        Log.d(TAG, "restored")
    }

    private fun clearLastUrl(prefs: SharedPreferences) {
        prefs.edit()
            .remove(KEY_LAST_URL)
            .commit()
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else {
            // show confirmation dialog
            ClosingAppDialogFragment(onConfirmAction = { finish() })
                .show(supportFragmentManager, "ClosingAppDialogFragment")
        }
    }

    class MyWebViewClient : WebViewClient() {
        companion object {
            const val PATH_SEGMENT_MAPS = "maps"
            const val PATH_SEGMENT_WEATHER = "pogoda"
            const val URI_APP_YANDEX_MAPS = "yandexmaps://maps.yandex.ru/"
            const val PACKAGE_APP_WEATHER = "ru.yandex.weatherplugin"
            const val YANDEX_HOST = "yandex.ru"
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                Log.d(TAG, it.url.toString())

                if (it.url.host == YANDEX_HOST) {
                    if (it.url.pathSegments.size > 0) {
                        when (it.url.pathSegments[0]) {
                            PATH_SEGMENT_MAPS -> {
                                Log.d(TAG, "open yandex maps")
                                return tryOpenYandexMapsApp(view!!.context)
                            }
                            PATH_SEGMENT_WEATHER -> {
                                Log.d(TAG, "open yandex weather")
                                return tryOpenYandexWeatherApp(view!!.context)
                            }
                        }
                    }
                }
            }
            return false
        }

        /**
         * Try to open yandex maps app
         * @return true for success, false for failure
         */
        private fun tryOpenYandexMapsApp(context: Context): Boolean {
            val uri = Uri.parse(URI_APP_YANDEX_MAPS)

            // Since Android 11 (API level 30) android is limiting package visibility
            // below is recommended approach to solve this for android R and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return try {
                    val intent = Intent(ACTION_VIEW, uri).apply {
                        // The URL should either launch directly in a non-browser app
                        // (if it’s the default), or in the disambiguation dialog
                        addCategory(CATEGORY_BROWSABLE)
                        flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                    }
                    context.startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    // Only browser apps are available, or a browser is the default app for this intent
                    false
                }
            } else { // Android < 11, do as always
                val intent = Intent(ACTION_VIEW, uri)
                return if (context.packageManager.queryIntentActivities(intent, 0).size > 0) {
                    context.startActivity(intent)
                    true
                } else false
            }
        }

        /**
         * Try to open yandex weathe app
         *
         * This approach is different from the one with Yandex Maps, since here you use package name for an intent
         * For this to work on SDK R (11) (API 30) and higher, you need to include in your app manifest
         *      <queries>
         *          <package android:name="ru.yandex.weatherplugin"/>
         *      </queries>
         *
         * @return true for success, false for failure
         */
        private fun tryOpenYandexWeatherApp(context: Context): Boolean {
            val intent = context.packageManager.getLaunchIntentForPackage(
                PACKAGE_APP_WEATHER
            )
            return if (intent != null) {
                context.startActivity(intent)
                true
            } else false
        }
    }
}