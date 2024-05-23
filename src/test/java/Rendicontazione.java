
import com.google.common.primitives.Ints;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import static rc.soop.exe.Utils.estraiEccezione;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.Registro_completo;
import rc.soop.gestione.Toscana_gestione;
import static rc.soop.gestione.Toscana_gestione.log;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Administrator
 */
public class Rendicontazione {

    private static final String separator = "|";
    private static final SimpleDateFormat sdfHHmm = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdfITA = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyy-MM-dd");
    private static final String formatdataCell = "#,#.00";
    private static final String formatdataCellint = "#,#";
    private static final byte[] bianco = {(byte) 255, (byte) 255, (byte) 255};
    private static final byte[] color1 = {(byte) 49, (byte) 134, (byte) 155};
    private static final byte[] color2 = {(byte) 83, (byte) 141, (byte) 213};
    private static final byte[] color3 = {(byte) 197, (byte) 217, (byte) 241};
    private static final byte[] color4 = {(byte) 238, (byte) 30, (byte) 30};
    private static final byte[] color5 = {(byte) 0, (byte) 204, (byte) 0};
    private static final XSSFColor myColor1 = new XSSFColor(color1, new DefaultIndexedColorMap());
    private static final XSSFColor myColor2 = new XSSFColor(color2, new DefaultIndexedColorMap());
    private static final XSSFColor myColor3 = new XSSFColor(color3, new DefaultIndexedColorMap());
    private static final XSSFColor myColor4 = new XSSFColor(color4, new DefaultIndexedColorMap());
    private static final XSSFColor myColor5 = new XSSFColor(color5, new DefaultIndexedColorMap());
    private static final XSSFColor white = new XSSFColor(bianco, new DefaultIndexedColorMap());

    public static File prospetto_riepilogo(int idestrazione, List<Integer> list_idpr, Db_Gest db1) {
        File output_xlsx = null;
        DateTime oggi = new DateTime();
        String nomerend_cod = "R" + idestrazione;
        String nomerend = nomerend_cod + "_" + oggi.toString("ddMMyyyy");
        String data_giustificativo = oggi.toString("dd/MM/yyyy");

        try {

            String pathdest = db1.getPath("output_excel_archive");
            String pathtemp = db1.getPath("pathTemp");

            String fileing = pathdest + "YISUT_Prospetto_Riepilogo_v1.xlsx";

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
                        cstotal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal.setFont(font_total);

                        XSSFCellStyle cstotal_double = wb.createCellStyle();

                        cstotal_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_double.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_double.setBorderBottom(BorderStyle.THIN);
                        cstotal_double.setBorderTop(BorderStyle.THIN);
                        cstotal_double.setBorderRight(BorderStyle.THIN);
                        cstotal_double.setBorderLeft(BorderStyle.THIN);
                        cstotal_double.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
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
                        cstotal_int.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal_int.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));
                        cstotal_int.setFont(font_total);

                        AtomicInteger index_row = new AtomicInteger(9);
                        AtomicDouble oretotali = new AtomicDouble(0.0);
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
                                    String regione = rs1.getString(4).toUpperCase();
                                    String start = sdfITA.format(rs1.getDate(5));
                                    String end = sdfITA.format(rs1.getDate(6));

                                    if (start_rend == null || start_rend.isAfter(new DateTime(rs1.getDate(5).getTime()))) {
                                        start_rend = new DateTime(rs1.getDate(5).getTime());
                                    }
                                    if (end_rend == null || end_rend.isBefore(new DateTime(rs1.getDate(6).getTime()))) {
                                        end_rend = new DateTime(rs1.getDate(6).getTime());
                                    }

                                    List<Registro_completo> registrocompleto = db1.registro_modello6(idpr);

                                    
                                    
                                }
                            }

                        }

                    }
                }
            }

        } catch (Exception ex1) {
            log.severe(estraiEccezione(ex1));
        }

        return null;
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
