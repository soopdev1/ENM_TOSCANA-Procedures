/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import static rc.soop.exe.Utils.createDir;
import static rc.soop.exe.Utils.formatStringtoStringDateSQL;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.patternid;
import static rc.soop.exe.Utils.timestamp;
import static rc.soop.exe.Utils.timestampITAcomplete;
import static rc.soop.gestione.Constant.calcoladurata;
import static rc.soop.gestione.Constant.checkPDF;
import static rc.soop.gestione.Constant.convertPDFA;
import static rc.soop.gestione.Constant.printbarcode;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author rcosco
 */
public class Complessivo {

//    private static final String loghineet = "/mnt/mcn/yisu_neet/loghineet_DEF.jpg";
//    private static final String loghided = "/mnt/mcn/yisu_ded/loghided_DEF.jpg";
    private static final String loghitoscana = "/mnt/mcn/yisu_toscana/loghitoscana_DEF.jpg";

    public String host;

    public Complessivo(String host) {
        this.host = host;
        log.log(Level.INFO, "HOST: {0}", this.host);
    }

    public File registro_complessivo(int idpr, String host, List<Lezione> fa, List<Lezione> fb, boolean save) {
        try {

            Db_Gest db1 = new Db_Gest(host);
            String linkpiattaforma = db1.getPath("dominio");
            String[] datisa = db1.sa_cip(idpr);
            String path_destinazione = db1.getPath("pathDocSA_Prg").replace("@rssa",
                    datisa[2]).replace("@folder",
                            String.valueOf(idpr));
            DateTime adesso = new DateTime();
            String now = adesso.toString(timestampITAcomplete);
            String now0 = adesso.toString(timestamp);
            String now1 = adesso.toString(patternid);
            String pathtemp = db1.getPath("pathTemp");
            //dati pdf
            Color lightgrey = new DeviceRgb(242, 242, 242);
            PdfFont fontbold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Style bold = new Style();
            bold.setFont(fontbold).setFontSize(11);
            PdfFont fontnormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            Style normal = new Style();
            normal.setFont(fontnormal).setFontSize(10);

//CREA PDF REPORT
            File out0 = new File(pathtemp + now0 + "reportcomplessivo_" + idpr + ".pdf");
            PdfWriter pw0 = new PdfWriter(out0);
            PdfDocument pdfDoc = new PdfDocument(pw0);
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdfDoc);
            AtomicInteger indice = new AtomicInteger(1);

            fa.forEach(cal -> {

                List<Registro_completo> registro = new ArrayList<>();
                Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();
                try {
                    String day = cal.getGiorno();
                    String sql = "SELECT * FROM registro_completo WHERE idprogetti_formativi = " + idpr + " AND data = '" + day
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

                    Cell cell0A = new Cell(1, 8);
                    cell0A.add(new Image(ImageDataFactory.create(loghitoscana)).setAutoScale(true));
                    cell0A.setTextAlignment(TextAlignment.CENTER);
                    cell0A.setBorder(Border.NO_BORDER);
                    table.addCell(cell0A);
                    Cell cell0B = new Cell(1, 8);
                    cell0B.add(new Paragraph("YES I START UP TOSCANA").addStyle(bold));
                    cell0B.add(new Paragraph("Formarsi per diventare imprenditore/imprenditrice in Toscana").addStyle(bold));
                    cell0B.add(new Paragraph("CUP D54D23002380007").addStyle(bold));
                    cell0B.setTextAlignment(TextAlignment.CENTER);
                    table.addCell(cell0B);

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
                    log.severe(Constant.estraiEccezione(ex));
                }
                doc.add(table);
                if (indice.get() < fa.size()) {
                    doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }
                indice.addAndGet(1);

            });
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            AtomicInteger indice1 = new AtomicInteger(1);
            fb.forEach(cal -> {
                List<Registro_completo> registro = new ArrayList<>();
                Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();
                try {
                    String day = cal.getGiorno();
                    String sql = "SELECT * FROM registro_completo WHERE idprogetti_formativi = " + idpr + " AND data = '" + day
                            + "' AND fase='B' AND gruppofaseb = '" + cal.getGruppo() + "' ORDER BY ruolo DESC,cognome ASC,nome ASC";
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

                    Cell cell0A = new Cell(1, 8);
                    cell0A.add(new Image(ImageDataFactory.create(loghitoscana)).setAutoScale(true));
                    cell0A.setTextAlignment(TextAlignment.CENTER);
                    cell0A.setBorder(Border.NO_BORDER);
                    table.addCell(cell0A);
                    Cell cell0B = new Cell(1, 8);
                    cell0B.add(new Paragraph("YES I START UP TOSCANA").addStyle(bold));
                    cell0B.add(new Paragraph("Formarsi per diventare imprenditore/imprenditrice in Toscana").addStyle(bold));
                    cell0B.add(new Paragraph("CUP D54D23002380007").addStyle(bold));
                    cell0B.setTextAlignment(TextAlignment.CENTER);
                    table.addCell(cell0B);

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
                        cell.add(new Paragraph("N.UD - FASE B").addStyle(bold));
                        table.addCell(cell);
                        cell = new Cell(1, 2);
                        cell.add(new Paragraph("NUMERO GRUPPO FASE B").addStyle(bold));
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
                        cell.add(new Paragraph(String.valueOf(primo.getGruppofaseb())).addStyle(normal));
                        cell.setBackgroundColor(lightgrey);
                        cell.setTextAlignment(TextAlignment.CENTER);
                        cell.setTextAlignment(TextAlignment.CENTER);
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
                            cel3.add(new Paragraph(r1.getRuolo()).addStyle(normal));
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
                    ex.printStackTrace();
                }
                doc.add(table);
                if (indice1.get() < fb.size()) {
                    doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }
                indice1.addAndGet(1);

            });

            db1.closeDB();

            if (indice.get() > 1) {

                doc.close();
                pdfDoc.close();
                pw0.close();

                String qrcontent = "ID " + idpr + " REGISTRO COMPLESSIVO- AGGIORNATO IL " + now;
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
                    File pdf_final = new File(path_destinazione + File.separator + "Registro Complessivo_" + now1 + ".pdf");
                    FileUtils.copyFile(out2, pdf_final);
                    if (checkPDF(pdf_final)) {
                        out2.deleteOnExit();
                        if (save) {
                            Db_Gest db3 = new Db_Gest(host);
                            String sql = "SELECT iddocumenti_progetti FROM documenti_progetti WHERE idprogetto = " + idpr + " AND tipo = 33";
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
                                        ps1.setInt(3, 33);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

//    public File registro_complessivo(int idpr, String host, boolean save) {
//
//        List<File> temp = new ArrayList<>();
//        Db_Bando db = new Db_Bando(host);
//        try {
//            String sql = "SELECT * FROM documenti_progetti WHERE idprogetto = " + idpr + " AND tipo IN (29,32) AND deleted=0 ORDER BY tipo";
//            try (Statement st = db.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
//                while (rs.next()) {
//                    String path = rs.getString("path");
//                    File t1 = new File(path);
//                    if (Constant.checkPDF(t1)) {
//                        temp.add(t1);
//                    }
//                }
//            }
//
//            PdfDocument pdf = new PdfDocument(new PdfWriter("C:\\mnt\\mcn\\yisu_neet\\SoggettiAttuatori\\36\\Progetti\\82\\testing.pdf"));
//            PdfMerger merger = new PdfMerger(pdf);
//            temp.forEach(f1 -> {
//                try {
//                    PdfDocument firstSourcePdf = new PdfDocument(new PdfReader(f1.getPath()));
//                    merger.merge(firstSourcePdf, 1, firstSourcePdf.getNumberOfPages());
//                    firstSourcePdf.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            });
//            pdf.close();
//
//        } catch (Exception ex) {
//            Create.log.severe(Constant.estraiEccezione(ex));
//        }
//        db.closeDB();
//        return null;
//    }

//    public static void main(String[] args) {
//        
//        
//        
//        
//        
//        String sql = "SELECT idprogetti_formativi FROM progetti_formativi WHERE END < CURDATE() "
//                + "AND idprogetti_formativi NOT IN (SELECT idprogetto FROM documenti_progetti WHERE tipo=33)";
//        
//        
//        
//        
//        
//        
//        boolean testing = false;
//        int idpr = 96;
//        Complessivo c = new Complessivo(testing);
//        FaseA FA = new FaseA(testing);
//        FaseB FB = new FaseB(testing);
////        List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, false, false);
//        List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, false, false);
////        File output = c.registro_complessivo(idpr, c.host, ca, cb, false);
//        
//        
//    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
