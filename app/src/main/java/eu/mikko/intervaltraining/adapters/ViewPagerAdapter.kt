package eu.mikko.intervaltraining.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, b: Lifecycle) : FragmentStateAdapter(fragmentManager, b) {

    private val mFragmentList = ArrayList<Fragment>()

    fun addFragment(fragment: Fragment) = mFragmentList.add(fragment)

    override fun getItemCount(): Int = mFragmentList.size

    override fun createFragment(position: Int): Fragment = mFragmentList[position]
}