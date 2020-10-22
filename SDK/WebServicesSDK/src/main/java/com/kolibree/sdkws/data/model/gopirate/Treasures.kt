package com.kolibree.sdkws.data.model.gopirate

import androidx.annotation.Keep

@Keep
class Treasures : ArrayList<Int>() {
    companion object {
        @JvmStatic
        fun fromList(treasuresList: List<Int>): Treasures {
            val treasures = Treasures()

            treasures.addAll(treasuresList)

            return treasures
        }
    }
}
