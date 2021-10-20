package dd.wan.ddwanmediaplayer.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dd.wan.ddwanmediaplayer.fragment.PlayFragment
import dd.wan.ddwanmediaplayer.fragment.RecommendFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, var bundle: Bundle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = PlayFragment()
                fragment.arguments = bundle
                fragment
            }
            1 -> {
                val fragment = RecommendFragment()
                fragment.arguments = bundle
                fragment
            }
            else -> PlayFragment()
        }
    }
}