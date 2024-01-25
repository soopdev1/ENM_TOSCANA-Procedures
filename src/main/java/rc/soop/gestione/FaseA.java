/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.stripAccents;
import org.joda.time.DateTime;
import rc.soop.exe.Items;
import static rc.soop.exe.Items.formatAction;
import rc.soop.exe.Utenti;
import static rc.soop.exe.Utils.conf;
import static rc.soop.exe.Utils.createDir;
import static rc.soop.exe.Utils.dtf;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.formatStringtoStringDateSQL;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.patternid;
import static rc.soop.exe.Utils.timestamp;
import static rc.soop.exe.Utils.timestampITAcomplete;
import static rc.soop.exe.Utils.timestampSQL;
import static rc.soop.gestione.Constant.calcoladurata;
import static rc.soop.gestione.Constant.checkPDF;
import static rc.soop.gestione.Constant.convertPDFA;
import static rc.soop.gestione.Constant.convertTS_Italy;
import static rc.soop.gestione.Constant.format;
import static rc.soop.gestione.Constant.getIdUser;
import static rc.soop.gestione.Constant.printbarcode;
import static rc.soop.gestione.Create.gestisciorerendicontabili;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author rcosco
 */
public class FaseA {

    public String host;

    public FaseA(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_gestione_toscana_prod";
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_gestione_toscana";
        }
        log.log(Level.INFO, "HOST: {0}", this.host);
    }

//    public static void main(String[] args) {
//        FaseA fa = new FaseA(false);
//        
//        List<Lezione> l = fa.calcolaegeneraregistrofasea(82, fa.getHost(), false, false,false);
//        
//        File f = fa.registro_aula_FaseA(82, fa.getHost(), false, l);
//
//    }
    public File registro_aula_FaseA(int idpr, String host, boolean save, List<Lezione> calendar) {
        try {
            Db_Gest db0 = new Db_Gest(host);
            String linkpiattaforma = db0.getPath("dominio");
            String[] datisa = db0.sa_cip(idpr);
            String path_destinazione = db0.getPath("pathDocSA_Prg").replace("@rssa",
                    datisa[2]).replace("@folder",
                            String.valueOf(idpr));
            DateTime adesso = new DateTime();
            String now = adesso.toString(timestampITAcomplete);
            String now0 = adesso.toString(timestamp);
            String now1 = adesso.toString(patternid);
            String pathtemp = db0.getPath("pathTemp");
            db0.closeDB();

            //dati pdf
            Color lightgrey = new DeviceRgb(242, 242, 242);
            PdfFont fontbold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Style bold = new Style();
            bold.setFont(fontbold).setFontSize(11);
            PdfFont fontnormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            Style normal = new Style();
            normal.setFont(fontnormal).setFontSize(10);

            //CREA PDF REPORT
            File out0 = new File(pathtemp + now0 + "reportfaseA_" + idpr + ".pdf");
            PdfWriter pw0 = new PdfWriter(out0);
            PdfDocument pdfDoc = new PdfDocument(pw0);
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdfDoc);

            Db_Gest db1 = new Db_Gest(host);
            AtomicInteger indice = new AtomicInteger(1);
            calendar.forEach(cal -> {
                List<Registro_completo> registro = new ArrayList<>();
                Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();
                try {
                    String day = cal.getGiorno();
                    String sql = "SELECT * FROM registro_completo WHERE idprogetti_formativi = "
                            + idpr + " AND data = '" + day
                            + "' AND fase='A' ORDER BY ruolo DESC,cognome ASC,nome ASC";
                    try (Statement st = db1.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            Registro_completo rc = new Registro_completo(
                                    rs.getInt(1),
                                    rs.getInt(2),
                                    rs.getInt(3),
                                    rs.getString(4),
                                    new DateTime(rs.getDate(5).getTime()),
                                    rs.getString(6),
                                    rs.getInt(7),
                                    rs.getString(8),
                                    rs.getString(9),
                                    rs.getLong(10),
                                    rs.getString(11),
                                    rs.getString(12),
                                    rs.getInt(13),
                                    rs.getString(14),
                                    rs.getString(15),
                                    rs.getString(16),
                                    rs.getString(17),
                                    rs.getString(18),
                                    rs.getString(19),
                                    rs.getLong(20),
                                    rs.getLong(21),
                                    rs.getInt(23));
                            registro.add(rc);
                        }
                    }

                    Cell cell0 = new Cell(1, 8);
                    cell0.add(new Paragraph("YES I START UP TOSCANA").addStyle(bold));
                    cell0.add(new Paragraph("Formarsi per diventare imprenditore/imprenditrice in Toscana").addStyle(bold));
                    cell0.add(new Paragraph("CUP D54D23002380007").addStyle(bold));
                    cell0.setTextAlignment(TextAlignment.CENTER);
                    table.addCell(cell0);

                    Cell cell = new Cell(1, 8);
                    cell.add(new Paragraph(" ").addStyle(normal));
                    cell.setTextAlignment(TextAlignment.CENTER);
                    cell.setBorder(Border.NO_BORDER);
                    table.addCell(cell);
                    cell = new Cell(1, 8);
                    cell.add(new Paragraph(" ").addStyle(normal));
                    cell.setTextAlignment(TextAlignment.CENTER);
                    cell.setBorder(Border.NO_BORDER);
                    table.addCell(cell);
                    cell = new Cell();
                    cell.add(new Paragraph("SOGGETTO ATTUATORE").addStyle(bold));
                    table.addCell(cell);
                    cell = new Cell();
                    cell.add(new Paragraph(datisa[0]).addStyle(normal));
                    cell.setBackgroundColor(lightgrey);
                    table.addCell(cell);
                    cell = new Cell();
                    cell.add(new Paragraph("CIP").addStyle(bold));
                    table.addCell(cell);
                    cell = new Cell();
                    cell.add(new Paragraph(datisa[1]).addStyle(normal));
                    cell.setBackgroundColor(lightgrey);
                    table.addCell(cell);

                    if (!registro.isEmpty()) {

                        Registro_completo primo = registro.get(0);
                        cell = new Cell();
                        cell.add(new Paragraph("DATA").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(primo.getData().toString(patternITA)).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("ID RIUNIONE").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(primo.getIdriunione()).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("N. PARTECIPANTI").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("ORA INIZIO").addStyle(bold));
                        cell.add(new Paragraph("(" + cal.getStart() + ")").addStyle(normal));

                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("ORA FINE").addStyle(bold));
                        cell.add(new Paragraph("(" + cal.getEnd() + ")").addStyle(normal));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("DURATA").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph("N.UD - FASE A").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph(" ").addStyle(bold));
                        cell.setBorderRight(Border.NO_BORDER);
                        cell.setBorderBottom(Border.NO_BORDER);
                        table.addCell(cell);

                        cell = new Cell();
                        cell.add(new Paragraph(String.valueOf(primo.getNumpartecipanti())).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(primo.getOrainizio()).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(primo.getOrafine()).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(calcoladurata(primo.getDurata())).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph(primo.getNud()).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph(" ").addStyle(normal));
                        cell.setTextAlignment(TextAlignment.CENTER);
                        cell.setBorderRight(Border.NO_BORDER);
                        cell.setBorderTop(Border.NO_BORDER);
                        cell.setBorderBottom(Border.NO_BORDER);
                        table.addCell(cell);
                        cell = new Cell(1, 8);
                        cell.add(new Paragraph(" ").addStyle(normal));
                        cell.setTextAlignment(TextAlignment.CENTER);
                        cell.setBorder(Border.NO_BORDER);
                        table.addCell(cell);

                        Cell cel2 = new Cell();
                        cel2.add(new Paragraph("COGNOME").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("NOME").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("RUOLO").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("EMAIL").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("ORA DI ENTRATA (LOGIN)").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("ORA DI USCITA (LOGOUT)").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("TOTALE ORE").addStyle(bold));
                        table.addCell(cel2);
                        cel2 = new Cell();
                        cel2.add(new Paragraph("TOTALE ORE RENDICONTABILI").addStyle(bold));
                        table.addCell(cel2);

                        registro.forEach(r1 -> {
                            Cell cel3 = new Cell();
                            cel3.add(new Paragraph(r1.getCognome()).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(r1.getNome()).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            String ruolo = r1.getRuolo();
                            cel3.add(new Paragraph(ruolo).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(r1.getEmail()).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(r1.getOrelogin()).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(r1.getOrelogout()).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(calcoladurata(r1.getTotaleore())).addStyle(normal));
                            table.addCell(cel3);
                            cel3 = new Cell();
                            cel3.add(new Paragraph(calcoladurata(r1.getTotaleorerendicontabili())).addStyle(normal));
                            table.addCell(cel3);
                        });
                    } else {
                        cell = new Cell();
                        cell.add(new Paragraph("DATA").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(formatStringtoStringDateSQL(day)).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph("NESSUNA LEZIONE TROVATA").addStyle(bold));
                        table.addCell(cell);
                    }
                } catch (Exception ex) {
                    log.severe(Constant.estraiEccezione(ex));
                }
                doc.add(table);
                if (indice.get() < calendar.size()) {
                    doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }
                indice.addAndGet(1);

            });
            db1.closeDB();

            if (indice.get() > 1) {

                doc.close();
                pdfDoc.close();
                pw0.close();

                String qrcontent = "ID " + idpr + " REGISTRO FASE A - AGGIORNATO IL " + now;
                File out1 = new File(StringUtils.replace(out0.getPath(), ".pdf", "_qr.pdf"));
                try (PdfReader p2 = new PdfReader(out0); PdfWriter p2w = new PdfWriter(out1); PdfDocument pdfDoc1 = new PdfDocument(p2, p2w)) {
                    BarcodeQRCode barcode = new BarcodeQRCode(qrcontent);
                    String add = "Questo registro è stato generato automaticamente dalla piattaforma raggiungibile al link: " + linkpiattaforma;
                    printbarcode(barcode, pdfDoc1, true, add);
                }
                File out2 = convertPDFA(out1, qrcontent);
                if (checkPDF(out2)) {
                    out0.deleteOnExit();
                    out1.deleteOnExit();

                    createDir(path_destinazione);
                    File pdf_final = new File(path_destinazione + File.separator + "Registro Fase A_" + now1 + ".pdf");
                    FileUtils.copyFile(out2, pdf_final);
                    if (checkPDF(pdf_final)) {
                        out2.deleteOnExit();
                        if (save) {
                            Db_Gest db3 = new Db_Gest(host);
                            String sql = "SELECT iddocumenti_progetti FROM documenti_progetti WHERE idprogetto = " + idpr + " AND tipo = 29";
                            try (Statement st = db3.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                                if (rs.next()) {
                                    try (Statement st1 = db3.getConnection().createStatement()) {
                                        String upd = "UPDATE documenti_progetti SET path = '" + pdf_final.getPath() + "' WHERE iddocumenti_progetti = " + rs.getInt(1);
                                        st1.executeUpdate(upd);
                                    }
                                } else {
                                    String ins = "INSERT INTO documenti_progetti (path,idprogetto,tipo) VALUES (?,?,?)";
                                    try (PreparedStatement ps1 = db3.getConnection().prepareStatement(ins)) {
                                        ps1.setString(1, pdf_final.getPath());
                                        ps1.setInt(2, idpr);
                                        ps1.setInt(3, 29);
                                        ps1.execute();
                                    }
                                }
                            }
                            db3.closeDB();
                        } else {
                            log.log(Level.INFO, "{0} RILASCIATO", pdf_final.getAbsolutePath());
                        }
                        return pdf_final;
                    }

                }
            }

        } catch (Exception ex) {
            log.severe(Constant.estraiEccezione(ex));
        }
        return null;
    }

    public File registro_aula_FaseA(int idpr, String host, boolean save, boolean today) {
        try {
            StringBuilder nomestanza = new StringBuilder();
            List<Lezione> calendar = new ArrayList<>();
            Db_Gest db0 = new Db_Gest(host);
            String linkpiattaforma = db0.getPath("dominio");
            String[] datisa = db0.sa_cip(idpr);
            String path_destinazione = db0.getPath("pathDocSA_Prg").replace("@rssa",
                    datisa[2]).replace("@folder",
                            String.valueOf(idpr));
            DateTime adesso = new DateTime();
            String now = adesso.toString(timestampITAcomplete);
            String now0 = adesso.toString(timestamp);
            String now1 = adesso.toString(patternid);

            String pathtemp = db0.getPath("pathTemp");
            String sql0 = "SELECT nomestanza FROM fad_multi WHERE idprogetti_formativi=" + idpr;
            try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {    //STANZA
                if (rs0.next()) {
                    nomestanza.append(rs0.getString(1));
                }
            }
            db0.closeDB();

            if (nomestanza.toString().trim().equals("")) {
                log.log(Level.SEVERE, "{0} NESSUNA STANZA TROVATA", idpr);
            } else {
                Db_Gest db1 = new Db_Gest(host);
                String sql1 = "SELECT lc.lezione,lm.giorno,lm.orario_start,lm.orario_end,lm.id_docente,ud.codice,lc.ore FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                        + "    WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                        + " AND mp.id_progettoformativo=" + idpr + " AND ud.fase = 'Fase A' AND lm.giorno < CURDATE() GROUP BY lm.giorno ORDER BY lc.lezione,lm.orario_start";

                if (today) {
                    sql1 = "SELECT lc.lezione,lm.giorno,lm.orario_start,lm.orario_end,lm.id_docente,ud.codice,lc.ore FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                            + " AND mp.id_progettoformativo=" + idpr + " AND ud.fase = 'Fase A' AND lm.giorno <= CURDATE() GROUP BY lm.giorno ORDER BY lc.lezione,lm.orario_start";
                }
                try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                    while (rs1.next()) {
                        calendar.add(new Lezione(rs1.getInt("lc.lezione"), rs1.getInt("lm.id_docente"),
                                rs1.getString("lm.giorno"), rs1.getString("lm.orario_start"),
                                rs1.getString("lm.orario_end"),
                                rs1.getString("ud.codice"), rs1.getString("lc.ore"), 1, nomestanza.toString()));
                    }
                }

                //dati pdf
                Color lightgrey = new DeviceRgb(242, 242, 242);
                PdfFont fontbold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                Style bold = new Style();
                bold.setFont(fontbold).setFontSize(11);
                PdfFont fontnormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                Style normal = new Style();
                normal.setFont(fontnormal).setFontSize(10);

                //CREA PDF REPORT
                File out0 = new File(pathtemp + now0 + "reportfaseA_" + idpr + ".pdf");
//                System.out.println(out0.getAbsolutePath());
                PdfWriter pw0 = new PdfWriter(out0);
                PdfDocument pdfDoc = new PdfDocument(pw0);
                pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
                Document doc = new Document(pdfDoc);

                AtomicInteger indice = new AtomicInteger(1);
                calendar.forEach(cal -> {

                    List<Registro_completo> registro = new ArrayList<>();
                    Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();
                    try {
                        String day = cal.getGiorno();
                        String sql = "SELECT * FROM registro_completo WHERE idprogetti_formativi = "
                                + idpr + " AND data = '" + day
                                + "' AND fase='A' ORDER BY ruolo DESC,cognome ASC,nome ASC";
                        try (Statement st = db1.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                            while (rs.next()) {
                                Registro_completo rc = new Registro_completo(
                                        rs.getInt(1),
                                        rs.getInt(2),
                                        rs.getInt(3),
                                        rs.getString(4),
                                        new DateTime(rs.getDate(5).getTime()),
                                        rs.getString(6),
                                        rs.getInt(7),
                                        rs.getString(8),
                                        rs.getString(9),
                                        rs.getLong(10),
                                        rs.getString(11),
                                        rs.getString(12),
                                        rs.getInt(13),
                                        rs.getString(14),
                                        rs.getString(15),
                                        rs.getString(16),
                                        rs.getString(17),
                                        rs.getString(18),
                                        rs.getString(19),
                                        rs.getLong(20),
                                        rs.getLong(21),
                                        rs.getInt(23));
                                registro.add(rc);
                            }
                        }

                        Cell cell0 = new Cell(1, 8);

                        cell0.add(new Paragraph("YES I START UP TOSCANA").addStyle(bold));
                        cell0.add(new Paragraph("Formarsi per diventare imprenditore/imprenditrice in Toscana").addStyle(bold));
                        cell0.add(new Paragraph("CUP D54D23002380007").addStyle(bold));

                        cell0.setTextAlignment(TextAlignment.CENTER);
                        table.addCell(cell0);

                        Cell cell = new Cell(1, 8);
                        cell.add(new Paragraph(" ").addStyle(normal));
                        cell.setTextAlignment(TextAlignment.CENTER);
                        cell.setBorder(Border.NO_BORDER);
                        table.addCell(cell);
                        cell = new Cell(1, 8);
                        cell.add(new Paragraph(" ").addStyle(normal));
                        cell.setTextAlignment(TextAlignment.CENTER);
                        cell.setBorder(Border.NO_BORDER);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("SOGGETTO ATTUATORE").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(datisa[0]).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph("CIP").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell();
                        cell.add(new Paragraph(datisa[1]).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        table.addCell(cell);

                        if (!registro.isEmpty()) {

                            Registro_completo primo = registro.get(0);
                            cell = new Cell();
                            cell.add(new Paragraph("DATA").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(primo.getData().toString(patternITA)).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph("ID RIUNIONE").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(primo.getIdriunione()).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph("N. PARTECIPANTI").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph("ORA INIZIO").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph("ORA FINE").addStyle(bold));

                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph("DURATA").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell(1, 2);
                            cell.add(new Paragraph("N.UD - FASE A").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell(1, 2);
                            cell.add(new Paragraph(" ").addStyle(bold));
                            cell.setBorderRight(Border.NO_BORDER);
                            cell.setBorderBottom(Border.NO_BORDER);
                            table.addCell(cell);

                            cell = new Cell();
                            cell.add(new Paragraph(String.valueOf(primo.getNumpartecipanti())).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            cell.setTextAlignment(TextAlignment.CENTER);
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(primo.getOrainizio()).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            cell.setTextAlignment(TextAlignment.CENTER);
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(primo.getOrafine()).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            cell.setTextAlignment(TextAlignment.CENTER);
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(calcoladurata(primo.getDurata())).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            cell.setTextAlignment(TextAlignment.CENTER);
                            table.addCell(cell);
                            cell = new Cell(1, 2);
                            cell.add(new Paragraph(primo.getNud()).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            cell.setTextAlignment(TextAlignment.CENTER);
                            table.addCell(cell);
                            cell = new Cell(1, 2);
                            cell.add(new Paragraph(" ").addStyle(normal));
                            cell.setTextAlignment(TextAlignment.CENTER);
                            cell.setBorderRight(Border.NO_BORDER);
                            cell.setBorderTop(Border.NO_BORDER);
                            cell.setBorderBottom(Border.NO_BORDER);
                            table.addCell(cell);
                            cell = new Cell(1, 8);
                            cell.add(new Paragraph(" ").addStyle(normal));
                            cell.setTextAlignment(TextAlignment.CENTER);
                            cell.setBorder(Border.NO_BORDER);
                            table.addCell(cell);

                            Cell cel2 = new Cell();
                            cel2.add(new Paragraph("COGNOME").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("NOME").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("RUOLO").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("EMAIL").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("ORA DI ENTRATA (LOGIN)").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("ORA DI USCITA (LOGOUT)").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("TOTALE ORE").addStyle(bold));
                            table.addCell(cel2);
                            cel2 = new Cell();
                            cel2.add(new Paragraph("TOTALE ORE RENDICONTABILI").addStyle(bold));
                            table.addCell(cel2);

                            registro.forEach(r1 -> {
                                Cell cel3 = new Cell();
                                cel3.add(new Paragraph(r1.getCognome()).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(r1.getNome()).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                String ruolo = r1.getRuolo();
                                cel3.add(new Paragraph(ruolo).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(r1.getEmail()).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(r1.getOrelogin()).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(r1.getOrelogout()).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(calcoladurata(r1.getTotaleore())).addStyle(normal));
                                table.addCell(cel3);
                                cel3 = new Cell();
                                cel3.add(new Paragraph(calcoladurata(r1.getTotaleorerendicontabili())).addStyle(normal));
                                table.addCell(cel3);
                            });
                        } else {
                            cell = new Cell();
                            cell.add(new Paragraph("DATA").addStyle(bold));
                            table.addCell(cell);
                            cell = new Cell();
                            cell.add(new Paragraph(formatStringtoStringDateSQL(day)).addStyle(normal));
                            cell.setBackgroundColor(lightgrey);
                            table.addCell(cell);
                            cell = new Cell(1, 2);
                            cell.add(new Paragraph("NESSUNA LEZIONE TROVATA").addStyle(bold));
                            table.addCell(cell);
                        }
                    } catch (Exception ex) {
                        log.severe(estraiEccezione(ex));
                    }
                    doc.add(table);
                    if (indice.get() < calendar.size()) {
                        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                    }
                    indice.addAndGet(1);

                });
                db1.closeDB();

                if (indice.get() > 1) {

                    doc.close();
                    pdfDoc.close();
                    pw0.close();

                    String qrcontent = "ID " + idpr + " REGISTRO FASE A - AGGIORNATO IL " + now;
                    File out1 = new File(StringUtils.replace(out0.getPath(), ".pdf", "_qr.pdf"));
                    try (PdfReader p2 = new PdfReader(out0); PdfWriter p2w = new PdfWriter(out1); PdfDocument pdfDoc1 = new PdfDocument(p2, p2w)) {
                        BarcodeQRCode barcode = new BarcodeQRCode(qrcontent);
                        String add = "Questo registro è stato generato automaticamente dalla piattaforma raggiungibile al link: " + linkpiattaforma;
                        printbarcode(barcode, pdfDoc1, true, add);
                    }
                    File out2 = convertPDFA(out1, qrcontent);
                    if (checkPDF(out2)) {
                        out0.deleteOnExit();
                        out1.deleteOnExit();

                        createDir(path_destinazione);
                        File pdf_final = new File(path_destinazione + File.separator + "Registro Fase A_" + now1 + ".pdf");
                        FileUtils.copyFile(out2, pdf_final);
                        if (checkPDF(pdf_final)) {
                            out2.deleteOnExit();
                            if (save) {
                                Db_Gest db3 = new Db_Gest(host);
                                String sql = "SELECT iddocumenti_progetti FROM documenti_progetti WHERE idprogetto = " + idpr + " AND tipo = 29";
                                try (Statement st = db3.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                                    if (rs.next()) {
                                        try (Statement st1 = db3.getConnection().createStatement()) {
                                            String upd = "UPDATE documenti_progetti SET path = '" + pdf_final.getPath() + "' WHERE iddocumenti_progetti = " + rs.getInt(1);
                                            st1.executeUpdate(upd);
                                        }
                                    } else {
                                        String ins = "INSERT INTO documenti_progetti (path,idprogetto,tipo) VALUES (?,?,?)";
                                        try (PreparedStatement ps1 = db3.getConnection().prepareStatement(ins)) {
                                            ps1.setString(1, pdf_final.getPath());
                                            ps1.setInt(2, idpr);
                                            ps1.setInt(3, 29);
                                            ps1.execute();
                                        }

                                    }
                                }
                                db3.closeDB();
                            } else {
                                log.log(Level.INFO, "{0} RILASCIATO", pdf_final.getAbsolutePath());
                            }
                            return pdf_final;
                        }

                    }
                }
            }
        } catch (Exception ex) {
            log.severe(Constant.estraiEccezione(ex));
        }
        return null;
    }

    public List<Lezione> calcolaegeneraregistrofasea(int idpr, String host, boolean printing, boolean save, boolean today) {
        List<Lezione> calendar = new ArrayList<>();
        try {
            StringBuilder nomestanza = new StringBuilder();
            List<Lezione> calendartemp = new ArrayList<>();
            List<Items> azioni = formatAction();
            Db_Gest db0 = new Db_Gest(host);
            String[] datisa = db0.sa_cip(idpr);
//            DateTime adesso = new DateTime();
            List<Utenti> docenti = db0.list_Docenti_noAccento(idpr);
            List<Utenti> docenti_corretti = db0.list_Docenti(idpr);
            List<Utenti> allievi = db0.list_Allievi_noAccento(idpr);
            List<Utenti> allievi_corretti = db0.list_Allievi(idpr);
            String sql0 = "SELECT nomestanza FROM fad_multi WHERE idprogetti_formativi=" + idpr;
            try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {    //STANZA
                if (printing) {
                    log.log(Level.INFO, "1) {0}", sql0);
                }
                if (rs0.next()) {
                    if (printing) {
                        log.info("1) TRUE");
                    }
                    nomestanza.append(rs0.getString(1));
                } else {
                    if (printing) {
                        log.info("1) FALSE");
                    }
                }
            }
            db0.closeDB();

            if (nomestanza.toString().trim().equals("")) {
                log.log(Level.SEVERE, "{0} NESSUNA STANZA TROVATA", idpr);
                return calendar;
            } else {
                Db_Gest db1 = new Db_Gest(host);

//                manage(db1, idpr);
                String sql1 = "SELECT lc.lezione,lm.giorno,lm.orario_start,lm.orario_end,lm.id_docente,ud.codice,lc.ore,mp.id_modello FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                        + " AND lm.tipolez='F' AND mp.id_progettoformativo=" + idpr + " AND ud.fase = 'Fase A' AND lm.giorno < CURDATE() ORDER BY lc.lezione,lm.orario_start";

                if (today) {
                    sql1 = "SELECT lc.lezione,lm.giorno,lm.orario_start,lm.orario_end,lm.id_docente,ud.codice,lc.ore,mp.id_modello FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                            + " AND lm.tipolez='F' AND mp.id_progettoformativo=" + idpr + " AND ud.fase = 'Fase A' AND lm.giorno <= CURDATE() ORDER BY lc.lezione,lm.orario_start";
                }

                try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                    if (printing) {
                        log.log(Level.INFO, "2) {0}", sql1);
                    }
                    while (rs1.next()) {

                        Lezione temp1 = new Lezione(rs1.getInt("lc.lezione"),
                                rs1.getInt("lm.id_docente"),
                                rs1.getString("lm.giorno"), rs1.getString("lm.orario_start"), rs1.getString("lm.orario_end"),
                                rs1.getString("ud.codice"), rs1.getString("lc.ore"), 1, nomestanza.toString());
                        temp1.setMpid_modello(rs1.getInt("mp.id_modello"));
                        calendartemp.add(temp1);
                    }
                    if (printing) {
                        log.log(Level.INFO, "2) {0}", calendartemp.size());
                    }
                }
                db1.closeDB();

                for (int i = 0; i < calendartemp.size(); i++) {
                    Lezione cal = calendartemp.get(i);
                    Lezione cal2;
                    try {
                        cal2 = calendartemp.get(i + 1);
                    } catch (Exception e) {
                        cal2 = null;
                    }
                    Lezione cal3;
                    try {
                        cal3 = calendartemp.get(i - 1);
                    } catch (Exception e) {
                        cal3 = null;
                    }

                    boolean hasnext = cal2 != null;
                    if (hasnext) {
                        if (cal.getGiorno().equals(cal2.getGiorno())) {
                            List<Integer> doc1 = new ArrayList<>();
                            doc1.addAll(cal.getDocente());
                            cal2.getDocente().forEach(d1 -> {
                                if (!doc1.contains(d1)) {
                                    doc1.add(d1);
                                }
                            });
                            double ore = Double.parseDouble(cal.getOre()) + Double.parseDouble(cal2.getOre());

                            Lezione l2temp = new Lezione(cal.getId(), doc1,
                                    cal.getGiorno(), cal.getStart(), cal2.getEnd(),
                                    cal.getCodiceud() + "_" + cal2.getCodiceud(),
                                    Constant.doubleformat.format(ore), cal.getGruppo(), nomestanza.toString());
                            l2temp.setMpid_modello(cal.getMpid_modello());
                            calendar.add(l2temp);
                        } else {
                            if (cal3 == null || !cal3.getGiorno().equals(cal.getGiorno())) {
                                calendar.add(cal);
                            }
                        }
                    } else {
                        if (i == 0) {
                            calendar.add(cal);
                        } else {
                            if (!cal.getGiorno().equals(cal3.getGiorno())) {
                                calendar.add(cal);
                            }
                        }
                    }
                }
                Db_Gest db2 = new Db_Gest(host);
                calendar.forEach(
                        cal -> {
                            AtomicInteger presente = new AtomicInteger(0);
                            String day = cal.getGiorno();
                            String idriuunione = StringUtils.remove(datisa[1], "_") + "_" + cal.getCodiceud() + "_" + dtf.parseDateTime(day).toString(patternid);
                            String nudfasea = cal.getCodiceud();
                            String sql = "SELECT id FROM registro_completo WHERE idprogetti_formativi = " + idpr + " AND data = '" + day + "' AND idriunione = '" + idriuunione + "'";
                            try {
                                try (Statement st = db2.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                                    if (printing) {
                                        log.log(Level.INFO, "3) {0}", sql);
                                    }
                                    if (rs.next()) {
                                        presente.addAndGet(1);
                                    }
                                    if (printing) {
                                        log.log(Level.INFO, "3) {0}", presente.get());
                                    }
                                }
                            } catch (Exception ex) {
                                log.severe(Constant.estraiEccezione(ex));
                            }
                            if (presente.get() == 0) {
                                AtomicInteger partecipanti = new AtomicInteger(0);
                                StringBuilder inizio = new StringBuilder("");
                                StringBuilder fine = new StringBuilder("");
                                AtomicLong durata = new AtomicLong(0L);
                                LinkedList<Presenti> presenti = new LinkedList<>();
                                LinkedList<Presenti> report = new LinkedList<>();
                                List<Track> tracking = new ArrayList<>();
                                List<Items> idutenti = new ArrayList<>();
                                List<Utenti> tutti = new ArrayList<>();
                                try {
                                    String sql2 = "SELECT type,action,date FROM fad_track f WHERE f.room = '" + cal.getNomestanza().trim() + "' "
                                    + "AND f.date > '" + day + " 04:00' AND f.date < '" + day + " 23:00'"
                                    + "ORDER BY f.date";
                                    try (Statement st2 = db2.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                                        if (printing) {
                                            log.log(Level.INFO, "4) {0}", sql2);
                                        }
                                        while (rs2.next()) {
                                            String tipoazione = rs2.getString(1);
                                            String action = rs2.getString(2);
                                            String azione = stripAccents(action.toUpperCase().replaceAll(":", ";"));
                                            if (azione.contains("MESSAGGIO ->")) {
                                                continue;
                                            }
                                            String date = convertTS_Italy(rs2.getString(3));
                                            switch (tipoazione) {
                                                case "L1", "L2", "L3" -> {
                                                    if (azione.startsWith("ALLIEVO")) {
                                                        try {
                                                            int idallievo = Integer.parseInt(azione.split(";")[1]);
                                                            Utenti u = allievi.stream().filter(al -> al.getId() == idallievo).findFirst().get();
                                                            azione = azioni.stream().filter(az -> az.getCod().equalsIgnoreCase(tipoazione)).findFirst().get().getDescrizione() + " -> "
                                                                    + u.getNome() + " "
                                                                    + u.getCognome();
                                                            Track t1 = new Track("USER", tipoazione, azione, date, day, null);
                                                            tracking.add(t1);
                                                            tutti.add(u);
                                                        } catch (Exception ex) {
                                                            log.severe(Constant.estraiEccezione(ex));
                                                        }
                                                    } else if (azione.startsWith("DOCENTE")) {
                                                        try {
                                                            int iddocente = Integer.parseInt(azione.split(";")[1]);
                                                            Utenti u = docenti.stream().filter(al -> al.getId() == iddocente).findFirst().get();
                                                            azione = azioni.stream().filter(az -> az.getCod().equalsIgnoreCase(tipoazione)).findFirst().get().getDescrizione() + " -> "
                                                                    + u.getNome() + " "
                                                                    + u.getCognome();
                                                            Track t1 = new Track("DOCENTE", tipoazione, azione, date, day, null);
                                                            tracking.add(t1);
                                                            tutti.add(u);
                                                        } catch (Exception ex) {
                                                            log.severe(Constant.estraiEccezione(ex));
                                                        }
                                                    }
                                                }
                                                case "L5" -> {
                                                }
                                                case "L4" -> {
                                                }
                                                case "IN" -> {
                                                    if (azione.startsWith("UTENTE LOGGATO CON ID")) {
                                                        String idfad = StringUtils.remove(azione.split("--")[0], "UTENTE LOGGATO CON ID").trim();
                                                        if (azione.split("--").length > 1) {
                                                            String nomecogn = azione.split("--")[1].trim().toUpperCase();
                                                            idutenti.add(new Items(idfad, nomecogn));
                                                        }
                                                    } else if (azione.startsWith("AVATAR MODIFICATO")) {
                                                        try {
                                                            String idfad = Splitter.on("->").splitToList(azione).get(1).trim();
                                                            if (StringUtils.isNumeric(idfad)) {
                                                                if (tutti.stream().filter(ut -> ut.getId() == Integer.parseInt(idfad)).findAny().orElse(null) != null) {
                                                                    Utenti u1 = tutti.stream().filter(ut -> ut.getId() == Integer.parseInt(idfad)).findAny().get();
                                                                    Track t1 = new Track(u1.getRuolo(), "L1", "Login -> " + u1.getNome() + " " + u1.getCognome(), date, day, null);
                                                                    tracking.add(t1);
                                                                }
                                                            } else {
                                                                if (idutenti.stream().filter(ut -> ut.getCod().equals(idfad)).findAny().orElse(null) != null) {
                                                                    String nomecogn = idutenti.stream().filter(ut -> ut.getCod().equals(idfad)).findFirst().get().getDescrizione().toUpperCase();
                                                                    Track t1 = new Track("", "L1", "Login -> " + nomecogn, date, day, idfad);
                                                                    tracking.add(t1);
                                                                }
                                                            }
                                                        } catch (Exception e1) {
                                                            log.severe(Constant.estraiEccezione(e1));
                                                        }
                                                    } else if (azione.startsWith("NUOVO PARTECIPANTE")) {
                                                        String nomecogn = azione.split("--")[1].trim().toUpperCase();
                                                        if (!nomecogn.contains("UNDEFINED")) {
                                                            String idfad = StringUtils.remove(azione.split("--")[0], "NUOVO PARTECIPANTE ->").trim();
                                                            idutenti.add(new Items(idfad, nomecogn));
                                                            Track t1 = new Track("", "L1", "Login -> " + nomecogn, date, day, idfad);
                                                            tracking.add(t1);
                                                        }
                                                    }
                                                }
                                                default -> {
                                                }
                                            }
                                            //USCITI TUTTI
//                                                List<Utenti> t11 = tutti.stream().distinct().collect(Collectors.toList());
//                                                t11.forEach(u1 -> {
//                                                    Track t1 = new Track(u1.getRuolo(), "L2", "Logout -> " + u1.getNome() + " " + u1.getCognome(), date, day, null);
//                                                    tracking.add(t1);
//                                                });
//                                                try {
//                                                    String idfad = StringUtils.remove(azione, "USCITA PARTECIPANTE -> ").trim();
//                                                    String nomecogn = "";
//                                                    if (idutenti.stream().filter(ut -> ut.getCod().equals(idfad)).findAny().orElse(null) != null) {
//                                                        nomecogn = idutenti.stream().filter(ut -> ut.getCod().equals(idfad)).findFirst().get().getDescrizione().toUpperCase();
//                                                        azione = "Logout -> " + nomecogn;
//                                                        Track t1 = new Track("", tipoazione, azione, date, day, idfad);
//                                                        tracking.add(t1);
//                                                    }
//                                                } catch (Exception ex) {
//                                                    Create.log.severe(Constant.estraiEccezione(ex));
//                                                }
                                                                                    }
                                    }
                                } catch (Exception ex) {
                                    log.severe(Constant.estraiEccezione(ex));
                                }
                                List<Track> finaltr = tracking.stream().distinct().collect(Collectors.toList());
                                finaltr.forEach(tr1 -> {

                                    boolean contentallievo = allievi.stream().anyMatch(al -> tr1.getDescr().contains(al.getDescrizione()));
                                    if (contentallievo) {
                                        Utenti a = allievi.stream().filter(al -> tr1.getDescr().contains(al.getDescrizione())).findAny().get();
                                        Utenti a1 = allievi_corretti.stream().filter(al -> al.getId() == a.getId()).findAny().get();

                                        Presenti pr1 = new Presenti(a1.getNome(), a1.getCognome(), a.getCf(), a.getEmail(), a.getRuolo());
                                        if (tr1.getDescr().contains("Login")) {
                                            pr1.setLogin(true);
                                        } else if (tr1.getDescr().contains("Logout")) {
                                            pr1.setLogout(true);
                                        }
                                        pr1.setDate(tr1.getDate());
                                        presenti.add(pr1);
                                    } else {
                                        boolean contentdocente = docenti.stream().anyMatch(al -> tr1.getDescr().contains(al.getDescrizione()));
                                        if (contentdocente) {
                                            Utenti a = docenti.stream().filter(al -> tr1.getDescr().contains(al.getDescrizione())).findAny().get();
                                            Utenti a1 = docenti_corretti.stream().filter(al -> al.getId() == a.getId()).findAny().get();
                                            if (cal.getDocente().contains(a1.getId())) {
                                                Presenti pr1 = new Presenti(a1.getNome(), a1.getCognome(), a.getCf(), a.getEmail(), a.getRuolo());
                                                if (tr1.getDescr().contains("Login")) {
                                                    pr1.setLogin(true);
                                                } else if (tr1.getDescr().contains("Logout")) {
                                                    pr1.setLogout(true);
                                                }
                                                pr1.setDate(tr1.getDate());
                                                presenti.add(pr1);
                                            } else {
                                                log.log(Level.WARNING, "DOCENTE {0} {1} OSPITE ", new Object[]{a1.getNome(), a1.getCognome()});
                                            }
                                        }
                                    }
                                });

                                presenti.sort(Comparator.comparing(a -> a.getRuolo()));

                                List<String> dist_cf = Lists.reverse(presenti).stream().map(cf -> cf.getCf()).distinct().collect(Collectors.toList());

                                dist_cf.forEach(tr1 -> {
                                    Presenti selected = presenti.stream().filter(cf -> cf.getCf().equalsIgnoreCase(tr1)).findFirst().get();
                                    List<Presenti> userp = presenti.stream().filter(d -> d.getCf().equalsIgnoreCase(tr1)).distinct().collect(Collectors.toList());
                                    List<Presenti> login = userp.stream().filter(d -> d.isLogin()).distinct().collect(Collectors.toList());
//                                    List<Presenti> logout = userp.stream().filter(d -> d.isLogout()).distinct().collect(Collectors.toList());
                                    if (!login.isEmpty()) {
                                        StringBuilder loginvalue = new StringBuilder();
                                        StringBuilder logoutvalue = new StringBuilder();
                                        LinkedList<Presenti> userp_final = new LinkedList<>();
                                        AtomicInteger ind = new AtomicInteger(0);
                                        userp.forEach(ba1 -> {
                                            if (ind.get() == 0) {
                                                if (ba1.isLogin()) {
                                                    userp_final.add(ba1);
                                                }
                                            } else {
                                                if (userp_final.isEmpty()) {
                                                    userp_final.add(ba1);
                                                } else {
                                                    Presenti precedente = userp_final.getLast();
                                                    if (precedente.isLogin() && !ba1.isLogin()) {
                                                        if (!precedente.getDate().equals(ba1.getDate())) {
                                                            userp_final.add(ba1);
                                                        }
                                                    } else if (precedente.isLogout() && !ba1.isLogout()) {
                                                        if (!precedente.getDate().equals(ba1.getDate())) {
                                                            userp_final.add(ba1);
                                                        }
                                                    } else if (precedente.isLogout() && ba1.isLogout()) {
                                                        userp_final.removeLast();
                                                        userp_final.add(ba1);
                                                    }
                                                }
                                            }
                                            ind.addAndGet(1);
                                        });

                                        if ((userp_final.size() % 2) != 0) {
                                            if (userp_final.getLast().isLogout()) {
                                                userp_final.removeLast();
                                            } else {

                                                try {
                                                    DateTime ultimo = Constant.dtfsql.parseDateTime(userp_final.getLast().getDate());
                                                    DateTime finelezione = Constant.dtfsql.parseDateTime(cal.getGiorno() + " " + cal.getEnd());
                                                    if (ultimo.isAfter(finelezione)) {

                                                        userp_final.removeLast();

                                                    } else {
                                                        Presenti pr1 = new Presenti(userp_final.getLast().getNome(), userp_final.getLast().getCognome(),
                                                                userp_final.getLast().getCf(), userp_final.getLast().getEmail(), userp_final.getLast().getRuolo());
                                                        pr1.setLogout(true);

                                                        pr1.setDate(cal.getGiorno() + " " + cal.getEnd());
                                                        pr1.setIdfad(userp_final.getLast().getIdfad());
                                                        userp_final.add(pr1);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }

                                        if (!userp_final.isEmpty()) {
                                            AtomicLong millis = new AtomicLong(0);
                                            userp_final.forEach(ba1 -> {
                                                if (ba1.isLogin()) {
                                                    millis.addAndGet(-format(ba1.getDate().split("\\.")[0], timestampSQL).getMillis());
                                                    loginvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                } else if (ba1.isLogout()) {
                                                    millis.addAndGet(format(ba1.getDate().split("\\.")[0], timestampSQL).getMillis());
                                                    logoutvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                }
                                            });

                                            long duratalogin = millis.get();
                                            String duratacollegamento = calcoladurata(duratalogin);

                                            partecipanti.addAndGet(1);
                                            Presenti per_report = new Presenti(selected.getCognome().toUpperCase(), selected.getNome().toUpperCase(),
                                                    selected.getRuolo(), selected.getEmail(),
                                                    loginvalue.toString().trim(), logoutvalue.toString().trim(),
                                                    duratacollegamento, duratacollegamento);
                                            per_report.setMillistotaleore(duratalogin);
                                            report.add(per_report);
                                        }

                                    }

                                });

                                List<Presenti> solodocenti = report.stream().filter(r1 -> r1.getRuolo().equals("DOCENTE")).sorted(Comparator.comparing(a -> a.getOradilogin())).collect(Collectors.toList());
                                for (Presenti ps : solodocenti) {
                                    List<String> login_S = Splitter.on("\n").splitToList(ps.getOradilogin());
                                    List<String> logout_S = Splitter.on("\n").splitToList(ps.getOradilogout());
                                    if (!login_S.isEmpty() && !logout_S.isEmpty()) {
                                        if (inizio.isEmpty()) {
                                            inizio.append(login_S.get(0));
                                        }
                                        if (fine.isEmpty()) {
                                            fine.append(logout_S.get(logout_S.size() - 1));
                                        } else {
                                            fine.setLength(0);
                                            fine.append(logout_S.get(logout_S.size() - 1));
                                        }

                                    }
                                }
                                
                                AtomicLong millisdurata = new AtomicLong(0);
                                millisdurata.addAndGet(-format("2021-01-01 " + StringUtils.substring(inizio.toString(), 0, 8), timestampSQL).getMillis());
                                millisdurata.addAndGet(format("2021-01-01 " + StringUtils.substring(fine.toString(), 0, 8), timestampSQL).getMillis());
                                long duratalogindurata = (millisdurata.get());
                                durata.set(duratalogindurata);

                                gestisciorerendicontabili(report, idpr, host, cal);

                                report.forEach(r1 -> {
                                    try {
                                        String ins = "INSERT INTO registro_completo (idprogetti_formativi,idsoggetti_attuatori,cip,data,idriunione,numpartecipanti,orainizio,orafine,durata,nud,fase,gruppofaseb,ruolo,cognome,nome,email,orelogin,orelogout,totaleore,totaleorerendicontabili,idutente) "
                                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        try (PreparedStatement ps = db2.getConnection().prepareStatement(ins);) {
                                            ps.setInt(1, idpr);
                                            ps.setInt(2, Integer.parseInt(datisa[2]));
                                            ps.setString(3, datisa[1]);
                                            ps.setString(4, day);
                                            ps.setString(5, idriuunione);
                                            ps.setInt(6, partecipanti.get());
                                            ps.setString(7, StringUtils.substring(inizio.toString(), 0, 8));
                                            ps.setString(8, StringUtils.substring(fine.toString(), 0, 8));
                                            ps.setLong(9, durata.get());
                                            ps.setString(10, nudfasea);
                                            ps.setString(11, "A");
                                            ps.setInt(12, 1);
                                            ps.setString(13, r1.getRuolo());
                                            ps.setString(14, r1.getCognome());
                                            ps.setString(15, r1.getNome());
                                            ps.setString(16, r1.getEmail());
                                            ps.setString(17, r1.getOradilogin());
                                            ps.setString(18, r1.getOradilogout());
                                            ps.setLong(19, r1.getMillistotaleore());
                                            ps.setLong(20, r1.getMillistotaleorerendicontabili());
                                            ps.setInt(21, getIdUser(db2, r1.getNome(), r1.getCognome(), idpr, Integer.parseInt(datisa[2]), r1.getRuolo()));
                                            if (save) {
                                                ps.execute();
                                                log.log(Level.WARNING, "{0} INFO) REGISTRO {1} {2} OK.", new Object[]{idpr, day, idriuunione});
                                            } else {
                                                log.info(r1.toString());
                                            }
                                        }
                                    } catch (Exception ex) {
                                        log.severe(Constant.estraiEccezione(ex));
                                    }
                                });
                            } else {
                                log.log(Level.WARNING, "{0} WARNING) REGISTRO {1} {2} GIA'' PRESENTE.", new Object[]{idpr, day, idriuunione});
                            }
                        }
                );
                db2.closeDB();
            }
        } catch (Exception ex) {
            log.severe(Constant.estraiEccezione(ex));
        }
        return calendar;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
