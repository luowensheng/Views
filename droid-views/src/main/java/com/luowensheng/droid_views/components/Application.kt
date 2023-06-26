package com.luowensheng.droid_views.components

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.app.AppCompatActivity


abstract class Application: AppCompatActivity() {
    private lateinit var parent: ViewBuilder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    fun setContentScreen(component: UIComponent) {
        supportActionBar?.hide()
        AppManager.setHomeScreen(component.id, component)
        component.shouldDisplay.setValue(true)
        parent = Stack()(
            component.frame(1000, LayoutParams.MATCH_PARENT),
        ).center()
            .background("#000000")
            .frame(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            .addTask { viewGroup->
                viewGroup.setBackgroundColor( Color.parseColor("white"))
                AppManager.setMainContainerIfNotExists(viewGroup)
            }
        setContentView(parent.build(this))
    }

}