package com.nexus.tune.core.designsystem.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.nexus.tune.core.model.ShapePreset

object NexusShapes {
    fun createShapes(preset: ShapePreset): Shapes {
        return when (preset) {
            ShapePreset.SOFT_ROUNDED -> Shapes(
                extraSmall = RoundedCornerShape(4.dp),
                small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(16.dp),
                extraLarge = RoundedCornerShape(20.dp)
            )
            ShapePreset.FULL_EXPRESSIVE -> Shapes(
                extraSmall = RoundedCornerShape(6.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(18.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(28.dp)
            )
            ShapePreset.GENTLE -> Shapes(
                extraSmall = RoundedCornerShape(2.dp),
                small = RoundedCornerShape(6.dp),
                medium = RoundedCornerShape(10.dp),
                large = RoundedCornerShape(14.dp),
                extraLarge = RoundedCornerShape(18.dp)
            )
            ShapePreset.SHARP -> Shapes(
                extraSmall = RoundedCornerShape(2.dp),
                small = RoundedCornerShape(2.dp),
                medium = RoundedCornerShape(4.dp),
                large = RoundedCornerShape(4.dp),
                extraLarge = RoundedCornerShape(6.dp)
            )
            ShapePreset.CUT_CORNER -> Shapes(
                extraSmall = CutCornerShape(4.dp),
                small = CutCornerShape(8.dp),
                medium = CutCornerShape(12.dp),
                large = CutCornerShape(16.dp),
                extraLarge = CutCornerShape(20.dp)
            )
        }
    }
}

