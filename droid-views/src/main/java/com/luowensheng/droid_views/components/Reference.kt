package com.luowensheng.droid_views.components


open class Reference<T>(value: T){
    private val tasks = mutableListOf<(T)->Unit>()
    private var value: T = value

    fun getValue():T {
        return value
    }
    fun setValue(newValue: T){
        try {
            this.value = newValue
            tasks.forEach { it(newValue) }
        } catch (e: ConcurrentModificationException){
            setValue(newValue)
        }
    }
    open fun onUpdate(updateFn: (T)->Unit){
        tasks.add(updateFn)
    }
}

fun Reference<Boolean>.toggle(){
    this.setValue(!this.getValue())
}