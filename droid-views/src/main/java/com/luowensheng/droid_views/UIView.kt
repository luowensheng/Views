package com.luowensheng.droid_views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.UUID

interface ToViewBuilder<T: ViewBuilder> {
    fun toViewBuilder(): T
}

enum class PaddingUnit { PX, DP }

abstract class UIView<V: View>(): ViewBuilder, HasAttributes {

    override var id: String = UUID.randomUUID().toString()

    private val tasks: MutableList<(Context, V)->Unit> = mutableListOf()
    abstract fun setup(context: Context): V
    override var shouldDisplay: Reference<Boolean> = Reference(true)

    fun padding(amount:Int = 10, unit: PaddingUnit = PaddingUnit.DP): UIView<V> {
        addTask { context, view ->
            if (unit == PaddingUnit.DP){
                val scale = context.resources.displayMetrics.density;
                val dpAsPixels = ((amount * scale) + 0.5f).toInt()
                view.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
            } else {
                view.setPadding(amount, amount, amount, amount)
            }
        }
        return this
    }
    fun padding(left:Int, top: Int, right: Int, bottom:Int, unit: PaddingUnit = PaddingUnit.DP): UIView<V> {
        addTask { context, view ->
            if (PaddingUnit.DP == unit){
                val scale = context.resources.displayMetrics.density;
                val f: (Int)-> Int = { ((it * scale) + 0.5f).toInt() }
                view.setPadding(f(left), f(top), f(right), f(bottom))
            } else {
                view.setPadding(left, top, right, bottom)
            }
        }
        return this
    }
    fun id(id: String): UIView<V> {
        this.id = id
        return addTask { view -> view.id = id.hashCode() }
    }
    fun screen(): UIView<V> {
        return screen(id)
    }
    fun screen(id: String): UIView<V> {
        this.id = id
        AppManager.register(this.id, this)
        return this
    }
    fun visibleIf(watch: Reference<Boolean>): UIView<V> {
        if (watch.getValue()){
            addTask { view -> view.visibility = View.INVISIBLE }
        }
        return addTask { view ->
            watch.onUpdate { visible ->
                view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            }
        }
    }
    fun frame(width: Int, height: Int): UIView<V> {
        return addTask { view ->
            if (view.layoutParams == null){
                view.layoutParams = ViewGroup.LayoutParams(width, height)
            } else {
                view.layoutParams.width = width
                view.layoutParams.height = height
            }
        }
    }
    fun replaceWith(item: UIView<V>, watch: Reference<Boolean>): UIView<V> {
        return addTask { view ->
            val parent = view.parent
            if (parent is ViewGroup && parent.childCount == 1){
                shouldDisplay.onUpdate {
                    parent.removeAllViews()
                    parent.addView(item)
                    watch.toggle()
                }
            }
        }
    }
    fun visibleIfNot(watch: Reference<Boolean>): UIView<V> {
        val notWatch = Reference(!watch.getValue())
        watch.onUpdate { notWatch.setValue(!it) }
        return visibleIf(notWatch)
    }
    fun displayIfNot(watch: Reference<Boolean>): UIView<V> {
        val notWatch = Reference(!watch.getValue())
        watch.onUpdate { notWatch.setValue(!it) }
        return displayIf(notWatch)
    }
    fun displayIf(display: Boolean): UIView<V> {
        shouldDisplay.setValue(display)
        return this
    }
    fun displayIf(watch: Reference<Boolean>): UIView<V> {
        shouldDisplay.setValue(watch.getValue())
        return addTask { _ ->
            watch.onUpdate { show ->
                if (show != shouldDisplay.getValue()){
                    shouldDisplay.setValue(show)
                }
            }
        }
    }
    fun addTask(task: (Context, V)->Unit): UIView<V> {
        tasks.add(task)
        return this
    }
    fun addTask(task: (V)->Unit): UIView<V> {
        tasks.add { _, view -> task(view) }
        return this
    }
    fun background(r:Int, g:Int, b:Int, a:Int): UIView<V> {
        return background(Color.argb(a, r, g, b))
    }
    fun background(r:Int, g:Int, b:Int): UIView<V> {
        return background(Color.rgb(r, g, b))
    }
    fun background(colorString: String): UIView<V> {
        return background(Color.parseColor(colorString))
    }
    fun background(color: Int): UIView<V> {
        return addTask { view -> view.setBackgroundColor(color) }
    }
    fun onTap(onTap: (V)->Unit): UIView<V> {
        return addTask { view ->
            view.setOnClickListener{ onTap(view) }
        }
    }
    fun border(content: String): UIView<V> {
        println(content)
        return this
    }
    override fun build(context: Context): V {
        val view = setup(context)
//         view.layoutParams = ViewGroup.LayoutParams(
//             ViewGroup.LayoutParams.WRAP_CONTENT,
//             ViewGroup.LayoutParams.WRAP_CONTENT
//         )
        tasks.forEach { fn-> fn(context, view) }
        return view
    }
}



class Text(val content:String) : UIView<TextView>() {
    override fun setup(context: Context): TextView {
        val textview = TextView(context)
        textview.text = content
        textview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textview.textSize = 10f
        return textview
    }
    constructor(contentReference: Reference<String>): this(contentReference.getValue()) {
        addTask { view -> contentReference.onUpdate { view.text = it } }
    }
    fun font(){
//        addTask { textView -> textView }
    }
    fun textSize(fontSize: Int): Text {
        return textSize(fontSize.toFloat())
    }
    fun textSize(fontSize: Float): Text {
        addTask { textView -> textView.textSize = fontSize }
        return this
    }
    fun textColor(r:Int, g:Int, b:Int, a:Int): Text {
        return textColor(Color.argb(a, r, g, b))
    }
    fun textColor(r:Int, g:Int, b:Int): Text {
        return textColor(Color.rgb(r, g, b))
    }
    fun textColor(colorString: String): Text {
        return textColor(Color.parseColor(colorString))
    }
    fun textColor(color: Int): Text {
        addTask { view -> view.setTextColor(color) }
        return this
    }
}
//
open class Spacer(private val width: Int, private val height: Int): UIView<View>(){

    companion object {
        fun from(orientation: Int, spacing: Int): Spacer {
            return if (orientation == LinearLayout.VERTICAL) VSpacer(spacing) else HSpacer(spacing)
        }
    }
    override fun setup(context: Context): View {
        val spacer = View(context)
        spacer.layoutParams = ViewGroup.LayoutParams(width, height)
        return spacer
    }
}

open class HSpacer(width: Int): Spacer(width, 0)
open class VSpacer(height: Int): Spacer(0, height)

open class Stack: UIView<LinearLayout> {

    private var children: MutableList<ViewBuilder>
    private var orientation = LinearLayout.VERTICAL
    private var spacing = 0

    constructor(orientation: Int, spacing: Int): super() {
        this.orientation = orientation
        this.children = mutableListOf()
        this.spacing = spacing
    }

    constructor(orientation: Int, child: ViewBuilder, vararg children: ViewBuilder): super() {
        this.orientation = orientation
        this.children = mutableListOf()
        this(child, *children)
    }
    constructor(orientation: Int=LinearLayout.VERTICAL): super() {
        this.orientation = orientation
        this.children = mutableListOf()
    }
    override fun setup(context: Context): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = orientation

        if (orientation == LinearLayout.VERTICAL){
            linearLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            linearLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val drawChildren = {
            linearLayout.removeAllViews()
            children.forEachIndexed{ index, view ->
                linearLayout.addIfShouldDisplayView(view)
                if (spacing!=0 && index < children.count()-1){
                    linearLayout.addView(Spacer.from(orientation, spacing))
                }
            }
        }
        children.forEach { view -> view.shouldDisplay.onUpdate { drawChildren() }  }
        drawChildren()
        return linearLayout
    }
    open operator fun invoke(vararg children: ViewBuilder): Stack {
        children.forEach { this.children.add(it) }
        return this
    }
    open operator fun <T: ViewBuilder> invoke(group: Group<T>, vararg items: ViewBuilder): Stack {
        group.items.forEach { children.add(it) }
        items.forEach { children.add(it) }
        return this
    }

    open operator fun <T: ViewBuilder> invoke(group: Group<T>): Stack {
        group.items.forEach { children.add(it) }
        return this
    }
    open fun center(): Stack {
        addTask { linearLayout -> linearLayout.gravity = Gravity.CENTER  }
        return this
    }
}
//
fun ViewGroup.addIfShouldDisplayView(view: ViewBuilder) {
    if (view.shouldDisplay.getValue()) addView(view)
}

fun ViewGroup.addView(view: ViewBuilder) {
    addView(view.build(context))
}
//
//open class Group<T: ViewBuilder>(vararg items: T): UIComponent(){
//    val items = items
//
//    override val body: ViewBuilder =
//    fun each(f: (T)->Unit): Group<T> {
//        items.forEach { f(it) }
//        return this
//    }
//    fun each(f: (Int, T)->Unit): Group<T> {
//        items.forEachIndexed {index, item -> f(index, item) }
//        return this
//    }
//}
//
open class Group<T: ViewBuilder>(vararg items: T) {
    val items = items
    fun each(f: (T)->Unit): Group<T> {
        items.forEach { f(it) }
        return this
    }
    fun each(f: (Int, T)->Unit): Group<T> {
        items.forEachIndexed {index, item -> f(index, item) }
        return this
    }
}

open class Scroll(private val item: ViewBuilder) : UIView<ScrollView>(){
    override fun setup(context: Context): ScrollView {
        val scrollView = ScrollView(context)

//        if (orientation == LinearLayout.VERTICAL){
        scrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
//        } else {
//            scrollView.layoutParams = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            )
//        }
        scrollView.addView(item.build(context))
//        item.shouldDisplay.onUpdate {
//            if (it) scrollView.addView(item) else scrollView.removeAllViews()
//        }
        return scrollView
    }
}

open class VStack : Stack {
    constructor(child: ViewBuilder, vararg children: ViewBuilder): super(LinearLayout.VERTICAL, child, *children)
    constructor(spacing:Int): super(LinearLayout.VERTICAL, spacing)
}

open class HStack : Stack {
    constructor(child: ViewBuilder, vararg children: ViewBuilder): super(LinearLayout.HORIZONTAL, child, *children)
    constructor(spacing:Int): super(LinearLayout.HORIZONTAL, spacing)
}

open class RStack() : UIView<RelativeLayout>() {
    override fun setup(context: Context): RelativeLayout {
        return RelativeLayout(context)
    }
}

open class ZStack() : UIView<ConstraintLayout>() {
    override fun setup(context: Context): ConstraintLayout {
        return ConstraintLayout(context)
    }
}

open class Button(private val content: String) : UIView<Button>() {
    override fun setup(context: Context): android.widget.Button {
        val button = android.widget.Button(context)
        button.text = content
        button.textSize = 10f
        return button
    }
    constructor(content: String, onTap: (android.widget.Button)->Unit) : this(content) {
        addTask { button ->
            button.setOnClickListener { onTap(button) }
        }
    }
}

open class Grid() : UIView<GridView>() {
    override fun setup(context: Context): GridView {
        return GridView(context)
    }
}

open class Table() : UIView<TableLayout>() {
    override fun setup(context: Context): TableLayout {
        return TableLayout(context)
    }
}


open class Frame() : UIView<FrameLayout>() {
    override fun setup(context: Context): FrameLayout {
        return FrameLayout(context)
    }
}

open class Image : UIView<ImageView> {
    constructor(imageId:Int){
        addTask { imageView -> imageView.setImageResource(imageId) }
    }
    constructor(uri:Uri){
        addTask { imageView -> imageView.setImageURI(uri) }
    }
    constructor(drawable: Drawable){
        addTask { imageView -> imageView.setImageDrawable(drawable) }
    }
    constructor(bitmap: Bitmap){
        addTask { imageView -> imageView.setImageBitmap(bitmap) }
    }
    constructor(icon: Icon){
        addTask { imageView -> imageView.setImageIcon(icon) }
    }
    override fun setup(context: Context): ImageView {
        return ImageView(context)
    }
}


open class List() : UIView<ListView>() {
    override fun setup(context: Context): ListView {
        return ListView(context)
    }
}

open class WebView(var url: String) : UIView<WebView>() {
    override fun setup(context: Context): android.webkit.WebView {
        val webView = android.webkit.WebView(context)
        webView.webViewClient= WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        return webView
    }
//    fun loadUrl(url: String): WebView {
//        this.url = url
//        addTask { webView -> webView.loadUrl(url) }
//        return this`
//    }
}

