/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID
import com.kolibree.android.baseui.R
import com.kolibree.android.failearly.FailEarly
import java.lang.ref.WeakReference
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate

@Suppress("TooManyFunctions", "LargeClass")
@Keep
sealed class KolibreeDialogDsl<VALUE>(
    protected val context: Context,
    initialValue: VALUE
) : KolibreeDialogCommon<AppCompatDialog, VALUE>(initialValue) {

    private var dialog: AlertDialog? = null
    private var lifecycleOwner: WeakReference<LifecycleOwner>? = null
    private var lifecycleObserver: DefaultLifecycleObserver? = null
    private var featureImage: KolibreeDialogDrawable? = null
    private var featureIcon: KolibreeDialogDrawable? = null
    protected val buttons = mutableListOf<MaterialButton>()
    protected var headlineText: KolibreeDialogHeadline? = null
    protected var cancellable: Boolean = true
    private var dismissAction: ((DialogInterface) -> Unit)? = null

    private val startMargin: Int = dimensionIntFromAttribute(context, R.attr.alertDialogInsetStart)
    private val endMargin: Int = dimensionIntFromAttribute(context, R.attr.alertDialogInsetEnd)
    private val verticalSpacing: Int =
        dimensionIntFromAttribute(context, R.attr.alertDialogVerticalSpacing)

    protected val hasNoButtons
        get() = buttons.isEmpty()

    abstract val bodyLayout: Int

    private val valueListeners: MutableList<Invoker<out View, VALUE>> = mutableListOf()

    fun lifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = WeakReference(lifecycleOwner)
    }

    fun cancellable(cancellable: Boolean) {
        this.cancellable = cancellable
    }

    /**
     * Dismisses the dialog
     */
    fun dismiss() {
        lifecycleObserver?.let { lifecycleOwner?.get()?.lifecycle?.removeObserver(it) }
        lifecycleObserver = null
        lifecycleOwner = null
        dialog?.dismiss()
    }

    /**
     * Pass a callback which is executed when the Dialog is dismissed.
     */
    fun dismissAction(action: ((DialogInterface) -> Unit)) {
        this.dismissAction = action
    }

    /**
     * Creates a Contained style MaterialButton within the dialog
     */
    fun containedButton(lambda: KolibreeDialogButton.ContainedButton<VALUE>.() -> Unit) {
        buttons += KolibreeDialogButton.ContainedButton(context, currentValue)
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates an Outlined style MaterialButton within the dialog
     */
    fun outlinedButton(lambda: KolibreeDialogButton.OutlinedButton<VALUE>.() -> Unit) {
        buttons += KolibreeDialogButton.OutlinedButton(context, currentValue)
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates a IconContained style MaterialButton within the dialog
     */
    fun iconContainedButton(
        lambda: KolibreeDialogButton.KolibreeDialogButtonWithIcon.IconContainedButton<VALUE>.() -> Unit
    ) {
        buttons += KolibreeDialogButton.KolibreeDialogButtonWithIcon.IconContainedButton(
            context,
            currentValue
        )
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates an IconOutlined style MaterialButton within the dialog
     */
    fun iconOutlinedButton(
        lambda: KolibreeDialogButton.KolibreeDialogButtonWithIcon.IconOutlinedButton<VALUE>.() -> Unit
    ) {
        buttons += KolibreeDialogButton.KolibreeDialogButtonWithIcon.IconOutlinedButton(
            context,
            currentValue
        )
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates a Text style MaterialButton within the dialog
     */
    fun textButton(lambda: KolibreeDialogButton.TextButton<VALUE>.() -> Unit) {
        buttons += KolibreeDialogButton.TextButton(context, currentValue)
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates a TextSecondary style MaterialButton within the dialog
     */
    fun textButtonSecondary(lambda: KolibreeDialogButton.TextButton<VALUE>.() -> Unit) {
        buttons += KolibreeDialogButton.TextButton(
            context,
            currentValue,
            R.attr.materialButtonTextStyleSecondary
        )
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     * Creates a TextTertiary style MaterialButton within the dialog
     */
    fun textButtonTertiary(lambda: KolibreeDialogButton.TextButton<VALUE>.() -> Unit) {
        buttons += KolibreeDialogButton.TextButton(
            context,
            currentValue,
            R.attr.materialButtonTextStyleTertiary
        )
            .apply(lambda)
            .buildWithListener(::addValueListener)
    }

    /**
     *  Creates a Feature image within the dialog
     */
    open fun featureImage(lambda: KolibreeDialogDrawable.() -> Unit) {
        featureImage = KolibreeDialogDrawable(context)
            .apply(lambda)
    }

    /**
     * Creates a Feature icon within the dialog
     */
    open fun featureIcon(lambda: KolibreeDialogDrawable.() -> Unit) {
        featureIcon = KolibreeDialogDrawable(context)
            .apply(lambda)
    }

    /**
     *  Creates a Headline within the dialog
     */
    fun headlineText(lambda: KolibreeDialogHeadline.() -> Unit) {
        headlineText = KolibreeDialogHeadline(context)
            .apply(lambda)
    }

    private fun <T : View> addValueListener(view: T, valueListener: ValueListener<T, VALUE>?) {
        valueListener?.run {
            valueListeners += Invoker(view, valueListener)
        }
    }

    protected fun updateValue(newValue: VALUE) {
        valueListeners.forEach {
            it.invoke(newValue)
        }
    }

    @Suppress("LongMethod")
    override fun build(): AppCompatDialog {
        return MaterialAlertDialogBuilder(context).run {
            setCancelable(cancellable)
            val bodyLayout = LayoutInflater.from(context)
                .inflate(bodyLayout, null) as ConstraintLayout
            val titleTextView: TextView = bodyLayout.safeFind(R.id.title_text)
            val featureImageView: ImageView = bodyLayout.safeFind(R.id.feature_image)
            val featureIconView: ImageView = bodyLayout.safeFind(R.id.feature_icon)
            val headlineTextView: TextView = bodyLayout.safeFind(R.id.headline_text)
            val constraintSet = ConstraintSet().apply { clone(bodyLayout) }
            checkImage()
            checkTitleAndImage()
            titleTextView.setTitle(constraintSet)
            featureImageView.setDrawable(constraintSet, featureImage)
            featureIconView.setDrawable(constraintSet, featureIcon)
            headlineTextView.setHeadline(constraintSet)
            roundDialogCorners(context, background)
            initDialogInset()
            val lastView = doBuild(bodyLayout, constraintSet)
            bodyLayout.addViews(constraintSet, buttons, lastView)
            setView(bodyLayout)
            constraintSet.applyTo(bodyLayout)
            setOnDismissListener(dismissAction)
            create()
        }.apply {
            postCreate()
        }
    }

    protected fun AlertDialog.postCreate() {
        applyFullWidth()
        lifecycleOwner?.get()?.let { owner -> registerToLifecycle(owner) }
        dialog = this
    }

    private fun registerToLifecycle(lifecycleOwner: LifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        lifecycleObserver = object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                lifecycle.removeObserver(this)
                dismiss()
            }
        }.also {
            lifecycle.addObserver(it)
        }
    }

    protected fun MaterialAlertDialogBuilder.initDialogInset() {
        dimensionIntFromAttribute(context, R.attr.alertDialogHorizontalBackgroundInset).also {
            setBackgroundInsetStart(it)
            setBackgroundInsetEnd(it)
        }
    }

    private fun checkImage() {
        if (featureImage != null && featureIcon != null) {
            FailEarly.fail(
                "Defining both a feature image and an logo in a dialog is not supported"
            )
        }
    }

    private fun checkTitleAndImage() {
        val titleDefined = title != null || titleId != UNDEFINED_RESOURCE_ID
        val drawableDefined = featureImage != null || featureIcon != null
        if (titleDefined && drawableDefined) {
            FailEarly.fail(
                "Defining both a title and an image in a dialog is not supported"
            )
        }
    }

    protected fun TextView.setTitle(constraintSet: ConstraintSet) {
        title?.also {
            text = it
            constraintSet.setVisibility(id, View.VISIBLE)
        }
        titleId.takeIf { it != UNDEFINED_RESOURCE_ID }?.also {
            text = context.getText(it)
            constraintSet.setVisibility(id, View.VISIBLE)
        }
    }

    private fun ImageView.setDrawable(
        constraintSet: ConstraintSet,
        drawable: KolibreeDialogDrawable?
    ) {
        drawable?.also {
            setImageDrawable(it.build())
            scaleType = it.scaleType
            constraintSet.setVisibility(id, View.VISIBLE)
        }
    }

    protected fun TextView.setHeadline(constraintSet: ConstraintSet) {
        headlineText?.build()?.also { attrs ->
            text = attrs.text
            gravity = attrs.gravity
            constraintSet.setVisibility(id, View.VISIBLE)
        }
    }

    protected fun roundDialogCorners(context: Context, drawable: Drawable?) {
        val radius = dimensionFromAttribute(context, R.attr.alertDialogCornerRadius)
        if (drawable is MaterialShapeDrawable) {
            drawable.setCornerSize(radius)
        }
    }

    protected fun ConstraintLayout.addViews(
        constraintSet: ConstraintSet,
        views: List<View>,
        firstView: View
    ): View {
        var lastView = firstView
        views.forEach { view ->
            addView(view)
            constraintSet.addView(view, lastView)
            lastView = view
        }
        constraintSet.connect(
            lastView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        return lastView
    }

    private fun ConstraintSet.addView(view: View, lastView: View) {
        view.id = View.generateViewId()
        constrainWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(view.id, ConstraintSet.WRAP_CONTENT)
        connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(view.id, ConstraintSet.TOP, lastView.id, ConstraintSet.BOTTOM)
        connect(lastView.id, ConstraintSet.BOTTOM, view.id, ConstraintSet.TOP)
        setMargin(view.id, ConstraintSet.START, startMargin)
        setMargin(view.id, ConstraintSet.END, endMargin)
        setGoneMargin(view.id, ConstraintSet.TOP, verticalSpacing)
    }

    protected inline fun <reified T : View> ViewGroup.safeFind(@IdRes viewId: Int): T {
        return findViewById<T>(viewId).also {
            if (it == null) {
                FailEarly.fail(
                    "No ${T::class.java.simpleName} found with ID: " +
                        context.resources.getResourceName(viewId)
                )
            }
        }
    }

    protected abstract fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View

    abstract class AlertBase<T>(context: Context, initialValue: T) :
        KolibreeDialogDsl<T>(context, initialValue) {

        @LayoutRes
        override val bodyLayout: Int = R.layout.kolibree_dialog_alert

        var body: CharSequence? = null
            private set

        @StringRes
        var bodyId: Int = UNDEFINED_RESOURCE_ID
            private set

        var gravity: Int = Gravity.START
            private set

        /**
         * Sets the description of the dialog to the specified string
         */
        fun body(body: CharSequence, gravity: Int? = null) {
            this.body = body
            gravity?.let { this.gravity = it }
        }

        /**
         * Sets the description of the dialog to the specified string resource
         */
        fun body(@StringRes bodyId: Int, gravity: Int? = null) {
            this.bodyId = bodyId
            gravity?.let { this.gravity = it }
        }

        @CallSuper
        override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
            val bodyView: TextView = parent.safeFind(R.id.body_text)
            val text =
                bodyId.takeIf { it != UNDEFINED_RESOURCE_ID }?.let { context.getText(it) } ?: body
            if (text?.isNotEmpty() == true) {
                bodyView.text = text
                constraintSet.setVisibility(bodyView.id, View.VISIBLE)
            }
            bodyView.gravity = gravity
            return bodyView
        }
    }

    open class Alert(context: Context) : AlertBase<Unit>(context, Unit)

    open class TextInputDialog(context: Context, initialValue: String) :
        AlertBase<String>(context, initialValue) {

        @LayoutRes
        override val bodyLayout: Int = R.layout.kolibree_dialog_text_input

        private var inputText: KolibreeDialogInputText? = null

        fun textInput(lambda: KolibreeDialogInputText.() -> Unit) {
            inputText = KolibreeDialogInputText(context)
                .apply(lambda)
                .build()
        }

        @CallSuper
        override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
            super.doBuild(parent, constraintSet)
            val textInputView: TextInputLayout = parent.safeFind(R.id.text_input_layout)
            textInputView.hint = inputText?.hintText
            val textInputEditText: TextInputEditText =
                textInputView.safeFind<TextInputEditText>(R.id.text_input_edit_text).apply {
                    text = Editable.Factory.getInstance().newEditable(inputText?.valueText ?: "")
                }
            textInputEditText.doAfterTextChanged { editable ->
                editable?.also {
                    updateValue(editable.toString())
                }
            }
            return textInputView
        }
    }

    class SingleSelect(context: Context) : Alert(context) {

        private val options = mutableListOf<MaterialRadioButton>()

        private var selectAction: (Selection) -> Unit = { /* No-OP */ }

        /**
         * Specifies an action which will be performed whenever the selection changes. The lambda
         * will receive a single Selection argument.
         */
        fun selectAction(lambda: ((Selection) -> Unit)?) {
            selectAction = lambda ?: { /* No-OP */ }
        }

        /**
         * Adds a single-selection item. When the user selects this item all others will be
         * de-selected
         */
        fun selection(lambda: KolibreeSelectionControls.RadioButton<Unit>.() -> Unit) {
            options += KolibreeSelectionControls.RadioButton(context, currentValue)
                .apply(lambda)
                .build()
                .also { radioButton ->
                    radioButton.setOnClickListener {
                        if (radioButton.isChecked) {
                            deselectAllExcept(radioButton)
                        }
                        radioButton.selected()
                    }
                }
        }

        private fun deselectAllExcept(currentSelection: CompoundButton) {
            options
                .filter { it !== currentSelection }
                .forEach { it.isChecked = false }
        }

        private fun CompoundButton.selected() {
            selectAction(Selection(options.indexOf(this), this, text))

            if (hasNoButtons) {
                val animatable = buttonDrawable?.current as? Animatable2
                animatable?.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        super.onAnimationEnd(drawable)
                        animatable.unregisterAnimationCallback(this)
                        dismiss()
                    }
                }) ?: dismiss()
            }
        }

        override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
            var lastView = super.doBuild(parent, constraintSet)
            lastView = parent.addViews(constraintSet, options, lastView)
            return lastView
        }
    }

    class MultiSelect(
        context: Context
    ) : Alert(context) {

        private val options = mutableListOf<MaterialCheckBox>()

        private var selectAction: (List<Selection>) -> Unit = { /* No-OP */ }

        /**
         * Specifies an action which will be performed whenever the selection changes. The lambda
         * will receive a list of Selection instances representing all of the currently
         * selected items.
         */
        fun selectAction(lambda: ((List<Selection>) -> Unit)?) {
            selectAction = lambda ?: { /* No-OP */ }
        }

        /**
         * Adds a multi-selection item.
         */
        fun selection(lambda: KolibreeSelectionControls.CheckBox<Unit>.() -> Unit) {
            options += KolibreeSelectionControls.CheckBox(context, currentValue)
                .apply {
                    lambda()
                }
                .build()
                .also { checkBox ->
                    checkBox.setOnClickListener { _ ->
                        val selected = options
                            .filter { it.isChecked }
                            .map { Selection(options.indexOf(it), it, it.text) }
                        selectAction(selected)
                    }
                }
        }

        override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
            if (hasNoButtons) {
                FailEarly.fail("Mutli-select control requires at least one button")
            }
            var lastView = super.doBuild(parent, constraintSet)
            lastView = parent.addViews(constraintSet, options, lastView)
            return lastView
        }
    }
}

/**
 * DSL to create a simple alert dialog with a title, description, and zero or more buttons.
 * It is the responsibility of the creating code to show the dialog, and to add actions to the
 * buttons to dismiss the dialog. Tapping outside the dialog will dismiss the dialog as is the
 * platform standard. Strings for the title, description, and button text can be specified either
 * as raw Strings (for quick testing / prototyping), or using String resources (for i18n).
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * alertDialog(this) {
 *     featureImage(R.drawable.feature_image)
 *     body("Hello World!")
 *     containedButton {
 *         title("Contained Button")
 *         action { println("Contained Button: Pressed") }
 *      }
 *     iconContainedButton {
 *         title("Contained Button")
 *         icon(R.drawable.ic_nav_close)
 *         action { println("Contained Button: Pressed") }
 *      }
 *     outlinedButton {
 *         title("Outlined Button")
 *         action { println("Outlined Button: Pressed") }
 *     }
 *     iconOutlinedButton {
 *         title("Outlined Button")
 *         icon(R.drawable.ic_nav_close)
 *         action { println("Outlined Button: Pressed") }
 *     }
 * }.show()
 * </pre>
 *
 * We can use an icon instead of an image (that will wrap_content instead of taking all the space)
 * to do that replace featureImage by featureIcon
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instance representing the dialog created
 */
@Keep
fun alertDialog(context: Context, lambda: KolibreeDialogDsl.Alert.() -> Unit): AppCompatDialog =
    KolibreeDialogDsl.Alert(context).apply(lambda).build()

/**
 * DSL to create a simple alert dialog with a title, description, single input text, and zero
 * or more buttons. It is the responsibility of the creating code to show the dialog, and to
 * add actions to the buttons to dismiss the dialog. Tapping outside the dialog will dismiss
 * the dialog as is the platform standard. Strings for the title, description, and button
 * text can be specified either as raw Strings (for quick testing / prototyping), or using
 * String resources (for i18n).
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * textInputDialog(this) {
 *     featureImage(R.drawable.feature_image)
 *     body("Hello World!")
 *     textInputDialog("Initial Value" {
 *         hintText = "Hint Text"
 *     }
 *     containedButton { value ->
 *         title("Contained Button")
 *         action { println("Contained Button: Pressed Input value = $value") }
 *      }
 *     outlinedButton { value ->
 *         title("Outlined Button")
 *         action { println("Outlined Button: Pressed Input value = $value") }
 *     }
 * }.show()
 * </pre>
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param initialValue The initial string with which the EditText will be populated
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instance representing the dialog created
 */
@Keep
fun textInputDialog(
    context: Context,
    initialValue: String,
    lambda: KolibreeDialogDsl.TextInputDialog.() -> Unit
): AppCompatDialog =
    KolibreeDialogDsl.TextInputDialog(context, initialValue).apply(lambda).build()

/**
 * DSL to create a single selection dialog with a title, description, a number of selectable
 * options, and zero or more buttons. It is the responsibility of the creating code to show the
 * dialog, and to add actions to the buttons to dismiss the dialog. Tapping outside the dialog
 * will dismiss the dialog as is the platform standard. If no buttons are specified, then tapping
 * an option will cause the dialog to be dismissed once the RadioButton selection animation has
 * completed (this makes for a nicer UX). Strings for the title, description, and
 * button text can be specified either as raw Strings (for quick testing / prototyping), or using
 * String resources (for i18n). If multiple items have <code>select = true</code> only the last one
 * will actually be selected as this is a single selection control.
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * singleSelectDialog(this) {
 *     title("Test")
 *     body("Hello World!")
 *     selection {
 *         title("Option 1")
 *         selected = true
 *      }
 *     selection {
 *         title("Option 2")
 *      }
 *     containedButton {
 *         title("Contained Button")
 *         action { println("Contained Button: Pressed") }
 *      }
 *     textButton {
 *         title("Text Button")
 *         action { println("Text Button: Pressed") }
 *     }
 * }.show()
 * </pre>
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instace representing the dialog created
 */
@Keep
fun singleSelectDialog(
    context: Context,
    lambda: KolibreeDialogDsl.SingleSelect.() -> Unit
): AppCompatDialog =
    KolibreeDialogDsl.SingleSelect(context).apply(lambda).build()

/**
 * DSL to create a multiple dialog with a title, description, one or more selection items, and one
 * or more buttons. It is the responsibility of the creating code to show the dialog, and to add a
 * ctions to the buttons to dismiss the dialog. Tapping outside the dialog will dismiss the dialog
 * as is the platform standard. Strings for the title, description, and button text can be
 * specified either as raw Strings (for quick testing / prototyping), or using String resources
 * (for i18n).
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * multiSelectDialog(this) {
 *     title("Test")
 *     body("Hello World!")
 *     selection {
 *         title("Option 1")
 *         selected = true
 *      }
 *     selection {
 *         title("Option 2")
 *      }
 *     containedButton {
 *         title("Contained Button")
 *         action { println("Contained Button: Pressed") }
 *      }
 *     textButton {
 *         title("Text Button")
 *         action { println("Text Button: Pressed") }
 *     }
 * }.show()
 * </pre>
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instace representing the dialog created
 */
@Keep
fun multiSelectDialog(
    context: Context,
    lambda: KolibreeDialogDsl.MultiSelect.() -> Unit
): AppCompatDialog =
    KolibreeDialogDsl.MultiSelect(context).apply(lambda).build()

/**
 * DSL to create a duration dialog with a title, description, minute and second picker, and one
 * or more buttons. It is the responsibility of the creating code to show the dialog, and to add
 * actions to the buttons to dismiss the dialog. Tapping outside the dialog will dismiss the dialog
 * as is the platform standard. Strings for the title, description, and button text can be
 * specified either as raw Strings (for quick testing / prototyping), or using String resources
 * (for i18n).
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * durationDialog(this, INITIAL_BRUSHING_TIME) {
 *     title("Duration")
 *     body("Enter your brushing duration")
 *     majorLabel("minutes")
 *     minorLabel("seconds")
 *     setRange(
 *         MINIMUM_BRUSHING_GOAL_TIME_SECONDS,
 *         MAXIMUM_BRUSHING_GOAL_TIME_SECONDS,
 *         BRUSHING_GOAL_TIME_STEP_SECONDS
 *     )
 *     containedButton {
 *         title("Save")
 *         action { value ->
 *             println("Contained Button: Pressed: $value")
 *         }
 *     }
 *     textButton {
 *         title("Cancel")
 *         action { println("Text Button: Pressed") }
 *     }
 * }.show()
 * </pre>
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instance representing the dialog created
 */

@Keep
fun durationDialog(
    context: Context,
    initialValue: Duration,
    lambda: KolbreeDurationPickerDialog.() -> Unit
): AppCompatDialog =
    KolbreeDurationPickerDialog(context, initialValue).apply(lambda).build()

/**
 * DSL to create a birth date dialog with a title, description, month and year picker, and one
 * or more buttons. It is the responsibility of the creating code to show the dialog, and to add
 * actions to the buttons to dismiss the dialog. Tapping outside the dialog will dismiss the dialog
 * as is the platform standard. Strings for the title, description, and button text can be
 * specified either as raw Strings (for quick testing / prototyping), or using String resources
 * (for i18n).
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * birthDateDialog(this, currentBirthDate) {
 *     title("Born")
 *     body("Enter the month and year you were born")
 *     containedButton {
 *         title("Save")
 *         action { value ->
 *             println("Contained Button: Pressed: $value")
 *         }
 *     }
 *     textButton {
 *         title("Cancel")
 *         action { println("Text Button: Pressed") }
 *     }
 * }.show()
 * </pre>
 *
 * @param context The context which will be used to inflate the layout and create the controls. The
 * theme of this context will be applied to the dialog and its control.
 * @param lambda The DSL body where the components are created
 * @return An AppCompatDialog instance representing the dialog created
 */

@Keep
fun birthDateDialog(
    context: Context,
    initialValue: LocalDate?,
    lambda: KolibreeBirthDatePickerDialog.() -> Unit
): AppCompatDialog =
    KolibreeBirthDatePickerDialog(context, initialValue).apply(lambda).build()

// As expected on the screens, our Dialogs should take the space available
internal fun AlertDialog.applyFullWidth() =
    this.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

internal fun AlertDialog.applyWrapWidth() =
    this.window?.setLayout(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
