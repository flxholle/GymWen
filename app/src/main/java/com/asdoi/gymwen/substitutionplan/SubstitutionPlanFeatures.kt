package com.asdoi.gymwen.substitutionplan

import org.jsoup.nodes.Document
import java.util.*

class PlanUtils {


    companion object {
        fun areArraysEqual(array1: Array<Array<String>>, array2: Array<Array<String>>): Boolean {
            if (array1.size != array2.size)
                return false
            return Arrays.equals(array1, array2)
        }

        fun arePlansEqual(doc1: Document, doc2: Document, today: Boolean = true) {
            var oldFilteredToday: Array<Array<String?>?>? = arrayOfNulls<Array<String?>?>(2)
            var oldFilteredTomorrow: Array<Array<String?>?>? = arrayOfNulls<Array<String?>?>(2)
            oldFilteredToday = SubstitutionPlanFeatures.getTodayArray()
            oldFilteredTomorrow = SubstitutionPlanFeatures.getTomorrowArray()

            //Now

            //Now
//            SubstitutionPlanFeatures.setDocs(now.get(0), now.get(1))
            var nowFilteredToday: Array<Array<String?>?>? = arrayOfNulls<Array<String?>?>(2)
            var nowFilteredTomorrow: Array<Array<String?>?>? = arrayOfNulls<Array<String?>?>(2)
            nowFilteredToday = SubstitutionPlanFeatures.getTodayArray()
            nowFilteredTomorrow = SubstitutionPlanFeatures.getTomorrowArray()

            /*if (oldFilteredToday == null || oldFilteredTomorrow == null || nowFilteredToday == null || nowFilteredTomorrow == null) *//*return false*//* //No internet
*/
        }
    }
}