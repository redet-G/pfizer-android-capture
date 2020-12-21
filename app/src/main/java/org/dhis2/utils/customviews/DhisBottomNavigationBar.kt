package org.dhis2.utils.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
import org.dhis2.Bindings.clipWithRoundedCorners
import org.dhis2.Bindings.dp
import org.dhis2.R

class DhisBottomNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val animations = DhisBottomNavigationBarAnimations(this)
    private var hidden = false
    private val currentItemIndicator: View by lazy { initCurrentItemIndicator() }

    private var currentItemIndicatorColor: Int
    private val itemIndicatorSize: Float
    private val itemIndicatorDrawable: Drawable?
    private var currentItemId: Int = -1

    init {
        labelVisibilityMode = LABEL_VISIBILITY_UNLABELED
        this.clipWithRoundedCorners()
        context.obtainStyledAttributes(attrs, R.styleable.DhisBottomNavigationBar).apply {
            currentItemIndicatorColor = getColor(
                R.styleable.DhisBottomNavigationBar_currentItemSelectorColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
            itemIndicatorSize = getDimension(
                R.styleable.DhisBottomNavigationBar_currentItemSelectorSize,
                40.dp.toFloat()
            )
            itemIndicatorDrawable =
                getDrawable(R.styleable.DhisBottomNavigationBar_currentItemSelectorDrawable)
            recycle()
        }
        addView(currentItemIndicator)
        post {
            menu.forEachIndexed { index, item ->
                if (index == 0) {
                    setCurrentItemIndicatorPosition(findViewById<View>(item.itemId))
                }
            }
        }
       setIconsColor(currentItemIndicatorColor)
    }

    fun hide() {
        animations.hide {
            hidden = true
        }
    }

    fun show() {
        animations.show {
            hidden = false
        }
    }

    override fun setOnNavigationItemSelectedListener(listener: OnNavigationItemSelectedListener?) {
        super.setOnNavigationItemSelectedListener { item ->
            currentItemId = item.itemId
            findViewById<View>(item.itemId)?.let { itemView ->
                animateItemIndicatorPosition(itemView)
            }
            updateBadges()
            listener?.onNavigationItemSelected(item) ?: false
        }
    }

    private fun intrinsicHorizontalMargin(): Int {
        return (width - (getChildAt(0) as BottomNavigationMenuView).width) / 2
    }

    private fun initCurrentItemIndicator(): View {
        return ImageView(context).apply {
            layoutParams =
                ViewGroup.LayoutParams(itemIndicatorSize.toInt(), itemIndicatorSize.toInt())
            x = 0f
            y = 0f
            setImageDrawable(itemIndicatorDrawable)
            DrawableCompat.setTint(DrawableCompat.wrap(drawable), currentItemIndicatorColor)
        }
    }

    private fun animateItemIndicatorPosition(selectedItemView: View) {
        animations.animateSelectionOut(currentItemIndicator.animate()) {
            setCurrentItemIndicatorPosition(selectedItemView)
            animations.animateSelectionIn(currentItemIndicator.animate())
        }
    }

    private fun setCurrentItemIndicatorPosition(selectedItemView: View) {
        currentItemIndicator.apply {
            x = selectedItemView.x +
                selectedItemView.width / 2f +
                intrinsicHorizontalMargin() -
                itemIndicatorSize / 2f
            y = (this@DhisBottomNavigationBar.height - itemIndicatorSize) / 2f
        }
        invalidate()
    }

    fun isHidden(): Boolean {
        return hidden
    }

    private fun updateBadges() {
        menu.forEach { updateBadge(it.itemId, getOrCreateBadge(it.itemId).number) }
    }

    fun updateBadge(menuItemId: Int, badgeCount: Int) {
        val badge = getOrCreateBadge(menuItemId)
        badge.isVisible = badgeCount > 0
        badge.number = badgeCount
        badge.horizontalOffset = -5
        if (currentItemId == menuItemId) {
            badge.backgroundColor = currentItemIndicatorColor
        } else {
            badge.backgroundColor = ColorUtils.setAlphaComponent(currentItemIndicatorColor, 128)
        }
    }

    fun selectItemAt(position: Int) {
        mutableListOf<Int>().apply {
            menu.forEach {
                if (it.isVisible) {
                    add(it.itemId)
                }
            }
            selectedItemId = get(position)
        }
    }

    fun setIconsColor(color: Int) {
        currentItemIndicatorColor = color
        val iconsColorStates =
            ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    currentItemIndicatorColor,
                    Color.argb(
                        114,
                        currentItemIndicatorColor.red,
                        currentItemIndicatorColor.green,
                        currentItemIndicatorColor.blue
                    )
                )
            )
        itemIconTintList = iconsColorStates
        itemIndicatorDrawable?.let {
            DrawableCompat.setTint(DrawableCompat.wrap(it), currentItemIndicatorColor)
        }
    }
}
