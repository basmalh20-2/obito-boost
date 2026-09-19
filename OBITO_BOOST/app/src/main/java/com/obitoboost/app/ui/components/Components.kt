package com.obitoboost.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obitoboost.app.data.model.MetricValue
import com.obitoboost.app.ui.theme.*

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = CyanGlow, fontWeight = FontWeight.Bold)
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
fun ObitoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .obitoCard()
            .then(Modifier)
            .padding(18.dp),
        content = content
    )
}

/** A single metric row that shows the value and — when relevant — a small
 *  "Measured / Estimated / Unavailable" tag so the user always knows how
 *  trustworthy a number is. */
@Composable
fun <T> MetricRow(label: String, metric: MetricValue<T>, formatter: (T) -> String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val text = when (metric) {
                is MetricValue.Measured -> formatter(metric.value)
                is MetricValue.Estimated -> formatter(metric.value)
                is MetricValue.Unavailable -> "—"
            }
            Text(text, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            MetricTag(metric)
        }
    }
}

@Composable
fun <T> MetricTag(metric: MetricValue<T>) {
    val (bg, textColor, label) = when (metric) {
        is MetricValue.Measured -> Triple(StatusGood.copy(alpha = 0.15f), StatusGood, "Measured")
        is MetricValue.Estimated -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "Estimated")
        is MetricValue.Unavailable -> Triple(TextMuted.copy(alpha = 0.15f), TextMuted, "N/A")
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

@Composable
fun StatChip(label: String, value: String, accent: androidx.compose.ui.graphics.Color = CyanGlow) {
    Column(
        modifier = Modifier
            .obitoCard()
            .border(BorderStroke(1.dp, ObitoCardBorderColor), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .widthIn(min = 110.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun TierBadge(tierName: String) {
    Box(
        modifier = Modifier
            .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Gold.copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(tierName, style = MaterialTheme.typography.labelLarge, color = Gold)
    }
}

@Composable
fun PrimaryGlowButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = NavyDeep)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
