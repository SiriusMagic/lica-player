package com.zirakusa.stone.ui.screens.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.stone.R
import com.zirakusa.stone.core.locale.AppLanguage
import com.zirakusa.stone.core.permissions.MediaPermissions
import com.zirakusa.stone.ui.theme.OnStoneYellow
import com.zirakusa.stone.ui.theme.StoneBackground
import com.zirakusa.stone.ui.theme.StoneCard
import com.zirakusa.stone.ui.theme.StoneCardBorder
import com.zirakusa.stone.ui.theme.StoneIconIdle
import com.zirakusa.stone.ui.theme.StoneTextSecondary
import com.zirakusa.stone.ui.theme.StoneYellow

/** Páginas de Términos y Privacidad (en Stone 1.3.2 apuntaban a example.com). */
private const val TERMS_URL = "https://github.com/SiriusMagic/lica-player/blob/main/docs/TERMINOS.md"
private const val PRIVACY_URL = "https://github.com/SiriusMagic/lica-player/blob/main/docs/PRIVACIDAD.md"

/**
 * Paso 1 de la bienvenida (`app/onboarding/language.tsx` de Stone): tocar un idioma lo
 * marca y **Empezar** lo guarda. [onStart] recibe el código elegido.
 */
@Composable
fun LanguageScreen(onStart: (String) -> Unit) {
    val context = LocalContext.current
    var selected by rememberSaveable { mutableStateOf(AppLanguage.current(context)) }
    Column(Modifier.fillMaxSize().background(StoneBackground).safeDrawingPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Penguin(120.dp, Modifier.align(Alignment.CenterHorizontally).padding(top = 24.dp, bottom = 24.dp))
            Text(
                stringResource(R.string.onb_choose_language),
                Modifier.fillMaxWidth().padding(bottom = 28.dp),
                color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Column(
                Modifier.clip(RoundedCornerShape(18.dp)).background(StoneCard)
                    .border(1.dp, StoneCardBorder, RoundedCornerShape(18.dp)).padding(horizontal = 6.dp),
            ) {
                AppLanguage.all.forEachIndexed { index, (code, label) ->
                    if (index > 0) HorizontalDivider(color = StoneCardBorder)
                    LanguageRow(label, active = code == selected) { selected = code }
                }
            }
        }
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)) {
            YellowButton(stringResource(R.string.onb_start), icon = null) { onStart(selected) }
            TermsNotice(Modifier.padding(top = 14.dp))
        }
    }
}

@Composable
private fun LanguageRow(label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 18.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Language, null, Modifier.padding(end = 14.dp).size(22.dp),
            tint = if (active) StoneYellow else StoneIconIdle,
        )
        Text(label, Modifier.weight(1f), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (active) Icon(Icons.Filled.Check, null, Modifier.size(22.dp), tint = StoneYellow)
    }
}

@Composable
private fun TermsNotice(modifier: Modifier) {
    val links = TextLinkStyles(SpanStyle(color = StoneYellow, textDecoration = TextDecoration.Underline))
    val text = buildAnnotatedString {
        append(stringResource(R.string.onb_terms_notice))
        append(' ')
        withLink(LinkAnnotation.Url(TERMS_URL, links)) { append(stringResource(R.string.onb_terms)) }
        append(' ')
        append(stringResource(R.string.onb_and))
        append(' ')
        withLink(LinkAnnotation.Url(PRIVACY_URL, links)) { append(stringResource(R.string.onb_privacy)) }
        append('.')
    }
    Text(
        text, modifier.fillMaxWidth(), color = StoneIconIdle, fontSize = 12.sp, lineHeight = 18.sp,
        textAlign = TextAlign.Center,
    )
}

/**
 * Paso 2 (`app/onboarding/welcome.tsx`): "Reproduce tu música" con el pingüino.
 * **Permitir acceso** pide el permiso de música una sola vez; pase lo que pase, la
 * bienvenida no vuelve a salir ([onDone]). Si lo niega, la app vuelve a pedirlo después.
 */
@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { onDone() }
    Box(Modifier.fillMaxSize().background(StoneBackground).safeDrawingPadding()) {
        // Baldosas decorativas inclinadas, como en Stone.
        FloatingTile(Icons.Filled.MusicNote, -15f, Modifier.align(Alignment.TopStart).offset((-20).dp, 60.dp))
        FloatingTile(Icons.Filled.Album, 20f, Modifier.align(Alignment.TopEnd).offset(30.dp, 80.dp))
        FloatingTile(Icons.Filled.GraphicEq, 12f, Modifier.align(Alignment.BottomStart).offset((-25).dp, (-220).dp))
        FloatingTile(Icons.Filled.PlayArrow, -18f, Modifier.align(Alignment.BottomEnd).offset(20.dp, (-200).dp))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Penguin(140.dp, Modifier.padding(bottom = 28.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.onb_title_1))
                    append(' ')
                    withStyle(SpanStyle(color = StoneYellow)) { append(stringResource(R.string.onb_title_2)) }
                },
                color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center, lineHeight = 40.sp,
            )
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.onb_subtitle_a))
                    withStyle(SpanStyle(color = StoneYellow, fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.onb_subtitle_b))
                    }
                    append(stringResource(R.string.onb_subtitle_c))
                },
                Modifier.padding(top = 14.dp),
                color = StoneTextSecondary, fontSize = 17.sp, lineHeight = 26.sp,
                fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
            )
        }
        YellowButton(
            stringResource(R.string.onb_allow_access), Icons.Filled.LibraryMusic,
            Modifier.align(Alignment.BottomCenter).padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        ) { launcher.launch(MediaPermissions.required.toTypedArray()) }
    }
}

/** El pingüino de Stone: el mismo dibujo del ícono de la app (sin archivo extra). */
@Composable
private fun Penguin(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(RoundedCornerShape(size / 5)).background(Color.Black)) {
        // La capa del ícono adaptativo trae margen: se amplía para llenar el cuadro.
        Image(
            painterResource(R.mipmap.ic_launcher_foreground), null,
            Modifier.fillMaxSize().scale(1.5f),
        )
    }
}

@Composable
private fun FloatingTile(icon: ImageVector, degrees: Float, modifier: Modifier) {
    Box(
        modifier.size(100.dp).rotate(degrees).clip(RoundedCornerShape(18.dp))
            .background(StoneCard.copy(alpha = 0.7f))
            .border(1.dp, Color(0xFF1F1F27), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(26.dp), tint = Color(0xFF2A2A35)) }
}

/** Botón amarillo en forma de píldora (el "cta" de Stone). */
@Composable
private fun YellowButton(text: String, icon: ImageVector?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().clip(CircleShape).background(StoneYellow).clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(20.dp), tint = OnStoneYellow)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = OnStoneYellow, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}
