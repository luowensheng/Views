package com.luowensheng.views

import android.os.Bundle
import com.luowensheng.droid_views.Application
import com.luowensheng.droid_views.Text
import com.luowensheng.droid_views.UIComponent
import com.luowensheng.droid_views.ViewBuilder

class MainActivity: Application() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentScreen(MainComponent())
    }
}

class MainComponent(): UIComponent() {
    override val body: ViewBuilder =
        Text("Black Mirror")
}