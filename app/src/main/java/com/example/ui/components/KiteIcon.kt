package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom Kite-shaped icon for Ask Tally.
 * Classic kite geometric profile: elevated horizontal apex with elongated lower tail.
 */
val KiteIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "KiteIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White),
            stroke = null
        ) {
            moveTo(12f, 2.5f)
            lineTo(21f, 9.5f)
            lineTo(12f, 22.5f)
            lineTo(3f, 9.5f)
            close()
        }
    }.build()
}

@Composable
fun KiteShapeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.10f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.94f)
            lineTo(w * 0.12f, h * 0.38f)
            close()
        }

        drawPath(
            path = path,
            color = tint,
            style = Fill
        )
    }
}
