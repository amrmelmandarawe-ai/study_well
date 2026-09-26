package com.example.utils

import android.content.Context
import android.content.SharedPreferences

object IntroPreferencesManager {
    private const val PREF_NAME = "study_well_intro_prefs"

    private const val KEY_FIRST_INSTALL_COMPLETED = "first_install_completed"
    private const val KEY_DOC_INTRO_SEEN = "doc_intro_seen"
    private const val KEY_VIDEO_INTRO_SEEN = "video_intro_seen"
    private const val PREFIX_SECTION_INTRO_SEEN = "section_intro_seen_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if section intro (BOOKS, NOTES, MARKSCHEMES, VIDEOS) has been seen on this install
     */
    fun hasSeenSectionIntro(context: Context, sectionKey: String): Boolean {
        return getPrefs(context).getBoolean(PREFIX_SECTION_INTRO_SEEN + sectionKey.uppercase(), false)
    }

    /**
     * Mark section intro as seen
     */
    fun markSectionIntroSeen(context: Context, sectionKey: String) {
        getPrefs(context).edit().putBoolean(PREFIX_SECTION_INTRO_SEEN + sectionKey.uppercase(), true).apply()
    }

    /**
     * Check if document study intro overlay has been seen
     */
    fun hasSeenDocumentIntro(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DOC_INTRO_SEEN, false)
    }

    /**
     * Mark document study intro overlay as seen
     */
    fun markDocumentIntroSeen(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_DOC_INTRO_SEEN, true).apply()
    }

    /**
     * Check if video player intro overlay has been seen
     */
    fun hasSeenVideoIntro(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIDEO_INTRO_SEEN, false)
    }

    /**
     * Mark video player intro overlay as seen
     */
    fun markVideoIntroSeen(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_VIDEO_INTRO_SEEN, true).apply()
    }

    /**
     * Reset all intro preferences (useful for testing or re-triggering from settings)
     */
    fun resetAllIntros(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
