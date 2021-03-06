package me.rei_m.hbfavmaterial.fragment.presenter

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.twitter.sdk.android.core.TwitterAuthConfig
import me.rei_m.hbfavmaterial.activity.OAuthActivity
import me.rei_m.hbfavmaterial.manager.ActivityNavigator
import me.rei_m.hbfavmaterial.repository.HatenaTokenRepository
import me.rei_m.hbfavmaterial.repository.TwitterSessionRepository
import me.rei_m.hbfavmaterial.repository.UserRepository
import me.rei_m.hbfavmaterial.service.TwitterService

class SettingPresenter(private val userRepository: UserRepository,
                       private val hatenaTokenRepository: HatenaTokenRepository,
                       private val twitterSessionRepository: TwitterSessionRepository,
                       private val twitterService: TwitterService) : SettingContact.Actions {

    private lateinit var view: SettingContact.View

    private var isLoading = false

    override fun onCreate(view: SettingContact.View) {
        this.view = view
    }

    override fun onClickTwitterAuthorize(activity: Activity) {
        if (isLoading) return
        isLoading = true
        twitterService.authorize(activity)
    }

    override fun onViewCreated() {

        view.setUserId(userRepository.resolve().id)

        view.setHatenaAuthoriseStatus(hatenaTokenRepository.resolve().isAuthorised)
    }

    override fun onResume() {
        view.setTwitterAuthoriseStatus(twitterSessionRepository.resolve().oAuthTokenEntity.isAuthorised)
    }

    override fun onPause() {
        isLoading = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE -> {
                // TwitterOAuth認可後の処理を行う.
                twitterService.onActivityResult(requestCode, resultCode, data)
                return
            }
            ActivityNavigator.REQ_CODE_OAUTH -> {

                data ?: return

                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // 認可の可否が選択されたかチェック
                    if (data.extras.getBoolean(OAuthActivity.ARG_IS_AUTHORIZE_DONE)) {
                        // 認可の結果により表示を更新する.
                        view.setHatenaAuthoriseStatus(data.extras.getBoolean(OAuthActivity.ARG_AUTHORIZE_STATUS))
                    } else {
                        // 認可を選択せずにresultCodeが設定された場合はネットワークエラーのケース.
                        view.showNetworkErrorMessage()
                    }
                }
                return
            }
        }
    }

    override fun onDismissEditUserIdDialog() {
        view.updateUserId(userRepository.resolve().id)
    }
}
