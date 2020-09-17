package com.cbs.demospike

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment


/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navGraphIds = listOf(R.navigation.tv_nav)
        setupDeepLinks(navGraphIds, supportFragmentManager, intent)
    }

    private fun setupDeepLinks(
        navGraphIds: List<Int>,
        fragmentManager: FragmentManager,
        intent: Intent
    ) {
        navGraphIds.forEachIndexed { _, navGraphId ->
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                navGraphId
            )
            navHostFragment.navController.handleDeepLink(intent)
        }
    }

    private fun obtainNavHostFragment(
        fragmentManager: FragmentManager,
        navGraphId: Int
    ): NavHostFragment {
        val existingFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        existingFragment?.let { return it }
        val navHostFragment = NavHostFragment.create(navGraphId)
        fragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, navHostFragment, MAIN_NAV_TAG)
            .commitNow()
        return navHostFragment
    }

    companion object {
        private const val MAIN_NAV_TAG = "MainNavigationTag"
    }
}
