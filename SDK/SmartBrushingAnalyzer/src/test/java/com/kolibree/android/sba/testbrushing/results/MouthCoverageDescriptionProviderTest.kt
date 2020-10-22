package com.kolibree.android.sba.testbrushing.results

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.sba.R
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test

internal class MouthCoverageDescriptionProviderTest : BaseUnitTest() {

    private val context = mock<Context>()

    private lateinit var provider: MouthCoverageDescriptionProvider

    override fun setup() {
        super.setup()

        provider = spy(MouthCoverageDescriptionProvider(context))
    }

    @Test
    fun `for coverage 100% method returns string resource mouth_coverage_perfect`() {
        Assert.assertEquals(R.string.mouth_coverage_perfect, provider.mouthCoverageCardDescription(100))
    }

    @Test
    fun `for coverage 0% method returns string resource mouth_coverage_no_data`() {
        Assert.assertEquals(R.string.mouth_coverage_no_data, provider.mouthCoverageCardDescription(0))
    }

    @Test
    fun `for coverage between 85% and 100% method returns string resource mouth_coverage_good`() {
        Assert.assertNotEquals(R.string.mouth_coverage_good, provider.mouthCoverageCardDescription(84))
        Assert.assertEquals(R.string.mouth_coverage_good, provider.mouthCoverageCardDescription(85))
        Assert.assertEquals(R.string.mouth_coverage_good, provider.mouthCoverageCardDescription(99))
        Assert.assertNotEquals(R.string.mouth_coverage_good, provider.mouthCoverageCardDescription(100))
    }

    @Test
    fun `for coverage between 50% and 85% method returns string resource mouth_coverage_medium`() {
        Assert.assertNotEquals(R.string.mouth_coverage_medium, provider.mouthCoverageCardDescription(49))
        Assert.assertEquals(R.string.mouth_coverage_medium, provider.mouthCoverageCardDescription(50))
        Assert.assertEquals(R.string.mouth_coverage_medium, provider.mouthCoverageCardDescription(70))
        Assert.assertEquals(R.string.mouth_coverage_medium, provider.mouthCoverageCardDescription(84))
        Assert.assertNotEquals(R.string.mouth_coverage_medium, provider.mouthCoverageCardDescription(85))
    }

    @Test
    fun `for coverage between 1% and 50% method returns string resource mouth_coverage_bad`() {
        Assert.assertNotEquals(R.string.mouth_coverage_bad, provider.mouthCoverageCardDescription(0))
        Assert.assertEquals(R.string.mouth_coverage_bad, provider.mouthCoverageCardDescription(1))
        Assert.assertEquals(R.string.mouth_coverage_bad, provider.mouthCoverageCardDescription(40))
        Assert.assertEquals(R.string.mouth_coverage_bad, provider.mouthCoverageCardDescription(49))
        Assert.assertNotEquals(R.string.mouth_coverage_bad, provider.mouthCoverageCardDescription(50))
    }

    @Test
    fun `description invokes getString on context object`() {
        val coverage = 99
        val results = BrushingResults(coverage = coverage)
        val resId = 1234
        val text = "description"
        whenever(context.getString(resId)).thenReturn(text)
        doReturn(resId).whenever(provider).mouthCoverageCardDescription(coverage)

        val description = provider.description(results)

        verify(context).getString(resId)
        Assert.assertEquals(text, description)
    }

    @Test
    fun `description invokes mouthCoverageCardDescription`() {
        val coverage = 99
        val results = BrushingResults(coverage = coverage)
        val resId = 1234
        val text = "description"
        whenever(context.getString(resId)).thenReturn(text)
        doReturn(resId).whenever(provider).mouthCoverageCardDescription(coverage)

        provider.description(results)

        verify(provider).mouthCoverageCardDescription(coverage)
    }
}
