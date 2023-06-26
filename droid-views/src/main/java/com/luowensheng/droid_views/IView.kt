package com.luowensheng.droid_views

import android.content.Context
import android.view.View
import java.util.Optional

interface Identifiable: ViewBuilder {
    var id: String
}

interface HasAttributes: Identifiable {
    fun setAttribute(key: String, value: String){
        AppManager.setAttribute(id, key, value)
    }
    fun getAttribute(key: String): Optional<String>{
        return AppManager.getAttribute(id, key)
    }
    fun hasAttribute(key: String): Boolean {
        return getAttribute(key).isPresent
    }
}

interface ViewBuilder {
    fun build(context: Context): View
    var shouldDisplay: Reference<Boolean>
}

//interface UIComponent: Body {
//    override var shouldDisplay: Reference<Boolean>
//        get() = body.shouldDisplay
//        set(value) { body.shouldDisplay = value }
//    val body: Body
//    override fun build(context: Context):View {
//      return body.build(context)
//   }
//}

typealias CustomView = UIView<View>

abstract class UIComponent: CustomView(), HasAttributes {
    abstract val body: ViewBuilder
    override var shouldDisplay: Reference<Boolean> = Reference(true)
    override fun setup(context: Context): View {
        return body.build(context)
    }
}
