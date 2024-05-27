import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.substring;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import rc.soop.exe.Items;
import rc.soop.exe.Utenti;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.getCell;
import static rc.soop.exe.Utils.getRow;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.setCell;
import static rc.soop.exe.Utils.timestamp;
import static rc.soop.gestione.Constant.roundDouble;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.Registro_completo;
import rc.soop.gestione.Toscana_gestione;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author Administrator
 */
public class Rendicontazione {

    private static final double costo_ora_docenza = 131.63;
    private static final String S_costo_ora_docenza = "131,63€";
    private static final double costo_ora_fasea = 0.90;
    private static final String S_costo_ora_fasea = "0,90€";
    private static final double costo_ora_faseb = 45;
    private static final String S_costo_ora_faseb = "45€";
    private static final SimpleDateFormat sdfITA = new SimpleDateFormat(patternITA);
    private static final String formatdataCell = "#,#.00";
    private static final String formatdataCellint = "#,#";
    private static final byte[] bianco = {(byte) 255, (byte) 255, (byte) 255};
    private static final byte[] color1 = {(byte) 49, (byte) 134, (byte) 155};
    private static final byte[] color2 = {(byte) 83, (byte) 141, (byte) 213};
    private static final byte[] color3 = {(byte) 197, (byte) 217, (byte) 241};
    private static final byte[] color4 = {(byte) 238, (byte) 30, (byte) 30};
    private static final byte[] color5 = {(byte) 0, (byte) 204, (byte) 0};
    private static final byte[] color6 = {(byte) 0, (byte) 128, (byte) 128};
    private static final byte[] color7 = {(byte) 192, (byte) 192, (byte) 192};
    private static final XSSFColor myColor1 = new XSSFColor(color1, new DefaultIndexedColorMap());
    private static final XSSFColor myColor2 = new XSSFColor(color2, new DefaultIndexedColorMap());
    private static final XSSFColor myColor3 = new XSSFColor(color3, new DefaultIndexedColorMap());
    private static final XSSFColor myColor4 = new XSSFColor(color4, new DefaultIndexedColorMap());
    private static final XSSFColor myColor5 = new XSSFColor(color5, new DefaultIndexedColorMap());
    private static final XSSFColor myColor6 = new XSSFColor(color6, new DefaultIndexedColorMap());
    private static final XSSFColor myColor7 = new XSSFColor(color7, new DefaultIndexedColorMap());
    private static final XSSFColor white = new XSSFColor(bianco, new DefaultIndexedColorMap());

    public static File prospetto_riepilogo(int idestrazione, List<Integer> list_idpr, Db_Gest db1) {
        File output_xlsx = null;
        DateTime oggi = new DateTime();
        String nomerend_cod = "R" + idestrazione;
        String nomerend = nomerend_cod + "_" + oggi.toString("ddMMyyyy");
        try {
            String pathdest = db1.getPath("output_excel_archive");
            String fileing = pathdest + "YISUT_Prospetto_Riepilogo_v1.xlsx";
            AtomicDouble total_ore = new AtomicDouble(0.0);
            AtomicDouble total_rend = new AtomicDouble(0.0);
            DateTime start_rend = null;
            DateTime end_rend = null;

            try (Connection conn = db1.getConnection()) {
                if (conn != null) {
                    try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {
                        XSSFSheet sh1 = wb.getSheet("Prospetto di riepilogo DdR XX");
                        wb.setSheetName(sh1.getWorkbook().getSheetIndex(sh1.getSheetName()), "Prospetto di riepilogo DdR " + nomerend_cod);

                        XSSFFont font_total = wb.createFont();
                        font_total.setFontHeightInPoints((short) 12);
                        font_total.setBold(true);

                        XSSFFont font_white = wb.createFont();
                        font_white.setFontHeightInPoints((short) 14);
                        font_white.setBold(true);
                        font_white.setColor(white);
                        XSSFFont font_int = wb.createFont();
                        font_int.setFontHeightInPoints((short) 14);
                        font_int.setBold(true);

                        XSSFCellStyle intestazione_1 = wb.createCellStyle();
                        intestazione_1.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_1.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_1.setBorderBottom(BorderStyle.THIN);
                        intestazione_1.setBorderTop(BorderStyle.THIN);
                        intestazione_1.setBorderRight(BorderStyle.THIN);
                        intestazione_1.setBorderLeft(BorderStyle.THIN);
                        intestazione_1.setFillForegroundColor(myColor1);
                        intestazione_1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_1.setFont(font_white);

                        XSSFCellStyle intestazione_2 = wb.createCellStyle();
                        intestazione_2.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_2.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_2.setBorderBottom(BorderStyle.THIN);
                        intestazione_2.setBorderTop(BorderStyle.THIN);
                        intestazione_2.setBorderRight(BorderStyle.THIN);
                        intestazione_2.setBorderLeft(BorderStyle.THIN);
                        intestazione_2.setFillForegroundColor(myColor2);
                        intestazione_2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_2.setFont(font_white);

                        XSSFCellStyle intestazione_2A = wb.createCellStyle();
                        intestazione_2A.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_2A.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_2A.setBorderBottom(BorderStyle.THIN);
                        intestazione_2A.setBorderTop(BorderStyle.THIN);
                        intestazione_2A.setBorderRight(BorderStyle.THIN);
                        intestazione_2A.setBorderLeft(BorderStyle.THIN);
                        intestazione_2A.setFillForegroundColor(myColor6);
                        intestazione_2A.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_2A.setFont(font_white);

                        XSSFCellStyle intestazione_2B = wb.createCellStyle();
                        intestazione_2B.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_2B.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_2B.setBorderBottom(BorderStyle.THIN);
                        intestazione_2B.setBorderTop(BorderStyle.THIN);
                        intestazione_2B.setBorderRight(BorderStyle.THIN);
                        intestazione_2B.setBorderLeft(BorderStyle.THIN);
                        intestazione_2B.setFillForegroundColor(myColor4);
                        intestazione_2B.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_2B.setFont(font_white);

                        XSSFCellStyle intestazione_3 = wb.createCellStyle();
                        intestazione_3.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_3.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_3.setBorderBottom(BorderStyle.THIN);
                        intestazione_3.setBorderTop(BorderStyle.THIN);
                        intestazione_3.setBorderRight(BorderStyle.THIN);
                        intestazione_3.setBorderLeft(BorderStyle.THIN);
                        intestazione_3.setFillForegroundColor(myColor3);
                        intestazione_3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_3.setFont(font_int);

                        XSSFCellStyle intestazione_4 = wb.createCellStyle();
                        intestazione_4.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_4.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_4.setBorderBottom(BorderStyle.THIN);
                        intestazione_4.setBorderTop(BorderStyle.THIN);
                        intestazione_4.setBorderRight(BorderStyle.THIN);
                        intestazione_4.setBorderLeft(BorderStyle.THIN);
                        intestazione_4.setFillForegroundColor(myColor4);
                        intestazione_4.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_4.setFont(font_white);

                        XSSFCellStyle intestazione_5 = wb.createCellStyle();
                        intestazione_5.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_5.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_5.setBorderBottom(BorderStyle.THIN);
                        intestazione_5.setBorderTop(BorderStyle.THIN);
                        intestazione_5.setBorderRight(BorderStyle.THIN);
                        intestazione_5.setBorderLeft(BorderStyle.THIN);
                        intestazione_5.setFillForegroundColor(myColor5);
                        intestazione_5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_5.setFont(font_white);

                        XSSFDataFormat xssfDataFormat = wb.createDataFormat();
                        XSSFCellStyle cellStyle_int = wb.createCellStyle();
                        cellStyle_int.setBorderBottom(BorderStyle.THIN);
                        cellStyle_int.setBorderTop(BorderStyle.THIN);
                        cellStyle_int.setBorderRight(BorderStyle.THIN);
                        cellStyle_int.setBorderLeft(BorderStyle.THIN);
                        cellStyle_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));

                        XSSFCellStyle cellStyle_double = wb.createCellStyle();
                        cellStyle_double.setBorderBottom(BorderStyle.THIN);
                        cellStyle_double.setBorderTop(BorderStyle.THIN);
                        cellStyle_double.setBorderRight(BorderStyle.THIN);
                        cellStyle_double.setBorderLeft(BorderStyle.THIN);
                        cellStyle_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));

                        XSSFCellStyle cs = wb.createCellStyle();
                        cs.setVerticalAlignment(VerticalAlignment.CENTER);
                        cs.setAlignment(HorizontalAlignment.CENTER);
                        cs.setBorderBottom(BorderStyle.THIN);
                        cs.setBorderTop(BorderStyle.THIN);
                        cs.setBorderRight(BorderStyle.THIN);
                        cs.setBorderLeft(BorderStyle.THIN);

                        XSSFCellStyle cstotal = wb.createCellStyle();
                        cstotal.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal.setAlignment(HorizontalAlignment.CENTER);
                        cstotal.setBorderBottom(BorderStyle.THIN);
                        cstotal.setBorderTop(BorderStyle.THIN);
                        cstotal.setBorderRight(BorderStyle.THIN);
                        cstotal.setBorderLeft(BorderStyle.THIN);
                        cstotal.setFillForegroundColor(myColor7);
                        cstotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal.setFont(font_total);

                        XSSFCellStyle cstotal_double = wb.createCellStyle();
                        cstotal_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_double.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_double.setBorderBottom(BorderStyle.THIN);
                        cstotal_double.setBorderTop(BorderStyle.THIN);
                        cstotal_double.setBorderRight(BorderStyle.THIN);
                        cstotal_double.setBorderLeft(BorderStyle.THIN);
                        cstotal_double.setFillForegroundColor(myColor7);
                        cstotal_double.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));
                        cstotal_double.setFont(font_total);

                        XSSFCellStyle cstotal_int = wb.createCellStyle();

                        cstotal_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_int.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_int.setBorderBottom(BorderStyle.THIN);
                        cstotal_int.setBorderTop(BorderStyle.THIN);
                        cstotal_int.setBorderRight(BorderStyle.THIN);
                        cstotal_int.setBorderLeft(BorderStyle.THIN);
                        cstotal_int.setFillForegroundColor(myColor7);
                        cstotal_int.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));
                        cstotal_int.setFont(font_total);

                        AtomicInteger index_row = new AtomicInteger(9);
                        AtomicInteger indice = new AtomicInteger(1);
                        for (int ss = 0; ss < list_idpr.size(); ss++) {

                            int idpr = list_idpr.get(ss);

                            String sql1 = "SELECT p.idprogetti_formativi,p.cip,s.ragionesociale,c.regione,p.start,p.end "
                                    + "FROM progetti_formativi p, soggetti_attuatori s,comuni c WHERE p.stato='CO' "
                                    + "AND p.idsoggetti_attuatori=s.idsoggetti_attuatori AND c.idcomune=s.comune AND p.idprogetti_formativi = " + idpr;

                            try (Statement st1 = conn.createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                                if (rs1.next()) {

                                    String cip = rs1.getString(2).toUpperCase();
                                    String ragionesociale = rs1.getString(3).toUpperCase();
                                    String start = sdfITA.format(rs1.getDate(5));
                                    String end = sdfITA.format(rs1.getDate(6));

                                    if (start_rend == null || start_rend.isAfter(new DateTime(rs1.getDate(5).getTime()))) {
                                        start_rend = new DateTime(rs1.getDate(5).getTime());
                                    }
                                    if (end_rend == null || end_rend.isBefore(new DateTime(rs1.getDate(6).getTime()))) {
                                        end_rend = new DateTime(rs1.getDate(6).getTime());
                                    }

                                    List<Registro_completo> registrocompleto = db1.registro_modello6(idpr);
                                    List<Utenti> allievi_OK = db1.list_Allievi_OK(idpr);
                                    List<Utenti> docenti_tab = db1.list_Docenti(idpr);
                                    List<Items> calendario = db1.calendario(idpr);
                                    LinkedList<Items> calendarioA = calendario.stream().filter(l1
                                            -> l1.getFase().endsWith("A")).collect(Collectors.toCollection(LinkedList::new));

                                    LinkedList<Items> calendarioB = calendario.stream().filter(l1
                                            -> l1.getFase().endsWith("B")).collect(Collectors.toCollection(LinkedList::new));

                                    HashMap<Integer, String> bus_pla = new HashMap<>();
                                    String sql1A = "SELECT m.allievo,m.businessplan_presente FROM maschera_m5 m WHERE m.progetto_formativo = " + idpr;
                                    try (Statement st2 = conn.createStatement(); ResultSet rs1A = st2.executeQuery(sql1A)) {
                                        while (rs1A.next()) {
                                            bus_pla.put(rs1A.getInt(1), rs1A.getString(2));
                                        }
                                    }

                                    String DATAINIZIOFASEA = calendarioA.getFirst().getData();
                                    String DATAFINEFASEA = calendarioA.getLast().getData();

                                    List<Registro_completo> faseA = registrocompleto.stream().filter(r1
                                            -> r1.getRuolo().contains("ALLIEVO")
                                            && r1.getFase().equalsIgnoreCase("A"))
                                            .collect(Collectors.toList());

                                    List<Registro_completo> faseB = registrocompleto.stream().filter(r1
                                            -> r1.getRuolo().contains("ALLIEVO")
                                            && r1.getFase().equalsIgnoreCase("B"))
                                            .collect(Collectors.toList());

                                    int numpartecipanti = allievi_OK.size();

                                    XSSFSheet sh_pr = wb.createSheet(cip);

                                    AtomicInteger indici_docenti = new AtomicInteger(15 + numpartecipanti);

                                    //docenti
                                    XSSFRow row_docenti = getRow(sh_pr, indici_docenti.get());
                                    CellRangeAddress region_20 = new CellRangeAddress(row_docenti.getRowNum(), row_docenti.getRowNum(), 5, (12 + calendarioA.size()));
                                    cleanBeforeMergeOnValidCells(sh_pr, region_20, cs);
                                    sh_pr.addMergedRegion(region_20);

                                    setCell(getCell(row_docenti, 5), intestazione_1, "DOCENTI FASE A", false, false);

                                    indici_docenti.addAndGet(1);
                                    XSSFRow row_docenti2 = getRow(sh_pr, indici_docenti.get());

                                    setCell(getCell(row_docenti2, 5), cs, "N.", false, false);
                                    CellRangeAddress region_21 = new CellRangeAddress(row_docenti2.getRowNum(), row_docenti2.getRowNum(), 6, 7);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_21, cs);
                                    sh_pr.addMergedRegion(region_21);
                                    setCell(getCell(row_docenti2, 6), cs, "COGNOME", false, false);
                                    setCell(getCell(row_docenti2, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_docenti2, 9), cs, "CODICE FISCALE", false, false);
                                    setCell(getCell(row_docenti2, 10), cs, "COSTO ORA/DOCENZA (" + S_costo_ora_docenza + ")", false, false);

                                    for (int x = 0; x < calendarioA.size(); x++) {
                                        String cal1 = calendarioA.get(x).getData();
                                        setCell(getCell(row_docenti2, 11 + x), cs, cal1, false, false);
                                    }

                                    setCell(getCell(row_docenti2, 11 + calendarioA.size()), cs, "TOTALE ORE (MAX 60h)", false, false);
                                    setCell(getCell(row_docenti2, 12 + calendarioA.size()), cs, "TOTALE IMPORTO DOCENZA FASE A", false, false);

                                    indici_docenti.addAndGet(1);

                                    AtomicInteger numdocenti = new AtomicInteger(0);
                                    AtomicDouble tot_ore_docenti = new AtomicDouble(0.0);
                                    AtomicDouble tot_docenti = new AtomicDouble(0.0);

                                    List<Registro_completo> docentifaseA = registrocompleto.stream().filter(r1
                                            -> r1.getRuolo().equalsIgnoreCase("DOCENTE")
                                            && r1.getFase().equalsIgnoreCase("A")).collect(Collectors.toList());

                                    List<Integer> docentiid = docentifaseA.stream().map(r1
                                            -> r1.getIdutente()).distinct().collect(Collectors.toList());

                                    docentiid.forEach(r1 -> {
                                        Utenti docente = docenti_tab.stream().filter(d1 -> d1.getId() == r1).findAny().orElse(null);
                                        if (docente != null) {
                                            numdocenti.addAndGet(1);
                                            XSSFRow row_d = getRow(sh_pr, indici_docenti.get());
                                            setCell(getCell(row_d, 5), cs, String.valueOf(numdocenti.get()), true, false);

                                            CellRangeAddress region_22 = new CellRangeAddress(row_d.getRowNum(), row_d.getRowNum(), 6, 7);
                                            cleanBeforeMergeOnValidCells(sh_pr, region_22, cs);
                                            sh_pr.addMergedRegion(region_22);

                                            setCell(getCell(row_d, 6), cs, docente.getCognome(), false, false);

                                            setCell(getCell(row_d, 8), cs, docente.getNome(), false, false);
                                            setCell(getCell(row_d, 9), cs, docente.getCf(), false, false);
                                            setCell(getCell(row_d, 10), cellStyle_double, String.valueOf(costo_ora_docenza), false, true);

                                            AtomicDouble tot_ore_fase_A = new AtomicDouble(0.0);

                                            for (int x = 0; x < calendarioA.size(); x++) {
                                                String cal1 = calendarioA.get(x).getData();
                                                List<Registro_completo> docente_ore = docentifaseA.stream().filter(
                                                        r3 -> r3.getIdutente() == r1 && r3.getData().toString(patternITA).equals(cal1))
                                                        .collect(Collectors.toList());

                                                if (!docente_ore.isEmpty()) {
                                                    long ore = 0L;

                                                    for (Registro_completo rr : docente_ore) {
                                                        ore += rr.getTotaleorerendicontabili();
                                                    }

                                                    setCell(getCell(row_d, 11 + x),
                                                            cellStyle_double,
                                                            String.valueOf(roundDouble(ore, true)),
                                                            false, true);
                                                    tot_ore_fase_A.addAndGet(roundDouble(ore, true));
                                                } else {
                                                    setCell(getCell(row_d, 11 + x),
                                                            cellStyle_double,
                                                            "0.00",
                                                            false, true);
                                                }

                                            }

                                            setCell(getCell(row_d, (11 + calendarioA.size())), cellStyle_double,
                                                    String.valueOf(tot_ore_fase_A.get()),
                                                    false, true);

                                            double tot_d1 = tot_ore_fase_A.get() * costo_ora_docenza;

                                            tot_docenti.addAndGet(tot_d1);
                                            tot_ore_docenti.addAndGet(tot_ore_fase_A.get());
                                            setCell(getCell(row_d, (12 + calendarioA.size())), cellStyle_double, String.valueOf(tot_d1), false, true);

                                            indici_docenti.addAndGet(1);

                                        }
                                    });

                                    XSSFRow row_dt = getRow(sh_pr, indici_docenti.get());
                                    setCell(getCell(row_dt, (11 + calendarioA.size())), cstotal_double,
                                            String.valueOf(tot_ore_docenti.get()), false, true);
                                    setCell(getCell(row_dt, (12 + calendarioA.size())), cstotal_double,
                                            String.valueOf(tot_docenti.get()), false, true);

                                    //ALLIEVI
                                    XSSFRow row_intest = getRow(sh_pr, 2);
                                    XSSFRow row_intest2 = getRow(sh_pr, 3);
                                    XSSFRow row_intest3 = getRow(sh_pr, 4);
                                    XSSFRow row_intest4 = getRow(sh_pr, 5);
                                    XSSFRow row_intest5 = getRow(sh_pr, 6);

                                    CellRangeAddress region_1 = new CellRangeAddress(2, 3, 1, 6);
                                    CellRangeAddress region_2 = new CellRangeAddress(2, 3, 7, 11);
                                    CellRangeAddress region_3 = new CellRangeAddress(2, 2, 12, 14 + calendarioA.size());
                                    CellRangeAddress region_4 = new CellRangeAddress(4, 6, 1, 1);
                                    CellRangeAddress region_5 = new CellRangeAddress(4, 6, 2, 2);
                                    CellRangeAddress region_6 = new CellRangeAddress(4, 6, 3, 3);
                                    CellRangeAddress region_7 = new CellRangeAddress(4, 6, 4, 4);
                                    CellRangeAddress region_8 = new CellRangeAddress(4, 6, 5, 5);
                                    CellRangeAddress region_9 = new CellRangeAddress(4, 6, 6, 6);
                                    CellRangeAddress region_10 = new CellRangeAddress(4, 6, 7, 7);
                                    CellRangeAddress region_11 = new CellRangeAddress(4, 6, 8, 8);
                                    CellRangeAddress region_12 = new CellRangeAddress(4, 6, 9, 9);
                                    CellRangeAddress region_13 = new CellRangeAddress(4, 6, 10, 10);
                                    CellRangeAddress region_14a = new CellRangeAddress(4, 6, 11, 11);

                                    cleanBeforeMergeOnValidCells(sh_pr, region_1, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_2, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_3, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_4, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_5, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_6, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_7, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_8, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_9, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_10, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_11, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_12, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_13, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_14a, cs);

                                    sh_pr.addMergedRegion(region_1);
                                    sh_pr.addMergedRegion(region_2);
                                    sh_pr.addMergedRegion(region_3);
                                    sh_pr.addMergedRegion(region_4);
                                    sh_pr.addMergedRegion(region_5);
                                    sh_pr.addMergedRegion(region_6);
                                    sh_pr.addMergedRegion(region_7);
                                    sh_pr.addMergedRegion(region_8);
                                    sh_pr.addMergedRegion(region_9);
                                    sh_pr.addMergedRegion(region_10);
                                    sh_pr.addMergedRegion(region_11);
                                    sh_pr.addMergedRegion(region_12);
                                    sh_pr.addMergedRegion(region_13);
                                    sh_pr.addMergedRegion(region_14a);

                                    setCell(getCell(row_intest, 1), intestazione_1, "ANAGRAFICA PERCORSO", false, false);
                                    setCell(getCell(row_intest, 7), intestazione_2, "ANAGRAFICA PARTECIPANTI", false, false);
                                    setCell(getCell(row_intest, 12), intestazione_3, "FASE A DATE", false, false);
                                    setCell(getCell(row_intest3, 1), cs, "N.", false, false);
                                    setCell(getCell(row_intest3, 2), cs, "ID", false, false);
                                    setCell(getCell(row_intest3, 3), cs, "CIP", false, false);
                                    setCell(getCell(row_intest3, 4), cs, "SA", false, false);
                                    setCell(getCell(row_intest3, 5), cs, "DATA INIZIO CORSO", false, false);
                                    setCell(getCell(row_intest3, 6), cs, "DATA FINE CORSO", false, false);
                                    setCell(getCell(row_intest3, 7), cs, "COGNOME", false, false);
                                    setCell(getCell(row_intest3, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_intest3, 9), cs, "CODICE FISCALE", false, false);
                                    setCell(getCell(row_intest3, 10), cs, "PATTO DEL LAVORO/GOL", false, false);
                                    setCell(getCell(row_intest3, 11), cs, "BUSINESS PLAN (SI/NO)", false, false);

                                    for (int x = 0; x < calendarioA.size(); x++) {
                                        Items it1 = calendarioA.get(x);
                                        String cal1 = it1.getData();
                                        setCell(getCell(row_intest3, 12 + x), cs, cal1, false, false);
                                        setCell(getCell(row_intest4, 12 + x), cs, substring(it1.getOrainizio(), 0, 5), false, false);
                                        setCell(getCell(row_intest5, 12 + x), cs, substring(it1.getOrafine(), 0, 5), false, false);
                                    }

                                    CellRangeAddress region_14 = new CellRangeAddress(3, 3, calendarioA.size() - 1, 14 + calendarioA.size());
//                                    CellRangeAddress region_15 = new CellRangeAddress(4, 6, 11 + calendarioA.size(), 11 + calendarioA.size());
                                    CellRangeAddress region_16 = new CellRangeAddress(4, 6, 12 + calendarioA.size(), 12 + calendarioA.size());
                                    CellRangeAddress region_17 = new CellRangeAddress(4, 6, 13 + calendarioA.size(), 13 + calendarioA.size());
                                    CellRangeAddress region_18 = new CellRangeAddress(4, 6, 14 + calendarioA.size(), 14 + calendarioA.size());

                                    cleanBeforeMergeOnValidCells(sh_pr, region_14, cs);
//                                    cleanBeforeMergeOnValidCells(sh_pr, region_15, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_16, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_17, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_18, cs);

                                    sh_pr.addMergedRegion(region_14);
//                                    sh_pr.addMergedRegion(region_15);
                                    sh_pr.addMergedRegion(region_16);
                                    sh_pr.addMergedRegion(region_17);
                                    sh_pr.addMergedRegion(region_18);

                                    setCell(getCell(row_intest2, calendarioA.size() - 1), intestazione_3, "DAL " + DATAINIZIOFASEA + " AL " + DATAFINEFASEA, false, false);
                                    setCell(getCell(row_intest3, 12 + calendarioA.size()), cs, "TOTALE ORE (MAX 60h)", false, false);
                                    setCell(getCell(row_intest3, 13 + calendarioA.size()), cs, "TOTALE ORE * " + S_costo_ora_fasea, false, false);
                                    setCell(getCell(row_intest3, 14 + calendarioA.size()), cs, "TOTALE FASE A DOCENZA", false, false);

                                    List<String> elencogruppi_B = calendario.stream().filter(it1 -> it1.getFase().equals("B"))
                                            .map(Items::getGruppo).distinct().sorted().collect(Collectors.toList());

                                    AtomicInteger iniziofaseb = new AtomicInteger(15 + calendarioA.size());

                                    HashMap<String, Integer> indicifaseB = new HashMap<>();
                                    HashMap<String, Double> totaliFaseB = new HashMap<>();

                                    int iniziofaseb_i;
                                    for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                        String numerogruppo = elencogruppi_B.get(ix);
                                        iniziofaseb_i = iniziofaseb.get();
                                        indicifaseB.put(numerogruppo, iniziofaseb_i);

                                        LinkedList<Items> calendarioB2 = calendarioB.stream().filter(l1
                                                -> l1.getFase().endsWith("B") && l1.getGruppo().equals(numerogruppo))
                                                .collect(Collectors.toCollection(LinkedList::new));

                                        String datainizioB = calendarioB2.getFirst().getData();
                                        String datafineB = calendarioB2.getLast().getData();

                                        for (int x = 0; x < calendarioB2.size(); x++) {
                                            Items it1 = calendarioB2.get(x);
                                            setCell(getCell(row_intest3, iniziofaseb.get()), cs, it1.getData(), false, false);
                                            setCell(getCell(row_intest4, iniziofaseb.get()), cs, substring(it1.getOrainizio(), 0, 5), false, false);
                                            setCell(getCell(row_intest5, iniziofaseb.get()), cs, substring(it1.getOrafine(), 0, 5), false, false);
                                            iniziofaseb.addAndGet(1);
                                        }
                                        iniziofaseb.addAndGet(2);

                                        CellRangeAddress region_B1 = new CellRangeAddress(2, 2, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B1, cs);
                                        sh_pr.addMergedRegion(region_B1);
                                        CellRangeAddress region_B2 = new CellRangeAddress(3, 3, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B2, cs);
                                        sh_pr.addMergedRegion(region_B2);
                                        setCell(getCell(row_intest, iniziofaseb_i), intestazione_2, "FASE B GRUPPO " + numerogruppo + " DATE", false, false);
                                        setCell(getCell(row_intest2, iniziofaseb_i), intestazione_2, "DAL " + datainizioB + " AL " + datafineB, false, false);
                                        CellRangeAddress region_B3 = new CellRangeAddress(4, 6, iniziofaseb.get() - 2, iniziofaseb.get() - 2);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B3, cs);
                                        sh_pr.addMergedRegion(region_B3);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 2), cs, "TOTALE ORE (MAX 40h)", false, false);
                                        CellRangeAddress region_B4 = new CellRangeAddress(4, 6, iniziofaseb.get() - 1, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B4, cs);
                                        sh_pr.addMergedRegion(region_B4);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 1), cs, "TOTALE ORE FASE B * " + S_costo_ora_faseb, false, false);

                                    }

                                    AtomicInteger index_allievo = new AtomicInteger(7);

                                    AtomicDouble componente_allievi_A = new AtomicDouble(0.0);
                                    AtomicDouble componente_docenti_A = new AtomicDouble(0.0);

                                    AtomicDouble componente_pdl_a = new AtomicDouble(0.0);
                                    AtomicDouble componente_gol_a = new AtomicDouble(0.0);
                                    AtomicDouble componente_pdl_d = new AtomicDouble(0.0);
                                    AtomicDouble componente_gol_d = new AtomicDouble(0.0);

                                    for (int y = 0; y < allievi_OK.size(); y++) {
                                        Utenti allievo = allievi_OK.get(y);
//                                        if (rc != null) {
                                        int indicepartenzafaseBallievo = 0;

                                        XSSFRow row_allievo = getRow(sh_pr, index_allievo.get());
                                        setCell(getCell(row_allievo, 1), cellStyle_int, String.valueOf(index_allievo.get() - 6), true, false);
                                        setCell(getCell(row_allievo, 2), cellStyle_int, String.valueOf(idpr), true, false);
                                        setCell(getCell(row_allievo, 3), cs, cip, false, false);
                                        setCell(getCell(row_allievo, 4), cs, ragionesociale, false, false);
                                        setCell(getCell(row_allievo, 5), cs, start, false, false);
                                        setCell(getCell(row_allievo, 6), cs, end, false, false);
                                        setCell(getCell(row_allievo, 7), cs, allievo.getCognome().toUpperCase(), false, false);
                                        setCell(getCell(row_allievo, 8), cs, allievo.getNome().toUpperCase(), false, false);
                                        setCell(getCell(row_allievo, 9), cs, allievo.getCf().toUpperCase(), false, false);

                                        String tos_tipofinanziamento = "";
                                        String sql1B = "SELECT a.tos_tipofinanziamento FROM allievi a WHERE a.idallievi = " + allievo.getId();
                                        try (Statement st2 = conn.createStatement(); ResultSet rs1A = st2.executeQuery(sql1B)) {
                                            if (rs1A.next()) {
                                                tos_tipofinanziamento = rs1A.getString(1);
                                                String tp = rs1A.getString(1).equals("PATTO") ? "PdL" : "GOL";
                                                setCell(getCell(row_allievo, 10), cs, tp, false, false);
                                            }
                                        }

                                        if (bus_pla.get(allievo.getId()) == null) {
                                            setCell(getCell(row_allievo, 11), cs, "NO", false, false);
                                        } else {
                                            if (bus_pla.get(allievo.getId()).equals("1")) {
                                                setCell(getCell(row_allievo, 11), cs, "SI", false, false);
                                            } else {
                                                setCell(getCell(row_allievo, 11), cs, "NO", false, false);
                                            }
                                        }

                                        AtomicDouble a_tot_ore = new AtomicDouble(0.0);
                                        AtomicDouble b_tot_ore = new AtomicDouble(0.0);

                                        AtomicDouble a_tot_al = new AtomicDouble(0.0);
                                        AtomicDouble b_tot_al = new AtomicDouble(0.0);

                                        AtomicDouble d_tot = new AtomicDouble(0.0);

                                        //FASE A
                                        for (int x = 0; x < calendarioA.size(); x++) {
                                            Items it1 = calendarioA.get(x);
                                            String cal1 = it1.getData();

                                            List<Registro_completo> rc_A = faseA.stream().filter(
                                                    a1 -> a1.getIdutente() == allievo.getId() && a1.getData().toString(patternITA).equals(cal1))
                                                    .collect(Collectors.toList());

                                            if (!rc_A.isEmpty()) {
                                                long ore = 0L;

                                                for (Registro_completo rr : rc_A) {
                                                    ore += rr.getTotaleorerendicontabili();
                                                }

                                                setCell(getCell(row_allievo, 12 + x),
                                                        cellStyle_double,
                                                        String.valueOf(roundDouble(ore, true)),
                                                        false, true);
                                                a_tot_ore.addAndGet(roundDouble(ore, true));
                                            } else {
                                                setCell(getCell(row_allievo, 12 + x),
                                                        cellStyle_double,
                                                        "0.0",
                                                        false, true);
                                            }

                                        }

                                        setCell(getCell(row_allievo, 12 + calendarioA.size()),
                                                cellStyle_double,
                                                String.valueOf(a_tot_ore.get()),
                                                false, true);

                                        a_tot_al.addAndGet(a_tot_ore.get() * costo_ora_fasea);
                                        componente_allievi_A.addAndGet(a_tot_ore.get() * costo_ora_fasea);
                                        setCell(getCell(row_allievo, 13 + calendarioA.size()),
                                                cellStyle_double,
                                                String.valueOf(a_tot_ore.get() * costo_ora_fasea),
                                                false, true);

                                        d_tot.addAndGet(tot_docenti.get() / numpartecipanti);
                                        componente_docenti_A.addAndGet(tot_docenti.get() / numpartecipanti);
                                        setCell(getCell(row_allievo, 14 + calendarioA.size()),
                                                cellStyle_double,
                                                String.valueOf(tot_docenti.get() / numpartecipanti),
                                                false, true);

//                                        System.out.println("Rendicontazione.prospetto_riepilogo() " + indicifaseB.toString());
                                        //FASE B
                                        for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                            String numerogruppo = elencogruppi_B.get(ix);
                                            if (numerogruppo.equals(allievo.getGruppofaseB())) {

                                                indicepartenzafaseBallievo = indicifaseB.get(numerogruppo);

                                                LinkedList<Items> calendarioB2 = calendarioB.stream().filter(l1
                                                        -> l1.getFase().endsWith("B") && l1.getGruppo().equals(numerogruppo))
                                                        .collect(Collectors.toCollection(LinkedList::new));

                                                for (int x = 0; x < calendarioB2.size(); x++) {
                                                    Items it1 = calendarioB2.get(x);

                                                    List<Registro_completo> rc_B = faseB.stream().filter(
                                                            a1 -> a1.getIdutente() == allievo.getId() && a1.getData().toString(patternITA).equals(it1.getData()))
                                                            .collect(Collectors.toList());

                                                    if (!rc_B.isEmpty()) {
                                                        long ore = 0L;

                                                        for (Registro_completo rr : rc_B) {
                                                            ore += rr.getTotaleorerendicontabili();
                                                        }

                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + x),
                                                                cellStyle_double,
                                                                String.valueOf(roundDouble(ore, true)),
                                                                false, true);

                                                        b_tot_ore.addAndGet(roundDouble(ore, true));
                                                    } else {
                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + x),
                                                                cellStyle_double,
                                                                "0.0",
                                                                false, true);
                                                    }

                                                }

                                                setCell(getCell(row_allievo, indicepartenzafaseBallievo + calendarioB2.size()),
                                                        cellStyle_double,
                                                        String.valueOf(b_tot_ore.get()),
                                                        false, true);

                                                b_tot_al.addAndGet(b_tot_ore.get() * costo_ora_faseb);

                                                setCell(getCell(row_allievo, indicepartenzafaseBallievo + calendarioB2.size() + 1),
                                                        cellStyle_double,
                                                        String.valueOf(b_tot_ore.get() * costo_ora_faseb),
                                                        false, true);

                                                if (totaliFaseB.get(numerogruppo) == null) {
                                                    totaliFaseB.put(numerogruppo, b_tot_ore.get() * costo_ora_faseb);
                                                } else {
                                                    totaliFaseB.put(numerogruppo, totaliFaseB.get(numerogruppo) + (b_tot_ore.get() * costo_ora_faseb));
                                                }

                                            }

                                        }

                                        //
                                        setCell(getCell(row_allievo, iniziofaseb.get()),
                                                cellStyle_double,
                                                String.valueOf(a_tot_ore.get() + b_tot_ore.get()),
                                                false, true);

                                        double a_pdl = 0.0;
                                        double a_gol = 0.0;
                                        double d_pdl = 0.0;
                                        double d_gol = 0.0;

                                        if (tos_tipofinanziamento.contains("PATTO")) {
                                            a_pdl = a_tot_al.get() + b_tot_al.get();
                                            d_pdl = d_tot.get();
                                        } else {
                                            a_gol = a_tot_al.get() + b_tot_al.get();
                                            d_gol = d_tot.get();
                                        }

                                        componente_pdl_a.addAndGet(a_pdl);
                                        componente_gol_a.addAndGet(a_gol);
                                        componente_pdl_d.addAndGet(d_pdl);
                                        componente_gol_d.addAndGet(d_gol);

                                        setCell(getCell(row_allievo, iniziofaseb.get() + 1),
                                                cellStyle_double,
                                                String.valueOf(a_pdl),
                                                false, true);
                                        setCell(getCell(row_allievo, iniziofaseb.get() + 2),
                                                cellStyle_double,
                                                String.valueOf(a_gol),
                                                false, true);
                                        setCell(getCell(row_allievo, iniziofaseb.get() + 3),
                                                cellStyle_double,
                                                String.valueOf(d_pdl),
                                                false, true);
                                        setCell(getCell(row_allievo, iniziofaseb.get() + 4),
                                                cellStyle_double,
                                                String.valueOf(d_gol),
                                                false, true);

                                        index_allievo.addAndGet(1);

                                        //PRIMO FOGLIO
                                        XSSFRow row = getRow(sh1, index_row.get());
                                        setCell(getCell(row, 0), cellStyle_int, String.valueOf(indice.get()), true, false);
                                        setCell(getCell(row, 1), cellStyle_int, String.valueOf(idpr), true, false);
                                        setCell(getCell(row, 2), cs, cip, false, false);
                                        setCell(getCell(row, 3), cs, ragionesociale, false, false);
                                        setCell(getCell(row, 4), cs, allievo.getCognome(), false, false);
                                        setCell(getCell(row, 5), cs, allievo.getNome(), false, false);
                                        setCell(getCell(row, 6), cs, allievo.getCf(), false, false);
                                        setCell(getCell(row, 7), cs, start, false, false);
                                        setCell(getCell(row, 8), cs, end, false, false);
                                        setCell(getCell(row, 9),
                                                cellStyle_double,
                                                String.valueOf(a_tot_ore.get() + b_tot_ore.get()),
                                                false, true);
                                        total_ore.addAndGet(a_tot_ore.get() + b_tot_ore.get());
                                        setCell(getCell(row, 10), cs, tos_tipofinanziamento, false, false);
                                        setCell(getCell(row, 11), cellStyle_double, String.valueOf(a_tot_al.get() + b_tot_al.get() + d_tot.get()), false, true);
                                        total_rend.addAndGet(a_tot_al.get() + b_tot_al.get() + d_tot.get());
                                        index_row.addAndGet(1);
                                        indice.addAndGet(1);

                                    }

                                    //RIGA TOTALI CIP
                                    XSSFRow row_total = getRow(sh_pr, index_allievo.get());

                                    for (int x = 1; x <= iniziofaseb.get() + 4; x++) {
                                        if (x == 13 + calendarioA.size()) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_allievi_A.get()), false, true);
                                        } else if (x == 14 + calendarioA.size()) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_docenti_A.get()), false, true);
                                        } else if (x == iniziofaseb.get() + 1) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_pdl_a.get()), false, true);
                                        } else if (x == iniziofaseb.get() + 2) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_gol_a.get()), false, true);
                                        } else if (x == iniziofaseb.get() + 3) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_pdl_d.get()), false, true);
                                        } else if (x == iniziofaseb.get() + 4) {
                                            setCell(getCell(row_total, x), cstotal_double, String.valueOf(componente_gol_d.get()), false, true);
                                        } else {
                                            setCell(getCell(row_total, x), cstotal, " ", false, false);
                                            for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                                String numerogruppo = elencogruppi_B.get(ix);
                                                if (indicifaseB.get(numerogruppo) != null) {
                                                    LinkedList<Items> calendarioB2 = calendarioB.stream().filter(l1
                                                            -> l1.getFase().endsWith("B") && l1.getGruppo().equals(numerogruppo))
                                                            .collect(Collectors.toCollection(LinkedList::new));
                                                    if (x == indicifaseB.get(numerogruppo) + calendarioB2.size() + 1) {
                                                        setCell(getCell(row_total, x), cstotal_double, String.valueOf(totaliFaseB.get(numerogruppo)), false, true);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    CellRangeAddress region_C1 = new CellRangeAddress(2, 3, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C1, cs);
                                    sh_pr.addMergedRegion(region_C1);
                                    CellRangeAddress region_C2 = new CellRangeAddress(4, 6, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C2, cs);
                                    sh_pr.addMergedRegion(region_C2);

                                    setCell(getCell(row_intest, iniziofaseb.get()), intestazione_2A, "TOTALE ORE", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get()), cs, "FASE A + FASE B", false, false);

                                    CellRangeAddress region_C3 = new CellRangeAddress(2, 3, iniziofaseb.get() + 1, iniziofaseb.get() + 4);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C3, cs);
                                    sh_pr.addMergedRegion(region_C3);

                                    CellRangeAddress region_C4a = new CellRangeAddress(4, 6, iniziofaseb.get() + 1, iniziofaseb.get() + 1);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4a, cs);
                                    sh_pr.addMergedRegion(region_C4a);
                                    CellRangeAddress region_C4b = new CellRangeAddress(4, 6, iniziofaseb.get() + 2, iniziofaseb.get() + 2);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4b, cs);
                                    sh_pr.addMergedRegion(region_C4b);
                                    CellRangeAddress region_C4c = new CellRangeAddress(4, 6, iniziofaseb.get() + 3, iniziofaseb.get() + 3);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4c, cs);
                                    sh_pr.addMergedRegion(region_C4c);
                                    CellRangeAddress region_C4d = new CellRangeAddress(4, 6, iniziofaseb.get() + 4, iniziofaseb.get() + 4);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4d, cs);
                                    sh_pr.addMergedRegion(region_C4d);

                                    setCell(getCell(row_intest, iniziofaseb.get() + 1), intestazione_2B, "TOTALE RIMBORSO", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 1), cs, "TOTALE PDL ALLIEVO", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 2), cs, "TOTALE GOL ALLIEVO", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 3), cs, "TOTALE PDL DOCENZA", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 4), cs, "TOTALE GOL DOCENZA", false, false);

                                    XSSFRow row_recap = getRow(sh_pr, index_allievo.get() + 2);
                                    setCell(getCell(row_recap, 7), intestazione_5, "TOTALE PARTECIPANTI", false, false);
                                    setCell(getCell(row_recap, 8), cstotal_int, String.valueOf(numpartecipanti), true, false);
                                    XSSFRow row_recap1 = getRow(sh_pr, index_allievo.get() + 3);
                                    setCell(getCell(row_recap1, 7), intestazione_5, "TOTALE IMPORTO CORSO €", false, false);
                                    setCell(getCell(row_recap1, 8), cstotal_double, String.valueOf((componente_pdl_a.get() + componente_gol_a.get() + componente_pdl_d.get() + componente_gol_d.get())), false, true);
                                    XSSFRow row_recap2 = getRow(sh_pr, index_allievo.get() + 4);
                                    setCell(getCell(row_recap2, 7), intestazione_5, "TOTALE IMPORTO PDL €", false, false);
                                    setCell(getCell(row_recap2, 8), cstotal_double, String.valueOf(componente_pdl_a.get() + componente_pdl_d.get()), false, true);
                                    XSSFRow row_recap3 = getRow(sh_pr, index_allievo.get() + 5);
                                    setCell(getCell(row_recap3, 7), intestazione_5, "TOTALE IMPORTO GOL €", false, false);
                                    setCell(getCell(row_recap3, 8), cstotal_double, String.valueOf(componente_gol_a.get() + componente_gol_d.get()), false, true);

                                    for (int ix = 1; ix < iniziofaseb.get() + 10; ix++) {
                                        sh_pr.autoSizeColumn(ix);
                                    }

                                }
                            }

                        }

                        XSSFRow row_total = getRow(sh1, index_row.get());

                        setCell(getCell(row_total, 0), cstotal, "", false, false);
                        setCell(getCell(row_total, 1), cstotal, "", false, false);
                        setCell(getCell(row_total, 2), cstotal, "", false, false);
                        setCell(getCell(row_total, 3), cstotal, "", false, false);
                        setCell(getCell(row_total, 4), cstotal, "", false, false);
                        setCell(getCell(row_total, 5), cstotal, "", false, false);
                        setCell(getCell(row_total, 6), cstotal, "", false, false);
                        setCell(getCell(row_total, 7), cstotal, "", false, false);
                        setCell(getCell(row_total, 8), cstotal, "", false, false);
                        setCell(getCell(row_total, 9), cstotal_double, String.valueOf(total_ore.get()), false, true);
                        setCell(getCell(row_total, 10), cstotal, "", false, false);
                        setCell(getCell(row_total, 11), cstotal_double, String.valueOf(total_rend.get()), false, true);
                        index_row.addAndGet(1);

                        String contentlast = "LUOGO                                                    DATA                                         firma del legale rappresentante  o suo delegato                                                             timbro";
                        XSSFRow row_LAST = getRow(sh1, index_row.get());
                        setCell(getCell(row_LAST, 0), contentlast.toUpperCase());
                        sh1.addMergedRegion(new CellRangeAddress(index_row.get(), index_row.get(), 0, 11));

                        for (int i = 0; i < 13; i++) {
                            sh1.autoSizeColumn(i);
                        }

//                        output_xlsx = new File(pathdest + "/Prospetto_Riepilogo_" + new DateTime().toString("yyyyMMdd") + ".xlsx");
                        output_xlsx = new File(pathdest + "/" + nomerend + "_Riepilogo_" + new DateTime().toString(timestamp) + ".xlsx");

                        try (FileOutputStream outputStream = new FileOutputStream(output_xlsx)) {
                            wb.write(outputStream);
                            System.out.println(output_xlsx.getPath());
                        }
                    }
                }
            }
        } catch (Exception ex1) {
            log.severe(estraiEccezione(ex1));
        }

        return output_xlsx;
    }

    private static void cleanBeforeMergeOnValidCells(XSSFSheet sheet, CellRangeAddress region, XSSFCellStyle cellStyle) {
        try {
            for (int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++) {
                XSSFRow row = getRow(sheet, rowNum);
                for (int colNum = region.getFirstColumn(); colNum <= region.getLastColumn(); colNum++) {
                    XSSFCell currentCell = getCell(row, colNum);
                    currentCell.setCellStyle(cellStyle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void generaRendicontazione(Db_Gest db1) {
        try {
            Gson gson = new Gson();
            String sql0 = "SELECT e.idestrazione,e.progetti FROM estrazioni e WHERE e.path IS NULL";
            try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    int idestrazione = rs0.getInt(1);
                    List<Integer> idpr = new ArrayList<>();
                    List<String> progetti = gson.fromJson(rs0.getString(2), new TypeToken<List<String>>() {
                    }.getType());
                    progetti.forEach(cip -> {
                        try {
                            String sql1 = "SELECT e.idprogetti_formativi FROM progetti_formativi e WHERE e.cip= '" + cip + "'";
                            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                                if (rs1.next()) {
                                    idpr.add(rs1.getInt(1));
                                }
                            }
                        } catch (Exception ex) {
                            log.severe(estraiEccezione(ex));
                        }
                    });
                    File xlsx = prospetto_riepilogo(idestrazione, idpr, db1);
                    if (xlsx != null) {
                        String update1 = "UPDATE estrazioni SET path = '" + StringUtils.replace(xlsx.getPath(), "\\", "/") + "' WHERE idestrazione=" + idestrazione;
                        try (Statement st1 = db1.getConnection().createStatement()) {
                            st1.executeUpdate(update1);
                        }
                        try (Statement st2 = db1.getConnection().createStatement()) {
                            for (int i = 0; i < idpr.size(); i++) {
                                String update2 = "UPDATE progetti_formativi SET extract = 1 WHERE idprogetti_formativi = " + idpr.get(i);
                                st2.executeUpdate(update2);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static void main(String[] args) {
        Toscana_gestione tg = new Toscana_gestione(false);
        Db_Gest db1 = new Db_Gest(tg.host);

        List<Integer> idpr = new ArrayList<>();
        idpr.add(3);
        prospetto_riepilogo(1, idpr, db1);
    }

    

}
