package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DesktopScreen(
    activity: FragmentActivity,
    viewModel: AppListViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val launcherApps by viewModel.launcherApps.collectAsStateWithLifecycle()
    val stealthTapsTarget by settingsViewModel.stealthTaps.collectAsStateWithLifecycle()
    val unlockMethod by settingsViewModel.unlockMethod.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Tap tracking state
    var currentTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showTapFeedback by remember { mutableStateOf(false) }

    // Dialog authentication states
    var showPinDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }

    // Live clock format
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(now)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            delay(1000L)
        }
    }

    // Function to trigger chosen authentication method
    fun triggerAuthentication() {
        when (unlockMethod) {
            "Biometría" -> {
                triggerBiometricPrompt(
                    activity = activity,
                    onSuccess = { onNavigateToSettings() },
                    onError = { /* Keep on desktop if canceled */ }
                )
            }
            "PIN" -> showPinDialog = true
            "Contraseña" -> showPasswordDialog = true
            "Patrón" -> showPatternDialog = true
            else -> onNavigateToSettings()
        }
    }

    // Handler for consecutive secret taps
    fun handleSecretTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 900L) {
            // Reset counter if too slow
            currentTapCount = 1
        } else {
            currentTapCount += 1
        }
        lastTapTime = now
        showTapFeedback = true

        if (currentTapCount >= stealthTapsTarget) {
            currentTapCount = 0
            showTapFeedback = false
            triggerAuthentication()
        }
    }

    // Auto-hide tap feedback after timeout
    LaunchedEffect(currentTapCount, lastTapTime) {
        if (currentTapCount > 0) {
            delay(1200L)
            if (System.currentTimeMillis() - lastTapTime >= 1100L) {
                currentTapCount = 0
                showTapFeedback = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            // Discreet quick-access button with lock/settings icon
            FloatingActionButton(
                onClick = { triggerAuthentication() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Panel de Control")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(stealthTapsTarget) {
                    detectTapGestures {
                        handleSecretTap()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Clock & Date Widget (Tapping clock also counts towards secret taps)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { handleSecretTap() }
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (currentTime.isNotEmpty()) currentTime else "12:00",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (currentDate.isNotEmpty()) currentDate else "FocusGuard Launcher",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Grid or Empty State
                if (launcherApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Protección",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Pantalla de Inicio Segura",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Todas las aplicaciones seleccionadas están ocultas.\n\nToca la pantalla $stealthTapsTarget veces seguidas para ingresar al panel.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(launcherApps, key = { it.packageName }) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.launchApp(context, app.packageName) }
                                    .padding(6.dp)
                            ) {
                                Image(
                                    bitmap = app.icon.toBitmap().asImageBitmap(),
                                    contentDescription = app.appName,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.appName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Discreet Tap Count Pill Feedback
            AnimatedVisibility(
                visible = showTapFeedback && currentTapCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Toque secreto: $currentTapCount / $stealthTapsTarget",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }

    // PIN Unlock Dialog
    if (showPinDialog) {
        PinAuthDialog(
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                onNavigateToSettings()
            },
            settingsViewModel = settingsViewModel
        )
    }

    // Password Unlock Dialog
    if (showPasswordDialog) {
        PasswordAuthDialog(
            onDismiss = { showPasswordDialog = false },
            onSuccess = {
                showPasswordDialog = false
                onNavigateToSettings()
            },
            settingsViewModel = settingsViewModel
        )
    }

    // Pattern Unlock Dialog
    if (showPatternDialog) {
        PatternAuthDialog(
            onDismiss = { showPatternDialog = false },
            onSuccess = {
                showPatternDialog = false
                onNavigateToSettings()
            },
            settingsViewModel = settingsViewModel
        )
    }
}
