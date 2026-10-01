package com.trackerx.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackerx.ui.vehicle.DataSource
import com.trackerx.ui.vehicle.Reading

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val prefs = LocalUiPrefs.current
    val shape = RoundedCornerShape(28.dp)
    val surface = MaterialTheme.colorScheme.surface
    var m = modifier
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(surface.copy(alpha = prefs.cardAlpha), surface.copy(alpha = prefs.cardAlpha * 0.82f))
            )
        )
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(modifier = m.padding(padding), content = content)
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun Chip(
    text: String,
    selected: Boolean = false,
    icon: ImageVector? = null,
    tint: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val fg = tint ?: if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    var m = Modifier
        .clip(shape)
        .background(bg)
        .border(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            shape
        )
    if (onClick != null) m = m.clickable(onClick = onClick)
    Row(
        modifier = m.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun StatusDot(color: Color) {
    Box(Modifier.size(9.dp).clip(CircleShape).background(color))
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    description: String,
    size: Dp = 64.dp,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    val fg = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = fg, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Selo que identifica a origem do dado: REAL, SIMULADO, ESTIMADO ou N/D. */
@Composable
fun SourceBadge(source: DataSource) {
    val color = when (source) {
        DataSource.REAL -> Accent
        DataSource.SIMULATED -> Warn
        DataSource.ESTIMATED -> Color(0xFF7AA2FF)
        DataSource.UNAVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = source.label,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

fun formatReading(r: Reading, decimals: Int = 0): String =
    r.value?.let { "%.${decimals}f".format(it) } ?: "N/D"

@Composable
fun DataCard(title: String, reading: Reading, unit: String, decimals: Int = 0, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label(title, Modifier.weight(1f))
            SourceBadge(reading.source)
        }
        Spacer(Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                formatReading(reading, decimals),
                color = if (reading.available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (30 * LocalUiPrefs.current.scale).sp,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )
            if (reading.available) {
                Spacer(Modifier.width(6.dp))
                Text(unit, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
        }
    }
}
