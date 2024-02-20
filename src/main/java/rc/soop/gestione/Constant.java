/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import static com.itextpdf.kernel.colors.ColorConstants.BLACK;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import static java.lang.Integer.parseInt;
import static java.lang.Math.toRadians;
import static java.lang.System.setProperty;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import static java.util.Calendar.getInstance;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import static java.util.Locale.ITALY;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.IOUtils.copy;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.replace;
import org.apache.commons.lang3.exception.ExceptionUtils;
import static org.apache.pdfbox.Loader.loadPDF;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import static org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.tika.Tika;
import org.apache.xmpbox.XMPMetadata;
import static org.apache.xmpbox.XMPMetadata.createXMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

public class Constant {

    //MOD
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

    // VARIABILI MS
    public static final double coeff_faseA = 0.80;
    public static final double coeff_faseB = 40.00;
    public static final double coeff_docfasciaA = 146.25;
    public static final double coeff_docfasciaB = 117.00;
    public static final double coeff_ddr_dd = 1.3857457807511;
    public static final String codice_yisu_neet = "MLPS-CLP-00081";
    public static final String codice_yisu_ded = "ANPAL-CLP-00266";
    public static final String codice_bb = "1375";
    public static final String contodocentiA = "195149";// "91018";
    public static final String contodocentiB = "191018";// "91019";
    public static final String contoallievifaseA = "191020";// "91021";
    public static final String contoallievifaseB = "191021";// "95149";
    public static final String contodocentiA_DD = "UCS_ITA_19";
    public static final String contodocentiB_DD = "UCS_ITA_20";
    public static final String contoallievifaseA_DD = "UCS_ITA_22";
    public static final String contoallievifaseB_DD = "UCS_MICRO_01";
    public static final String percentuale_attribuzioneDD = "100";
    public static final String tipologia_costo = "8";
    public static final String tipologia_giustificativo = "16";
    public static final String cf_soggetto_DD = "97538720588";

    public static Logger createLog(String nameapp, String logpath) {
        try {
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
            return Logger.getLogger(Constant.class.getName());
        }
    }

    public static String estraiEccezione(Exception ec1) {
        try {
            String stack_nam = ec1.getStackTrace()[0].getMethodName();
            String stack_msg = ExceptionUtils.getStackTrace(ec1);
            return stack_nam + " - " + stack_msg;
        } catch (Exception e) {
        }
        return ec1.getMessage();

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

    public static String cp_toUTF(String ing) {
        try {
            String t = new String(ing.getBytes("Windows-1252"), "UTF-8");
            return t.trim();
        } catch (UnsupportedEncodingException ex) {

        }
        return ing;
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

    public static String randomP() {
        try {
            return RandomStringUtils.randomAlphanumeric(6) + "!1";
        } catch (Exception e) {
            final SecureRandom random = new SecureRandom();
            String r = new BigInteger(130, random).toString(32);
            r = r.substring(0, 6);
            r = r + "!1";
            return r;
        }
    }

    public static String convMd5(String psw) {
        try {
            String md5Hex = DigestUtils.md5Hex(psw);
            return md5Hex;
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
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

    public static long getTimeDiff() {
//        try {
//            TimeZone tz1 = TimeZone.getTimeZone("Europe/Rome");
//            TimeZone tz2 = TimeZone.getTimeZone("GMT");
//            TimeZone tz3 = TimeZone.getTimeZone("GMT+1");
//            ZoneId arrivingZone = ZoneId.of("Europe/Rome");
//            ZonedDateTime arrival = Instant.now().atZone(arrivingZone);
//            if (arrivingZone.getRules().isDaylightSavings(arrival.toInstant())) {
//                return tz1.getRawOffset() - tz2.getRawOffset() + tz1.getDSTSavings() - tz2.getDSTSavings();
//            } else {
//                return tz1.getRawOffset() - tz3.getRawOffset() + tz1.getDSTSavings() - tz3.getDSTSavings();
//            }
//        } catch (Exception e) {
//        }
        return 0L;
    }

    public static String convertTS_Italy(String ts1) {
//        TimeZone tz1 = TimeZone.getTimeZone("Europe/Rome");
//        TimeZone tz2 = TimeZone.getTimeZone("GMT");
//        long timeDifference = tz1.getRawOffset() - tz2.getRawOffset() + tz1.getDSTSavings() - tz2.getDSTSavings();
        String dt1 = StringUtils.substring(ts1, 0, 26);
        try {
            if (dt1.length() != timestampFAD.length()) {
                if (dt1.length() == 19) {
                    dt1 += ".000000";
                }
            }
        } catch (Exception e) {
        }
        DateTime start = new DateTime(dtfad.parseDateTime(dt1));
        DateTime dateTimeIT = start.plus(getTimeDiff());
        return dateTimeIT.toString(timestampSQL);
    }

//    public static void main(String[] args) {
//        System.out.println(convertTS_Italy("2021-11-02 15:38:36"));
//    }
//    public static String checkCalendar(String date, List<Lezione> calendar, Presenti pr1) {
//        StringBuilder newdate = new StringBuilder();
//        String solodata = date.split(" ")[0];
//        String soloora = date.split(" ")[1];
//        DateTime orario = DateTimeFormat.forPattern(patternH).parseDateTime(soloora);
//
//        List<Lezione> lezionidelgiorno = calendar.stream().filter(lez -> lez.getGiorno().equals(solodata)).collect(Collectors.toList());
//
//        if (lezionidelgiorno.isEmpty()) {
//            return "";
//        }
//
//        lezionidelgiorno.sort(Comparator.comparing(a -> a.getStart()));
////        System.out.println(lezionidelgiorno.size());
//        if (lezionidelgiorno.size() == 2) {
//            for (int z = 0; z < lezionidelgiorno.size(); z++) {
//                Lezione lezioneoggi = lezionidelgiorno.get(z);
//                DateTime start = DateTimeFormat.forPattern(patternH).parseDateTime(lezioneoggi.getStart()).minusMinutes(30);
//                
//                DateTime end = DateTimeFormat.forPattern(patternH).parseDateTime(lezioneoggi.getEnd()).plusMinutes(30);
//                boolean compreso = !orario.isBefore(start) && !orario.isAfter(end);
//                if (compreso) {
//                    break;
//                } else {
//                    boolean prima = orario.isBefore(start);
//                    boolean dopo = orario.isAfter(end);
//                    if (prima) {
//                        newdate.append(solodata).append(" ").append(start.toString(patternH));
//                        break;
//                    }
//                    if (dopo) {
//                        if (z == 0) {
//                            Lezione lezionepom = lezionidelgiorno.get(z + 1);
//                            DateTime start_P = DateTimeFormat.forPattern(patternH).parseDateTime(lezionepom.getStart()).minusMinutes(30);
//                            DateTime end_P = DateTimeFormat.forPattern(patternH).parseDateTime(lezionepom.getEnd()).plusMinutes(30);
//                            boolean prima_P = orario.isBefore(start_P);
//                            boolean dopo_P = orario.isAfter(end_P);
//
//                            if (prima_P) {
//                                if (pr1.isLogin()) {
//                                    newdate.append(solodata).append(" ").append(start_P.toString(patternH));
//                                    break;
//                                } else if (pr1.isLogout()) {
//                                    newdate.append(solodata).append(" ").append(end.toString(patternH));
//                                    break;
//                                }
//                            }
//                            if (dopo_P) {
//                                newdate.append(solodata).append(" ").append(end_P.toString(patternH));
//                                break;
//                            }
//                        }
//                    }
//
//                }
//
//            }
//        } else {
//            Iterator<Lezione> cal = lezionidelgiorno.iterator();
//            while (cal.hasNext()) {
//                Lezione lezione = cal.next();
//                DateTime start = DateTimeFormat.forPattern(patternH).parseDateTime(lezione.getStart()).minusMinutes(30);
//                DateTime end = DateTimeFormat.forPattern(patternH).parseDateTime(lezione.getEnd()).plusMinutes(30);
//                boolean compreso = !orario.isBefore(start) && !orario.isAfter(end);
//                if (!compreso) {
//                    boolean prima = orario.isBefore(start);
//                    boolean dopo = orario.isAfter(end);
//                    if (prima) {
//                        newdate.append(solodata).append(" ").append(start.toString(patternH));
//                        break;
//                    }
//                    if (dopo) {
//                        newdate.append(solodata).append(" ").append(end.toString(patternH));
//                        break;
//                    }
//                }
//
//            }
//        }
//
//        if (newdate.toString().trim().equals("")) {
////            System.out.println(date);
//            return date;
//        } else {
////            System.out.println(newdate.toString());
//            return newdate.toString();
//        }
//    }
//    public static long arrotonda(long durata) {
//        try {
//            long r = durata % (15 * 60 * 1000);
//            durata -= r;
//            durata += 15 * 60 * 1000;
//        } catch (Exception ex) {
//        }
//        return durata;
//    }
    public static DateTime format(String ing, String pattern) {
        try {
            if (ing.contains(".")) {
                ing = ing.split("\\.")[0];
            }
            return DateTimeFormat.forPattern(pattern).parseDateTime(ing);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        return null;
    }

    public static final long MAX = 18000000;

    public static long convertHours(String ore) {
        try {
            double d1 = Double.parseDouble(ore);
            long tot = Math.round(d1) * 3600000;
            if (tot < MAX) {
                return tot;
            }
        } catch (Exception e) {
        }
        return MAX;
    }

//    public static long calcolaDurataLezione(String date, List<Lezione> calendar) {
//        try {
//            List<Lezione> ita = calendar.stream().filter(day -> day.getGiorno().equals(date)).collect(Collectors.toList());
//            Iterator<Lezione> i = ita.iterator();
//            long start = 0;
//            long end = 0;
//            while (i.hasNext()) {
//                Lezione it = i.next();
//                DateTime start1 = DateTimeFormat.forPattern(timestampSQL).parseDateTime(it.getGiorno() + " " + it.getStart());
//                DateTime end1 = DateTimeFormat.forPattern(timestampSQL).parseDateTime(it.getGiorno() + " " + it.getEnd());
//                start += start1.getMillis();
//                end += end1.getMillis();
//            }
//            long out = end - start;
//            if (out > MAX) {
//                return MAX;
//            }
//            return end - start;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return 0;
//    }
    public static String calcoladurata(long millis) {
        if (millis == 0) {
            return "0h 0min 0sec";
        }
        if (millis < 0) {
            return "Dati non congrui per calcolare il tempo di permanenza.";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append("h ");
        sb.append(minutes);
        sb.append("min ");
        sb.append(seconds);
        sb.append("sec");
        return sb.toString();
    }

    public static <T> LinkedList<T> convertALtoLL(
            List<T> aL) {
        return aL
                .stream()
                .collect(Collectors
                        .toCollection(LinkedList::new));
    }

    public static String convert_Ore(String orelezione) {
        if (orelezione.contains(",")) {
            return orelezione.split(",")[0] + "h 30min 0sec";
        } else {
            return orelezione.split("\\.")[0] + "h 0min 0sec";
        }
    }

    public static void printbarcode(BarcodeQRCode barcode, PdfDocument pdfDoc, boolean page, String add) {
        try {
            Rectangle rect = barcode.getBarcodeSize();
            PdfFormXObject formXObject = new PdfFormXObject(new Rectangle(rect.getWidth(), rect.getHeight() + 10));
            PdfCanvas pdfCanvas = new PdfCanvas(formXObject, pdfDoc);
            barcode.placeBarcode(pdfCanvas, BLACK);
            Image bCodeImage = new Image(formXObject);
            bCodeImage.setRotationAngle(toRadians(90));
            bCodeImage.setFixedPosition(25, 5);

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                new Canvas(pdfDoc.getPage(i), pdfDoc.getDefaultPageSize()).add(bCodeImage);
                if (page) {
                    Canvas canvas = new Canvas(pdfDoc.getPage(i), pdfDoc.getDefaultPageSize());

                    canvas.showTextAligned((("Pag. " + i + " di " + pdfDoc.getNumberOfPages())),
                            pdfDoc.getPage(i).getPageSize().getWidth() - 100,
                            5, TextAlignment.CENTER)
                            .close();

                    if (add != null) {
                        Text text = new Text(add);
                        PdfFont fontnormal = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
                        text.setFont(fontnormal);
                        text.setFontSize(8);
                        Paragraph p1 = new Paragraph();
                        p1.add(text);
                        canvas.showTextAligned(p1,
                                5,
                                pdfDoc.getPage(i).getPageSize().getHeight() - 15,
                                TextAlignment.LEFT).close();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File convertPDFA(File pdf_ing, String nomepdf) {
        if (pdf_ing == null) {
            return null;
        }
        try {
            File pdfOutA = new File(replace(pdf_ing.getPath(), ".pdf", "_pdfA.pdf"));
            setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            try (PDDocument doc = loadPDF(pdf_ing)) {
                int numPageTOT = 0;
                Iterator<PDPage> it1 = doc.getPages().iterator();
                while (it1.hasNext()) {
                    numPageTOT++;
                    it1.next();
                }
                PDPage page = new PDPage();
                doc.setVersion(1.7f);
                try (PDPageContentStream contents = new PDPageContentStream(doc, page); PDDocument docSource = loadPDF(pdf_ing)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(docSource);
                    for (int i = 0; i < numPageTOT; i++) {
                        BufferedImage imagePage = pdfRenderer.renderImageWithDPI(i, 100);
                        PDImageXObject pdfXOImage = createFromImage(doc, imagePage);
                        contents.drawImage(pdfXOImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                    }
                }
                XMPMetadata xmp = createXMPMetadata();
                PDDocumentCatalog catalogue = doc.getDocumentCatalog();
                Calendar cal = getInstance();
                try {
                    DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
                    dc.addCreator("YISU");
                    dc.addDate(cal);
                    PDFAIdentificationSchema id = xmp.createAndAddPDFAIdentificationSchema();
                    id.setPart(3);  //value => 2|3
                    id.setConformance("A"); // value => A|B|U
                    XmpSerializer serializer = new XmpSerializer();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    serializer.serialize(xmp, baos, true);
                    PDMetadata metadata = new PDMetadata(doc);
                    metadata.importXMPMetadata(baos.toByteArray());
                    catalogue.setMetadata(metadata);
                } catch (BadFieldValueException e) {
                    throw new IllegalArgumentException(e);
                }
                try (InputStream colorProfile = new FileInputStream(pathICC)) {
                    PDOutputIntent intent = new PDOutputIntent(doc, colorProfile);
                    intent.setInfo("sRGB IEC61966-2.1");
                    intent.setOutputCondition("sRGB IEC61966-2.1");
                    intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
                    intent.setRegistryName("http://www.color.org");
                    catalogue.addOutputIntent(intent);
                    catalogue.setLanguage("it-IT");
                    PDViewerPreferences pdViewer = new PDViewerPreferences(page.getCOSObject());
                    pdViewer.setDisplayDocTitle(true);
                    catalogue.setViewerPreferences(pdViewer);
                    PDMarkInfo mark = new PDMarkInfo();
                    PDStructureTreeRoot treeRoot = new PDStructureTreeRoot();
                    catalogue.setMarkInfo(mark);
                    catalogue.setStructureTreeRoot(treeRoot);
                    catalogue.getMarkInfo().setMarked(true);
                    PDDocumentInformation info = doc.getDocumentInformation();
                    info.setCreationDate(cal);
                    info.setModificationDate(cal);
                    info.setAuthor("YISU");
                    info.setProducer("YISU");
                    info.setCreator("YISU");
                    info.setTitle(nomepdf);
                    info.setSubject("PDF/A");
                    doc.save(pdfOutA);
                }
            }
            return pdfOutA;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkPDF(File pdffile) {
        if (pdffile.exists()) {
            try {
                int pag;
                try (PDDocument pdf = loadPDF(pdffile)) {
                    pag = pdf.getNumberOfPages();
                }
                return pag > 0;
            } catch (Exception e) {
                System.err.println(pdffile.getName() + " ERRORE: " + estraiEccezione(e));
            }
        }
        return false;
    }

    public static void createDir(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
        }
    }

    public static int getIdUser(Db_Gest db, String nome, String cognome, int idpr, int idsa, String ruolo) {
        if (ruolo.equalsIgnoreCase("DOCENTE")) {
            return getIdDocente(db, nome, cognome, idsa);
        } else if (ruolo.equalsIgnoreCase("ALLIEVO")) {
            return getIdAllievo(db, nome, cognome, idpr);
        }
        return 0;
    }

    private static int getIdAllievo(Db_Gest db, String nome, String cognome, int idpr) {
        try {
            String sql = "SELECT idallievi FROM allievi WHERE nome = ? AND cognome = ? AND idprogetti_formativi = ? AND id_statopartecipazione = ? ORDER BY idallievi DESC LIMIT 1";
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setString(1, nome);
                ps.setString(2, cognome);
                ps.setInt(3, idpr);
                ps.setString(4, "15");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getIdDocente(Db_Gest db, String nome, String cognome, int idsa) {
        try {
            String sql = "SELECT iddocenti FROM docenti WHERE nome = ? AND cognome = ? AND idsoggetti_attuatori = ? AND stato = ? ORDER BY iddocenti DESC LIMIT 1";
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setString(1, nome);
                ps.setString(2, cognome);
                ps.setInt(3, idsa);
                ps.setString(4, "A");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static final NumberFormat numITA = NumberFormat.getCurrencyInstance(Locale.ITALY);

    public static String roundFloatAndFormat(float f, boolean converttoHours) {
        try {

            if (converttoHours) {
                double hours = f / 1000.0 / 60.0 / 60.0;
                BigDecimal bigDecimal = new BigDecimal(hours);
                bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
                return numITA.format(bigDecimal).replaceAll("[^0123456789.,()-]", "").trim();
            } else {
                BigDecimal bigDecimal = new BigDecimal(Float.toString(f));
                bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
                return numITA.format(bigDecimal).replaceAll("[^0123456789.,()-]", "").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String roundDoubleAndFormat(double f) {
        try {
            BigDecimal bigDecimal = new BigDecimal(f);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
            return numITA.format(bigDecimal).replaceAll("[^0123456789.,()-]", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String roundDoubleAndFormatCurrency(double f) {
        try {
            BigDecimal bigDecimal = new BigDecimal(f);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
            return numITA.format(bigDecimal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static Map<Integer, String> bando_SE() {
        Map<Integer, String> s = new HashMap<>();
        s.put(1, "Microcredito");
        s.put(2, "Microcredito esteso");
        s.put(3, "Piccoli prestiti");
        return s;
    }

    public static Map<Integer, String> bando_SUD() {
        Map<Integer, String> s = new HashMap<>();
        s.put(1, "Finanziamento più consistente");
        s.put(2, "Procedure più semplici");
        s.put(3, "Criteri di selezione meno stringenti");
        s.put(4, "Tempi di istruttoria più veloci");
        s.put(5, "Piano di ammortamento e restituzione più conveniente");
        s.put(6, "Presenza di quota a fondo perduto");
        return s;
    }

    public static Map<Integer, String> no_agenvolazione() {
        Map<Integer, String> s = new HashMap<>();
        s.put(1, "L'investimento iniziale non raggiunge l'investimento minimo per accedere alle agevolazioni di legge");
        s.put(2, "La copertura è assicurata interamente con fondi propri");
        s.put(3, "Ricordo al criterio ordinario - non ci sono bandi attivi o l'iniziativa non rientra tra le iniziative ammissibili a finanziamento dei bandi attivi");
        return s;
    }

    public static String formatStatoDomanda(String statoDomanda) {
        try {

            switch (statoDomanda) {
                case "S":
                    return "NON PROCESSATA";
                case "R":
                    return "RIGETTATA";
                case "A":
                    return "APPROVATA";
                case "A1":
                    return "CONVENZIONE SA";
                case "A2":
                    return "SA ATTIVO";
                case "A3":
                    return "IN ATTESA FIRMA ENM";
                default:
                    break;
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String formatStatoDocente(String statoDocente) {
        if (null == statoDocente) {
            return "";
        } else {
            switch (statoDocente) {
                case "A":
                    return "ACCREDITATO";
                case "DV":
                    return "DA VALIDARE";
                case "R":
                    return "RIGETTATO";
                default:
                    return "";
            }
        }
    }

    public static String getCellValue(XSSFCell cella) {
        try {
            switch (cella.getCellType()) {
                case STRING:
                    return cella.getRichStringCellValue().getString().toUpperCase().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cella)) {
                        final DataFormatter df = new DataFormatter();
                        return df.formatCellValue(cella).toUpperCase().trim();
                    } else {
                        return String.valueOf(cella.getNumericCellValue()).trim();
                    }
                case BOOLEAN:
                    return String.valueOf(cella.getBooleanCellValue()).toUpperCase().trim();
                case FORMULA:
                    return (cella.getCellFormula()).toUpperCase().trim();
                default:
                    return "";
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String preparefileforupload(File file) {
        try {
            String mimeType = new Tika().detect(file);
            String content = encodeBase64String(readFileToByteArray(file));
            return file.getName() + "###" + mimeType + "###" + content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "FILE ERROR";
        }
    }

    public static byte[] extractSignatureInformation_P7M(byte[] p7m_bytes) {
        CMSSignedData cms;
        try {
            cms = new CMSSignedData(p7m_bytes);
        } catch (CMSException e) {
            System.err.println("ERRORE NEL FILE - " + e.getMessage());
            return null;
        }
        if (cms == null || cms.getSignedContent() == null) {
            System.err.println("ERRORE NEL FILE - CONTENUTO ERRATO");
            return null;
        }
        try {
            return (byte[]) cms.getSignedContent().getContent();
        } catch (Exception ex) {
            System.err.println("ERRORE NEL FILE - " + ex.getMessage());
        }
        return null;
    }

    public static boolean copyR(String source, String dest) {
        boolean es;
        try {
            long byteing = new File(source).length();
            try (OutputStream out = new FileOutputStream(dest)) {
                long contenuto = FileUtils.copyFile(new File(source), out);
                es = byteing == contenuto;
            }
        } catch (Exception e) {
            e.printStackTrace();
            es = false;
        }
        return es;
    }

    public static boolean zipListFiles(List<File> files, File targetZipFile) {
        try {
            try (OutputStream out = new FileOutputStream(targetZipFile); ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("zip", out)) {
                for (int i = 0; i < files.size(); i++) {
                    File ing = files.get(i);
                    os.putArchiveEntry(new ZipArchiveEntry(ing.getName()));
                    copy(new FileInputStream(ing), os);
                    os.closeArchiveEntry();
                }
            }
            return targetZipFile.length() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

}
