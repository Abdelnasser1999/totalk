package com.callberry.callingapp.ui.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.callberry.callingapp.R

class OnBoarding : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var btnGotIt: Button? = null
    private val titleArray = ArrayList<String>()
    private val descriptionArray = ArrayList<String>()
    private val aboutImagesArray = intArrayOf(
        R.drawable.ic_obj_global_calls,
        R.drawable.ic_obj_free_credits,
        R.drawable.ic_obj_private_number
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        titleArray.addAll(
            arrayOf(
                getString(R.string.string_global_calls),
                getString(R.string.string_free_credits),
                getString(R.string.string_private_number)
            )
        )
        descriptionArray.addAll(
            arrayOf(
                getString(R.string.global_call_description),
                getString(R.string.free_credits_description),
                getString(R.string.private_number_description)
            )
        )
        initComponent()
    }

    private fun initComponent() {
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        btnGotIt = findViewById<View>(R.id.btn_got_it) as Button
        bottomProgressDots(0)
        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        btnGotIt!!.setOnClickListener { v: View? ->
            if (viewPager!!.currentItem == titleArray.size - 1) {
                val intent = Intent(this, NumberVerificationActivity::class.java)
                startActivity(intent)
                finish()
            } else viewPager!!.currentItem = viewPager!!.currentItem + 1
        }
    }

    private fun bottomProgressDots(currentIndex: Int) {
        val dotsLayout = findViewById<View>(R.id.layoutDots) as LinearLayout
        val dots = arrayOfNulls<ImageView>(MAX_STEP)
        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = ImageView(this)
            val widthHeight = 15
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(widthHeight, widthHeight))
            params.setMargins(10, 10, 10, 10)
            dots[i]!!.layoutParams = params
            dots[i]!!.setImageResource(R.drawable.shape_circle)
            dots[i]!!.setColorFilter(
                resources.getColor(R.color.colorGray50),
                PorterDuff.Mode.SRC_IN
            )
            dotsLayout.addView(dots[i])
        }
        if (dots.size > 0) {
            dots[currentIndex]!!.setImageResource(R.drawable.shape_circle)
            dots[currentIndex]!!.setColorFilter(
                resources.getColor(R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            bottomProgressDots(position)
            if (position == titleArray.size - 1) {
                btnGotIt!!.text = getString(R.string.got_it)
            } else {
                btnGotIt!!.text = getString(R.string.next)
                btnGotIt!!.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            Log.d("onPagescrolled", "")
        }

        override fun onPageScrollStateChanged(arg0: Int) {
            Log.d("onPageScrollChanged", "")
        }
    }

    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(R.layout.item_onboarding, container, false)
            (view.findViewById<View>(R.id.title) as TextView).text = titleArray[position]
            (view.findViewById<View>(R.id.description) as TextView).text =
                descriptionArray[position]
            (view.findViewById<View>(R.id.image) as ImageView).setImageResource(aboutImagesArray[position])
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return titleArray.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    companion object {
        private const val MAX_STEP = 3
    }
}