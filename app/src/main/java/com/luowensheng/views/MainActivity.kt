package com.luowensheng.views

import android.os.Bundle
import com.luowensheng.droid_views.components.Application
import com.luowensheng.droid_views.components.Text
import com.luowensheng.droid_views.components.UIComponent
import com.luowensheng.droid_views.components.ViewBuilder

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