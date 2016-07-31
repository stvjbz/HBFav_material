package me.rei_m.hbfavmaterial.activitiy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import me.rei_m.hbfavmaterial.R
import me.rei_m.hbfavmaterial.entities.OAuthTokenEntity
import me.rei_m.hbfavmaterial.extensions.hide
import me.rei_m.hbfavmaterial.extensions.showSnackbarNetworkError
import me.rei_m.hbfavmaterial.network.HatenaOAuthManager
import me.rei_m.hbfavmaterial.repositories.HatenaTokenRepository
import me.rei_m.hbfavmaterial.service.HatenaService
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class OAuthActivity : BaseSingleActivity() {

    companion object {

        const val ARG_AUTHORIZE_STATUS = "ARG_AUTHORIZE_STATUS"
        const val ARG_IS_AUTHORIZE_DONE = "ARG_IS_AUTHORIZE_DONE"

        fun createIntent(context: Context): Intent = Intent(context, OAuthActivity::class.java)
    }

    @Inject
    lateinit var hatenaTokenRepository: HatenaTokenRepository

    @Inject
    lateinit var hatenaService: HatenaService

    private var subscription: CompositeSubscription? = null

    private var isLoading = false

    private val webView: WebView by lazy {
        WebView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)

        webView.apply {
            clearCache(true)
            settings.javaScriptEnabled = true
            setWebChromeClient(WebChromeClient())
            setWebViewClient(object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (url?.startsWith(HatenaOAuthManager.CALLBACK) ?: false) {
                        stopLoading()
                        hide()
                        val oauthVerifier = Uri.parse(url).getQueryParameter("oauth_verifier")
                        oauthVerifier ?: finish()
                        fetchAccessToken(oauthVerifier)
                    } else if (url?.startsWith(HatenaOAuthManager.AUTHORIZATION_DENY_URL) ?: false) {
                        stopLoading()
                        hatenaTokenRepository.delete(applicationContext)
                        setAuthorizeResult(false, true)
                        finish()
                    } else {
                        super.onPageStarted(view, url, favicon)
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return super.shouldOverrideUrlLoading(view, url)
                }
            })
        }

        with(findViewById(R.id.content) as FrameLayout) {
            addView(webView)
        }
        findViewById(R.id.fab)?.hide()
    }

    override fun onResume() {
        super.onResume()
        subscription = CompositeSubscription()
        isLoading = false
        fetchRequestToken()
    }

    override fun onPause() {
        super.onPause()
        subscription?.unsubscribe()
        subscription = null
    }

    private fun fetchRequestToken() {

        if (isLoading) return

        isLoading = true

        val observer = object : Observer<String> {

            override fun onNext(t: String) {
                webView.loadUrl(t)
            }

            override fun onCompleted() {

            }

            override fun onError(e: Throwable?) {
                showSnackbarNetworkError(findViewById(R.id.activity_layout))
            }
        }

        subscription?.add(hatenaService.fetchRequestToken()
                .doOnUnsubscribe { isLoading = false }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer))
    }

    private fun fetchAccessToken(oauthVerifier: String) {

        if (isLoading) return

        isLoading = true

        val observer = object : Observer<OAuthTokenEntity> {

            override fun onNext(t: OAuthTokenEntity) {
                hatenaTokenRepository.store(applicationContext, t)
                setAuthorizeResult(true, true)
                finish()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                setAuthorizeResult(false, false)
                finish()
            }
        }

        subscription?.add(hatenaService.fetchAccessToken(oauthVerifier)
                .doOnUnsubscribe { isLoading = false }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer))
    }

    private fun setAuthorizeResult(isAuthorize: Boolean, isDone: Boolean) {
        val intent = Intent().apply {
            putExtras(Bundle().apply {
                putBoolean(ARG_AUTHORIZE_STATUS, isAuthorize)
                putBoolean(ARG_IS_AUTHORIZE_DONE, isDone)
            })
        }
        setResult(RESULT_OK, intent)
    }
}