package com.luowensheng.droid_views.components

import android.view.ViewGroup
import java.util.Optional

class AppManager {
    companion object {
        private var parent: Optional<ViewGroup> = Optional.empty()
        private val attributes = mutableMapOf<String, MutableMap<String, String>>()
        private val items = mutableMapOf<String, ViewBuilder>()
        private var previousScreen: ViewBuilder? = null
        private var currentScreen: ViewBuilder? = null

        fun register(id: String, viewBuilder: ViewBuilder){
            items[id] = viewBuilder
        }
        private fun useParent(f: (ViewGroup)->Unit):Boolean{
            if (!parent.isPresent) return false
            f(parent.get())
            return true
        }
        fun setMainContainerIfNotExists(container: ViewGroup){
            if (parent.isPresent) return
            parent = Optional.of(container)
        }
        fun setHomeScreen(id: String, viewBuilder: ViewBuilder){
            currentScreen = viewBuilder
            register(id, viewBuilder)
        }
        fun hasRegistered(id: String): Boolean {
            return items.containsKey(id)
        }
        fun requestScreenChange(id: String): Boolean {
            val item = items[id] ?: return false
            return requestScreenChange(item)
        }
        fun requestScreenChange(item: ViewBuilder): Boolean {
            return useParent { parent ->
                previousScreen = currentScreen
                parent.removeAllViews()
                parent.addView(item.build(parent.context))
                currentScreen = item
            }
        }
        fun requestPreviousScreen(): Boolean {
            val item = previousScreen ?: return false
            return requestScreenChange(item)
        }
        fun setAttribute(id: String, key: String, value: String){
            if (!attributes.containsKey(id)){
                attributes[id] = mutableMapOf()
            }
            attributes[id]?.set(key, value)
        }
        fun getAttribute(id: String, key: String): Optional<String> {
            if (!attributes.containsKey(id) || !attributes[id]?.containsKey(key)!!){
                return Optional.empty()
            }
            return Optional.of(attributes[id]?.get(key)!! )
        }

    }
}