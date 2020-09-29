package com.asdoi.gymwen.teacherlist

import androidx.preference.PreferenceManager
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.util.External_Const
import com.asdoi.gymwen.util.PreferenceUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object MainTeacherList {
    var document: Document? = null
        set(value) {
            teacherList = value?.let { parseList(it) }
            field = value
        }

    var teacherList: TeacherList? = null
        get() {
            init()
            return field
        }
        private set

    private fun parseList(document: Document): TeacherList? {
        return Parse.parseTeacherList(document)
    }

    fun isDocumentSet() = document != null

    fun init() {
        if (!isDocumentSet() && PreferenceUtil.isOfflineMode())
            reload()
    }

    fun reload() {
        try {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext())
            val docString = sharedPref.getString("docList", "")!!
            if (docString.isNotBlank())
                document = Jsoup.parse(docString)!!

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save() {
        if (!isDocumentSet())
            return

        val prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit()
        prefsEditor.putString("docList", document.toString())
        prefsEditor.commit()
    }

    fun isAOL(query: String) = query == External_Const.AOLShort
}