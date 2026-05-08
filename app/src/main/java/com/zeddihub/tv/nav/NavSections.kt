package com.zeddihub.tv.nav

/**
 * Visual grouping for the side nav. The drawer renders items as flat list
 * inside groups (with a header per group). Order here = order on screen.
 *
 * Why explicit groups instead of relying on TopDestination order:
 * with 20+ destinations a single flat list is overwhelming. Grouping
 * makes the rail scannable in collapsed state (sections of related icons)
 * and in expanded state (labels grouped under headers).
 */
data class NavGroup(val titleRes: Int, val items: List<TopDestination>)

object NavLayout {
    val groups: List<NavGroup> = listOf(
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_home,
            listOf(TopDestination.Dashboard),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_timer,
            listOf(
                TopDestination.Timer,
                TopDestination.Schedule,
                TopDestination.Routine,
                TopDestination.WakeUp,
                TopDestination.Health,
            ),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_smarthome,
            listOf(
                TopDestination.SmartHome,
                TopDestination.HomeAssistant,
            ),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_network,
            listOf(
                TopDestination.Network,
                TopDestination.ConnectionTest,
                TopDestination.Audio,
            ),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_share,
            listOf(
                TopDestination.LocalSend,
                TopDestination.Files,
                TopDestination.Browser,
                TopDestination.WatchLater,
            ),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_media,
            listOf(
                TopDestination.Media,
                TopDestination.Servers,
                TopDestination.Alerts,
            ),
        ),
        NavGroup(
            com.zeddihub.tv.R.string.nav_group_personal,
            listOf(
                TopDestination.Accessibility,
                TopDestination.Parental,
                TopDestination.Settings,
            ),
        ),
    )
}
