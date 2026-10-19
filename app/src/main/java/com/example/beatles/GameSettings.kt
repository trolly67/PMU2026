package com.example.beatles

import android.content.Context

object GameSettings {

    private const val PREFS_NAME = "game_settings"

    private const val KEY_SPEED = "speed"
    private const val KEY_MAX_BUGS = "max_bugs"
    private const val KEY_BONUS_INTERVAL = "bonus_interval"
    private const val KEY_ROUND_DURATION = "round_duration"
    private const val KEY_PRESET_INDEX = "preset_index"
    private const val KEY_CURRENT_PLAYER_ID = "current_player_id"

    const val DEFAULT_PRESET_INDEX = 1
    const val DEFAULT_SPEED = 2
    const val DEFAULT_MAX_BUGS = 5
    const val DEFAULT_BONUS_INTERVAL = 11
    const val DEFAULT_ROUND_DURATION = 3

    fun getCurrentPlayerId(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_CURRENT_PLAYER_ID, -1L)

    fun setCurrentPlayerId(context: Context, id: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_CURRENT_PLAYER_ID, id).apply()
    }

    fun getPresetIndex(context: Context): Int =
        getInt(context, KEY_PRESET_INDEX, DEFAULT_PRESET_INDEX)

    fun setPresetIndex(context: Context, index: Int) {
        setInt(context, KEY_PRESET_INDEX, index)
        if (index != 5) {
            val preset = DifficultyPreset.fromIndex(index)
            setInt(context, KEY_SPEED, preset.speed)
            setInt(context, KEY_MAX_BUGS, preset.maxBugs)
            setInt(context, KEY_ROUND_DURATION, preset.roundDuration)
        }
    }

    fun setCustomFlag(context: Context) {
        setInt(context, KEY_PRESET_INDEX, 5)
    }

    fun getSpeed(context: Context): Int = getInt(context, KEY_SPEED, DEFAULT_SPEED)
    fun setSpeed(context: Context, value: Int) = setInt(context, KEY_SPEED, value)

    fun getMaxBugs(context: Context): Int = getInt(context, KEY_MAX_BUGS, DEFAULT_MAX_BUGS)
    fun setMaxBugs(context: Context, value: Int) = setInt(context, KEY_MAX_BUGS, value)

    fun getBonusInterval(context: Context): Int = getInt(context, KEY_BONUS_INTERVAL, DEFAULT_BONUS_INTERVAL)
    fun setBonusInterval(context: Context, value: Int) = setInt(context, KEY_BONUS_INTERVAL, value)

    fun getRoundDuration(context: Context): Int = getInt(context, KEY_ROUND_DURATION, DEFAULT_ROUND_DURATION)
    fun setRoundDuration(context: Context, value: Int) = setInt(context, KEY_ROUND_DURATION, value)

    private fun getInt(context: Context, key: String, default: Int): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(key, default)

    private fun setInt(context: Context, key: String, value: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(key, value).apply()
    }
}