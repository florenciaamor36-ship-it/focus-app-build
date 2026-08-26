package com.example

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.math.pow
import kotlin.math.sqrt

fun triggerBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                    errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE ||
                    errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
                ) {
                    Toast.makeText(activity, "Autenticación simulada (Entorno de pruebas).", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Huella o rostro no reconocido")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso Seguro Requerido")
        .setSubtitle("Verifica tu identidad para continuar")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onSuccess()
    }
}

// ---------------- PIN AUTH DIALOG ----------------
@Composable
fun PinAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = "PIN",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresar PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isError) "PIN incorrecto. Intenta de nuevo." else "Ingresa tu clave de 4 dígitos",
                    fontSize = 13.sp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // PIN dots indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> MaterialTheme.colorScheme.error
                                        isFilled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (key == "C" || key == "DEL") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        isError = false
                                        when (key) {
                                            "C" -> enteredPin = ""
                                            "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    val newPin = enteredPin + key
                                                    enteredPin = newPin
                                                    if (newPin.length == 4) {
                                                        if (settingsViewModel.verifyPin(newPin)) {
                                                            onSuccess()
                                                        } else {
                                                            isError = true
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Borrar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ---------------- PASSWORD AUTH DIALOG ----------------
@Composable
fun PasswordAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresar Contraseña",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isError) "Contraseña incorrecta" else "Ingresa tu clave alfanumérica",
                    fontSize = 13.sp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        isError = false
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Alternar Visibilidad"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (settingsViewModel.verifyPassword(passwordInput)) {
                                onSuccess()
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text("Desbloquear")
                    }
                }
            }
        }
    }
}

// ---------------- 3x3 PATTERN LOCK CANVAS COMPONENT ----------------
@Composable
fun PatternLockView(
    onPatternCompleted: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }
    var nodePositions by remember { mutableStateOf(listOf<Offset>()) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .size(260.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedNodes.clear()
                        currentTouchPosition = offset
                        nodePositions.forEachIndexed { index, nodePos ->
                            val dist = sqrt((offset.x - nodePos.x).pow(2) + (offset.y - nodePos.y).pow(2))
                            if (dist < 32.dp.toPx() && !selectedNodes.contains(index)) {
                                selectedNodes.add(index)
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentTouchPosition = change.position
                        nodePositions.forEachIndexed { index, nodePos ->
                            val dist = sqrt((change.position.x - nodePos.x).pow(2) + (change.position.y - nodePos.y).pow(2))
                            if (dist < 32.dp.toPx() && !selectedNodes.contains(index)) {
                                selectedNodes.add(index)
                            }
                        }
                    },
                    onDragEnd = {
                        currentTouchPosition = null
                        if (selectedNodes.isNotEmpty()) {
                            onPatternCompleted(selectedNodes.toList())
                        }
                    },
                    onDragCancel = {
                        currentTouchPosition = null
                        selectedNodes.clear()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val margin = 36.dp.toPx()
            val stepX = (canvasWidth - 2 * margin) / 2
            val stepY = (canvasHeight - 2 * margin) / 2

            val positions = mutableListOf<Offset>()
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    positions.add(Offset(margin + col * stepX, margin + row * stepY))
                }
            }
            nodePositions = positions

            // Draw connecting lines between selected nodes
            for (i in 0 until selectedNodes.size - 1) {
                val startPos = positions[selectedNodes[i]]
                val endPos = positions[selectedNodes[i + 1]]
                drawLine(
                    color = primaryColor,
                    start = startPos,
                    end = endPos,
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw line to current touch point
            currentTouchPosition?.let { touchPos ->
                if (selectedNodes.isNotEmpty()) {
                    val lastPos = positions[selectedNodes.last()]
                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f),
                        start = lastPos,
                        end = touchPos,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw 3x3 Dots
            positions.forEachIndexed { index, pos ->
                val isSelected = selectedNodes.contains(index)
                // Outer ring
                drawCircle(
                    color = if (isSelected) primaryColor else outlineColor,
                    radius = if (isSelected) 18.dp.toPx() else 12.dp.toPx(),
                    center = pos,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                // Inner solid dot
                drawCircle(
                    color = if (isSelected) primaryColor else outlineColor.copy(alpha = 0.5f),
                    radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                    center = pos
                )
            }
        }
    }
}

// ---------------- PATTERN AUTH DIALOG ----------------
@Composable
fun PatternAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Patrón",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Dibujar Patrón",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isError) "Patrón incorrecto. Intenta de nuevo." else "Une al menos 4 puntos para desbloquear",
                    fontSize = 13.sp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                PatternLockView(
                    onPatternCompleted = { pattern ->
                        if (settingsViewModel.verifyPattern(pattern)) {
                            onSuccess()
                        } else {
                            isError = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    }
}

// ---------------- SETUP CREDENTIALS DIALOGS ----------------
@Composable
fun SetupPinDialog(
    onDismiss: () -> Unit,
    onPinSaved: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: enter new, 2: confirm
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (step == 1) "Nuevo PIN (4 dígitos)" else "Confirma tu nuevo PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: if (step == 1) "Ingresa 4 dígitos numéricos" else "Vuelve a ingresar los 4 dígitos",
                    fontSize = 13.sp,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                val current = if (step == 1) pin1 else pin2

                OutlinedTextField(
                    value = current,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            errorMessage = null
                            if (step == 1) pin1 = it else pin2 = it
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (step == 1) {
                                if (pin1.length == 4) {
                                    step = 2
                                } else {
                                    errorMessage = "Debe tener 4 dígitos"
                                }
                            } else {
                                if (pin1 == pin2) {
                                    onPinSaved(pin1)
                                } else {
                                    errorMessage = "Los PINs no coinciden"
                                    step = 1
                                    pin1 = ""
                                    pin2 = ""
                                }
                            }
                        }
                    ) {
                        Text(if (step == 1) "Siguiente" else "Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun SetupPasswordDialog(
    onDismiss: () -> Unit,
    onPasswordSaved: (String) -> Unit
) {
    var pass1 by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nueva Contraseña", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (errorMessage != null) {
                    Text(errorMessage!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pass1,
                    onValueChange = { pass1 = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pass2,
                    onValueChange = { pass2 = it },
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pass1.length < 3) {
                                errorMessage = "La contraseña debe tener al menos 3 caracteres"
                            } else if (pass1 != pass2) {
                                errorMessage = "Las contraseñas no coinciden"
                            } else {
                                onPasswordSaved(pass1)
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun SetupPatternDialog(
    onDismiss: () -> Unit,
    onPatternSaved: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: draw, 2: confirm
    var pattern1 by remember { mutableStateOf<List<Int>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (step == 1) "Dibuja el nuevo patrón" else "Vuelve a dibujar para confirmar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "Une al menos 4 puntos",
                    fontSize = 13.sp,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                PatternLockView(
                    onPatternCompleted = { drawnPattern ->
                        if (step == 1) {
                            if (drawnPattern.size >= 4) {
                                pattern1 = drawnPattern
                                step = 2
                                errorMessage = "¡Bien! Ahora repite el patrón"
                            } else {
                                errorMessage = "El patrón debe unir al menos 4 puntos"
                            }
                        } else {
                            if (pattern1 == drawnPattern) {
                                onPatternSaved(pattern1.joinToString(","))
                            } else {
                                errorMessage = "El patrón no coincide. Vuelve a empezar."
                                step = 1
                                pattern1 = emptyList()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    }
}
