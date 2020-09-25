package com.asdoi.gymwen.substitutionplan

import androidx.preference.PreferenceManager
import com.asdoi.gymwen.ApplicationFeatures
import org.jsoup.Jsoup

object MainSubstitutionPlan : SubstitutionPlan("1A") {
    fun changeCourses(courses: Array<String>) {
        this.courses = courses
    }

    fun getInstance(courses: Array<String>): SubstitutionPlan {
        init()
        val substitutionPlan = SubstitutionPlan(courses)
        substitutionPlan.setDocuments(todayDocument, tomorrowDocument)
        return substitutionPlan
    }

    fun getInstance(): SubstitutionPlan = getInstance(courses)

    override fun getDay(today: Boolean): SubstitutionList? {
        init()
        return super.getDay(today)
    }

    fun init() {
        if (!areDocumentsSet())
            reload()
    }

    fun save() {
        if (!areDocumentsSet())
            return

        val prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit()
        val doc: String = todayDocument.toString()
        prefsEditor.putString("doc1", doc)

        val doc2: String = tomorrowDocument.toString()
        prefsEditor.putString("doc2", doc2)

        prefsEditor.commit()
    }

    fun reload() {
        try {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext())
            val docString1 = sharedPref.getString("doc1", "")!!
            val doc1 = Jsoup.parse(docString1)!!
            val docString2 = sharedPref.getString("doc2", "")!!
            val doc2 = Jsoup.parse(docString2)!!
            if (docString1.isNotBlank() && docString2.isNotBlank()) {
                setDocuments(doc1, doc2)
            }
        } catch (ignore: Exception) {
        }
    }
}