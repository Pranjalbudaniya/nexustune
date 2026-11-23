package com.nexus.tune.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

object NexusMotion {
    // Tier 1: Bouncy (Mini-player expand/collapse, FABs, toggles)
    val BouncySpring: SpringSpec<Float> = spring(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMediumLow
    )

    // Tier 2: Snappy (Bottom sheet dragging, queue reorder drag handles)
    val SnappySpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Tier 3: Subtle (Color transitions, crossfades, scrims)
    val SubtleSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}

