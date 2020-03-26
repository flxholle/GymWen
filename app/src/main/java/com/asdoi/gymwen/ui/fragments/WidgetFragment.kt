/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RemoteViews
import androidx.fragment.app.Fragment
import com.asdoi.gymwen.R
import com.asdoi.gymwen.widgets.SubstitutionWidgetService


class WidgetFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return createWidgetView()
    }

    private fun createWidgetView(): View {

//        remoteViews.setInt(R.id.widget2_frame, "setBackgroundColor", SubstitutionWidgetProvider.backgroundColor)
        val parent = FrameLayout(context!!)

        val remoteViews = RemoteViews(context!!.packageName, R.layout.widget_substitution)
        val intent = Intent(context, SubstitutionWidgetService::class.java)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setRemoteAdapter(R.id.widget_substitution_listview, intent)

        val view: View = remoteViews.apply(activity, parent)
        parent.addView(view)
        view.findViewById<ImageButton>(R.id.widget_substitution_open_button).visibility = View.GONE
        view.findViewById<ImageButton>(R.id.widget_substiution_refresh_button).visibility = View.GONE
        return parent
    }


}