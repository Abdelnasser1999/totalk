package com.callberry.callingapp.util

import android.view.View
import android.animation.ObjectAnimator
import android.view.animation.OvershootInterpolator

const val DURATION: Long = 400
val interpolator = OvershootInterpolator()

fun slideNext(viewOut: View, viewIn: View) {
    viewOut.slideX(0, (-1 * viewOut.width))
    viewIn.visibility = View.VISIBLE
    viewIn.slideX(viewIn.width, 0)
}

fun slidePrevious(viewOut: View, viewIn: View) {
    viewOut.slideX(0, (viewOut.width))
    viewIn.visibility = View.VISIBLE
    viewIn.slideX((-1 * viewIn.width), 0)
}


fun View.slideInRight() {
    val view = this
    view.visibility = View.VISIBLE
    val animator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        view.width.toFloat(),
        0f

    )
    animator.duration = DURATION
    animator.interpolator = OvershootInterpolator()
    animator.start()
}

fun View.slideOutRight() {
    val view = this
    view.visibility = View.INVISIBLE
    val animator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        0f,
        view.width.toFloat()
    )
    animator.duration = DURATION
    animator.interpolator = OvershootInterpolator()
    animator.start()
}

fun View.slideInLeft() {
    val view = this
    view.visibility = View.VISIBLE
    val animator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        (-1 * view.width).toFloat(),
        0f
    )
    animator.duration = DURATION
    animator.interpolator = OvershootInterpolator()
    animator.start()
}

fun View.slideOutLeft() {
    val view = this
    view.visibility = View.INVISIBLE
    val animator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        0f,
        (-1 * view.width).toFloat()

    )
    animator.duration = DURATION
    animator.interpolator = OvershootInterpolator()
    animator.start()
}

fun View.slideX(from: Int, to: Int) {
    val view = this
    val animator = ObjectAnimator.ofFloat(view, "translationX", from.toFloat(), to.toFloat())
    animator.duration = 380
    animator.interpolator = OvershootInterpolator()
    animator.start()
}
