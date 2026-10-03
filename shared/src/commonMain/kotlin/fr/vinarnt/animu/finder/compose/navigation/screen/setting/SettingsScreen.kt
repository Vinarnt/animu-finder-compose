package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.LocalWindowWidth
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.SettingsViewModel

/** Width of the tab strip down the side. */
internal val TabStripWidth = 220.dp

/** Widest a section's rows may run, so a row's label and its control stay near each other. */
internal val SectionMaxWidth = 640.dp

/** Width the side layout needs: the strip, the gap, and a readable section. */
private val SettingsTabsMinWidth: Dp =
    TabStripWidth + Spacing.xl + SectionMaxWidth + Spacing.md * 2

/**
 * One settings page, for every window width.
 *
 * Which section is open lives in [navigation], above the width branch, so resizing redraws the same
 * state with the other layout: compact turns the section into a page behind the list, wide puts it
 * beside the strip.
 */
@Composable
internal fun SettingsScreen(
    vm: SettingsViewModel,
    onOpenCloudflare: (label: String, host: String) -> Unit,
    onSectionChange: (label: String?, onBack: (() -> Unit)?) -> Unit,
) {
    val sections = settingsSections(vm, onOpenCloudflare)
    val navigation = remember { SettingsNavigation() }
    val onCompact = LocalWindowWidth.current < SettingsTabsMinWidth
    val openSection = navigation.open
    val leaveSection: () -> Unit = { navigation.open = null }
    SettingBackHandler(enabled = onCompact && openSection != null, onBack = leaveSection)

    val label = (openSection?.label).takeIf { onCompact }
    LaunchedEffect(label, leaveSection) {
        onSectionChange(label, leaveSection.takeIf { label != null })
    }

    if (onCompact) {
        SettingsListLayout(sections, navigation)
    } else {
        LaunchedEffect(sections) {
            if (navigation.open == null) navigation.open = sections.firstOrNull()
        }
        SettingsTabsLayout(sections, navigation)
    }
}
