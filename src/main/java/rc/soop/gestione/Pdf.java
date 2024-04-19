/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.gestione;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.forms.PdfAcroForm;
import static com.itextpdf.forms.PdfAcroForm.getAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.constants.StandardFonts;
import static com.itextpdf.kernel.colors.ColorConstants.BLACK;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import static java.lang.Math.toRadians;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import static rc.soop.exe.Utils.createDir;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.gestione.Constant.checkPDF;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author Administrator
 */
public class Pdf {

    public static File MODELLO7_UD(
            Db_Gest db0,
            Long idallievo,
            Map<String, String[]> tipoud,
            List<String> li_A,
            List<String> li_B,
            DateTime dataconsegna,
            boolean flatten) {
        return MODELLO7_UD_BASE(db0, idallievo, tipoud, li_A, li_B, dataconsegna, flatten);
    }

    public static File MODELLO7_OK(
            Db_Gest db0,
            Long idallievo,
            Map<String, String> moduli,
            DateTime dataconsegna,
            boolean flatten) {
        return MODELLO7_OK_BASE(db0, idallievo, moduli, dataconsegna, flatten);
    }

    public static File MODELLO7_UD11(
            Db_Gest db0,
            Long idallievo,
            String A_UD11_MOD,
            DateTime dataconsegna,
            boolean flatten) {
        return MODELLO7_UD11_BASE(db0, idallievo, A_UD11_MOD, dataconsegna, flatten);
    }

    private static File MODELLO7_OK_BASE(
            Db_Gest db0,
            Long idallievo,
            Map<String, String> moduli,
            DateTime dataconsegna,
            boolean flatten) {
        try {

            String codicefiscale = "";
            String nome = "";
            String cognome = "";
            String nascita_data = "";
            String nascita_comune = "";
            String nascita_provincia = "";
            String NOMESA = "";
            String idsa = "";
            String SEDE = "";
            String CIP = "";
            String DATAINIZIO = "";
            String DATAFINE = "";

            String sql0 = "SELECT a.idsoggetto_attuatore,a.codicefiscale,a.nome,a.cognome,c.nome,c.cod_provincia,a.datanascita,s.ragionesociale,p.sedefisica,p.cip,a.data_inizio_UD11,a.data_fine_UD11,p.start,p.end "
                    + "FROM allievi a, soggetti_attuatori s, progetti_formativi p , comuni c "
                    + "WHERE a.idsoggetto_attuatore=s.idsoggetti_attuatori AND a.idprogetti_formativi=p.idprogetti_formativi AND a.comune_nascita=c.idcomune "
                    + "AND a.idallievi=" + idallievo;

            try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql0)) {
                if (rs.next()) {
                    idsa = rs.getString("a.idsoggetto_attuatore");
                    codicefiscale = rs.getString("a.codicefiscale").toUpperCase();
                    nome = rs.getString("a.nome");
                    cognome = rs.getString("a.cognome");
                    nascita_data = new DateTime(rs.getDate("a.datanascita").getTime()).toString(patternITA);
                    nascita_comune = rs.getString("c.nome");
                    nascita_provincia = rs.getString("c.cod_provincia");
                    NOMESA = rs.getString("s.ragionesociale");
                    if (rs.getString("p.sedefisica") != null) {
                        String sql1 = "SELECT s.indirizzo,c.nome,c.cod_provincia FROM sedi_formazione s, comuni c WHERE s.idsedi="
                                + rs.getString("p.sedefisica") + " AND s.comune=c.idcomune";
                        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                            if (rs1.next()) {
                                SEDE = rs1.getString(1) + " - " + rs1.getString(2) + " (" + rs1.getString(3) + ")";
                            }
                        }
                    }
                    CIP = rs.getString("p.cip");

                    DATAINIZIO = new DateTime(rs.getDate("p.start").getTime()).toString(patternITA);
                    DATAFINE = new DateTime(rs.getDate("p.end").getTime()).toString(patternITA);
//                    DATA = new DateTime().toString(patternITA);

                    String contentb64 = db0.getTipoDocAllievi("22"); // template

                    String path = db0.getPath("pathDocSA_Allievi")
                            .replace("@rssa", idsa + "")
                            .replace("@folder", codicefiscale);
                    createDir(path);
                    File pdfOut = new File(path + "Attestato_Finale_M7A_" + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".pdf");

                    try (InputStream is = new ByteArrayInputStream(decodeBase64(contentb64)); PdfReader reader = new PdfReader(is); PdfWriter writer = new PdfWriter(pdfOut); PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                        PdfAcroForm form = getAcroForm(pdfDoc, true);
                        form.setGenerateAppearance(true);
                        Map<String, PdfFormField> fields = form.getAllFormFields();

                        setFieldsValue(form, fields, "nome", nome.toUpperCase());
                        setFieldsValue(form, fields, "cognome", cognome.toUpperCase());
                        setFieldsValue(form, fields, "nascita_comune", nascita_comune.toUpperCase());
                        setFieldsValue(form, fields, "nascita_provincia", nascita_provincia.toUpperCase());
                        setFieldsValue(form, fields, "nascita_data", nascita_data);
                        setFieldsValue(form, fields, "NOMESA", NOMESA.toUpperCase());
                        setFieldsValue(form, fields, "SEDE", SEDE.toUpperCase());
                        setFieldsValue(form, fields, "CIP", CIP.toUpperCase());
                        setFieldsValue(form, fields, "DATA", DATAFINE);
                        setFieldsValue(form, fields, "DATAINIZIO", DATAINIZIO);
                        setFieldsValue(form, fields, "DATAFINE", DATAFINE);

                        moduli.forEach((KEY, VALUE) -> {
                            setFieldsValue(form, fields, KEY, VALUE);
                        });

                        fields.forEach((KEY, VALUE) -> {
                            form.partialFormFlattening(KEY);
                        });
                        if (flatten) {
                            form.flattenFields();
                            form.flush();
                        }

                        BarcodeQRCode barcode = new BarcodeQRCode("MODELLO7A / "
                                + StringUtils.deleteWhitespace(cognome + "_" + nome)
                                + " / " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                        printbarcode(barcode, pdfDoc);
                    }
                    if (checkPDF(pdfOut)) {
                        return pdfOut;
                    }

                }
            } catch (Exception ex1) {
                log.severe(estraiEccezione(ex1));
            }

        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return null;
    }

    private static File MODELLO7_UD11_BASE(
            Db_Gest db0,
            Long idallievo,
            String A_UD11_MOD,
            DateTime dataconsegna,
            boolean flatten) {

        try {

            String codicefiscale = "";
            String nome = "";
            String cognome = "";
            String nascita_data = "";
            String nascita_comune = "";
            String nascita_provincia = "";
            String NOMESA = "";
            String idsa = "";
            String SEDE = "";
            String CIP = "";
            String DATAINIZIO = "";
            String DATAFINE = "";
            String DATA = "";

            String sql0 = "SELECT a.idsoggetto_attuatore,a.codicefiscale,a.nome,a.cognome,c.nome,c.cod_provincia,a.datanascita,s.ragionesociale,p.sedefisica,p.cip,a.data_inizio_UD11,a.data_fine_UD11,p.start,p.end "
                    + "FROM allievi a, soggetti_attuatori s, progetti_formativi p , comuni c "
                    + "WHERE a.idsoggetto_attuatore=s.idsoggetti_attuatori AND a.idprogetti_formativi=p.idprogetti_formativi AND a.comune_nascita=c.idcomune "
                    + "AND a.idallievi=" + idallievo;

            try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql0)) {
                if (rs.next()) {
                    idsa = rs.getString("a.idsoggetto_attuatore");
                    codicefiscale = rs.getString("a.codicefiscale").toUpperCase();
                    nome = rs.getString("a.nome");
                    cognome = rs.getString("a.cognome");
                    nascita_data = new DateTime(rs.getDate("a.datanascita").getTime()).toString(patternITA);
                    nascita_comune = rs.getString("c.nome");
                    nascita_provincia = rs.getString("c.cod_provincia");
                    NOMESA = rs.getString("s.ragionesociale");
                    if (rs.getString("p.sedefisica") != null) {
                        String sql1 = "SELECT s.indirizzo,c.nome,c.cod_provincia FROM sedi_formazione s, comuni c WHERE s.idsedi=" + rs.getString("p.sedefisica") + " AND s.comune=c.idcomune";
                        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                            if (rs1.next()) {
                                SEDE = rs1.getString(1) + " - " + rs1.getString(2) + " (" + rs1.getString(3) + ")";
                            }
                        }
                    }
                    CIP = rs.getString("p.cip");

                    DATAINIZIO = new DateTime(rs.getDate("p.start").getTime()).toString(patternITA);
                    DATAFINE = new DateTime(rs.getDate("p.end").getTime()).toString(patternITA);
                    DATA = new DateTime(rs.getDate("a.data_fine_UD11").getTime()).toString(patternITA);

                    String contentb64 = db0.getTipoDocAllievi("24"); // template

                    String path = db0.getPath("pathDocSA_Allievi")
                            .replace("@rssa", idsa + "")
                            .replace("@folder", codicefiscale);
                    createDir(path);
                    File pdfOut = new File(path + "Attestato_CompetenzeDigitali_M7C" + "_" + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".pdf");

                    try (InputStream is = new ByteArrayInputStream(decodeBase64(contentb64)); PdfReader reader = new PdfReader(is); PdfWriter writer = new PdfWriter(pdfOut); PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                        PdfAcroForm form = getAcroForm(pdfDoc, true);
                        form.setGenerateAppearance(true);
                        Map<String, PdfFormField> fields = form.getAllFormFields();

                        setFieldsValue(form, fields, "nome", nome.toUpperCase());
                        setFieldsValue(form, fields, "cognome", cognome.toUpperCase());
                        setFieldsValue(form, fields, "nascita_comune", nascita_comune.toUpperCase());
                        setFieldsValue(form, fields, "nascita_provincia", nascita_provincia.toUpperCase());
                        setFieldsValue(form, fields, "nascita_data", nascita_data);
                        setFieldsValue(form, fields, "NOMESA", NOMESA.toUpperCase());
                        setFieldsValue(form, fields, "SEDE", SEDE.toUpperCase());
                        setFieldsValue(form, fields, "CIP", CIP.toUpperCase());
                        setFieldsValue(form, fields, "DATA", DATA);
                        setFieldsValue(form, fields, "DATAINIZIO", DATAINIZIO);
                        setFieldsValue(form, fields, "DATAFINE", DATAFINE);
                        setFieldsValue(form, fields, "A_UD11_MOD", A_UD11_MOD);

                        fields.forEach((KEY, VALUE) -> {
                            form.partialFormFlattening(KEY);
                        });
                        if (flatten) {
                            form.flattenFields();
                            form.flush();
                        }

                        BarcodeQRCode barcode = new BarcodeQRCode("MODELLO7C / "
                                + StringUtils.deleteWhitespace(cognome + "_" + nome)
                                + " / " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                        printbarcode(barcode, pdfDoc);
                    }
                    if (checkPDF(pdfOut)) {
                        return pdfOut;
                    }

                }
            } catch (Exception ex1) {
                log.severe(estraiEccezione(ex1));
            }

        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return null;
    }

    private static File MODELLO7_UD_BASE(
            Db_Gest db0,
            Long idallievo,
            Map<String, String[]> tipoud,
            List<String> li_A,
            List<String> li_B,
            DateTime dataconsegna,
            boolean flatten) {
        try {

            String codicefiscale = "";
            String nome = "";
            String cognome = "";
            String nascita_data = "";
            String nascita_comune = "";
            String nascita_provincia = "";
            String NOMESA = "";
            String idsa = "";
            String SEDE = "";
            String CIP = "";
            String DATAINIZIO = "";
            String DATAFINE = "";
            String DATA = "";

            String sql0 = "SELECT a.idsoggetto_attuatore,a.codicefiscale,a.nome,a.cognome,c.nome,c.cod_provincia,a.datanascita,s.ragionesociale,p.sedefisica,p.cip,a.data_inizio_UD11,a.data_fine_UD11,p.start,p.end "
                    + "FROM allievi a, soggetti_attuatori s, progetti_formativi p , comuni c "
                    + "WHERE a.idsoggetto_attuatore=s.idsoggetti_attuatori AND a.idprogetti_formativi=p.idprogetti_formativi AND a.comune_nascita=c.idcomune "
                    + "AND a.idallievi=" + idallievo;

            try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql0)) {
                if (rs.next()) {
                    idsa = rs.getString("a.idsoggetto_attuatore");
                    codicefiscale = rs.getString("a.codicefiscale").toUpperCase();
                    nome = rs.getString("a.nome");
                    cognome = rs.getString("a.cognome");
                    nascita_data = new DateTime(rs.getDate("a.datanascita").getTime()).toString(patternITA);
                    nascita_comune = rs.getString("c.nome");
                    nascita_provincia = rs.getString("c.cod_provincia");
                    NOMESA = rs.getString("s.ragionesociale");
                    if (rs.getString("p.sedefisica") != null) {
                        String sql1 = "SELECT s.indirizzo,c.nome,c.cod_provincia FROM sedi_formazione s, comuni c WHERE s.idsedi=" + rs.getString("p.sedefisica") + " AND s.comune=c.idcomune";
                        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                            if (rs1.next()) {
                                SEDE = rs1.getString(1) + " - " + rs1.getString(2) + " (" + rs1.getString(3) + ")";
                            }
                        }
                    }
                    CIP = rs.getString("p.cip");

                    DATAINIZIO = new DateTime(rs.getDate("p.start").getTime()).toString(patternITA);
                    DATAFINE = new DateTime(rs.getDate("p.end").getTime()).toString(patternITA);
                    DATA = new DateTime().toString(patternITA);

                    String contentb64 = db0.getTipoDocAllievi("23"); // template

                    String path = db0.getPath("pathDocSA_Allievi")
                            .replace("@rssa", idsa + "")
                            .replace("@folder", codicefiscale);
                    createDir(path);
                    File pdfOut = new File(path + "Attestato_UD_M7B" + "_" + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".pdf");

                    try (InputStream is = new ByteArrayInputStream(decodeBase64(contentb64)); PdfReader reader = new PdfReader(is); PdfWriter writer = new PdfWriter(pdfOut); PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                        PdfAcroForm form = getAcroForm(pdfDoc, true);
                        form.setGenerateAppearance(true);
                        Map<String, PdfFormField> fields = form.getAllFormFields();

                        setFieldsValue(form, fields, "nome", nome.toUpperCase());
                        setFieldsValue(form, fields, "cognome", cognome.toUpperCase());
                        setFieldsValue(form, fields, "nascita_comune", nascita_comune.toUpperCase());
                        setFieldsValue(form, fields, "nascita_provincia", nascita_provincia.toUpperCase());
                        setFieldsValue(form, fields, "nascita_data", nascita_data);
                        setFieldsValue(form, fields, "NOMESA", NOMESA.toUpperCase());
                        setFieldsValue(form, fields, "SEDE", SEDE.toUpperCase());
                        setFieldsValue(form, fields, "CIP", CIP.toUpperCase());
                        setFieldsValue(form, fields, "DATA", DATA);
                        setFieldsValue(form, fields, "DATAINIZIO", DATAINIZIO);
                        setFieldsValue(form, fields, "DATAFINE", DATAFINE);

                        //UD A
                        PdfWidgetAnnotation widgetAnnotation = fields.get("A_UD").getWidgets().get(0);
                        PdfArray annotationRect = widgetAnnotation.getRectangle();
                        List<Paragraph> list_pA = print(li_A, tipoud, true);
                        PdfPage page = pdfDoc.getPage(2);
                        try (Canvas canvas = new Canvas(new PdfCanvas(page), widgetAnnotation.getRectangle().toRectangle())) {
                            int index = 1;
                            for (Paragraph p1 : list_pA) {
                                float xCoord = annotationRect.getAsNumber(0).floatValue();
                                float yCoord = annotationRect.getAsNumber(3).floatValue() - (18 * index);
                                canvas.showTextAligned(p1, xCoord, yCoord, TextAlignment.LEFT);
                                index++;
                            }
                        }

                        //UD B
                        PdfWidgetAnnotation widgetAnnotation1 = fields.get("B_UD").getWidgets().get(0);
                        PdfArray annotationRect1 = widgetAnnotation1.getRectangle();
                        List<Paragraph> list_pB = print(li_B, tipoud, false);
                        try (Canvas canvas = new Canvas(new PdfCanvas(page), widgetAnnotation1.getRectangle().toRectangle())) {
                            int index = 1;
                            for (Paragraph p1 : list_pB) {
                                float xCoord = annotationRect1.getAsNumber(0).floatValue();
                                float yCoord = annotationRect1.getAsNumber(3).floatValue() - (18 * index);
                                canvas.showTextAligned(p1, xCoord, yCoord, TextAlignment.LEFT);
                                index++;
                            }
                        }

                        fields.forEach((KEY, VALUE) -> {
                            form.partialFormFlattening(KEY);
                        });

                        if (flatten) {
                            form.flattenFields();
                            form.flush();
                        }
                        BarcodeQRCode barcode = new BarcodeQRCode("MODELLO7C / "
                                + StringUtils.deleteWhitespace(cognome + "_" + nome)
                                + " / " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                        printbarcode(barcode, pdfDoc);
                    }
                    if (checkPDF(pdfOut)) {
                        return pdfOut;
                    }

                }
            } catch (Exception ex1) {
                log.severe(estraiEccezione(ex1));
            }

        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return null;
    }

    private static List<Paragraph> print(List<String> elenco, Map<String, String[]> tipoud, boolean faseA) {
        List<Paragraph> ok = new ArrayList<>();
        try {
            Style normal = new Style();
            normal.setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN)).setFontSize(8);
            Style bold = new Style();
            bold.setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD)).setFontSize(8);
            elenco.forEach(ud1 -> {
                if (faseA) {
                    String nameget = "A_" + ud1;
                    String[] valori = tipoud.get(nameget);
                    if (valori != null) {
                        switch (nameget) {
                            case "A_UD1" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 1 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Analisi delle competenze in ingresso e delle soft skills, analisi delle motivazioni del discente e percorso incentivante, motivazione all'imprenditorialità, Piramide di Maslow ed auto-motivazione. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD2" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 2 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Autoimprenditorialità, lavoro autonomo e start up. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);

                                ok.add(p);
                            }
                            case "A_UD3" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 3 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("I 5 pilastri per la costruzione dell’idea di impresa/lavoro autonomo. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD4" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 4 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Profili soggettivi e coerenza con l'idea di impresa/lavoro autonomo. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);

                                ok.add(p);
                            }
                            case "A_UD5" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 5 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Strumenti innovativi per l'efficace definizione del proprio modello di business. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD6" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 6 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("L'Analisi S.W.O.T: necessità e utilità di uno strumento di pianificazione strategica. Analisi di casi concreti di successo/insuccesso imprenditoriale. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD7" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 7 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Strategie di vendita e di promozione della propria iniziativa imprenditoriale/lavoro autonomo. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD8" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 8 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("La cantierabilità amministrativa dell'iniziativa imprenditoriale o di lavoro autonomo. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD9" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 9 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Le tipologie d'impresa. Adempimenti giuridico-amministrativi e fiscali per l'avvio dell'attività. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD10" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 10 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Piano operativo e organizzazione: il processo produttivo ed il piano degli investimenti e degli approvvigionamenti; il prospetto delle risorse umane, attività e responsabilità. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "A_UD11" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 11 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Competenze Digitali. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("10 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                        }
                    }
                } else {
                    String nameget = "B_" + ud1;
                    String[] valori = tipoud.get(nameget);
                    if (valori != null) {
                        switch (nameget) {
                            case "B_UD1" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 1 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Il Mercato di riferimento dell'iniziativa Imprenditoriale/lavoro autonomo. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("12 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "B_UD2" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 2 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("La S.W.O.T. Analysis della propria iniziativa imprenditoriale/lavoro autonomo, analisi del settore di riferimento con il quale il potenziale imprenditore si dovrà interfacciare. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("6 ore ").addStyle(normal));
                                p.setWidth(485);

                                ok.add(p);
                            }
                            case "B_UD3" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 3 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("La sostenibilità Economico-Finanziaria. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("12 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                            case "B_UD4" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 4 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("La comunicazione efficace, la gestione delle relazioni interpersonali e gli strumenti presenti sul territorio "
                                        + "al fine di ampliare la propria rete istituzionale/marketing. Comunicare e condividere all'interno della propria azienda/altre imprese/PA. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);

                                ok.add(p);
                            }
                            case "B_UD5" -> {
                                Paragraph p = new Paragraph();
                                p.add(new Text("Titolo U.D.: 5 – Contenuti Formativi: ").addStyle(bold));
                                p.add(new Text("Revisione finale del piano di impresa. ").addStyle(normal));
                                p.add(new Text("Modalità di svolgimento: ").addStyle(bold));
                                p.add(new Text(valori[0] + " ").addStyle(normal));
                                p.add(new Text("Durata: ").addStyle(bold));
                                p.add(new Text("5 ore ").addStyle(normal));
                                p.setWidth(485);
                                ok.add(p);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok;
    }

    public static void printbarcode(BarcodeQRCode barcode, PdfDocument pdfDoc) {
        try {
            Rectangle rect = barcode.getBarcodeSize();
            PdfFormXObject formXObject = new PdfFormXObject(new Rectangle(rect.getWidth(), rect.getHeight() + 10));
            PdfCanvas pdfCanvas = new PdfCanvas(formXObject, pdfDoc);
            barcode.placeBarcode(pdfCanvas, BLACK);
            Image bCodeImage = new Image(formXObject);
            bCodeImage.setRotationAngle(toRadians(90));
            bCodeImage.setFixedPosition(30, 30);
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                new Canvas(pdfDoc.getPage(i), pdfDoc.getDefaultPageSize()).add(bCodeImage);
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
    }

    public static void setFieldsValue(
            PdfAcroForm form,
            Map<String, PdfFormField> fields_list,
            String field_name,
            String field_value) {
        try {
            if (fields_list.get(field_name) != null) {
                if (field_value == null) {
                    field_value = "";
                }
                fields_list.get(field_name).setValue(field_value);
                form.partialFormFlattening(field_name);
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
    }

    public static void setFieldsValueRICH(
            PdfAcroForm form,
            Map<String, PdfFormField> fields_list,
            String field_name,
            String field_value) {
        try {
            if (fields_list.get(field_name) != null) {
                if (field_value == null) {
                    field_value = "";
                }
                fields_list.get(field_name).setValue(field_value);
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
    }

}
