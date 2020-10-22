package com.kolibree.android.app.test;

import androidx.annotation.NonNull;
import com.kolibree.android.jaws.Kolibree3DModel;
import com.kolibree.android.jaws.MemoryManager;
import com.kolibree.android.jaws.MemoryManagerInternal;
import com.kolibree.android.jaws.coach.renderer.CoachPlusRenderer;
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererFactory;
import com.kolibree.android.jaws.color.ColorJawsModule;
import com.kolibree.android.jaws.color.ColorMouthZones;
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRenderer;
import com.kolibree.android.jaws.hum.HumJawsViewRenderer;
import com.kolibree.android.jaws.opengl.OptimizedVbo;
import com.kolibree.android.jaws.tilt.JawsTiltController;
import com.kolibree.kml.MouthZone16;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Completable;
import io.reactivex.functions.Consumer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.jetbrains.annotations.NotNull;

/**
 * A Espresso tests were crashing emulators because of the 3D jaws
 *
 * <p>This module provides a dummy renderer implementation to prevent espresso tests from rendering
 * the JawsViews content
 */
@Module(includes = ColorJawsModule.class)
public abstract class EspressoJawsModule {

  @Provides
  static CoachPlusRenderer providesCoachPlusRenderer() {
    return new CoachPlusRenderer() {
      @Override
      public void setCurrentlyBrushedZone(@NonNull MouthZone16 zone, int color) {}

      @Override
      public void showToothbrushHead(boolean show) {}

      @Override
      public void setBackgroundColor(int color) {}

      @Override
      public void reset() {}

      @Override
      public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

      @Override
      public void onSurfaceChanged(GL10 gl, int width, int height) {}

      @Override
      public void onDrawFrame(GL10 gl) {}

      @Override
      public void setRingLedColor(int color) {}
    };
  }

  @Provides
  static CoachPlusRendererFactory provideCoachPlusRendererFactory(CoachPlusRenderer renderer) {
    return usePlaqlessAssets -> renderer;
  }

  @Provides
  static MemoryManager provideMemoryManager() {
    return model -> Completable.complete();
  }

  @Provides
  static MemoryManagerInternal provideMemoryManagerInternal() {
    return new MemoryManagerInternal() {
      @Override
      public void lockAndUse(
          @NotNull Kolibree3DModel model, @NotNull Consumer<OptimizedVbo> objectConsumer) {}

      @NotNull
      @Override
      public Completable preloadFromAssets(@NotNull Kolibree3DModel model) {
        return Completable.complete();
      }
    };
  }

  @Provides
  static HumJawsViewRenderer provideHumJawsViewRenderer() {
    return new HumJawsViewRenderer() {
      @Override
      public void colorMouthZones(@NotNull ColorMouthZones colors) {}

      @Override
      public void setTiltController(@NotNull JawsTiltController jawsTiltController) {}

      @NotNull
      @Override
      public HashMap<MouthZone16, Integer> lastMouthZones() {
        return new HashMap<>();
      }

      @Override
      public void pause() {}

      @Override
      public void resume() {}

      @Override
      public void setEglBackgroundColor(int color) {}

      @Override
      public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {}

      @Override
      public void onSurfaceChanged(GL10 gl10, int i, int i1) {}

      @Override
      public void onDrawFrame(GL10 gl10) {}
    };
  }

  @Provides
  static GuidedBrushingJawsViewRenderer provideGuidedBrushingJawsViewRenderer() {
    return new GuidedBrushingJawsViewRenderer() {
      @Override
      public void setCurrentlyBrushedZone(@NotNull MouthZone16 zone, int progress) {}

      @Override
      public void reset() {}

      @NotNull
      @Override
      public AtomicInteger getCurrentZoneColor() {
        return new AtomicInteger();
      }

      @NotNull
      @Override
      public AtomicInteger getMissedZonesColor() {
        return new AtomicInteger();
      }

      @Override
      public void colorMouthZones(@NotNull ColorMouthZones colors) {}

      @Override
      public void setTiltController(@NotNull JawsTiltController jawsTiltController) {}

      @NotNull
      @Override
      public HashMap<MouthZone16, Integer> lastMouthZones() {
        return new HashMap<>();
      }

      @Override
      public void pause() {}

      @Override
      public void resume() {}

      @Override
      public void setEglBackgroundColor(int color) {}

      @Override
      public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {}

      @Override
      public void onSurfaceChanged(GL10 gl10, int i, int i1) {}

      @Override
      public void onDrawFrame(GL10 gl10) {}
    };
  }
}
