package com.kolibree.android.sba.testbrushing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.activity.KolibreeServiceActivity
import com.kolibree.android.app.utils.keepScreenOn
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreator
import com.kolibree.android.sba.testbrushing.results.TestBrushingModelsLoader
import com.kolibree.android.translationssupport.Translations
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

@VisibleForApp
class TestBrushingActivity : KolibreeServiceActivity(),
    HasAndroidInjector,
    TestBrushingNavigator {

    @Inject
    internal lateinit var brushingCreator: TestBrushingCreator

    @Inject
    internal lateinit var modelsLoader: TestBrushingModelsLoader

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_brushing)

        if (savedInstanceState == null) {
            val navHost = NavHostFragment.create(R.navigation.testbrushing_nav_graph, null)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, navHost)
                .setPrimaryNavigationFragment(navHost)
                .commit()
        }

        lifecycle.addObserver(brushingCreator)
        lifecycle.addObserver(modelsLoader)
        keepScreenOn()
    }

    override fun androidInjector() = fragmentInjector

    override fun navigateToProgressScreen() =
        navigateTo(R.id.action_optimizeAnalysisFragment_to_testBrushProgressFragment)

    override fun navigateToDuringSessionScreen() =
        navigateTo(R.id.action_testBrushSessionFragment_to_testBrushingDuringSessionFragment)

    override fun navigateToSessionScreen() =
        navigateTo(R.id.action_testBrushIntroFragment_to_testBrushingSessionFragment)

    override fun navigateToOptimizeAnalysisScreen() =
        navigateTo(R.id.action_testBrushingDuringSessionFragment_to_optimizeAnalysisFragment)

    override fun navigateToResultsScreen() {
        val isPlaqlessResultScreen = extractModel().isPlaqless
        if (isPlaqlessResultScreen) {
            navigateTo(R.id.action_testBrushProgressFragment_to_mouthMapFragment)
        } else {
            navigateTo(R.id.action_testBrushProgressFragment_to_resultsFragment)
        }
    }

    override fun finishScreen() = finish()

    private fun navigateTo(actionId: Int) {
        val nav = Navigation.findNavController(this, R.id.fragment_container)
        nav.addOnDestinationChangedListener { _, destination, _ -> supportActionBar?.title = destination.label }
        nav.navigateSafe(actionId)
    }

    fun extractMac(): Optional<String> = Optional.of(requireNotNull(
        intent.getStringExtra(EXTRA_TOOTHBRUSH_MAC),
        { "Test brushing doesn't support manual mode, you need to provide TB MAC address" }
    ))

    fun extractModel(): ToothbrushModel {
        val modelName = intent.getStringExtra(EXTRA_TOOTHBRUSH_MODEL)
        return requireNotNull(
            ToothbrushModel.getModelByInternalName(modelName),
            { "Test brushing doesn't support manual mode, you need to provide TB model" }
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Translations.wrapContext(newBase))
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context, mac: String, model: ToothbrushModel): Intent {
            val intent = Intent(context, TestBrushingActivity::class.java)
            intent.putExtra(EXTRA_TOOTHBRUSH_MODEL, model.internalName)
            intent.putExtra(EXTRA_TOOTHBRUSH_MAC, mac)
            return intent
        }

        val TAG = TestBrushingActivity::class.java.simpleName
        val EXTRA_TOOTHBRUSH_MAC = "${TAG}_EXTRA_TOOTHBRUSH_MAC"
        val EXTRA_TOOTHBRUSH_MODEL = "${TAG}_EXTRA_TOOTHBRUSH_MODEL"
    }
}
