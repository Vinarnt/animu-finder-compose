package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.SearchBar

/** Stateful home search field. */
@Composable
internal fun HomeSearchBar(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }

    SearchBar(
        query = query,
        onValueChange = { query = it },
        onSearch = { onSearch(query) },
    )
}
