package me.rei_m.hbfavmaterial.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ShareCompat
import android.view.Menu
import android.view.MenuItem
import com.squareup.otto.Subscribe
import me.rei_m.hbfavmaterial.R
import me.rei_m.hbfavmaterial.entities.BookmarkEntity
import me.rei_m.hbfavmaterial.entities.EntryEntity
import me.rei_m.hbfavmaterial.events.*
import me.rei_m.hbfavmaterial.extensions.replaceFragment
import me.rei_m.hbfavmaterial.extensions.setFragment
import me.rei_m.hbfavmaterial.extensions.showSnackbarNetworkError
import me.rei_m.hbfavmaterial.fragments.BookmarkFragment
import me.rei_m.hbfavmaterial.fragments.EditBookmarkDialogFragment
import me.rei_m.hbfavmaterial.fragments.EntryWebViewFragment
import me.rei_m.hbfavmaterial.managers.ModelLocator
import me.rei_m.hbfavmaterial.models.HatenaModel
import me.rei_m.hbfavmaterial.utils.ConstantUtil

public class BookmarkActivity : BaseActivity() {

    private var mEntryTitle: String = ""
    private var mEntryLink: String = ""

    companion object {

        private val ARG_BOOKMARK = "ARG_BOOKMARK"
        private val ARG_ENTRY = "ARG_ENTRY"

        private val KEY_ENTRY_TITLE = "KEY_ENTRY_TITLE"
        private val KEY_ENTRY_LINK = "KEY_ENTRY_LINK"

        public fun createIntent(context: Context, bookmarkEntity: BookmarkEntity): Intent {
            val intent = Intent(context, BookmarkActivity::class.java)
            intent.putExtra(ARG_BOOKMARK, bookmarkEntity)
            return intent
        }

        public fun createIntent(context: Context, entryEntity: EntryEntity): Intent {
            val intent = Intent(context, BookmarkActivity::class.java)
            intent.putExtra(ARG_ENTRY, entryEntity)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            if (intent.hasExtra(ARG_BOOKMARK)) {
                val bookmarkEntity = intent.getSerializableExtra(ARG_BOOKMARK) as BookmarkEntity
                mEntryTitle = bookmarkEntity.title
                mEntryLink = bookmarkEntity.link
                setFragment(BookmarkFragment.newInstance(bookmarkEntity))
            } else {
                val entryEntity = intent.getSerializableExtra(ARG_ENTRY) as EntryEntity
                mEntryTitle = entryEntity.title
                mEntryLink = entryEntity.link
                setFragment(EntryWebViewFragment.newInstance(entryEntity.link))
            }
            supportActionBar.title = mEntryTitle
        }

        val fab = findViewById(R.id.fab) as FloatingActionButton

        fab.setOnClickListener({
            // はてぶ投稿ボタン
            val hatenaModel = ModelLocator.get(ModelLocator.Companion.Tag.HATENA) as HatenaModel

            if (!hatenaModel.isAuthorised()) {
                startActivityForResult(OAuthActivity.createIntent(this), ConstantUtil.REQ_CODE_OAUTH)
            } else {
                hatenaModel.fetchBookmark(mEntryLink)
            }
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        mEntryTitle = savedInstanceState?.getString(KEY_ENTRY_TITLE)!!
        mEntryLink = savedInstanceState?.getString(KEY_ENTRY_LINK)!!
        supportActionBar.title = mEntryTitle
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(KEY_ENTRY_TITLE, mEntryTitle)
        outState?.putString(KEY_ENTRY_LINK, mEntryLink)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bookmark, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val id = item?.itemId;

        when (id) {
            R.id.menu_share ->
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle("記事をシェアします")
                        .setSubject(mEntryTitle)
                        .setText(mEntryLink)
                        .setType("text/plain")
                        .startChooser()
            else ->
                return super.onOptionsItemSelected(item);
        }

        return true
    }

    override fun onBackPressed() {
        var fragment = supportFragmentManager.findFragmentByTag(EntryWebViewFragment.TAG)
        if (fragment != null) {
            if (!(fragment as EntryWebViewFragment).backHistory()) {
                return
            }
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != ConstantUtil.REQ_CODE_OAUTH) {
            return
        }

        when (resultCode) {
            RESULT_OK -> {
                if (data!!.extras.getBoolean(OAuthActivity.ARG_AUTHORIZE_STATUS)) {
                    val hatenaModel = ModelLocator.get(ModelLocator.Companion.Tag.HATENA) as HatenaModel
                    hatenaModel.fetchBookmark(mEntryLink)
                }
            }
            RESULT_CANCELED -> {
                showSnackbarNetworkError(findViewById(R.id.activity_layout))
            }
            else -> {

            }
        }
    }

    @Subscribe
    public fun onBookmarkUserClicked(event: BookmarkUserClickedEvent) {
        startActivity(OthersBookmarkActivity.createIntent(this, event.userId))
    }

    @Subscribe
    public fun onBookmarkClicked(event: BookmarkClickedEvent) {
        replaceFragment(EntryWebViewFragment.newInstance(event.bookmarkEntity.link), EntryWebViewFragment.TAG)
    }

    @Subscribe
    public fun onBookmarkCountClicked(event: BookmarkCountClickedEvent) {
        startActivity(BookmarkUsersActivity.createIntent(this, event.bookmarkEntity))
    }

    @Subscribe
    public fun onHatenaGetBookmarkLoaded(event: HatenaGetBookmarkLoadedEvent) {
        when (event.status) {
            LoadedEventStatus.OK -> {
                // 更新用ダイアログを表示
                println("ok")
            }
            LoadedEventStatus.NOT_FOUND -> {
                // 新規用ダイアログを表示
                val dialog = EditBookmarkDialogFragment.newInstance(mEntryTitle)
                dialog.show(supportFragmentManager, EditBookmarkDialogFragment.TAG)
            }
            LoadedEventStatus.ERROR -> {
                showSnackbarNetworkError(findViewById(R.id.content))
            }
        }
    }
}
