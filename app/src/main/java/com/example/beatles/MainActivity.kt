package com.example.beatles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beatles.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = MainPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_registration)
                1 -> getString(R.string.tab_rules)
                2 -> getString(R.string.tab_authors)
                3 -> getString(R.string.tab_settings)
                else -> ""
            }
        }.attach()
    }
}