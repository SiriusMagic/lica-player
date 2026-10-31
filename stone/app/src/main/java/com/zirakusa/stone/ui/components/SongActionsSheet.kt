package com.zirakusa.stone.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AddToQueue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.QueuePlayNext
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.zirakusa.stone.R
import com.zirakusa.stone.data.model.Song
import com.zirakusa.stone.data.repository.MediaRepository
import com.zirakusa.stone.data.user.UserLibrary
import com.zirakusa.stone.playback.PlayerViewModel
import com.zirakusa.stone.ui.theme.StoneSurface
import com.zirakusa.stone.ui.theme.StoneTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Actions on one song. Shared by every screen that lists songs. */
@HiltViewModel
class SongActionsViewModel @Inject constructor(
    private val library: UserLibrary,
    private val repository: MediaRepository,
) : ViewModel() {
    val favoriteIds: StateFlow<Set<Long>> = library.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { library.toggleFavorite(songId) }
    }

    /**
     * Deletes [song] from MediaStore and from the Room cache.
     * On API 30+ returns an [android.content.IntentSender] that the caller
     * must launch so the system can show its own confirmation dialog.
     * On older devices the deletion happens inline and null is returned.
     */
    suspend fun deleteSong(song: Song) = repository.deleteSong(song)
}

/**
 * Long-press menu for a song. Opens over the current screen; no navigation.
 *
 * [playerViewModel] is the activity-scoped [PlayerViewModel] passed down from
 * [com.zirakusa.stone.ui.navigation.StoneNavHost] so queue operations go to the
 * already-connected player without a second ViewModel instance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsSheet(
    song: Song,
    onDismiss: () -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: SongActionsViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val isFavorite = song.id in favorites
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // On API 30+ MediaStore.createDeleteRequest returns an IntentSender that the
    // system uses to show its own "delete file?" dialog. We launch it here.
    val deleteLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                // The system already deleted the file; remove from cache and dismiss.
                scope.launch { viewModel.deleteSong(song) }
                onDismiss()
            }
        }
    } else null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StoneSurface,
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            // Header: cover + title + artist
            Row(
                Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AlbumArt(albumId = song.albumId, size = 48.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StoneTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 1. Favorite / Unfavorite
            ActionRow(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = stringResource(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite),
                onClick = {
                    viewModel.toggleFavorite(song.id)
                    onDismiss()
                },
            )

            // 2. Share
            ActionRow(
                icon = Icons.Outlined.Share,
                label = stringResource(R.string.action_share),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(song.path))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, song.title))
                    onDismiss()
                },
            )

            // 3. Play next
            ActionRow(
                icon = Icons.Outlined.QueuePlayNext,
                label = stringResource(R.string.action_play_next),
                onClick = {
                    playerViewModel.playNext(song)
                    onDismiss()
                },
            )

            // 4. Add to queue
            ActionRow(
                icon = Icons.Outlined.AddToQueue,
                label = stringResource(R.string.action_add_to_queue),
                onClick = {
                    playerViewModel.addToQueue(song)
                    onDismiss()
                },
            )

            // 5. Delete
            ActionRow(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.action_delete),
                onClick = { showDeleteDialog = true },
            )
        }
    }

    // Delete confirmation dialog (shown on top of the sheet, which stays open).
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body, song.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        val intentSender = viewModel.deleteSong(song)
                        if (intentSender != null && deleteLauncher != null) {
                            // API 30+: let the system confirm and perform the deletion.
                            deleteLauncher.launch(
                                androidx.activity.result.IntentSenderRequest.Builder(intentSender).build(),
                            )
                        } else {
                            // API < 30: deletion already happened inside deleteSong().
                            onDismiss()
                        }
                    }
                }) {
                    Text(stringResource(R.string.delete_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.delete_confirm_cancel))
                }
            },
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Icon(icon, contentDescription = null, tint = StoneTextSecondary)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}
