package com.example.appblocker.ui.blocked

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appblocker.data.BlockedAppsRepository
import com.example.appblocker.data.InstalledApp
import com.example.appblocker.util.AppLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlockedAppsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BlockedAppsRepository.get(app)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()

    init {
        // reload list whenever blocked set changes
        viewModelScope.launch(Dispatchers.Default) {
            repo.blockedFlow.collect { blocked ->
                val list = AppLoader.loadLaunchableApps(getApplication(), blocked)
                _apps.value = filter(list, _query.value)
            }
        }
        // react to query
        viewModelScope.launch(Dispatchers.Default) {
            _query.collect { q ->
                _apps.value = filter(_apps.value, q)
            }
        }
    }

    private fun filter(source: List<InstalledApp>, q: String): List<InstalledApp> {
        val s = q.trim().lowercase()
        if (s.isEmpty()) return source
        return source.filter { it.label.lowercase().contains(s) || it.packageName.lowercase().contains(s) }
    }

    fun updateQuery(q: String) { _query.value = q }

    fun toggle(app: InstalledApp, block: Boolean) {
        repo.toggle(app.packageName, block)
    }

    fun getBlocked(): Set<String> = repo.getBlocked()
    fun isBlocked(pkg: String) = repo.isBlocked(pkg)
}