package cn.com.cys.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding


/**
 * Version:V1.0
 * Author: Damon
 * Date: 2023/2/2 11:03
 * Description
 */
class VerificationInputView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        const val TAG = "VerificationInputView"
    }

    /**
     * 预置显示风格
     */
    enum class Style(val value: Int) {
        RECT(0),
        ROUND(1),
        LINE(2);
    }

    interface OnVerifyCompletionListener {
        /**
         * 返回值 true 则震动输入框，false无动作
         */
        fun onVerify(text: String): Boolean
    }

    private var onVerifyListener: OnVerifyCompletionListener? = null

    fun setOnVerifyCompletionListener(listener: OnVerifyCompletionListener) {
        this.onVerifyListener = listener
    }

    private val views = mutableListOf<TextView>()
    private lateinit var editText: EditText
    private var currentIndex = 0
    private var inputCount = 4
    private var inputStyle = Style.ROUND.value
    private var inputBoxSize = 0
    private var inputBoxSpace = 0
    private var inputTextSize = 0f
    private var inputTextColor = Color.parseColor("#000000")
    private var inputSelectedColor = Color.parseColor("#72CF21")
    private var inputNormalColor = Color.parseColor("#C3A9A9")
    private var inputBoxBackground: Drawable? = null

    init {
        inputBoxSize = dp2px(context, 20f)
        inputBoxSpace = dp2px(context, 2f)
        inputTextSize = sp2px(context, 16f).toFloat()

        initAttrs(attrs)
        setOnClickListener {
            if (editText.visibility != View.VISIBLE) {
                editText.visibility = View.VISIBLE
                editText.x = views[currentIndex].x
                editText.y = views[currentIndex].y
            }
            views[currentIndex].text = ""
            updateFocusStatus()
            showKeyBoard(editText)
        }
    }

    private fun initAttrs(attrs: AttributeSet) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.VerificationInputView)
        this.inputStyle =
            a.getInt(R.styleable.VerificationInputView_viv_inputStyle, this.inputStyle)
        this.inputCount =
            a.getInt(R.styleable.VerificationInputView_viv_inputCount, this.inputCount)
        this.inputTextSize =
            a.getDimension(R.styleable.VerificationInputView_viv_textSize, this.inputTextSize)
        this.inputTextColor =
            a.getColor(R.styleable.VerificationInputView_viv_textColor, this.inputTextColor)
        this.inputSelectedColor =
            a.getColor(R.styleable.VerificationInputView_viv_selectedColor, this.inputSelectedColor)
        this.inputNormalColor =
            a.getColor(R.styleable.VerificationInputView_viv_normalColor, this.inputNormalColor)
        this.inputBoxSize = a.getDimensionPixelSize(
            R.styleable.VerificationInputView_viv_boxSize,
            this.inputBoxSize
        )
        this.inputBoxSpace = a.getDimensionPixelSize(
            R.styleable.VerificationInputView_viv_boxSpace,
            this.inputBoxSpace
        )
        val drawable = a.getDrawable(R.styleable.VerificationInputView_viv_boxBackground)
        this.inputBoxBackground = drawable

        a.recycle()

        val paint = Paint()
        paint.textSize = inputTextSize
        val height = ((paint.fontMetrics.bottom - paint.fontMetrics.top) * 1.3).toInt()
        inputBoxSize = if (this.inputBoxSize < height) height else this.inputBoxSize

        recreateViews()
    }

    private fun recreateViews() {
        removeAllViews()
        views.clear()
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        addView(
            linearLayout,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        for (i in 0 until inputCount) {
            val textView = TextView(context)
            textView.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputTextSize)
            textView.transformationMethod = PasswordTransformationMethod.getInstance()
            textView.setTextColor(inputTextColor)
            if(inputBoxBackground != null){
                textView.background = inputBoxBackground
            } else {
                if (this.inputStyle == Style.LINE.value) {
                    val shapeListDrawable = StateListDrawable()
                    val radius = inputBoxSize / 20f
                    val paddingHorizontal = (inputBoxSize * 0.1).toInt()
                    val paddingTop = (inputBoxSize * 0.95).toInt()
                    var shapeDrawable = ShapeDrawable(
                        RoundRectShape(
                            floatArrayOf(
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius
                            ), null, null
                        )
                    )
                    shapeDrawable.paint.color = inputSelectedColor
                    shapeDrawable.paint.strokeWidth = radius
                    shapeDrawable.paint.style = Paint.Style.FILL
                    var layerDrawable = LayerDrawable(arrayOf(shapeDrawable))
                    layerDrawable.setLayerInset(
                        0,
                        paddingHorizontal,
                        paddingTop,
                        paddingHorizontal,
                        0
                    )
                    shapeListDrawable.addState(
                        intArrayOf(android.R.attr.state_selected),
                        layerDrawable
                    )
                    shapeDrawable = ShapeDrawable(
                        RoundRectShape(
                            floatArrayOf(
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius
                            ), null, null
                        )
                    )
                    shapeDrawable.paint.color = inputNormalColor
                    shapeDrawable.paint.strokeWidth = radius
                    shapeDrawable.paint.style = Paint.Style.FILL
                    layerDrawable = LayerDrawable(arrayOf(shapeDrawable))
                    layerDrawable.setLayerInset(
                        0,
                        paddingHorizontal,
                        paddingTop,
                        paddingHorizontal,
                        0
                    )
                    shapeListDrawable.addState(
                        intArrayOf(-android.R.attr.state_selected),
                        layerDrawable
                    )
                    textView.background = shapeListDrawable
                } else {
                    val shapeListDrawable = StateListDrawable()
                    val padding = inputBoxSize / 20f
                    val radius = if (inputStyle == Style.ROUND.value) inputBoxSize / 20f else 0f
                    var shapeDrawable = ShapeDrawable(
                        RoundRectShape(
                            floatArrayOf(
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius
                            ), RectF(padding, padding, padding, padding), null
                        )
                    )
                    shapeDrawable.paint.color = inputSelectedColor
                    shapeDrawable.paint.strokeWidth = radius
                    shapeDrawable.paint.style = Paint.Style.FILL
                    shapeListDrawable.addState(
                        intArrayOf(android.R.attr.state_selected),
                        shapeDrawable
                    )
                    shapeDrawable = ShapeDrawable(
                        RoundRectShape(
                            floatArrayOf(
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius,
                                radius
                            ), RectF(padding, padding, padding, padding), null
                        )
                    )
                    shapeDrawable.paint.color = inputNormalColor
                    shapeDrawable.paint.strokeWidth = radius
                    shapeDrawable.paint.style = Paint.Style.FILL
                    shapeListDrawable.addState(
                        intArrayOf(-android.R.attr.state_selected),
                        shapeDrawable
                    )

                    textView.background = shapeListDrawable
                }
            }
            linearLayout.addView(textView, MarginLayoutParams(inputBoxSize, inputBoxSize).apply {
                setMargins(inputBoxSpace, inputBoxSpace, inputBoxSpace, inputBoxSpace)
            })
            views.add(textView)
        }
        editText = EditText(context)
        editText.setPadding(0)
        addView(editText, MarginLayoutParams(inputBoxSize, inputBoxSize).apply {
            setMargins(inputBoxSpace, inputBoxSpace, inputBoxSpace, inputBoxSpace)
        })
        editText.gravity = Gravity.CENTER
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputTextSize * 0.7f)
        editText.filters = arrayOf(InputFilter.LengthFilter(1))
        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.x = views[0].x
        editText.y = views[0].y
        currentIndex = 0
        updateFocusStatus()
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length == 1) {
                        views[currentIndex].text = it
                        editText.setText("")
                        if (currentIndex == inputCount - 1) {
                            editText.visibility = View.GONE
                            val sb = StringBuilder()
                            views.forEach { sb.append(it.text) }
                            closeKeyBoard(editText)
                            val index = currentIndex
                            currentIndex = -1
                            updateFocusStatus()
                            currentIndex = index
                            val onVerifyResult = onVerifyListener?.onVerify(sb.toString())
                            onVerifyResult?.let { backValue ->
                                if (backValue) {
                                    val animation = AnimationUtils.loadAnimation(
                                        context,
                                        R.anim.verification_error
                                    )
                                    linearLayout.startAnimation(animation)
                                }
                            }
                            return
                        }
                        for (index in views.indices) {
                            val view = views[index]
                            if (view.text.isEmpty()) {
                                editText.x = view.x
                                editText.y = view.y
                                currentIndex = index
                                updateFocusStatus()
                                break
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        editText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                val view = views[currentIndex]
                view.text = ""
                currentIndex = if (currentIndex - 1 <= 0) 0 else currentIndex - 1
                views[currentIndex].text = ""
                editText.x = views[currentIndex].x
                editText.y = views[currentIndex].y
                updateFocusStatus()
            }
            false
        }
    }

    /**
     * 更新输入框状态
     */
    private fun updateFocusStatus() {
        for (index in views.indices) {
            views[index].isSelected = index == currentIndex
        }
    }

    /**
     * 展示软键盘
     */
    private fun showKeyBoard(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     */
    private fun closeKeyBoard(editText: EditText) {
        editText.clearFocus()
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    /**
     * dp转px
     */
    private fun dp2px(ctx: Context, dp: Float): Int {
        val scale = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * sp转px
     */
    private fun sp2px(ctx: Context, sp: Float): Int {
        val scale = ctx.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    //region 属性设置
    fun setInputCount(count: Int) {
        this.inputCount = count
        recreateViews()
    }

    fun setInputStyle(style: Style) {
        this.inputStyle = style.value
        recreateViews()
    }

    fun setInputTextSize(textSize: Float) {
        this.inputTextSize = textSize
        recreateViews()
    }

    fun setInputTextColor(color: Int) {
        this.inputTextColor = color
        recreateViews()
    }

    fun setInputSelectedColor(color: Int) {
        this.inputSelectedColor = color
        recreateViews()
    }

    fun setInputNormalColor(color: Int) {
        this.inputNormalColor = color
        recreateViews()
    }
    //endregion
}