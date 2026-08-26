package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel,
    appListViewModel: AppListViewModel
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabTitles = listOf("Ocultar Apps", "Seguridad", "Toques", "Guía", "Acerca de")
    val tabIcons = listOf(
        Icons.Default.Apps,
        Icons.Default.Security,
        Icons.Default.TouchApp,
        Icons.Default.MenuBook,
        Icons.Default.Info
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel de Control",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "FocusGuard Launcher",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al escritorio")
                    }
                },
                actions = {
                    // Quick Light/Dark Mode Switch in Header
                    IconButton(onClick = { settingsViewModel.setDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable Tab Row for 5 full sections
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(tabIcons[index], contentDescription = title, modifier = Modifier.size(20.dp)) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> HideAppsTab(appListViewModel)
                1 -> SecurityTab(settingsViewModel)
                2 -> StealthTapsTab(settingsViewModel)
                3 -> GuideTab()
                4 -> AboutAndLegalTab(settingsViewModel)
            }
        }
    }
}

// ---------------- TAB 0: HIDE APPS SELECTION ----------------
@Composable
fun HideAppsTab(viewModel: AppListViewModel) {
    val apps by viewModel.settingsApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalAppsCount.collectAsStateWithLifecycle()
    val hiddenCount by viewModel.hiddenAppsCount.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary & Stats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estado de Privacidad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$hiddenCount aplicaciones ocultas de $totalCount",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Buscar aplicaciones...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        // Filter Chips & Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Todas", "Ocultas", "Visibles").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.setFilterCategory(filter) },
                    label = { Text(filter, fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { if (hiddenCount > 0) viewModel.unhideAllApps() else viewModel.hideAllApps() }
            ) {
                Text(
                    text = if (hiddenCount > 0) "Mostrar todas" else "Ocultar todas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // App List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap().asImageBitmap(),
                                contentDescription = "Icono de ${app.appName}",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (app.isHidden) MaterialTheme.colorScheme.error
                                                else Color(0xFF10B981)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (app.isHidden) "Oculta en Escritorio" else "Visible",
                                        fontSize = 11.sp,
                                        fontWeight = if (app.isHidden) FontWeight.Bold else FontWeight.Normal,
                                        color = if (app.isHidden) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                        Switch(
                            checked = app.isHidden,
                            onCheckedChange = { isHidden ->
                                viewModel.toggleAppVisibility(app.packageName, isHidden)
                            },
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- TAB 1: SECURITY SETTINGS ----------------
@Composable
fun SecurityTab(settingsViewModel: SettingsViewModel) {
    val unlockMethod by settingsViewModel.unlockMethod.collectAsStateWithLifecycle()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangePatternDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Método de Desbloqueo y Autenticación",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Selecciona cómo deseas verificar tu identidad al ingresar al panel o invocar el modo secreto.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Security Methods Cards
        val methods = listOf(
            Triple("Biometría", "Huella Digital o Reconocimiento Facial", Icons.Default.Fingerprint),
            Triple("PIN", "Clave Numérica de 4 Dígitos", Icons.Default.Pin),
            Triple("Contraseña", "Contraseña Alfanumérica Personalizada", Icons.Default.Lock),
            Triple("Patrón", "Patrón de Seguridad Táctil (3x3)", Icons.Default.Pattern)
        )

        methods.forEach { (methodKey, desc, icon) ->
            val isSelected = unlockMethod == methodKey
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setUnlockMethod(methodKey) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = methodKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = methodKey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { settingsViewModel.setUnlockMethod(methodKey) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Configuración de Claves y Credenciales",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Action Buttons for changing specific credentials
        OutlinedButton(
            onClick = { showChangePinDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar PIN Numérico (4 Dígitos)")
        }

        OutlinedButton(
            onClick = { showChangePasswordDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar Contraseña Alfanumérica")
        }

        OutlinedButton(
            onClick = { showChangePatternDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dibujar Nuevo Patrón de Desbloqueo")
        }
    }

    if (showChangePinDialog) {
        SetupPinDialog(
            onDismiss = { showChangePinDialog = false },
            onPinSaved = {
                settingsViewModel.setCustomPin(it)
                showChangePinDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        SetupPasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onPasswordSaved = {
                settingsViewModel.setCustomPassword(it)
                showChangePasswordDialog = false
            }
        )
    }

    if (showChangePatternDialog) {
        SetupPatternDialog(
            onDismiss = { showChangePatternDialog = false },
            onPatternSaved = {
                settingsViewModel.setCustomPattern(it)
                showChangePatternDialog = false
            }
        )
    }
}

// ---------------- TAB 2: STEALTH TAPS INVOCATION ----------------
@Composable
fun StealthTapsTab(settingsViewModel: SettingsViewModel) {
    val stealthTaps by settingsViewModel.stealthTaps.collectAsStateWithLifecycle()

    // Interactive Practice Area state
    var practiceCount by remember { mutableIntStateOf(0) }
    var practiceSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Invocación Secreta por Toques",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Configura la cantidad de toques rápidos consecutivos necesarios en el fondo del escritorio para abrir el panel secreto.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Options: 5, 10, 20 taps
        val tapOptions = listOf(
            Pair(5, "5 Toques (Rápido y conveniente)"),
            Pair(10, "10 Toques (Equilibrado y seguro)"),
            Pair(20, "20 Toques (Máxima seguridad stealth)")
        )

        tapOptions.forEach { (taps, desc) ->
            val isSelected = stealthTaps == taps
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setStealthTaps(taps) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$taps Toques Seguidos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { settingsViewModel.setStealthTaps(taps) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Interactive Practice Zone
        Text(
            text = "Zona de Prueba y Práctica de Toques",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    if (!practiceSuccess) {
                        practiceCount += 1
                        if (practiceCount >= stealthTaps) {
                            practiceSuccess = true
                        }
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (practiceSuccess) Color(0xFF10B981).copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (practiceSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (practiceSuccess) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (practiceSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (practiceSuccess) "¡Invocación Exitosa!" else "Toca repetidamente aquí",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (practiceSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (practiceSuccess) "Has completado la ráfaga de $stealthTaps toques" else "Progreso: $practiceCount / $stealthTaps toques",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (practiceCount.toFloat() / stealthTaps).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (practiceSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
                if (practiceSuccess || practiceCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        practiceCount = 0
                        practiceSuccess = false
                    }) {
                        Text("Reiniciar Prueba")
                    }
                }
            }
        }
    }
}

// ---------------- TAB 3: STEP-BY-STEP USER GUIDE ----------------
@Composable
fun GuideTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Guía Paso a Paso de FocusGuard",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Sigue estos sencillos pasos para dominar el uso de tu pantalla de inicio segura y la ocultación de iconos.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        val steps = listOf(
            Triple(
                "Paso 1: Establecer como Pantalla de Inicio",
                "Al presionar el botón central (Home) de tu teléfono, Android te preguntará qué app usar de pantalla de inicio. Elige 'Focus App' y selecciona 'Siempre'. De esta forma, este Launcher se convertirá en tu escritorio principal.",
                Icons.Default.Home
            ),
            Triple(
                "Paso 2: Ocultar Aplicaciones",
                "Dirígete a la pestaña 'Ocultar Apps' en este panel. Busca cualquier app instalada (redes sociales, banca, juegos) y activa su interruptor. Inmediatamente el icono desaparecerá por completo de la pantalla de inicio.",
                Icons.Default.VisibilityOff
            ),
            Triple(
                "Paso 3: Configurar Seguridad",
                "En la pestaña 'Seguridad', selecciona tu método de protección preferido: Biometría (huella o reconocimiento facial de tu celular), PIN de 4 dígitos, Contraseña o Patrón táctil.",
                Icons.Default.Lock
            ),
            Triple(
                "Paso 4: Invocación Secreta",
                "Cuando estés en tu pantalla de inicio, toca el fondo de la pantalla rápidamente la cantidad de veces que configuraste (5, 10 o 20 toques). El sistema detectará la ráfaga y abrirá el diálogo de seguridad para que ingreses al panel oculto.",
                Icons.Default.TouchApp
            ),
            Triple(
                "Paso 5: Desocultar o Abrir Apps",
                "Para volver a mostrar cualquier app en el escritorio, simplemente desactiva su interruptor desde este panel de control en cualquier momento.",
                Icons.Default.CheckCircle
            )
        )

        steps.forEachIndexed { index, (title, content, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ---------------- TAB 4: ABOUT AND FULL LEGAL DISCLAIMER ----------------
@Composable
fun AboutAndLegalTab(settingsViewModel: SettingsViewModel) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "FocusGuard",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "FocusGuard Launcher",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Versión 2.0 (Custom Stealth Launcher)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Theme switch row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isDarkMode) "Modo Oscuro Activo" else "Modo Claro Activo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }
        }

        // Developers Credits Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Desarrolladores y Autoría",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "La Clave Argentina y Tienda SSH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reservados todos los derechos. Software registrado para uso y personalización.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Exhaustive Legal Disclaimer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Términos Legales y Exención de Responsabilidad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                val legalClauses = listOf(
                    Pair(
                        "1. EXENCIÓN TOTAL DE RESPONSABILIDAD POR USO INDEBIDO:",
                        "Los desarrolladores ('La Clave Argentina' y 'Tienda SSH') quedan expresa, absoluta e irrevocablemente exentos de cualquier responsabilidad civil, penal, contravencional o administrativa derivada del mal uso, uso irresponsable, desleal, fraudulento o ilícito que terceros o el usuario final puedan realizar mediante las funciones de filtrado y ocultación de iconos que provee este software."
                    ),
                    Pair(
                        "2. RESPONSABILIDAD EXCLUSIVA DEL USUARIO:",
                        "El usuario declara bajo su exclusiva cuenta y riesgo que la utilización de este Launcher se realiza para fines lícitos y personales. La custodia de contraseñas, PINs, patrones de desbloqueo y el acceso a las aplicaciones instaladas en el dispositivo es responsabilidad única e indelegable del propietario del equipo."
                    ),
                    Pair(
                        "3. PRIVACIDAD LOCAL Y SEGURIDAD OFFLINE:",
                        "Este software no recopila, almacena, transmite ni comparte información privada, listas de aplicaciones ni credenciales con servidores externos. Toda la configuración opera de manera 100% local en la memoria protegida del dispositivo."
                    ),
                    Pair(
                        "4. PROPIEDAD INTELECTUAL Y DERECHOS RESERVADOS:",
                        "Queda prohibida la ingeniería inversa, redistribución fraudulenta o alteración de los créditos de autoría de 'La Clave Argentina' y 'Tienda SSH' sin la debida autorización de sus titulares."
                    )
                )

                legalClauses.forEach { (clauseTitle, clauseBody) ->
                    Text(
                        text = clauseTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = clauseBody,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
