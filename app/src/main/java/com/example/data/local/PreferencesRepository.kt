package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AiImageModel
import com.example.model.AiImageSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pandulipi_prefs")

data class StudioPreferences(
    val autoSaveEnabled: Boolean = true,
    val selectedPageId: Long = 0L,
    val preferredAiModel: AiImageModel = AiImageModel.NANO_BANANA_2_1,
    val preferredImageSize: AiImageSize = AiImageSize.SIZE_1K
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val AUTO_SAVE = booleanPreferencesKey("auto_save_enabled")
        val SELECTED_PAGE_ID = longPreferencesKey("selected_page_id")
        val AI_MODEL = stringPreferencesKey("preferred_ai_model")
        val AI_IMAGE_SIZE = stringPreferencesKey("preferred_image_size")
    }

    val preferences: Flow<StudioPreferences> = context.dataStore.data.map { prefs ->
        val modelId = prefs[Keys.AI_MODEL] ?: AiImageModel.NANO_BANANA_2_1.modelId
        val sizeLabel = prefs[Keys.AI_IMAGE_SIZE] ?: AiImageSize.SIZE_1K.label
        StudioPreferences(
            autoSaveEnabled = prefs[Keys.AUTO_SAVE] ?: true,
            selectedPageId = prefs[Keys.SELECTED_PAGE_ID] ?: 0L,
            preferredAiModel = AiImageModel.entries.firstOrNull { it.modelId == modelId }
                ?: AiImageModel.NANO_BANANA_2_1,
            preferredImageSize = AiImageSize.entries.firstOrNull { it.label == sizeLabel }
                ?: AiImageSize.SIZE_1K
        )
    }

    suspend fun setAutoSaveEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    }

    suspend fun setSelectedPageId(pageId: Long) {
        context.dataStore.edit { it[Keys.SELECTED_PAGE_ID] = pageId }
    }

    suspend fun setPreferredAiModel(model: AiImageModel) {
        context.dataStore.edit { it[Keys.AI_MODEL] = model.modelId }
    }

    suspend fun setPreferredImageSize(size: AiImageSize) {
        context.dataStore.edit { it[Keys.AI_IMAGE_SIZE] = size.label }
    }
}
