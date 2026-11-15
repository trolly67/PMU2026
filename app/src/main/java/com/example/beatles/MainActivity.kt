package com.example.beatles

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beatles.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.beatles.repository.GoldRateRepositoryProvider
import com.example.beatles.widget.GoldRateWidgetUpdater
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
                4 -> getString(R.string.tab_records)
                else -> ""
            }
        }.attach()

        val tabPosition = intent.getIntExtra("tab_position", 0)
        if (tabPosition > 0) {
            binding.viewPager.setCurrentItem(tabPosition, false)
        }
        // Временная загрузка данных при старте
        lifecycleScope.launch {
            val repo = GoldRateRepositoryProvider.get(this@MainActivity)
            val result = repo.refresh() // Загружаем прямо сейчас

            android.util.Log.d("GOLD_CHECK", "Результат загрузки: $result")

            // Принудительно обновляем виджет
            GoldRateWidgetUpdater.updateAll(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tabPosition = intent.getIntExtra("tab_position", 0)
        if (tabPosition > 0) {
            binding.viewPager.setCurrentItem(tabPosition, false)
        }
    }
}