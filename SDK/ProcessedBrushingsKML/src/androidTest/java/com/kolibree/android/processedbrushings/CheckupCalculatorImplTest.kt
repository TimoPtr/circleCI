/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.TestFeatureToggle
import com.kolibree.kml.Kml
import com.kolibree.kml.MouthZone16
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

class CheckupCalculatorImplTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var checkupCalculator: CheckupCalculatorImpl

    private lateinit var goalDurationFeatureToggle: TestFeatureToggle<Long>

    override fun setUp() {
        super.setUp()
        Kml.init()
        goalDurationFeatureToggle = TestFeatureToggle(CheckupGoalDurationConfigurationFeature, 5000)
        checkupCalculator = CheckupCalculatorImpl(setOf(goalDurationFeatureToggle))
    }

    @Test
    fun calculateFor8ZoneProcessedData_withKML_doesNotCrash() {
        val zone8ProcessedData = """{
      "LoLeExt": {
        "expected_time": 200,
        "passes": [{
          "pass_datetime": 0,
          "effective_time": 27
        }, {
          "pass_datetime": 32,
          "effective_time": 9
        }, {
          "pass_datetime": 47,
          "effective_time": 4
        }, {
          "pass_datetime": 82,
          "effective_time": 18
        }, {
          "pass_datetime": 101,
          "effective_time": 34
        }, {
          "pass_datetime": 161,
          "effective_time": 39
        }]
      },
      "LoRiExt": {
        "expected_time": 200,
        "passes": []
      },
      "UpLeExt": {
        "expected_time": 200,
        "passes": []
      },
      "UpRiExt": {
        "expected_time": 200,
        "passes": []
      },
      "LoLeInt": {
        "expected_time": 100,
        "passes": []
      },
      "LoRiInt": {
        "expected_time": 100,
        "passes": []
      },
      "UpLeInt": {
        "expected_time": 100,
        "passes": []
      },
      "UpRiInt": {
        "expected_time": 100,
        "passes": []
      }
    }"""

        val checkup = checkupCalculator.calculateCheckup(zone8ProcessedData, 1L, Duration.ZERO)

        checkup.surfacePercentage
    }

    @Test
    fun calculateFor12ZoneProcessedData_withKML_doesNotCrash() {
        val zone12ProcessedData = """{
          "LoMolRiInt12": {
            "passes": [
              {
                "pass_datetime": 388,
                "effective_time": 20
              },
              {
                "pass_datetime": 525,
                "effective_time": 0
              },
              {
                "pass_datetime": 525,
                "effective_time": 73
              },
              {
                "pass_datetime": 598,
                "effective_time": 0
              }
            ],
            "expected_time": 70
          },
          "LoMolLeInt12": {
            "passes": [
              {
                "pass_datetime": 323,
                "effective_time": 40
              },
              {
                "pass_datetime": 363,
                "effective_time": 12
              },
              {
                "pass_datetime": 375,
                "effective_time": 13
              },
              {
                "pass_datetime": 408,
                "effective_time": 42
              }
            ],
            "expected_time": 70
          },
          "UpMolRiInt12": {
            "passes": [
              {
                "pass_datetime": 1125,
                "effective_time": 0
              },
              {
                "pass_datetime": 1125,
                "effective_time": 86
              }
            ],
            "expected_time": 70
          },
          "LoMolRiExt12": {
            "passes": [
              {
                "pass_datetime": 150,
                "effective_time": 0
              },
              {
                "pass_datetime": 150,
                "effective_time": 150
              }
            ],
            "expected_time": 150
          },
          "UpIncInt12": {
            "passes": [
              {
                "pass_datetime": 1078,
                "effective_time": 5
              }
            ],
            "expected_time": 70
          },
          "LoMolLeExt12": {
            "passes": [
              {
                "pass_datetime": 0,
                "effective_time": 150
              },
              {
                "pass_datetime": 308,
                "effective_time": 0
              },
              {
                "pass_datetime": 388,
                "effective_time": 0
              },
              {
                "pass_datetime": 598,
                "effective_time": 2
              },
              {
                "pass_datetime": 600,
                "effective_time": 8
              }
            ],
            "expected_time": 150
          },
          "LoIncInt12": {
            "passes": [
              {
                "pass_datetime": 308,
                "effective_time": 15
              },
              {
                "pass_datetime": 450,
                "effective_time": 0
              },
              {
                "pass_datetime": 450,
                "effective_time": 75
              }
            ],
            "expected_time": 70
          },
          "UpMolLeExt12": {
            "passes": [
              {
                "pass_datetime": 613,
                "effective_time": 137
              },
              {
                "pass_datetime": 993,
                "effective_time": 0
              },
              {
                "pass_datetime": 1068,
                "effective_time": 10
              },
              {
                "pass_datetime": 1083,
                "effective_time": 42
              }
            ],
            "expected_time": 150
          },
          "UpIncExt12": {
            "passes": [
              {
                "pass_datetime": 900,
                "effective_time": 0
              },
              {
                "pass_datetime": 900,
                "effective_time": 75
              },
              {
                "pass_datetime": 993,
                "effective_time": 40
              }
            ],
            "expected_time": 70
          },
          "LoIncExt12": {
            "passes": [
              {
                "pass_datetime": 300,
                "effective_time": 0
              },
              {
                "pass_datetime": 300,
                "effective_time": 8
              }
            ],
            "expected_time": 70
          },
          "UpMolLeInt12": {
            "passes": [
              {
                "pass_datetime": 975,
                "effective_time": 0
              },
              {
                "pass_datetime": 975,
                "effective_time": 18
              },
              {
                "pass_datetime": 1033,
                "effective_time": 17
              },
              {
                "pass_datetime": 1050,
                "effective_time": 18
              },
              {
                "pass_datetime": 1083,
                "effective_time": 0
              }
            ],
            "expected_time": 70
          },
          "UpMolRiExt12": {
            "passes": [
              {
                "pass_datetime": 608,
                "effective_time": 5
              },
              {
                "pass_datetime": 750,
                "effective_time": 0
              },
              {
                "pass_datetime": 750,
                "effective_time": 150
              }
            ],
            "expected_time": 150
          }
        }"""

        val checkup = checkupCalculator.calculateCheckup(zone12ProcessedData, 1L, Duration.ZERO)

        checkup.surfacePercentage
    }

    @Test
    fun calculateFor16ZoneProcessedData_withKML() {
        val zone16ProcessedData = """{
      "UpMolRiExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 435
        }]
      },
      "LoIncExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "LoMolRiInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "LoMolLeExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "UpIncInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 92,
          "pass_datetime": 440
        }]
      },
      "LoMolLeOcc": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "UpMolRiOcc": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 185,
          "pass_datetime": 0
        }]
      },
      "LoMolRiExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "UpMolLeExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 5,
          "pass_datetime": 435
        }]
      },
      "UpMolLeOcc": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 245,
          "pass_datetime": 190
        }]
      },
      "UpMolRiInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 5,
          "pass_datetime": 532
        }]
      },
      "UpIncExt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 5,
          "pass_datetime": 185
        }]
      },
      "LoMolLeInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "LoMolRiOcc": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      },
      "UpMolLeInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 11,
          "pass_datetime": 537
        }]
      },
      "LoIncInt": {
        "expected_time": 75,
        "passes": [{
          "effective_time": 0,
          "pass_datetime": 0
        }]
      }
    }"""

        val checkup = checkupCalculator.calculateCheckup(zone16ProcessedData, 1L, Duration.ZERO)

        // TODO investigate KML return 22 not 21
        assertEquals(22, checkup.surfacePercentage)
    }

    @Test
    fun noProcessData_return_emptyMap_in_checkupData_withKML() {
        val dateTime = TrustedClock.getNowOffsetDateTime()
        val expectedDuration = Duration.ofSeconds(1)
        val checkup =
            checkupCalculator.calculateCheckup(
                null,
                dateTime.toEpochSecond(),
                expectedDuration
            )

        assertTrue(checkup.checkupDataMap.isEmpty())
        assertTrue(checkup.zoneSurfaceMap.isEmpty())
        assertEquals(expectedDuration, checkup.duration)
        assertEquals(dateTime.truncatedTo(ChronoUnit.SECONDS), checkup.dateTime)
        assertEquals(CheckupData.NO_AVERAGE_SURFACE, checkup.surfacePercentage)
        MouthZone16.values().forEach { zone ->
            assertEquals(CheckupData.NO_ZONE_SURFACE, checkup.zoneSurface(zone))
        }
    }

    @Test
    fun isValidJsonObject_empty_string_return_false() {
        assertFalse(checkupCalculator.isValidJsonObject(""))
    }

    @Test
    fun isValidJsonObject_null_return_false() {
        assertFalse(checkupCalculator.isValidJsonObject(null))
    }

    @Test
    fun isValidJsonObject_valid_json_object_return_true() {
        assertTrue(checkupCalculator.isValidJsonObject("{}"))
    }

    @Test
    fun isValidJsonObject_invalid_json_object_return_false() {
        assertFalse(checkupCalculator.isValidJsonObject("nfdlfd"))
    }

    @Test
    fun isValidJsonObject_not_json_object_return_false() {
        assertFalse(checkupCalculator.isValidJsonObject("'hello' = 123"))
    }
}
