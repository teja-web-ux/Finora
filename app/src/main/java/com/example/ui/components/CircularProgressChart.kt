package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@Composable
fun CircularProgressChart(
    percentage: Float, // 0.0f to 1.0f+
    size: Dp = 150.dp,
    strokeWidth: Dp = 14.dp,
    modifier: Modifier = Modifier,
    label: String = "Spent"
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800)
        )
    }

    val progressColor = when {
        percentage >= 1.0f -> ExpenseRed
        percentage >= 0.8f -> WarningOrange
        else -> MintGreen
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx
            val topLeft = strokePx / 2f

            // Track background
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLeft, topLeft),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Animated progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLeft, topLeft),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val percentDisplay = (percentage * 100).toInt()
            Text(
                text = "$percentDisplay%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
