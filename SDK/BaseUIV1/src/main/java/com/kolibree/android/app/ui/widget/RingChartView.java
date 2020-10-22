/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import com.kolibree.android.baseui.v1.R;

/**
 * Ring chart view.
 *
 * <p>Use the following attribute in XML declaration
 *
 * <p>thickness: ring thickness in dp ringColor: filled ring color (foreground) ringBackgroundColor:
 * background color of the ring
 */
@Keep
public final class RingChartView extends View {

  /** Default size if not specified (in DP). */
  private static final float DEFAULT_SIZE = 120f;

  /** Lifetime paint. */
  private final Paint paint;

  /** View's drawing area. */
  private final RectF drawingArea;

  /** Foreground ring color. */
  @ColorInt private int ringColor;

  /** Non-drawn ring color. */
  @ColorInt private int ringBackgroundColor;

  /** Thickness of the ring in pixels. */
  private float ringThickness;

  /** Ring angle in degrees. */
  private float ringAngle;

  /**
   * XML declaration constructor.
   *
   * @param context non null {@link Context}
   * @param attrs nullable {@link AttributeSet}
   */
  public RingChartView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    parseAttributes(context, attrs);

    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Paint.Cap.ROUND);
    drawingArea = new RectF();
  }

  /**
   * Get the ring foreground color.
   *
   * @return ColorInt int color
   */
  @VisibleForTesting
  @ColorInt
  public int getRingColor() {
    return ringColor;
  }

  /**
   * Set the ring foreground color.
   *
   * @param ringColor @{@link ColorInt} color
   */
  public void setRingColor(int ringColor) {
    this.ringColor = ringColor;
    postInvalidate();
  }

  public void useDefaultColors(int percentage) {
    final int perfectPercentage = 100;
    Context context = getContext();
    if (context != null) {
      final boolean isPerfect = percentage >= perfectPercentage;
      int colorRes = isPerfect ? R.color.metric_perfect_smooth : R.color.metric_average;
      setRingColor(ContextCompat.getColor(context, colorRes));
    }
  }

  /**
   * Set the ring background color.
   *
   * @param ringBackgroundColor @{@link ColorInt} color
   */
  public void setRingBackgroundColor(int ringBackgroundColor) {
    this.ringBackgroundColor = ringBackgroundColor;
    postInvalidate();
  }

  /**
   * Set ring thickness in DP.
   *
   * @param ringThickness @{@link Dimension} thickness
   */
  public void setRingThickness(@Dimension float ringThickness) {
    this.ringThickness = getResources().getDisplayMetrics().density * ringThickness;
    postInvalidate();
  }

  /**
   * Set the ring angle in percents <code>percents</code>'s value is truncated to 100.
   *
   * @param percents foreground ring coverage in percent
   */
  public void setRingCoverage(int percents) {
    ringAngle = Math.min(100, percents) * 3.6f;
    postInvalidate();
  }

  public void animateRingCoverage(int percents) {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(
        animation -> {
          float progress = (float) animation.getAnimatedValue();
          float currentValue = progress * percents;
          setRingCoverage((int) currentValue);
        });
    animator.setStartDelay(100);
    animator.start();
  }

  @VisibleForTesting
  public float getRingAngle() {
    return ringAngle;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Draw background
    super.onDraw(canvas);

    updateDrawingArea();

    // Draw ring's background
    paint.setStrokeWidth(ringThickness);
    paint.setColor(ringBackgroundColor);
    canvas.drawArc(drawingArea, 0f, 360f, false, paint);

    // Draw ring's foreground
    paint.setColor(ringColor);
    canvas.drawArc(drawingArea, 270f, ringAngle, false, paint);
  }

  @SuppressWarnings("SuspiciousNameCombination")
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int desiredWidth = parseMeasureSpec(widthMeasureSpec);
    final int desiredHeight = parseMeasureSpec(heightMeasureSpec);

    if (desiredWidth == -1 && desiredHeight == -1) {
      final int defaultSize = getDefaultSizePixel();
      setMeasuredDimension(defaultSize, defaultSize);
    } else if (desiredWidth == -1) {
      setMeasuredDimension(desiredHeight, desiredHeight);
    } else if (desiredHeight == -1) {
      setMeasuredDimension(desiredWidth, desiredWidth);
    } else {
      setMeasuredDimension(desiredWidth, desiredHeight);
    }
  }

  /**
   * Parse a measure spec and compute a desired size.
   *
   * @param measureSpec int measure spec
   * @return int size in pixel or -1 if the mode is wrap_content
   */
  private int parseMeasureSpec(int measureSpec) {
    final int mode = MeasureSpec.getMode(measureSpec);
    final int desiredMeasure = MeasureSpec.getSize(measureSpec);

    if (mode == EXACTLY) {
      return desiredMeasure;
    } else if (mode == AT_MOST) {
      return Math.min(getDefaultSizePixel(), desiredMeasure);
    }

    return -1;
  }

  /**
   * Get the default size of the view sides.
   *
   * @return default size in pixels
   */
  private int getDefaultSizePixel() {
    return (int) (DEFAULT_SIZE * getResources().getDisplayMetrics().density);
  }

  /**
   * Parse attributes and set default ones if missing.
   *
   * @param context non null {@link Context}
   * @param attrs nullable {@link AttributeSet}
   */
  private void parseAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    final int defaultThickness =
        (int) (context.getResources().getDisplayMetrics().density * 12f); // 12 dp
    final @ColorInt int defaultRingColor =
        ContextCompat.getColor(context, android.R.color.primary_text_dark);
    final @ColorInt int defaultBackgroundRingColor =
        ContextCompat.getColor(context, android.R.color.background_light);

    if (attrs != null) {
      final TypedArray attributes =
          context.getTheme().obtainStyledAttributes(attrs, R.styleable.RingChartView, 0, 0);

      ringThickness =
          attributes.getDimensionPixelSize(R.styleable.RingChartView_thickness, defaultThickness);
      ringColor = attributes.getColor(R.styleable.RingChartView_ringColor, defaultRingColor);
      ringBackgroundColor =
          attributes.getColor(
              R.styleable.RingChartView_ringBackgroundColor, defaultBackgroundRingColor);

      attributes.recycle();
    } else {
      ringThickness = defaultThickness;
      ringColor = defaultRingColor;
      ringBackgroundColor = defaultBackgroundRingColor;
    }
  }

  /** Update the drawing area according to the ring's thickness and padding. */
  private void updateDrawingArea() {
    final float halfThickness = ringThickness / 2f;
    final float intrinsicDiameter =
        Math.min(
            getWidth() - getPaddingStart() - getPaddingEnd(),
            getHeight() - getPaddingTop() - getPaddingBottom());

    drawingArea.set(
        (getWidth() - intrinsicDiameter) / 2 + halfThickness,
        (getHeight() - intrinsicDiameter) / 2 + halfThickness,
        getWidth() - (getWidth() - intrinsicDiameter) / 2 - halfThickness,
        getHeight() - (getHeight() - intrinsicDiameter) / 2 - halfThickness);
  }
}
