# FocusGuard Stealth Launcher (Focus App) 🛡️

**FocusGuard Stealth Launcher** es un lanzador de escritorio y gestor de privacidad para Android desarrollado por **La Clave Argentina** y **Tienda SSH**. Permite reemplazar la pantalla de inicio habitual del celular para ocultar de manera instantánea y segura los iconos de cualquier aplicación instalada, sin requerir permisos de administrador de dispositivo (MDM) ni comandos por computadora (ADB).

---

## ✨ Características Principales

1. **Escritorio Personalizado (Custom Launcher)**:
   - Funciona como pantalla de inicio predeterminada de Android (`android.intent.category.HOME`).
   - Muestra un reloj grande, fecha en tiempo real y cuadrícula dinámica de aplicaciones visibles.
   - Si se ocultan todas las aplicaciones, el escritorio queda en un modo minimalista y discreto.

2. **Ocultación Instantánea de Aplicaciones**:
   - Selector completo de aplicaciones con buscador en tiempo real.
   - Interruptor individual por aplicación con guardado local privado (`SharedPreferences`).
   - Filtros rápidos por estado (*Todas*, *Ocultas*, *Visibles*) y acciones rápidas (*Ocultar todas* / *Mostrar todas*).

3. **Invocación Secreta por Toques en Pantalla (Stealth Multi-Tap)**:
   - Configurable a **5 toques**, **10 toques** o **20 toques** consecutivos en el fondo o reloj del escritorio.
   - Al detectar la secuencia de toques rápidos, la aplicación solicita la autenticación configurada para ingresar al panel de control.
   - Zona de práctica y prueba interactiva en los ajustes con barra de progreso.

4. **Sistemas de Seguridad Multi-Método**:
   - **Biometría Nativa**: Desbloqueo por huella dactilar o reconocimiento facial mediante `BiometricPrompt`.
   - **PIN Numérico**: Teclado numérico táctil de 4 dígitos.
   - **Contraseña Alfanumérica**: Cuadro seguro con soporte para caracteres alfanuméricos.
   - **Patrón Táctil (3x3)**: Lienzo interactivo en pantalla para deslizar y unir puntos.
   - Posibilidad de cambiar o redefinir cualquier clave desde la interfaz.

5. **Modo Oscuro y Modo Claro**:
   - Soporte dinámico para temas claros y oscuros de alto contraste.

6. **Guía de Uso Paso a Paso**:
   - Manual integrado con instrucciones claras para configurar la app como launcher predeterminado y dominar todas sus funciones.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje**: Kotlin 100%
- **Interfaz de Usuario**: Jetpack Compose con Material Design 3 (M3)
- **Navegación**: Navigation Compose
- **Seguridad**: AndroidX Biometric API
- **Arquitectura**: MVVM (Model-View-ViewModel) con StateFlow y Coroutines
- **Persistencia**: Almacenamiento local privado y cifrado por sandbox de Android

---

## 📦 Compilación y Generación del APK

### Requisitos:
- Android Studio Ladybug / Koala o superior
- JDK 17 o 21
- Android SDK 35 (Android 15) con compatibilidad mínima Android 8.0 (API 26)

### Pasos para compilar desde terminal:
```bash
# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>

# Ingresar al directorio
cd focus-app

# Compilar APK en modo Debug
gradle assembleDebug
```
El archivo APK generado estará disponible en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Instalación y Configuración en el Celular

1. Instala el APK generado en tu teléfono Android.
2. Presiona el botón de **Inicio / Home** de tu dispositivo.
3. El sistema Android te preguntará: *"¿Qué aplicación deseas usar como pantalla de inicio?"*.
4. Selecciona **Focus App** y presiona **"Siempre"**.
5. ¡Listo! Tu escritorio estará protegido y podrás ocultar aplicaciones en cualquier momento.

---

## 👥 Créditos y Autoría

- **Desarrolladores**:
  - **La Clave Argentina**
  - **Tienda SSH**
- **Todos los derechos reservados**. Software registrado para uso y personalización.

---

## ⚖️ Marco Legal y Exención de Responsabilidad

1. **Exención total de responsabilidad por uso indebido**:
   Los desarrolladores (*La Clave Argentina* y *Tienda SSH*) quedan expresa, absoluta e irrevocablemente exentos de cualquier responsabilidad civil, penal, contravencional o administrativa derivada del mal uso, uso irresponsable, desleal, fraudulento o ilícito que terceros o el usuario final puedan realizar mediante las funciones de este software.

2. **Responsabilidad exclusiva del usuario**:
   El usuario declara bajo su exclusiva cuenta y riesgo que la utilización de este software se realiza para fines lícitos y personales. La custodia de contraseñas, PINs, patrones y el acceso a las aplicaciones instaladas en el dispositivo es responsabilidad única del propietario del equipo.

3. **Privacidad Local (Cero recolección de datos)**:
   Este software no recopila, almacena, transmite ni comparte información personal, listas de aplicaciones ni contraseñas con servidores externos. Toda la configuración opera 100% de manera local y offline en la memoria protegida del dispositivo.
