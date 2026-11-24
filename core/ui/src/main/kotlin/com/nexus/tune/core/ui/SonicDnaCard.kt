package com.nexus.tune.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.tune.core.model.SonicDnaProfile

@Composable
fun SonicDnaCard(
    profile: SonicDnaProfile,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = runCatching {
        Color(android.graphics.Color.parseColor(profile.gradientStartHex))
    }.getOrDefault(MaterialTheme.colorScheme.primaryContainer)

    val endColor = runCatching {
        Color(android.graphics.Color.parseColor(profile.gradientEndHex))
    }.getOrDefault(MaterialTheme.colorScheme.surfaceVariant)

    val gradientBrush = Brush.linearGradient(
        colors = listOf(startColor, endColor)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            // Header Row: Badge & Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.archetype.badgeIcon,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SONIC DNA PROFILE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.archetype.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "NexusTune",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Archetype Tagline
            Text(
                text = "\"${profile.archetype.tagline}\"",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Three Dimensions Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionMeter(label = "Variety Score", percent = profile.varietyScorePercent)
                DimensionMeter(label = "Energy Pulse", percent = profile.energyScorePercent)
                DimensionMeter(label = "Acoustic Depth", percent = profile.acousticScorePercent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GENRE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                    Text(
                        profile.dominantGenre,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TRACKS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                    Text("${profile.totalTracksPlayed}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TIME", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                    Text("${profile.estimatedMinutes}m", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STREAK", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                    Text("🔥 ${profile.streakDays}d", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (profile.topArtists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Top Artists: " + profile.topArtists.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Share Button
            Button(
                onClick = onShareClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = startColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Sonic DNA Profile",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DimensionMeter(label: String, percent: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (percent.coerceIn(0, 100) / 100f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}
