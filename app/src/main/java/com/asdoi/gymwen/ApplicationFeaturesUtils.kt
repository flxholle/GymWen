package com.asdoi.gymwen

import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures


class ApplicationFeaturesUtils {

    companion object {
        class CreateMainNotification(val title: String, val content: Array<Array<String>>) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {

            override fun onPostExecute(v: Void?) {
                super.onPostExecute(v)
                try {
                    if (ProfileManagement.isUninit())
                        ProfileManagement.reload()
                    if (!ApplicationFeatures.coursesCheck(false))
                        return
                    if (SubstitutionPlanFeatures.getTodayTitle() == ApplicationFeatures.getContext().getString(R.string.noInternetConnection)) {
                        return
                    }
                    sendNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fun sendNotification() {
            }
        }
    }
}