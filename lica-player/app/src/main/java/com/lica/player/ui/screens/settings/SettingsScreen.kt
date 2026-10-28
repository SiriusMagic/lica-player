package com.lica.player.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lica.player.R
import com.lica.player.data.settings.AudioProfile
import com.lica.player.data.settings.PlaybackSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: PlaybackSettings,
) : ViewModel() {
    val profile: StateFlow<AudioProfile> = settings.profile
    fun select(profile: AudioProfile) = settings.setProfile(profile)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val selected by viewModel.profile.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        Text(
            stringResource(R.string.settings_audio_profile),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Column(Modifier.selectableGroup()) {
            AudioProfile.entries.forEach { profile ->
                ProfileRow(
                    title = profile.titleRes,
                    body = profile.bodyRes,
                    selected = profile == selected,
                    onClick = { viewModel.select(profile) },
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(@StringRes title: Int, @StringRes body: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.padding(start = 16.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val AudioProfile.titleRes: Int
    get() = when (this) {
        AudioProfile.LONG_LIFE -> R.string.profile_long_life
        AudioProfile.BALANCED -> R.string.profile_balanced
        AudioProfile.HIGH_FIDELITY -> R.string.profile_high_fidelity
    }

private val AudioProfile.bodyRes: Int
    get() = when (this) {
        AudioProfile.LONG_LIFE -> R.string.profile_long_life_body
        AudioProfile.BALANCED -> R.string.profile_balanced_body
        AudioProfile.HIGH_FIDELITY -> R.string.profile_high_fidelity_body
    }
