package io.github.turskyi.expandedradiobuttons

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import io.github.turskyi.expandedradiobuttons.FirstFragment.Companion.LOG_TAG

class DropdownRadioButtons : LinearLayout {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (!isInEditMode) {
            readAttributes(attrs)
            initialize()
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        if (!isInEditMode) {
            readAttributes(attrs)
            initialize()
        }
    }

    companion object {
        fun collapse(dropdownTextView: DropdownRadioButtons, animate: Boolean) {
            if (!dropdownTextView.isExpanded) {
                return
            }

            dropdownTextView.collapseInternal(animate)
        }

        fun expand(dropdownTextView: DropdownRadioButtons, animate: Boolean) {
            if (dropdownTextView.isExpanded) {
                return
            }
            dropdownTextView.expandInternal(animate)
        }
    }

    private lateinit var panelView: View
    private lateinit var titleTextView: TextView
    private lateinit var contentView: RadioGroup
    private lateinit var arrowView: ImageView

    private var isExpanded: Boolean = false
    private var titleText: String? = null
    private var contentText: String? = null
    private var expandDuration: Int = -1
    private var titleTextColor: Int? = null
    private var titleTextColorExpanded: Int? = null
    private var titleTextSizeRes: Int = -1
    private var titleFontRes: Int = -1
    private var contentTextColor: Int? = null
    private var contentTextSizeRes: Int = -1
    private var contentFontRes: Int = -1
    private var rawHtmlTitle: String? = null
    private var spannableHtmlTitle: String? = null
    private var linkHandler: ((url: String) -> Unit)? = null
    private var linkTextColor: Int? = null
    private var isLinkUnderline: Boolean = true
    private var arrowDrawableRes: Int = -1
    private var bgRegularDrawableRes: Int = -1
    private var bgExpandedDrawableRes: Int = -1
    private var panelPaddingRes: Int = -1
    private var contentPaddingRes: Int = -1
    private var radioButtonsSize: Int = -1

    private inline fun <reified V> changeValue(
        from: V,
        to: V,
        duration: Long,
        crossinline update: (value: V) -> Unit
    ) {
        val vH: PropertyValuesHolder = when (from) {
            is Int -> PropertyValuesHolder.ofInt("prop", from as Int, to as Int)
            is Float -> PropertyValuesHolder.ofFloat("prop", from as Float, to as Float)
            else -> throw UnsupportedOperationException("$from and $to types are not supported")
        }

        ValueAnimator.ofPropertyValuesHolder(vH).apply {
            this.duration = duration
            addUpdateListener {
                update(this.getAnimatedValue("prop") as V)
            }
            start()
        }
    }

    fun getContent() = this.contentView

    @Suppress("unused")
    fun setHtmlTitle(@StringRes text: Int) {
        titleTextView.text = text.toHtml(context)
        titleTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setHtmlTitle(text: String) {
        titleTextView.setText(text.toSpannedHtml(), TextView.BufferType.SPANNABLE)
        titleTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setRadioButtonsSize(size: Int) {
        radioButtonsSize = size
        generateRadioButtons(this)
    }

    private fun setHtmlTitle(text: String, clickHandler: (url: String) -> Unit) {
        val spannableBuilder = SpannableStringBuilder(text.toSpannedHtml())
        val urlSPans =
            spannableBuilder.getSpans(0, text.toSpannedHtml().length, URLSpan::class.java)
        urlSPans.forEach {
            makeClickable(spannableBuilder, it, isLinkUnderline) { url ->
                clickHandler(url)
            }
        }

        titleTextView.text = spannableBuilder
        titleTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun makeClickable(
        spannableBuilder: SpannableStringBuilder,
        span: URLSpan,
        makeUnderline: Boolean,
        clickHandler: (url: String) -> Unit
    ) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                clickHandler(span.url)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = makeUnderline
            }
        }
        spannableBuilder.setSpan(
            clickableSpan,
            spannableBuilder.getSpanStart(span),
            spannableBuilder.getSpanEnd(span),
            spannableBuilder.getSpanFlags(span)
        )
        spannableBuilder.removeSpan(span)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putBoolean("expanded", this.isExpanded)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            isExpanded = state.getBoolean("expanded")
            superState = state.getParcelable("superState")
        }

        super.onRestoreInstanceState(superState)
    }

    private fun readAttributes(attrs: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DropdownRadioButtons,
            0, 0
        )

        val typeValue = TypedValue()

        attributes.getValue(R.styleable.DropdownRadioButtons_title_text, typeValue)
        titleText = when (typeValue.type) {
            TypedValue.TYPE_STRING -> typeValue.string.toString()
            TypedValue.TYPE_REFERENCE -> resources.getString(typeValue.resourceId)
            else -> null
        }

        attributes.getValue(R.styleable.DropdownRadioButtons_title_text_color, typeValue)
        titleTextColor = when (typeValue.type) {
            TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
            else -> typeValue.data
        }

        attributes.getValue(R.styleable.DropdownRadioButtons_title_text_color_expanded, typeValue)
        titleTextColorExpanded = when (typeValue.type) {
            TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
            else -> typeValue.data
        }

        titleTextSizeRes =
            attributes.getResourceId(R.styleable.DropdownRadioButtons_title_text_size, -1)
        titleFontRes = attributes.getResourceId(R.styleable.DropdownRadioButtons_title_font, -1)

        attributes.getValue(R.styleable.DropdownRadioButtons_content_text, typeValue)
        contentText = when (typeValue.type) {
            TypedValue.TYPE_STRING -> typeValue.string.toString()
            TypedValue.TYPE_REFERENCE -> resources.getString(typeValue.resourceId)
            else -> null
        }

        attributes.getValue(R.styleable.DropdownRadioButtons_content_text_color, typeValue)
        contentTextColor = when (typeValue.type) {
            TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
            else -> typeValue.data
        }

        attributes.getValue(R.styleable.DropdownRadioButtons_link_text_color, typeValue)
        linkTextColor = when (typeValue.type) {
            TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
            else -> typeValue.data
        }

        attributes.getValue(R.styleable.DropdownRadioButtons_underline_link, typeValue)
        isLinkUnderline = when (typeValue.type) {
            TypedValue.TYPE_INT_BOOLEAN -> typeValue.data == 1
            else -> true
        }

        contentTextSizeRes =
            attributes.getResourceId(R.styleable.DropdownRadioButtons_content_text_size, -1)
        contentFontRes = attributes.getResourceId(R.styleable.DropdownRadioButtons_content_font, -1)

        bgRegularDrawableRes =
            attributes.getResourceId(R.styleable.DropdownRadioButtons_bg_drawable_regular, -1)
        bgExpandedDrawableRes = attributes.getResourceId(
            R.styleable.DropdownRadioButtons_bg_drawable_expanded,
            -1
        )

        panelPaddingRes = attributes.getResourceId(
            R.styleable.DropdownRadioButtons_panel_padding,
            R.dimen.offset_panel_default
        )
        contentPaddingRes = attributes.getResourceId(
            R.styleable.DropdownRadioButtons_content_padding,
            R.dimen.offset_content_default
        )

        radioButtonsSize = attributes.getResourceId(
            R.styleable.DropdownRadioButtons_radio_buttons_size,
            0
        )

        arrowDrawableRes = attributes.getResourceId(
            R.styleable.DropdownRadioButtons_arrow_drawable,
            R.drawable.ic_arrow
        )

        expandDuration =
            attributes.getInteger(R.styleable.DropdownRadioButtons_expand_duration, 300)
    }

    private fun initialize() {
        inflateView()
        bindView()
        setResources()
        post {
            if (isExpanded) {
                expandInternal(false)
            } else {
                collapseInternal(false)
            }
            setArrowViewState(isExpanded, false)
            setBackgroundState(isExpanded)
        }
    }

    private fun bindView() {
        panelView = findViewById(R.id.panel_view)
        titleTextView = findViewById(R.id.title_text_view)
        contentView = findViewById(R.id.content)
        arrowView = findViewById(R.id.arrow_view)

        titleTextView.setOnClickListener {
            if (isExpanded) {
                collapse(this, true)
            } else {
                expand(this, true)
            }
        }

        panelView.setOnClickListener {
            if (isExpanded) {
                collapse(this, true)
            } else {
                expand(this, true)
            }
        }
    }

    private fun setResources() {
        arrowView.setImageResource(arrowDrawableRes)
        titleTextView.text = titleText

        titleTextColor?.let { titleTextView.setTextColor(it) }
        if (titleTextSizeRes != -1) {
            titleTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(titleTextSizeRes)
            )
        }
        if (titleFontRes != -1) {
            titleTextView.typeface = ResourcesCompat.getFont(context, titleFontRes)
        }


        if (contentTextSizeRes != -1) {

            contentView.children.forEach { radioButton ->
                (radioButton as RadioButton).apply {
                    setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        context.resources.getDimension(contentTextSizeRes)
                    )
                    contentTextColor?.let { radioButton.setTextColor(it) }
                }
            }
        }
        if (contentFontRes != -1) {
            contentView.children.forEach { radioButton ->
                (radioButton as RadioButton).typeface =
                    ResourcesCompat.getFont(context, contentFontRes)
            }
        }

        rawHtmlTitle?.let {
            setHtmlTitle(it)
        }
        spannableHtmlTitle?.let {
            setHtmlTitle(it, linkHandler!!)
        }

        linkTextColor?.let {
            contentView.children.forEach { radioButton ->
                (radioButton as RadioButton).setLinkTextColor(it)

            }
        }

        context.resources.getDimension(panelPaddingRes).toInt().apply {
            panelView.setPadding(this, this, this, this)
        }
        context.resources.getDimension(contentPaddingRes).toInt().apply {
            contentView.setPadding(this, this, this, this)
        }
    }

    private fun inflateView(): View? {
        val view = View.inflate(context, R.layout.view_dropdown_radiobuttons, this)
        generateRadioButtons(view)
        return view
    }

    private fun generateRadioButtons(view: View) {
        /* total number of radioButtons to add */
        val size = radioButtonsSize
        Log.d(LOG_TAG, "inflate size $size")
        val root = view.findViewById<RadioGroup>(R.id.content)
        val radioButtons = arrayOfNulls<RadioButton>(size) // create an empty array;
        val ltInflater = LayoutInflater.from(context)
        val nullParent: ViewGroup? = null

        for (i in 1..size) {
            /* create a new radioButton */
            val radioButton = ltInflater.inflate(R.layout.radio_button, nullParent, false)

            /* set some properties of rowRadioButton */
            (radioButton as RadioButton).text = "This is row #$i"

            /* add the radio button to the radio group */
            root.addView(radioButton)

            /* save a reference to the radio button for later */
            radioButtons[i - 1] = radioButton
        }
    }

    private fun expandInternal(animate: Boolean) {
        setHeightToContentHeight(animate)
        setArrowViewState(true, animate)
        setBackgroundState(true)
        setTitleTextState(true)
        isExpanded = true
        getContent().check(getContent().getChildAt(0).id)
    }

    private fun collapseInternal(animate: Boolean) {
        setHeightToZero(animate)
        setArrowViewState(false, animate)
        setBackgroundState(false)
        setTitleTextState(false)
        isExpanded = false
    }

    private fun setBackgroundState(expand: Boolean) {
        if (!expand && bgRegularDrawableRes != -1) {
            setBackgroundResource(bgRegularDrawableRes)
        } else if (bgExpandedDrawableRes != -1) {
            setBackgroundResource(bgExpandedDrawableRes)
        }
    }

    private fun setTitleTextState(expand: Boolean) = if (expand) {
        (titleTextColorExpanded ?: titleTextColor)?.let { titleTextView.setTextColor(it) }
    } else {
        titleTextColor?.let { titleTextView.setTextColor(it) }
    }

    private fun setArrowViewState(expand: Boolean, animate: Boolean) {
        val angle = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            if (expand) -180.0f else 180.0f
        } else {
            if (expand) -180.0f else 0.0f
        }

        arrowView.animate()
            .rotation(angle)
            .setDuration((if (animate) expandDuration else 0).toLong())
            .start()
    }

    private fun setHeightToZero(animate: Boolean) {
        val targetHeight = panelView.height
        if (animate) {
            animate(this, height, targetHeight, expandDuration)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setHeightToContentHeight(animate: Boolean) {
        measureContentTextView()
        val targetHeight = panelView.height + contentView.measuredHeight
        if (animate) {
            animate(this, height, targetHeight, expandDuration)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setContentHeight(height: Int) {
        layoutParams.height = height
        requestLayout()
    }

    private fun animate(view: View, from: Int, to: Int, duration: Int) =
        changeValue(from, to, duration.toLong()) {
            view.layoutParams.height = it
            view.requestLayout()
            invalidate()
        }

    private fun measureContentTextView() {
        val widthMS = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        contentView.measure(widthMS, heightMS)
    }
}