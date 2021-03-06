package me.rei_m.hbfavmaterial.fragment

import android.os.Build
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import me.rei_m.hbfavmaterial.BuildConfig
import me.rei_m.hbfavmaterial.R
import me.rei_m.hbfavmaterial.TestApp
import me.rei_m.hbfavmaterial.activity.SplashActivity
import me.rei_m.hbfavmaterial.fragment.presenter.InitializeContact
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
        application = TestApp::class,
        sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class InitializeFragmentTest {

    lateinit var fragment: InitializeFragment

    private val view: View by lazy {
        fragment.view ?: throw IllegalStateException("fragment's view is Null")
    }

    private val editHatenaId: EditText
        get() = view.findViewById(R.id.fragment_initialize_edit_hatena_id) as EditText

    private val buttonSetHatenaId: Button
        get() = view.findViewById(R.id.fragment_initialize_button_set_hatena_id) as Button

    private val textInputLayoutHatenaId: TextInputLayout
        get() = view.findViewById(R.id.fragment_initialize_layout_hatena_id) as TextInputLayout

    private val snackbarTextView: TextView
        get() = fragment.activity.findViewById(android.support.design.R.id.snackbar_text) as TextView

    private fun getString(resId: Int): String {
        return fragment.getString(resId)
    }

    @Before
    fun setUp() {
        fragment = InitializeFragment.newInstance()
        SupportFragmentTestUtil.startFragment(fragment, SplashActivity::class.java)
    }

    @Test
    fun initialize() {
        assertThat(editHatenaId.visibility, `is`(View.VISIBLE))
        assertThat(buttonSetHatenaId.visibility, `is`(View.VISIBLE))
        assertThat(buttonSetHatenaId.isEnabled, `is`(false))
    }

    @Test
    fun testButtonSetHatenaIdStatus_input_id() {
        editHatenaId.setText("a")
        assertThat(buttonSetHatenaId.isEnabled, `is`(true))
    }

    @Test
    fun testButtonSetHatenaIdStatus_not_input_id() {
        editHatenaId.setText("")
        assertThat(buttonSetHatenaId.isEnabled, `is`(false))
    }

    @Test
    fun testButtonSetHatenaIdClick() {
        val presenter = mock(InitializeContact.Actions::class.java)
        doAnswer { Unit }.`when`(presenter).onClickButtonSetId("valid")
        fragment.presenter = presenter
        editHatenaId.setText("valid")
        buttonSetHatenaId.performClick()
        verify(presenter, times(1)).onClickButtonSetId("valid")
    }

    @Test
    fun testShowNetworkErrorMessage() {
        fragment.showNetworkErrorMessage()
        assertThat(snackbarTextView.visibility, `is`(View.VISIBLE))
        assertThat(snackbarTextView.text.toString(), `is`(getString(R.string.message_error_network)))
    }

    @Test
    fun testDisplayInvalidUserIdMessage() {
        fragment.displayInvalidUserIdMessage()
        assertThat(textInputLayoutHatenaId.error.toString(), `is`(getString(R.string.message_error_input_user_id)))
    }

    @Test
    fun testShowHideProgress() {
        fragment.showProgress()
        assertThat(fragment.progressDialog?.isShowing, `is`(true))
        fragment.hideProgress()
        assertNull(fragment.progressDialog)
    }

    @Test
    fun testNavigateToMain() {
        val navigator = spy(fragment.navigator)
        doAnswer { Unit }.`when`(navigator).navigateToMain(fragment.activity)
        fragment.navigator = navigator
        fragment.navigateToMain()
        verify(navigator, times(1)).navigateToMain(fragment.activity)
    }
}
