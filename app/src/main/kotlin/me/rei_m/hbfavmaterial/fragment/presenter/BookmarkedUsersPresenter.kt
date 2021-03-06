package me.rei_m.hbfavmaterial.fragment.presenter

import me.rei_m.hbfavmaterial.entity.BookmarkEntity
import me.rei_m.hbfavmaterial.enum.BookmarkCommentFilter
import me.rei_m.hbfavmaterial.service.BookmarkService
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class BookmarkedUsersPresenter(private val bookmarkService: BookmarkService) : BookmarkedUsersContact.Actions {

    private lateinit var view: BookmarkedUsersContact.View

    private lateinit var bookmarkEntity: BookmarkEntity

    private var subscription: CompositeSubscription? = null

    private val bookmarkList: MutableList<BookmarkEntity> = mutableListOf()

    private var isLoading = false

    override var bookmarkCommentFilter: BookmarkCommentFilter = BookmarkCommentFilter.ALL

    override fun onCreate(view: BookmarkedUsersContact.View,
                          bookmarkEntity: BookmarkEntity,
                          bookmarkCommentFilter: BookmarkCommentFilter) {
        this.view = view
        this.bookmarkEntity = bookmarkEntity
        this.bookmarkCommentFilter = bookmarkCommentFilter
    }

    override fun onResume() {
        subscription = CompositeSubscription()
        if (bookmarkList.isEmpty()) {
            initializeListContents()
        } else {
            if (bookmarkCommentFilter == BookmarkCommentFilter.COMMENT) {
                view.showUserList(bookmarkList.filter { bookmark -> bookmark.description.isNotEmpty() })
            } else {
                view.showUserList(bookmarkList)
            }
        }
    }

    override fun onPause() {
        subscription?.unsubscribe()
        subscription = null
    }

    private fun initializeListContents() {

        if (isLoading) return

        subscription?.let {
            view.showProgress()
            it.add(request())
        }
    }

    override fun onRefreshList() {

        if (isLoading) return

        subscription?.add(request())
    }

    override fun onOptionItemSelected(bookmarkCommentFilter: BookmarkCommentFilter) {

        if (this.bookmarkCommentFilter == bookmarkCommentFilter) return

        this.bookmarkCommentFilter = bookmarkCommentFilter

        if (bookmarkCommentFilter == BookmarkCommentFilter.COMMENT) {
            view.showUserList(bookmarkList.filter { bookmark -> bookmark.description.isNotEmpty() })
        } else {
            view.showUserList(bookmarkList)
        }
    }

    private fun request(): Subscription? {

        return bookmarkService.findByArticleUrl(bookmarkEntity.articleEntity.url)
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
                    onFindByArticleUrlSuccess(it)
                }, {
                    onFindByArticleUrlFailure(it)
                })
    }

    private fun onFindByArticleUrlSuccess(bookmarkList: List<BookmarkEntity>) {
        this.bookmarkList.clear()
        this.bookmarkList.addAll(bookmarkList)

        if (bookmarkList.isEmpty()) {
            view.hideUserList()
            view.showEmpty()
        } else {
            view.hideEmpty()
            if (bookmarkCommentFilter == BookmarkCommentFilter.COMMENT) {
                view.showUserList(bookmarkList.filter { bookmark -> bookmark.description.isNotEmpty() })
            } else {
                view.showUserList(bookmarkList)
            }
        }
    }

    private fun onFindByArticleUrlFailure(e: Throwable) {
        view.showNetworkErrorMessage()
    }

    override fun onClickUser(bookmarkEntity: BookmarkEntity) {
        view.navigateToOthersBookmark(bookmarkEntity)
    }
}
