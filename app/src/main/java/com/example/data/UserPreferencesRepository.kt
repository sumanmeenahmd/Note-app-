package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.NoteSortOrder
import com.example.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "folio_preferences")

data class UserPreferences(
    val isGridView: Boolean = true,
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val sortName = prefs[Keys.SORT_ORDER]
        val themeName = prefs[Keys.THEME_PREFERENCE]
        UserPreferences(
            isGridView = prefs[Keys.IS_GRID_VIEW] ?: true,
            sortOrder = NoteSortOrder.entries.firstOrNull { it.name == sortName }
                ?: NoteSortOrder.UPDATED_DESC,
            themePreference = ThemePreference.entries.firstOrNull { it.name == themeName }
                ?: ThemePreference.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false
        )
    }

    suspend fun setGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.IS_GRID_VIEW] = isGrid }
    }

    suspend fun setSortOrder(sortOrder: NoteSortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = sortOrder.name }
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.dataStore.edit { it[Keys.THEME_PREFERENCE] = themePreference.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }
}
