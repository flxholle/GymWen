package com.asdoi.gymwen.main.Fragments;


import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asdoi.gymwen.R;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImpressumFragment extends Fragment {
    private static View root;
    private static String datenschutz;

//
//            "<h1>Datenschutzerklärung</h1>\n" +
//            "<p>Verantwortlicher im Sinne der Datenschutzgesetze, insbesondere der EU-Datenschutzgrundverordnung (DSGVO), ist:</p>\n" +
//            "<p>Name einfügen...</p>\n" +
//            "<br>"+
//            "<h2>Ihre Betroffenenrechte</h2>\n" +
//            "<p>Unter den angegebenen Kontaktdaten unseres Datenschutzbeauftragten können Sie jederzeit folgende Rechte ausüben:</p>\n" +
//            "<ul>\n" +
//            "<li>Auskunft über Ihre bei uns gespeicherten Daten und deren Verarbeitung (Art. 15 DSGVO),</li>\n" +
//            "<li>Berichtigung unrichtiger personenbezogener Daten (Art. 16 DSGVO),</li>\n" +
//            "<li>Löschung Ihrer bei uns gespeicherten Daten (Art. 17 DSGVO),</li>\n" +
//            "<li>Einschränkung der Datenverarbeitung, sofern wir Ihre Daten aufgrund gesetzlicher Pflichten noch nicht löschen dürfen (Art. 18 DSGVO),</li>\n" +
//            "<li>Widerspruch gegen die Verarbeitung Ihrer Daten bei uns (Art. 21 DSGVO) und</li>\n" +
//            "<li>Datenübertragbarkeit, sofern Sie in die Datenverarbeitung eingewilligt haben oder einen Vertrag mit uns abgeschlossen haben (Art. 20 DSGVO).</li>\n" +
//            "</ul>\n" +
//            "<p>Sofern Sie uns eine Einwilligung erteilt haben, können Sie diese jederzeit mit Wirkung für die Zukunft widerrufen.</p>\n" +
//            "<p>Sie können sich jederzeit mit einer Beschwerde an eine Aufsichtsbehörde wenden, z. B. an die zuständige Aufsichtsbehörde des Bundeslands Ihres Wohnsitzes oder an die für uns als verantwortliche Stelle zuständige Behörde.</p>\n" +
//            "<p>Eine Liste der Aufsichtsbehörden (für den nichtöffentlichen Bereich) mit Anschrift finden Sie unter: <a href=\"https://www.bfdi.bund.de/DE/Infothek/Anschriften_Links/anschriften_links-node.html\" target=\"_blank\" rel=\"nofollow noopener\">https://www.bfdi.bund.de/DE/Infothek/Anschriften_Links/anschriften_links-node.html</a>.</p>\n" +
//            "<br>" +
//            "<p></p><h2>Änderung unserer Datenschutzbestimmungen</h2>\n" +
//            "<p>Wir behalten uns vor, diese Datenschutzerklärung anzupassen, damit sie stets den aktuellen rechtlichen Anforderungen entspricht oder um Änderungen unserer Leistungen in der Datenschutzerklärung umzusetzen, z.B. bei der Einführung neuer Services. Für Ihren erneuten Besuch gilt dann die neue Datenschutzerklärung.</p>\n" +
//            "<br>" +
//            "<h2>Fragen an den Datenschutzbeauftragten</h2>\n" +
//            "<p>Wenn Sie Fragen zum Datenschutz haben, schreiben Sie uns bitte eine E-Mail oder wenden Sie sich direkt an die für den Datenschutz verantwortliche Person in unserer Organisation:</p>\n" +
//            "Name einfügen..."+
//            "<p><em>Die Datenschutzerklärung wurde mithilfe der activeMind AG erstellt, den Experten für <a href=\"https://www.activemind.de/datenschutz/datenschutzhinweis-generator/\" target=\"_blank\" rel=\"noopener\">externe Datenschutzbeauftragte</a> (Version #2019-04-10).</em></p>";


    public ImpressumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_impressum, container, false);
        datenschutz = getString(R.string.privacy);
        TextView textView = root.findViewById(R.id.impressum_datenschutzerklärung);

        textView.setText(Html.fromHtml(datenschutz, Html.FROM_HTML_MODE_COMPACT));

        return root;
    }

}
