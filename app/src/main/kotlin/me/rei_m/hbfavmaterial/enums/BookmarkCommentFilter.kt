package me.rei_m.hbfavmaterial.enums

import me.rei_m.hbfavmaterial.R

/**
 * ブックマークのフィルタ.
 */
enum class BookmarkCommentFilter(override val menuId: Int,
                                 override val titleResId: Int) : FilterItemI {
    ALL(
            R.id.menu_filter_users_all,
            R.string.filter_bookmark_users_all
    ),
    COMMENT(
            R.id.menu_filter_users_comment,
            R.string.filter_bookmark_users_comment
    );
}