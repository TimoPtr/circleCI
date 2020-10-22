/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kolibree.android.glimmer.R;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.LimitExceededListener;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.Listener.DefaultLimitExceededListener;
import com.travijuu.numberpicker.library.Listener.DefaultValueChangedListener;
import timber.log.Timber;

/** Fork of Created by travijuu on 26/05/16. */
public class NumberPicker extends LinearLayout {

  // default values
  private final int DEFAULT_MIN = 0;
  private final int DEFAULT_MAX = 999999;
  private final int DEFAULT_VALUE = 1;
  private final int DEFAULT_UNIT = 1;
  private final int DEFAULT_LAYOUT = R.layout.number_picker_layout;
  private final boolean DEFAULT_FOCUSABLE = false;

  // required variables
  private int minValue;
  private int maxValue;
  private int unit;
  private int currentValue;
  private int layout;
  private boolean focusable;

  // ui components
  private Context mContext;
  private Button decrementButton;
  private Button incrementButton;
  private EditText displayEditText;

  // listeners
  private LimitExceededListener limitExceededListener;
  private ValueChangedListener valueChangedListener;

  public NumberPicker(Context context) {
    super(context, null);
  }

  public NumberPicker(Context context, AttributeSet attrs) {
    super(context, attrs);

    this.initialize(context, attrs);
  }

  public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void initialize(Context context, AttributeSet attrs) {
    TypedArray attributes =
        context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPicker, 0, 0);

    // set required variables with values of xml layout attributes or default ones
    this.minValue = attributes.getInteger(R.styleable.NumberPicker_min, this.DEFAULT_MIN);
    this.maxValue = attributes.getInteger(R.styleable.NumberPicker_max, this.DEFAULT_MAX);
    this.currentValue = attributes.getInteger(R.styleable.NumberPicker_value, this.DEFAULT_VALUE);
    this.unit = attributes.getInteger(R.styleable.NumberPicker_unit, this.DEFAULT_UNIT);
    this.layout =
        attributes.getResourceId(R.styleable.NumberPicker_custom_layout, this.DEFAULT_LAYOUT);
    this.focusable =
        attributes.getBoolean(R.styleable.NumberPicker_focusable, this.DEFAULT_FOCUSABLE);
    this.mContext = context;

    // if current value is greater than the max. value, decrement it to the max. value
    this.currentValue = this.currentValue > this.maxValue ? maxValue : currentValue;

    // if current value is less than the min. value, decrement it to the min. value
    this.currentValue = this.currentValue < this.minValue ? minValue : currentValue;

    // set layout view
    LayoutInflater.from(this.mContext).inflate(layout, this, true);

    // init ui components
    this.decrementButton = (Button) findViewById(R.id.decrement);
    this.incrementButton = (Button) findViewById(R.id.increment);
    this.displayEditText = (EditText) findViewById(R.id.display);

    // register button click and action listeners
    this.incrementButton.setOnClickListener(
        new ActionListener(this, this.displayEditText, ActionEnum.INCREMENT));
    this.decrementButton.setOnClickListener(
        new ActionListener(this, this.displayEditText, ActionEnum.DECREMENT));
    this.displayEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no-op
          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            int newValue = 0;

            try {
              newValue = Integer.parseInt(s.toString());
            } catch (NumberFormatException e) {
              Timber.e(e);
            }

            if (!valueIsAllowed(newValue)) {
              return;
            }
            currentValue = newValue;
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    // init listener for exceeding upper and lower limits
    this.setLimitExceededListener(new DefaultLimitExceededListener());
    // init listener for increment&decrement
    this.setValueChangedListener(new DefaultValueChangedListener());
    // init listener for focus change
    this.setOnFocusChangeListener(new DefaultOnFocusChangeListener(this));
    // init listener for done action in keyboard
    this.setOnEditorActionListener(new DefaultOnEditorActionListener(this));

    // set default display mode
    this.setDisplayFocusable(this.focusable);

    // update ui view
    this.refresh();
  }

  public void refresh() {
    this.displayEditText.setText(Integer.toString(this.currentValue));
  }

  public void clearFocus() {
    this.displayEditText.clearFocus();
  }

  public boolean valueIsAllowed(int value) {
    return (value >= this.minValue && value <= this.maxValue);
  }

  public void setMin(int value) {
    this.minValue = value;
  }

  public void setMax(int value) {
    this.maxValue = value;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public int getUnit() {
    return this.unit;
  }

  public int getMin() {
    return this.minValue;
  }

  public int getMax() {
    return this.maxValue;
  }

  public void setValue(int value) {
    if (!this.valueIsAllowed(value)) {
      this.limitExceededListener.limitExceeded(
          value < this.minValue ? this.minValue : this.maxValue, value);
      return;
    }

    this.currentValue = value;
    this.refresh();
  }

  public int getValue() {
    return this.currentValue;
  }

  public void setLimitExceededListener(LimitExceededListener limitExceededListener) {
    this.limitExceededListener = limitExceededListener;
  }

  public LimitExceededListener getLimitExceededListener() {
    return this.limitExceededListener;
  }

  public void setValueChangedListener(ValueChangedListener valueChangedListener) {
    this.valueChangedListener = valueChangedListener;
  }

  public ValueChangedListener getValueChangedListener() {
    return this.valueChangedListener;
  }

  public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
    this.displayEditText.setOnEditorActionListener(onEditorActionListener);
  }

  public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
    this.displayEditText.setOnFocusChangeListener(onFocusChangeListener);
  }

  public void setActionEnabled(ActionEnum action, boolean enabled) {
    if (action == ActionEnum.INCREMENT) {
      this.incrementButton.setEnabled(enabled);
    } else if (action == ActionEnum.DECREMENT) {
      this.decrementButton.setEnabled(enabled);
    }
  }

  public void setDisplayFocusable(boolean focusable) {
    this.displayEditText.setFocusable(focusable);

    // required for making EditText focusable
    if (focusable) {
      this.displayEditText.setFocusableInTouchMode(true);
    }
  }

  public void increment() {
    this.changeValueBy(this.unit);
  }

  public void increment(int unit) {
    this.changeValueBy(unit);
  }

  public void decrement() {
    this.changeValueBy(-this.unit);
  }

  public void decrement(int unit) {
    this.changeValueBy(-unit);
  }

  private void changeValueBy(int unit) {
    int oldValue = this.getValue();

    this.setValue(this.currentValue + unit);

    if (oldValue != this.getValue()) {
      this.valueChangedListener.valueChanged(
          this.getValue(), unit > 0 ? ActionEnum.INCREMENT : ActionEnum.DECREMENT);
    }
  }

  public static class ActionListener implements View.OnClickListener {

    NumberPicker layout;
    ActionEnum action;
    EditText display;

    public ActionListener(NumberPicker layout, EditText display, ActionEnum action) {
      this.layout = layout;
      this.action = action;
      this.display = display;
    }

    @Override
    public void onClick(View v) {
      try {
        int newValue = Integer.parseInt(this.display.getText().toString());

        if (!this.layout.valueIsAllowed(newValue)) {
          return;
        }

        this.layout.setValue(newValue);
      } catch (NumberFormatException e) {
        this.layout.refresh();
      }

      switch (this.action) {
        case INCREMENT:
          this.layout.increment();
          break;
        case DECREMENT:
          this.layout.decrement();
          break;
      }
    }
  }

  public static class DefaultOnFocusChangeListener implements View.OnFocusChangeListener {

    NumberPicker layout;

    public DefaultOnFocusChangeListener(NumberPicker layout) {
      this.layout = layout;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
      EditText editText = (EditText) v;

      if (!hasFocus) {
        try {
          int value = Integer.parseInt(editText.getText().toString());
          layout.setValue(value);

          if (layout.getValue() == value) {
            layout.getValueChangedListener().valueChanged(value, ActionEnum.MANUAL);
          } else {
            layout.refresh();
          }
        } catch (NumberFormatException e) {
          layout.refresh();
        }
      }
    }
  }

  public static class DefaultOnEditorActionListener implements TextView.OnEditorActionListener {

    NumberPicker layout;

    public DefaultOnEditorActionListener(NumberPicker layout) {
      this.layout = layout;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        try {
          int value = Integer.parseInt(v.getText().toString());

          layout.setValue(value);

          if (layout.getValue() == value) {
            layout.getValueChangedListener().valueChanged(value, ActionEnum.MANUAL);
            return false;
          }
        } catch (NumberFormatException e) {
          layout.refresh();
        }
      }
      return true;
    }
  }
}
