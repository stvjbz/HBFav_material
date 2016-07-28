package me.rei_m.hbfavmaterial.fragments.presenter

import android.content.Context
import me.rei_m.hbfavmaterial.entities.BookmarkEditEntity
import me.rei_m.hbfavmaterial.extensions.getAppContext
import me.rei_m.hbfavmaterial.fragments.BaseFragment
import me.rei_m.hbfavmaterial.repositories.HatenaTokenRepository
import me.rei_m.hbfavmaterial.repositories.TwitterSessionRepository
import me.rei_m.hbfavmaterial.service.HatenaService
import me.rei_m.hbfavmaterial.service.TwitterService
import me.rei_m.hbfavmaterial.utils.BookmarkUtil
import retrofit2.adapter.rxjava.HttpException
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.net.HttpURLConnection
import javax.inject.Inject

class EditBookmarkDialogPresenter(private val view: EditBookmarkDialogContact.View) : EditBookmarkDialogContact.Actions {

    @Inject
    lateinit var hatenaTokenRepository: HatenaTokenRepository

    @Inject
    lateinit var hatenaService: HatenaService

    @Inject
    lateinit var twitterSessionRepository: TwitterSessionRepository

    @Inject
    lateinit var twitterService: TwitterService

    private var isLoading = false

    private val appContext: Context
        get() = (view as BaseFragment).getAppContext()

    override fun changeCheckedShareTwitter(isChecked: Boolean) {
        val twitterSessionEntity = twitterSessionRepository.resolve()
        if (isChecked) {
            if (!twitterSessionEntity.oAuthTokenEntity.isAuthorised) {
                view.startSettingActivity()
                view.dismiss()
                return
            }
        }
        twitterSessionEntity.isShare = isChecked
        twitterSessionRepository.store(appContext, twitterSessionEntity)
    }

    override fun registerBookmark(url: String,
                                  title: String,
                                  comment: String,
                                  isOpen: Boolean,
                                  tags: List<String>,
                                  isShareAtTwitter: Boolean): Subscription? {

        if (isLoading) return null

        view.showProgress()

        isLoading = true

        if (isShareAtTwitter) {
            twitterService.postTweet(BookmarkUtil.createShareText(url, title, comment))
        }

        val oAuthTokenEntity = hatenaTokenRepository.resolve()

        return hatenaService.upsertBookmark(oAuthTokenEntity, url, comment, isOpen, tags)
                .doOnUnsubscribe {
                    isLoading = false
                    view.hideProgress()
                }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onUpsertBookmarkSuccess(it)
                }, {
                    onUpsertBookmarkError(it)
                })
    }

    private fun onUpsertBookmarkSuccess(bookmarkEditEntity: BookmarkEditEntity) {
        view.dismiss()
    }

    private fun onUpsertBookmarkError(e: Throwable?) {
        view.showNetworkErrorMessage()
    }

    override fun deleteBookmark(bookmarkUrl: String): Subscription? {

        if (isLoading) return null

        view.showProgress()

        isLoading = true

        val oAuthTokenEntity = hatenaTokenRepository.resolve()

        return hatenaService.deleteBookmark(oAuthTokenEntity, bookmarkUrl)
                .doOnUnsubscribe {
                    isLoading = false
                    view.hideProgress()
                }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onDeleteBookmarkSuccess(it)
                }, {
                    onDeleteBookmarkError(it)
                })
    }

    private fun onDeleteBookmarkSuccess(void: Void?) {
        view.dismiss()
    }

    private fun onDeleteBookmarkError(e: Throwable?) {
        if (e is HttpException) {
            if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                view.dismiss()
                return
            }
        }
        view.showNetworkErrorMessage()
    }
}
