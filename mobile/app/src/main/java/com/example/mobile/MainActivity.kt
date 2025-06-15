package com.example.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobile.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    val bundle = Bundle().apply {
                        putBoolean("isOwnProfile", true)
                    }
                    navController.navigate(R.id.navigation_profile, bundle)
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        navView.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_home) {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh_home", true)
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_addVideo,
                R.id.navigation_notifications,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}