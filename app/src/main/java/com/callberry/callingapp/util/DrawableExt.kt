package com.callberry.callingapp.util

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.util.drawable.DrawableUtil

fun View.primaryDivider(context: Context) {
    background =
        DrawableUtil.setSolid(ContextCompat.getColor(context, R.color.colorRipple))
            .setStroke(1, R.color.colorRipple)
            .setCornerRadius(16f)
            .build();
}

fun View.setPhoneNoBackground(context: Context) {
    background =
        DrawableUtil.setSolid(ContextCompat.getColor(context, R.color.colorWhite))
            .setCornerRadius(8f)
            .build();
}