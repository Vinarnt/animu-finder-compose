package fr.vinarnt.animu.finder.compose.ui.component.dropdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.vinarnt.animu.finder.compose.i18n.Locales
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.SearchDropdown
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.SearchDropdownMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LanguageDropdown() {
    val settingManager = koinInject<SettingManager>()
    val localeStrings = strings.languages.locales
    val items = localeStrings.map { LanguageDropdownItem(it, strings.languages.getLocaleLabel(it)) }
    val currentLocale = settingManager.getLocale().collectAsStateWithLifecycle(Locales.EN)

    SearchDropdown(
        items = items,
        selectedItem = items.find { it.locale == currentLocale.value } ?: items.first(),
        onItemSelected = { item ->
            CoroutineScope(Dispatchers.Default).launch {
                settingManager.setLocale(item.locale)
            }
        },
        filter = { item, query -> item.label.contains(query, ignoreCase = true) },
        itemContent = { item ->
            SearchDropdownMenuItem(
                text = { Text(item.label) }
            )
        },
        displayText = { item -> item.label },
    )
}

data class LanguageDropdownItem(val locale: String, val label: String)
