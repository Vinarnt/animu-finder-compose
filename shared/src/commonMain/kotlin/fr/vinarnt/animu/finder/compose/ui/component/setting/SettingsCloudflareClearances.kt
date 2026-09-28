package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.CloudflareClearance

/**
 * Lists the providers that may present a Cloudflare challenge, with per-host capture
 * status. Tapping a row opens the reusable clearance screen for that host.
 */
@Composable
internal fun SettingsCloudflareClearances(
    clearances: Map<String, CloudflareClearance>,
    onOpen: (CloudflareSite) -> Unit,
) {
    val s = strings.settings.cloudflare

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = s.description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))

        cloudflareSites.forEach { site ->
            val configured = clearances.containsKey(site.host)
            SettingsRow(onClick = { onOpen(site) }) {
                SettingsRowTitle(site.label, description = site.host)
                Text(
                    text = if (configured) s.statusConfigured else s.statusMissing,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (configured) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
