/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.exe;

import java.io.File;
import java.io.UnsupportedEncodingException;
import static java.lang.Integer.parseInt;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.util.Locale.ITALY;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Administrator
 */
public class Utils {

    public static final ResourceBundle conf = ResourceBundle.getBundle("conf.conf");
    public static final String bando = "BAT1";
    public static DecimalFormat doubleformat = new DecimalFormat("#.##");
    public static final String patternH = "HH:mm:ss";
    public static final String patternHmin = "HH:mm";
    public static final String patternid = "yyyyMMdd";
    public static final String patternSql = "yyyy-MM-dd";
    public static final String patternITA = "dd/MM/yyyy";
    public static final String timestamp = "yyyyMMddHHmmssSSS";
    public static final String timestampFAD = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String timestampSQLZONE = "yyyy-MM-dd HH:mm:ss Z";
    public static final String timestampSQL = "yyyy-MM-dd HH:mm:ss";
    public static final String timestampITA = "dd/MM/yyyy HH:mm:ss";
    public static final String timestampITAcomplete = "dd/MM/yyyy HH:mm:ss.SSS";
    public static final SimpleDateFormat sd0 = new SimpleDateFormat(timestampSQL);
    public static final DateTimeFormatter dtf = DateTimeFormat.forPattern(patternSql);
    public static final DateTimeFormatter dtfad = DateTimeFormat.forPattern(timestampFAD);
    public static final DateTimeFormatter dtfh = DateTimeFormat.forPattern(patternHmin);
    public static final DateTimeFormatter dtfsql = DateTimeFormat.forPattern(timestampSQL);
    public static final SimpleDateFormat sdfITA = new SimpleDateFormat(patternITA);
    public static final String pathICC = "/home/tomcat/jar/sRGB.icc";
    public static String estraiEccezione(Exception ec1) {
        try {
            String stack_nam = ec1.getStackTrace()[0].getMethodName();
            String stack_msg = ExceptionUtils.getStackTrace(ec1);
            return stack_nam + " - " + stack_msg;
        } catch (Exception e) {
        }
        return ec1.getMessage();
    }

    public static Logger createLog(String nameapp) {
        try {
            
            Db_Accr db1 = new Db_Accr(conf.getString("db.host") + ":3306/enm_toscana_prod");
            String logpath = db1.getPath("pathtemp");
            db1.closeDB();
            File dir1 = new File(logpath);
            createDir(logpath);
            Date d = new Date();
            String dataOdierna = (new SimpleDateFormat("ddMMyyyy")).format(d);
            File dir2 = new File(dir1.getPath() + File.separator + dataOdierna);
            createDir(dir1.getPath() + File.separator + dataOdierna);
            String ora = (new SimpleDateFormat("HHmmss")).format(d);
            Logger logger = Logger.getLogger("MyLog");
            FileHandler fh = new FileHandler(dir2.getPath() + File.separator + nameapp + "_" + ora + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            return logger;
        } catch (Exception e) {
            e.printStackTrace();
            return Logger.getLogger(Utils.class.getName());
        }
    }

    public static void createDir(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
        }
    }

    public static String cp_toUTF(String ing) {
        try {
            String t = new String(ing.getBytes("Windows-1252"), "UTF-8");
            return t.trim();
        } catch (UnsupportedEncodingException ex) {

        }
        return ing;
    }

    public static void setEmptyString(Object object) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (String.class.equals(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(object) == null) {
                        field.set(object, "");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public static String formatStringtoStringDateSQL(String dat) {
        return formatStringtoStringDate(dat, patternSql, patternITA, false);

    }

    public static String formatStringtoStringDate(String dat, String pattern1, String pattern2, boolean timestamp) {
        try {
            if (timestamp) {
                dat = StringUtils.substring(dat, 0, pattern1.length());
            }
            if (dat.length() == pattern1.length()) {
                DateTimeFormatter fmt = forPattern(pattern1);
                DateTime dtout = fmt.parseDateTime(dat);
                return dtout.toString(pattern2, ITALY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DATA ERRATA";
    }
    
    public static int parseIntR(String value) {
        value = value.replaceAll("-", "").trim();
        if (value.contains(".")) {
            StringTokenizer st = new StringTokenizer(value, ".");
            value = st.nextToken();
        }
        int d1;
        try {
            d1 = parseInt(value);
        } catch (Exception e) {
            d1 = 0;
        }
        return d1;
    }
    
    public static void setCell(XSSFCell cella, String valore) {
        try {
            
            
            cella.setCellValue(valore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCell(XSSFCell cella, XSSFCellStyle style, String valore, boolean integervalue, boolean doublevalue) {
        try {
            cella.setCellStyle(style);

            if (integervalue) {
                cella.setCellValue(Integer.parseInt(valore));
            } else if (doublevalue) {
                cella.setCellValue(Double.parseDouble(valore));
            } else {
                cella.setCellValue(valore);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static XSSFCell getCell(XSSFRow riga, int indice) {
        XSSFCell cell1;
        try {
            cell1 = riga.getCell(indice);
            if (cell1 == null) {
                cell1 = riga.createCell(indice);

            }
        } catch (Exception e) {
            e.printStackTrace();
            cell1 = null;
        }
        return cell1;
    }

    public static XSSFRow getRow(XSSFSheet foglio, int indice) {
        XSSFRow riga;
        try {
            riga = foglio.getRow(indice);
            if (riga == null) {
                riga = foglio.createRow(indice);
            }
            riga.setHeight((short) -1);
        } catch (Exception e) {
            e.printStackTrace();
            riga = null;
        }
        return riga;
    }
}
