package com.obitoboost.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.GamingProfileType
import com.obitoboost.app.ui.components.ObitoCard
import com.obitoboost.app.ui.components.PrimaryGlowButton
import com.obitoboost.app.ui.components.SectionHeader
import com.obitoboost.app.ui.theme.*
import com.obitoboost.app.util.ExportManager

@Composable
fun ExportScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader("Export & Share", "Generate a branded OBITO BOOST result card for YouTube, TikTok, Instagram or WhatsApp")

        val device = state.deviceInfo
        if (device == null) {
            ObitoCard { Text("Waiting for device scan…", color = TextSecondary) }
            return@Column
        }

        PrimaryGlowButton(
            "Generate Result Card",
            onClick = {
                cardBitmap = ExportManager.generateResultCard(
                    device = device,
                    profileType = state.selectedProfileType,
                    comparison = state.comparison
                )
            }
        )

        cardBitmap?.let { bmp ->
            Spacer(Modifier.height(20.dp))
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "OBITO BOOST result card",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.height(16.dp))
            PrimaryGlowButton(
                "Share",
                onClick = { ExportManager.shareBitmap(context, bmp) }
            )
        }

        if (state.comparison == null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Tip: run a Before/After benchmark first so the card includes real FPS numbers instead of \"No benchmark run yet\".",
                style = MaterialTheme.typography.bodySmall, color = TextMuted
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
