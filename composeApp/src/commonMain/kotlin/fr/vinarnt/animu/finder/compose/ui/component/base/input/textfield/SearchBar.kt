package fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius

@Composable
fun SearchBar(query: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BaseTextField(
        value = query,
        onValueChange = onValueChange,
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(CornerRadius.md))
            .fillMaxWidth(),
        placeholder = { Text(strings.animeList.searchPlaceholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(percent = 50)
    )
}