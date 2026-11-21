package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.CertificateRecord
import com.example.model.DisciplineStatus
import com.example.model.FitBusterCatalog
import com.example.model.MedalTier
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.CarbonCardBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.GoldMedal
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlatinumMedal
import com.example.ui.theme.SilverMedal
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator

@Composable
fun MedalsAndCertificatesScreen(
    data: FitBusterDashboardData,
    signatureBitmap: Bitmap?,
    initialTab: Int = 0,
    onExportPdf: (CertificateRecord, Boolean) -> Unit,
    onOpenSignatureSettings: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var previewCert by remember { mutableStateOf<CertificateRecord?>(null) }
    val unlockedMedalsByKey = remember(data.medals) { data.medals.associateBy { it.medalKey } }
    val unlockedCertsById = remember(data.certificates) { data.certificates.associateBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonSurface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Fitness Medals (${data.medals.size}/11)") },
                modifier = Modifier.testTag("tab_medals")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Certificates (${data.certificates.size}/10)") },
                modifier = Modifier.testTag("tab_certificates")
            )
        }

        if (selectedTab == 0) {
            val unlockedKeys = unlockedMedalsByKey.keys
            val disciplineCount = data.disciplineLogs.count { it.status == DisciplineStatus.COMPLETED.name }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FitBusterCard(highlighted = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MEDAL COLLECTION", style = MaterialTheme.typography.labelMedium, color = GoldMedal)
                                Text("${data.medals.size} / ${FitBusterCatalog.medalDefinitions.size} Unlocked", style = MaterialTheme.typography.headlineMedium)
                            }
                            Image(
                                painter = painterResource(id = R.drawable.img_achievement_seal),
                                contentDescription = "Medal Seal",
                                modifier = Modifier.size(48.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        NeonProgressBar(
                            progress = data.medals.size.toFloat() / FitBusterCatalog.medalDefinitions.size.toFloat(),
                            color = GoldMedal
                        )
                    }
                }

                items(FitBusterCatalog.medalDefinitions, key = { it.key }) { def ->
                    val unlocked = unlockedMedalsByKey[def.key]
                    val currentProgress = FitnessCalculator.evaluateMedalProgress(
                        medalKey = def.key,
                        missions = data.missions,
                        workouts = data.workouts,
                        sprints = data.sprints,
                        waterLogs = data.waterLogs,
                        profile = data.profile,
                        disciplineCompletedCount = disciplineCount,
                        unlockedMedalKeys = unlockedKeys
                    )
                    val tierColor = when (def.tier) {
                        MedalTier.BRONZE -> BronzeMedal
                        MedalTier.SILVER -> SilverMedal
                        MedalTier.GOLD -> GoldMedal
                        MedalTier.PLATINUM -> PlatinumMedal
                        MedalTier.SPECIAL -> MaterialTheme.colorScheme.primary
                    }

                    FitBusterCard(highlighted = unlocked != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (unlocked != null) tierColor.copy(alpha = 0.2f) else Color(0xFF1D222B))
                                    .border(1.5.dp, if (unlocked != null) tierColor else CarbonCardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (unlocked != null) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                                    contentDescription = def.title,
                                    tint = if (unlocked != null) tierColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (unlocked != null) "UNLOCKED • ${FitBusterDateUtil.formatDisplayDate(unlocked.unlockedDateKey)}" else "LOCKED • ${def.requirementLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (unlocked != null) tierColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(def.title, style = MaterialTheme.typography.titleMedium)
                                Text(def.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                NeonProgressBar(
                                    progress = if (unlocked != null) 1f else currentProgress.toFloat() / def.targetCount.coerceAtLeast(1).toFloat(),
                                    color = tierColor
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (signatureBitmap == null) {
                    item {
                        FitBusterCard {
                            Text("Authorized Signature Not Uploaded", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload your original signature image once in Signature Settings so it appears untouched at the bottom-right of every certificate and PDF.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onOpenSignatureSettings,
                                modifier = Modifier.testTag("cert_upload_signature_btn")
                            ) {
                                Text("Upload Original Signature")
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Certificate Gallery",
                        subtitle = "${data.certificates.size} earned • Recipient: ${data.profile.displayName}"
                    )
                }

                if (data.certificates.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Filled.WorkspacePremium,
                            title = "No certificates unlocked yet",
                            description = "Complete missions, streaks, sprints, strength, or discipline milestones to unlock official FIT BUSTER certificates."
                        )
                    }
                } else {
                    items(data.certificates, key = { it.id }) { cert ->
                        FitBusterCard(highlighted = true, onClick = { previewCert = cert }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cert.certificateName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                    Text("Recipient: ${cert.recipientName}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Medal: ${cert.medalEarned} • ${FitBusterDateUtil.formatDisplayDate(cert.completionDateKey)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("ID: ${cert.certificateCode}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Filled.Verified, contentDescription = "View Certificate", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onExportPdf(cert, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                                    modifier = Modifier.weight(1f).testTag("download_pdf_${cert.id}")
                                ) {
                                    Icon(Icons.Filled.Download, null, Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onExportPdf(cert, true) },
                                    modifier = Modifier.weight(1f).testTag("share_pdf_${cert.id}")
                                ) {
                                    Icon(Icons.Filled.Share, null, Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }

                // Catalog preview of locked certificates
                val lockedCatalog = FitBusterCatalog.certificateDefinitions.filter { !unlockedCertsById.containsKey(it.key) }
                if (lockedCatalog.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Upcoming Certificates", subtitle = "${lockedCatalog.size} remaining to unlock")
                    }
                    items(lockedCatalog, key = { it.key }) { def ->
                        FitBusterCard {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Column {
                                    Text(def.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${def.description} (Requires: ${def.medalTitle})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewCert?.let { cert ->
        CertificatePreviewDialog(
            cert = cert,
            signatureBitmap = signatureBitmap,
            onDownload = { onExportPdf(cert, false) },
            onShare = { onExportPdf(cert, true) },
            onDismiss = { previewCert = null }
        )
    }
}

@Composable
private fun CertificatePreviewDialog(
    cert: CertificateRecord,
    signatureBitmap: Bitmap?,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = ObsidianBlack,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FIT BUSTER", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text("OFFICIAL ACHIEVEMENT CERTIFICATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_achievement_seal),
                        contentDescription = "Certificate Seal",
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = CarbonCardBorder)
                Text(cert.certificateName, style = MaterialTheme.typography.headlineMedium)
                Text("PRESENTED TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = cert.recipientName,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(cert.achievementDescription, style = MaterialTheme.typography.bodyLarge)
                Text("Medal Earned: ${cert.medalEarned}", style = MaterialTheme.typography.labelLarge, color = GoldMedal)

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("COMPLETION DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FitBusterDateUtil.formatDisplayDate(cert.completionDateKey), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("CERTIFICATE ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(cert.certificateCode, style = MaterialTheme.typography.labelMedium)
                    }

                    // Bottom-right original untouched signature above Authorized Signature label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (signatureBitmap != null) {
                            Image(
                                bitmap = signatureBitmap.asImageBitmap(),
                                contentDescription = "Authorized Original Signature",
                                contentScale = ContentScale.Fit,
                                filterQuality = FilterQuality.High,
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(48.dp)
                            )
                        } else {
                            Text(
                                text = "(No signature uploaded)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(140.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Authorized Signature", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Download PDF", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                        Text("Share")
                    }
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
