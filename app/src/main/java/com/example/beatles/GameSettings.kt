package com.example.beatles

import android.content.Context

object GameSettings {

    private const val PREFS_NAME = "game_settings"

    private const val KEY_SPEED = "speed"
    private const val KEY_MAX_BUGS = "max_bugs"
    private const val KEY_BONUS_INTERVAL = "bonus_interval"
    private const val KEY_ROUND_DURATION = "round_duration"

    const val DEFAULT_SPEED = 2
    const val DEFAULT_MAX_BUGS = 5
    const val DEFAULT_BONUS_INTERVAL = 11
    const val DEFAULT_ROUND_DURATION = 3

    fun getSpeed(context: Context): Int =
        getInt(context, KEY_SPEED, DEFAULT_SPEED)

    fun setSpeed(context: Context, value: Int) =
        setInt(context, KEY_SPEED, value)

    fun getMaxBugs(context: Context): Int =
        getInt(context, KEY_MAX_BUGS, DEFAULT_MAX_BUGS)

    fun setMaxBugs(context: Context, value: Int) =
        setInt(context, KEY_MAX_BUGS, value)

    fun getBonusInterval(context: Context): Int =
        getInt(context, KEY_BONUS_INTERVAL, DEFAULT_BONUS_INTERVAL)

    fun setBonusInterval(context: Context, value: Int) =
        setInt(context, KEY_BONUS_INTERVAL, value)

    fun getRoundDuration(context: Context): Int =
        getInt(context, KEY_ROUND_DURATION, DEFAULT_ROUND_DURATION)

    fun setRoundDuration(context: Context, value: Int) =
        setInt(context, KEY_ROUND_DURATION, value)

    private fun getInt(context: Context, key: String, default: Int): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(key, default)

    private fun setInt(context: Context, key: String, value: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }
}