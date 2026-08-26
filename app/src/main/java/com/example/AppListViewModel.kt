package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isHidden: Boolean
)

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val pm = application.packageManager

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())

    // Apps visibles en el escritorio del Launcher
    private val _launcherApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val launcherApps: StateFlow<List<AppInfo>> = _launcherApps.asStateFlow()

    // Apps mostradas en el panel de configuración (filtradas por búsqueda y categoría)
    private val _settingsApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val settingsApps: StateFlow<List<AppInfo>> = _settingsApps.asStateFlow()

    // Estadísticas
    private val _totalAppsCount = MutableStateFlow(0)
    val totalAppsCount: StateFlow<Int> = _totalAppsCount.asStateFlow()

    private val _hiddenAppsCount = MutableStateFlow(0)
    val hiddenAppsCount: StateFlow<Int> = _hiddenAppsCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Todas") // "Todas", "Ocultas", "Visibles"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val hiddenSet = prefs.getStringSet("lista_ocultas", emptySet()) ?: emptySet()

            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val appList = mutableListOf<AppInfo>()

            val ourPackage = getApplication<Application>().packageName

            for (resolveInfo in resolveInfos) {
                val pkg = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val isHidden = hiddenSet.contains(pkg)

                appList.add(AppInfo(pkg, appName, icon, isHidden))
            }

            val distinctApps = appList.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
            _allApps.value = distinctApps
            updateFlows(distinctApps, hiddenSet)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val hiddenSet = prefs.getStringSet("lista_ocultas", emptySet()) ?: emptySet()
        updateFlows(_allApps.value, hiddenSet)
    }

    fun setFilterCategory(category: String) {
        _selectedFilter.value = category
        val hiddenSet = prefs.getStringSet("lista_ocultas", emptySet()) ?: emptySet()
        updateFlows(_allApps.value, hiddenSet)
    }

    private fun updateFlows(all: List<AppInfo>, hiddenSet: Set<String>) {
        val ourPackage = getApplication<Application>().packageName
        val q = _searchQuery.value.trim().lowercase()
        val filter = _selectedFilter.value

        // Conteo
        _totalAppsCount.value = all.size
        _hiddenAppsCount.value = all.count { hiddenSet.contains(it.packageName) }

        // Filtro para configuración
        val filteredSettings = all.filter { app ->
            val matchesQuery = q.isEmpty() || app.appName.lowercase().contains(q) || app.packageName.lowercase().contains(q)
            val matchesCategory = when (filter) {
                "Ocultas" -> hiddenSet.contains(app.packageName)
                "Visibles" -> !hiddenSet.contains(app.packageName)
                else -> true
            }
            matchesQuery && matchesCategory
        }.map { it.copy(isHidden = hiddenSet.contains(it.packageName)) }

        _settingsApps.value = filteredSettings

        // Filtro para el escritorio del Launcher: solo las visibles y sin nuestra propia app
        _launcherApps.value = all.filter { !hiddenSet.contains(it.packageName) && it.packageName != ourPackage }
    }

    fun toggleAppVisibility(packageName: String, hide: Boolean) {
        val hiddenSet = prefs.getStringSet("lista_ocultas", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (hide) {
            hiddenSet.add(packageName)
        } else {
            hiddenSet.remove(packageName)
        }
        prefs.edit().putStringSet("lista_ocultas", hiddenSet).apply()
        updateFlows(_allApps.value, hiddenSet)
    }

    fun hideAllApps() {
        val ourPackage = getApplication<Application>().packageName
        val allPackages = _allApps.value.map { it.packageName }.filter { it != ourPackage }.toSet()
        prefs.edit().putStringSet("lista_ocultas", allPackages).apply()
        updateFlows(_allApps.value, allPackages)
    }

    fun unhideAllApps() {
        prefs.edit().putStringSet("lista_ocultas", emptySet()).apply()
        updateFlows(_allApps.value, emptySet())
    }

    fun launchApp(context: Context, packageName: String) {
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
