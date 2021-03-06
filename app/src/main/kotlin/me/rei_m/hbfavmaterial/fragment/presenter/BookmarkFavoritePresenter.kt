package me.rei_m.hbfavmaterial.fragment.presenter

import me.rei_m.hbfavmaterial.entity.BookmarkEntity
import me.rei_m.hbfavmaterial.repository.UserRepository
import me.rei_m.hbfavmaterial.service.BookmarkService
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class BookmarkFavoritePresenter(private val userRepository: UserRepository,
                                private val bookmarkService: BookmarkService) : BookmarkFavoriteContact.Actions {

    private lateinit var view: BookmarkFavoriteContact.View

    private var subscription: CompositeSubscription? = null

    private val bookmarkList: MutableList<BookmarkEntity> = mutableListOf()

    private var isLoading = false

    override fun onCreate(view: BookmarkFavoriteContact.View) {
        this.view = view
    }

    override fun onResume() {
        subscription = CompositeSubscription()
        if (bookmarkList.isEmpty()) {
            initializeListContents()
        } else {
            view.showBookmarkList(bookmarkList)
        }
    }

    override fun onPause() {
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onRefreshList() {

        if (isLoading) return

        subscription?.add(request(0))
    }

    override fun onScrollEnd(nextIndex: Int) {

        if (isLoading) return

        subscription?.add(request(nextIndex))
    }

    private fun initializeListContents() {

        if (isLoading) return

        subscription?.let {
            view.showProgress()
            it.add(request(0))
        }
    }

    private fun request(nextIndex: Int): Subscription? {

        val userId = userRepository.resolve().id

        return bookmarkService.findByUserIdForFavorite(userId, nextIndex)
                .doOnSubscribe {
                    isLoading = true
                }
                .doOnUnsubscribe {
                    isLoading = false
                    view.hideProgress()
                }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onFindByUserIdForFavoriteSuccess(it, nextIndex)
                }, {
                    onFindByUserIdForFavoriteFailure(it)
                })
    }

    private fun onFindByUserIdForFavoriteSuccess(bookmarkList: List<BookmarkEntity>, nextIndex: Int) {
        if (nextIndex === 0) {
            this.bookmarkList.clear()
        }
        this.bookmarkList.addAll(bookmarkList)

        if (this.bookmarkList.isEmpty()) {
            view.hideBookmarkList()
            view.showEmpty()
        } else {
            view.hideEmpty()
            view.showBookmarkList(this.bookmarkList)
        }

        if (bookmarkList.isEmpty()) {
            view.stopAutoLoading()
        } else {
            view.startAutoLoading()
        }
    }

    private fun onFindByUserIdForFavoriteFailure(@Suppress("unused") e: Throwable) {
        view.showNetworkErrorMessage()
    }

    override fun onClickBookmark(bookmarkEntity: BookmarkEntity) {
        view.navigateToBookmark(bookmarkEntity)
    }
}
