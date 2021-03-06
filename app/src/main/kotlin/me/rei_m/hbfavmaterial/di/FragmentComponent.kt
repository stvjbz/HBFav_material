package me.rei_m.hbfavmaterial.di

import dagger.Subcomponent
import me.rei_m.hbfavmaterial.fragment.*
import me.rei_m.hbfavmaterial.fragment.presenter.NewEntryPresenter
import me.rei_m.hbfavmaterial.fragment.presenter.SettingPresenter

@Subcomponent(modules = arrayOf(FragmentModule::class))
interface FragmentComponent {

    fun inject(fragment: BookmarkFavoriteFragment)

    fun inject(fragment: BookmarkUserFragment)

    fun inject(fragment: BookmarkedUsersFragment)

    fun inject(fragment: EditBookmarkDialogFragment)

    fun inject(fragment: EditUserIdDialogFragment)

    fun inject(fragment: InitializeFragment)

    fun inject(fragment: NewEntryFragment)

    fun inject(fragment: HotEntryFragment)

    fun inject(fragment: SettingFragment)

    fun inject(fragment: ExplainAppFragment)
}
