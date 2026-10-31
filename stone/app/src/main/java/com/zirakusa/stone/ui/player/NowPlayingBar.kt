package com.zirakusa.stone.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.zirakusa.stone.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zirakusa.stone.playback.PlayerViewModel
import com.zirakusa.stone.ui.components.AlbumArt
import com.zirakusa.stone.ui.theme.StoneYellow

// Stone 1.3.2: rgba(68,49,0,0.95) warm dark amber background
private val MiniPlayerBackground = Color(0xF2443100)
// Stone 1.3.2: StoneYellow at 42 % alpha for the border
private val MiniPlayerBorder = StoneYellow.copy(alpha = 0.42f)
// Stone 1.3.2: "#E8C35D" for the playing/paused status label
private val MiniPlayerStatusColor = Color(0xFFE8C35D)

/** Mini player floating above the nav bar, styled after Stone 1.3.2 MiniPlayer. */
@Composable
fun NowPlayingBar(viewModel: PlayerViewModel, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nowPlaying = state.nowPlaying ?: return
    // Collected to keep the position ticker alive while this composable is visible;
    // the value itself is not displayed in the mini player (no progress bar).
    @Suppress("UNUSED_VARIABLE")
    val position by viewModel.position.collectAsStateWithLifecycle(initialValue = state.positionAtEventMs)

    // Stone layout: paddingHorizontal 16dp, paddingBottom 8dp (bottom margin before nav bar)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MiniPlayerBackground)
            .border(1.dp, MiniPlayerBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onOpen)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Cover: 56dp, cornerRadius 14dp — Box clips to override AlbumArt's internal 6dp radius.
        nowPlaying.albumId?.let {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
                AlbumArt(albumId = it, size = 56.dp, sizePx = 128)
            }
        }

        // Title + status
        Column(Modifier.weight(1f)) {
            Text(
                nowPlaying.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (state.isPlaying) stringResource(R.string.home_now_playing) else nowPlaying.artist,
                fontSize = 12.sp,
                color = if (state.isPlaying) MiniPlayerStatusColor else MiniPlayerStatusColor.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Prev/Next buttons
        IconButton(onClick = viewModel::previous) {
            Icon(
                Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.player_previous),
                tint = Color.White,
            )
        }

        // Play/Pause button: Stone style — circular border 2dp StoneYellow, icon StoneYellow
        IconButton(
            onClick = viewModel::togglePlayPause,
            modifier = Modifier
                .size(46.dp)
                .border(2.dp, StoneYellow, CircleShape),
        ) {
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(if (state.isPlaying) R.string.player_pause else R.string.player_play),
                tint = StoneYellow,
            )
        }

        IconButton(onClick = viewModel::next) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.player_next),
                tint = Color.White,
            )
        }
    }
}
