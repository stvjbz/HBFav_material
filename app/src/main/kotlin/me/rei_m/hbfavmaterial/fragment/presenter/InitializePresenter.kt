package me.rei_m.hbfavmaterial.fragment.presenter

import me.rei_m.hbfavmaterial.entity.UserEntity
import me.rei_m.hbfavmaterial.repository.UserRepository
import me.rei_m.hbfavmaterial.service.UserService
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.net.HttpURLConnection

class InitializePresenter(private val userRepository: UserRepository,
                          private val userService: UserService) : InitializeContact.Actions {

    private lateinit var view: InitializeContact.View

    private var subscription: CompositeSubscription? = null

    private var isLoading = false

    override fun onCreate(view: InitializeContact.View) {

        this.view = view

        val userEntity = userRepository.resolve()
        if (userEntity.isCompleteSetting) {
            view.navigateToMain()
        }
    }

    override fun onResume() {
        subscription = CompositeSubscription()
    }

    override fun onPause() {
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onClickButtonSetId(userId: String) {

        if (isLoading) return

        isLoading = true
        view.showProgress()

        subscription?.add(userService.confirmExistingUserId(userId)
                .doOnUnsubscribe {
                    isLoading = false
                    view.hideProgress()
                }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onConfirmExistingUserIdSuccess(it, userId)
                }, {
                    onConfirmExistingUserIdFailure(it)
                }))
    }

    private fun onConfirmExistingUserIdSuccess(isValid: Boolean, userId: String) {
        if (isValid) {
            userRepository.store(UserEntity(userId))
            view.navigateToMain()
        } else {
            view.displayInvalidUserIdMessage()
        }
    }

    private fun onConfirmExistingUserIdFailure(e: Throwable) {
        if (e is HttpException) {
            if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                view.displayInvalidUserIdMessage()
                return
            }
        }
        view.showNetworkErrorMessage()
    }
}
