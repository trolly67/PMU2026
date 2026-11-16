package com.example.beatles.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.beatles.ui.main.authors.AuthorsFragment
import com.example.beatles.ui.main.records.RecordsFragment
import com.example.beatles.ui.main.registration.RegistrationFragment
import com.example.beatles.ui.main.rules.RulesFragment
import com.example.beatles.ui.main.settings.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RegistrationFragment()
        1 -> RulesFragment()
        2 -> AuthorsFragment()
        3 -> SettingsFragment()
        4 -> RecordsFragment()
        else -> RegistrationFragment()
    }
}