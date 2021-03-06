package me.rei_m.hbfavmaterial.fragment.presenter

import me.rei_m.hbfavmaterial.entity.UserEntity
import me.rei_m.hbfavmaterial.repository.UserRepository
import me.rei_m.hbfavmaterial.service.UserService
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
import rx.Observable
import rx.Scheduler
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.schedulers.Schedulers
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class InitializePresenterTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var view: InitializeContact.View

    @Before
    fun setUp() {

        doAnswer { Unit }.`when`(view).navigateToMain()
        doAnswer { Unit }.`when`(view).showProgress()
        doAnswer { Unit }.`when`(view).hideProgress()
        doAnswer { Unit }.`when`(view).displayInvalidUserIdMessage()
        doAnswer { Unit }.`when`(view).showNetworkErrorMessage()

        RxAndroidPlugins.getInstance().reset()
        RxAndroidPlugins.getInstance().registerSchedulersHook(object : RxAndroidSchedulersHook() {
            override fun getMainThreadScheduler(): Scheduler? {
                return Schedulers.immediate()
            }
        })
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.getInstance().reset()
    }

    @Test
    fun testOnCreate_initialize_not_complete_register_user() {

        `when`(userRepository.resolve()).thenReturn(UserEntity(""))

        val presenter = InitializePresenter(userRepository, userService)

        presenter.onCreate(view)
        verify(view, times(0)).navigateToMain()
    }

    @Test
    fun testOnCreate_initialize_complete_register_user() {

        `when`(userRepository.resolve()).thenReturn(UserEntity("test"))

        val presenter = InitializePresenter(userRepository, userService)

        presenter.onCreate(view)
        verify(view, times(1)).navigateToMain()
    }

    @Test
    fun testOnClickButtonSetId_success_check_id() {

        `when`(userRepository.resolve()).thenReturn(UserEntity(""))

        doAnswer { Unit }.`when`(userRepository).store(UserEntity("success"))

        `when`(userService.confirmExistingUserId("success")).thenReturn(Observable.just(true))

        val presenter = InitializePresenter(userRepository, userService)
        presenter.onCreate(view)
        presenter.onResume()
        presenter.onClickButtonSetId("success")

        verify(userRepository, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).store(UserEntity("success"))
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).showProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).hideProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).navigateToMain()
    }

    @Test
    fun testOnClickButtonSetId_fail_check_id() {

        `when`(userRepository.resolve()).thenReturn(UserEntity(""))

        `when`(userService.confirmExistingUserId("fail")).thenReturn(Observable.just(false))

        val presenter = InitializePresenter(userRepository, userService)
        presenter.onCreate(view)
        presenter.onResume()
        presenter.onClickButtonSetId("fail")

        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).showProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).hideProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).displayInvalidUserIdMessage()
    }

    @Test
    fun testOnClickButtonSetId_fail_check_id_404() {

        `when`(userRepository.resolve()).thenReturn(UserEntity(""))

        `when`(userService.confirmExistingUserId("fail"))
                .thenReturn(Observable.error(HttpException(Response.error<HttpException>(HttpURLConnection.HTTP_NOT_FOUND, ResponseBody.create(MediaType.parse("text/html"), "")))))

        val presenter = InitializePresenter(userRepository, userService)
        presenter.onCreate(view)
        presenter.onResume()
        presenter.onClickButtonSetId("fail")

        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).showProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).hideProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).displayInvalidUserIdMessage()
    }

    @Test
    fun testOnClickButtonSetId_fail_check_id_network_error() {

        `when`(userRepository.resolve()).thenReturn(UserEntity(""))

        `when`(userService.confirmExistingUserId("fail"))
                .thenReturn(Observable.error(HttpException(Response.error<HttpException>(HttpURLConnection.HTTP_INTERNAL_ERROR, ResponseBody.create(MediaType.parse("text/html"), "")))))

        val presenter = InitializePresenter(userRepository, userService)
        presenter.onCreate(view)
        presenter.onResume()
        presenter.onClickButtonSetId("fail")

        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).showProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).hideProgress()
        verify(view, timeout(TimeUnit.SECONDS.toMillis(1).times(1))).showNetworkErrorMessage()
    }
}
