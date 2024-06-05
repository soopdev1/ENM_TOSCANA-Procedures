/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.accreditamento;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import rc.soop.exe.Db_Accr;
import rc.soop.exe.Domande;
import rc.soop.exe.ExcelDomande;
import static rc.soop.exe.MainSelector.log;
import static rc.soop.exe.Utils.bando;
import static rc.soop.exe.Utils.conf;
import static rc.soop.exe.Utils.createDir;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.formatStringtoStringDate;
import static rc.soop.exe.Utils.getCell;
import static rc.soop.exe.Utils.getRow;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.patternSql;
import static rc.soop.exe.Utils.setCell;
import static rc.soop.exe.Utils.timestamp;
import static rc.soop.exe.Utils.timestampSQL;
import static rc.soop.gestione.Constant.createFile;

/**
 *
 * @author Administrator
 */
public class Engine {

    public String host;

    public Engine(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_toscana_prod";
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_toscana";
        }
    }

    public void elenco_domande_fase1() {
        Db_Accr db1 = new Db_Accr(this.host);
        try {

            String sql1 = "SELECT username,id,stato,datainvio FROM domandecomplete WHERE stato = '1' AND id NOT IN (SELECT DISTINCT(coddomanda) FROM bando_toscana_mcn) GROUP BY id";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {

                while (rs1.next()) {
                    Domande d1 = new Domande();
                    d1.setCodicedomanda(rs1.getString("id"));
                    d1.setDataconsegna(rs1.getString("datainvio"));
                    d1.setStato(rs1.getString("stato"));
                    boolean ok = false;
                    String sql2 = "SELECT * FROM usersvalori WHERE username = ?";
                    try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                        ps2.setString(1, rs1.getString("username"));
                        try (ResultSet rs2 = ps2.executeQuery(sql2)) {
                            while (rs2.next()) {
                                ok = true;
                                String nomecampo = rs2.getString("campo");
                                String valorecampo = rs2.getString("valore").toUpperCase().trim();
                                switch (nomecampo) {
                                    case "nome" ->
                                        d1.setNome(valorecampo);
                                    case "cognome" ->
                                        d1.setCognome(valorecampo);
                                    case "cfuser" ->
                                        d1.setCodiceFiscale(valorecampo);
                                    case "pec" ->
                                        d1.setPEC(valorecampo.toLowerCase());
                                    case "societa" ->
                                        d1.setRagioneSociale(valorecampo);
                                    case "piva" ->
                                        d1.setPartitaIVA(valorecampo);
                                    case "sedecomune" ->
                                        d1.setSedeComune(valorecampo);
                                    case "sedecap" ->
                                        d1.setSedeCap(valorecampo);
                                    case "cell" ->
                                        d1.setCellulare(valorecampo);
                                    case "data" ->
                                        d1.setDataNascita(valorecampo);
                                    case "email" ->
                                        d1.setEmail(valorecampo);
                                    case "sedeindirizzo" ->
                                        d1.setSedeIndirizzo(valorecampo);
                                    case "docric1" ->
                                        d1.setNumeroDocumento(valorecampo);
                                    case "datasc1" ->
                                        d1.setScadenzaDoc(valorecampo);
                                    case "caricasoc" ->
                                        d1.setCaricaSoc(valorecampo);
                                    default -> {
                                    }
                                }
                            }
                        }
                    }

                    if (ok) {
                        String insert = "INSERT INTO bando_toscana_mcn (codbando,username,nome,cognome,cf,pivacf,pec,societa,dataconsegna,coddomanda,sedecomune,sedecap,cellulare,data,mail,sedeindirizzo,docric,scadenzadoc,caricasoc)"
                                + " VALUES ("
                                + "?,?,?,?,?,?,?,?,?,?," //10
                                + "?,?,?,?,?,?,?,?,?"
                                + ")";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(insert)) {
                            ps1.setString(1, bando);
                            ps1.setString(2, rs1.getString("username"));
                            ps1.setString(3, d1.getNome());
                            ps1.setString(4, d1.getCognome());
                            ps1.setString(5, d1.getCodiceFiscale());
                            ps1.setString(6, d1.getPartitaIVA());
                            ps1.setString(7, d1.getPEC());
                            ps1.setString(8, d1.getRagioneSociale());
                            ps1.setString(9, d1.getDataconsegna());
                            ps1.setString(10, d1.getCodicedomanda());
                            ps1.setString(11, d1.getSedeComune());
                            ps1.setString(12, d1.getSedeCap());
                            ps1.setString(13, d1.getCellulare());
                            ps1.setString(14, d1.getDataNascita());
                            ps1.setString(15, d1.getEmail());
                            ps1.setString(16, d1.getSedeIndirizzo());
                            ps1.setString(17, d1.getNumeroDocumento());
                            ps1.setString(18, d1.getScadenzaDoc());
                            ps1.setString(19, d1.getCaricaSoc());
                            ps1.execute();
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void update_domande_fase1() {
        Db_Accr db1 = new Db_Accr(this.host);
        try {
            String sql1 = "SELECT username FROM bando_toscana_mcn a WHERE decreto = '-'";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    Domande d1 = new Domande();
                    boolean ok = false;
                    String sql2 = "SELECT * FROM usersvalori WHERE username = ?";
                    try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                        ps2.setString(1, rs1.getString("username"));
                        try (ResultSet rs2 = ps2.executeQuery(sql2)) {
                            while (rs2.next()) {
                                ok = true;
                                String nomecampo = rs2.getString("campo");
                                String valorecampo = rs2.getString("valore").toUpperCase().trim();
                                switch (nomecampo) {
                                    case "sedecomune" ->
                                        d1.setSedeComune(valorecampo);
                                    case "sedecap" ->
                                        d1.setSedeCap(valorecampo);
                                    case "cell" ->
                                        d1.setCellulare(valorecampo);
                                    case "data" ->
                                        d1.setDataNascita(valorecampo);
                                    case "email" ->
                                        d1.setEmail(valorecampo);
                                    case "sedeindirizzo" ->
                                        d1.setSedeIndirizzo(valorecampo);
                                    case "docric1" ->
                                        d1.setNumeroDocumento(valorecampo);
                                    case "datasc1" ->
                                        d1.setScadenzaDoc(valorecampo);
                                    case "caricasoc" ->
                                        d1.setCaricaSoc(valorecampo);
                                    default -> {
                                    }
                                }
                            }
                        }
                    }
                    d1.setAccreditato("");
                    if (ok) {
                        String sql3 = "SELECT * FROM allegato_a WHERE username = ?";
                        try (PreparedStatement ps3 = db1.getConnection().prepareStatement(sql3)) {
                            ps3.setString(1, rs1.getString("username"));
                            try (ResultSet rs3 = ps3.executeQuery(sql2)) {
                                if (rs3.next()) {
                                    d1.setAccreditato(rs3.getString("iscrizione"));
                                }
                            }
                        }

                        String UPDATE = "UPDATE bando_toscana_mcn SET sedecomune = ?,sedecap = ?,cellulare = ?,data = ?,mail = ?,sedeindirizzo = ?,docric = ?,scadenzadoc = ?,caricasoc = ?, accreditato = ? where username = ?";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(UPDATE)) {
                            ps1.setString(1, d1.getSedeComune());
                            ps1.setString(2, d1.getSedeCap());
                            ps1.setString(3, d1.getCellulare());
                            ps1.setString(4, d1.getDataNascita());
                            ps1.setString(5, d1.getEmail());
                            ps1.setString(6, d1.getSedeIndirizzo());
                            ps1.setString(7, d1.getNumeroDocumento());
                            ps1.setString(8, d1.getScadenzaDoc());
                            ps1.setString(9, d1.getCaricaSoc());
                            ps1.setString(10, d1.getAccreditato());
                            ps1.setString(11, rs1.getString("username"));
                            ps1.execute();
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void aggiorna_dataconvenzione_fase1() {
        Db_Accr db1 = new Db_Accr(this.host);
        try {
            String sql1 = "SELECT a.username FROM bando_toscana_mcn a WHERE a.dataupconvenzionefinale ='-'";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1);) {
                while (rs1.next()) {
                    String sql2 = "SELECT timestamp FROM convenzioniroma WHERE username = ? ORDER BY timestamp DESC LIMIT 1";
                    try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                        ps2.setString(1, rs1.getString("a.username"));
                        try (ResultSet rs2 = ps2.executeQuery(sql2)) {
                            if (rs2.next()) {
                                String data = formatStringtoStringDate(rs2.getString(1), timestampSQL, patternITA, true);
                                if (!data.equals("DATA ERRATA")) {
                                    String upd = "UPDATE bando_toscana_mcn SET dataupconvenzionefinale = ? where username = ?";
                                    try (PreparedStatement ps1 = db1.getConnection().prepareStatement(upd)) {
                                        ps1.setString(1, data);
                                        ps1.setString(2, rs1.getString("a.username"));
                                        ps1.executeUpdate();
                                    }
                                } else {
                                    log.log(Level.SEVERE, "DATA ERRATA: {0}", rs1.getString("a.username"));
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void aggiorna_reportistica() {
        Db_Accr db1 = new Db_Accr(this.host);
        try {
            String sql1 = "SELECT count(*),stato_domanda FROM bando_toscana_mcn GROUP BY stato_domanda";
            AtomicInteger count_dc0;
            AtomicInteger count_dc1;
            AtomicInteger count_dc2;
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                count_dc0 = new AtomicInteger(0);
                count_dc1 = new AtomicInteger(0);
                count_dc2 = new AtomicInteger(0);
                while (rs1.next()) {
                    String stato = rs1.getString(2);
                    int count = rs1.getInt(1);
                    if (stato.equals("S")) {
                        count_dc1.addAndGet(count);
                    } else {
                        count_dc2.addAndGet(count);
                    }
                    count_dc0.addAndGet(count);
                }
            }

            String upd0 = "UPDATE reportistica SET valore = ? WHERE codice = 'dc0'";
            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(upd0)) {
                ps2.setInt(1, count_dc0.get());
                ps2.executeUpdate();
            }

            String upd1 = "UPDATE reportistica SET valore = ? WHERE codice = 'dc1'";
            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(upd1)) {
                ps2.setInt(1, count_dc1.get());
                ps2.executeUpdate();
            }
            String upd2 = "UPDATE reportistica SET valore = ? WHERE codice = 'dc2'";
            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(upd2)) {
                ps2.setInt(1, count_dc2.get());
                ps2.executeUpdate();
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void crea_report() {
        try {
            DateTime dt1 = new DateTime();

            Db_Accr db1 = new Db_Accr(this.host);
            String contentb64 = db1.getPath("excel.templatereport.2023");
            List<ExcelDomande> list = db1.listaconsegnate("bando_toscana_mcn");
            String pathTemp = db1.getPath("pathtemp");
            String sq1;
            String sq2;
            File output;
            try (InputStream is = new ByteArrayInputStream(decodeBase64(contentb64)); XSSFWorkbook wb = new XSSFWorkbook(is)) {
                XSSFSheet foglio1 = wb.getSheet("ELENCO DOMANDE INVIATE");
                XSSFSheet foglio2 = wb.getSheet("SCHEDA_SINT_SA");
                AtomicInteger indice = new AtomicInteger(1);
                list.forEach(v1 -> {

                    XSSFRow rigaprimofoglio = getRow(foglio1, indice.get());
                    setCell(getCell(rigaprimofoglio, 0), v1.getCODICEDOMANDA());
                    setCell(getCell(rigaprimofoglio, 1), v1.getDATACONSEGNA());
                    setCell(getCell(rigaprimofoglio, 2), v1.getORACONSEGNA());
                    setCell(getCell(rigaprimofoglio, 3), v1.getRAGIONESOCIALE());
                    setCell(getCell(rigaprimofoglio, 4), v1.getPIVA());
                    setCell(getCell(rigaprimofoglio, 5), v1.getSEDELEGALEINDIRIZZO());
                    setCell(getCell(rigaprimofoglio, 6), v1.getSEDELEGALECAP());
                    setCell(getCell(rigaprimofoglio, 7), v1.getSEDELEGALECOMUNE());
                    setCell(getCell(rigaprimofoglio, 8), v1.getSEDELEGALEPROVINCIA());
                    setCell(getCell(rigaprimofoglio, 9), v1.getSEDELEGALEREGIONE());
                    setCell(getCell(rigaprimofoglio, 10), v1.getPEC());
                    setCell(getCell(rigaprimofoglio, 11), v1.getEMAIL());
                    setCell(getCell(rigaprimofoglio, 12), v1.getTELEFONO());
                    setCell(getCell(rigaprimofoglio, 13), v1.getNPROTOCOLLO());
                    setCell(getCell(rigaprimofoglio, 14), v1.getSTATODOMANDA());

                    AtomicInteger rowind = new AtomicInteger(0);

                    XSSFRow rigasecondofoglio = getRow(foglio2, indice.get());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCODICEDOMANDA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getDATACONSEGNA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getORACONSEGNA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getRAGIONESOCIALE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getPIVA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNPROTOCOLLO());

                    //REF. VALUTATORE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    //CODICE ACCR.REG.
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCODICEACCREDITAMENTO());
                    //VERIFICA CODICE 
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    //VERIFICA DOCUMENTO
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    //TIPOLOGIA SOGGETTO 
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getTIPOLOGIASOGGETTO());
                    //VERIFICA ALLEGATO CDE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNSEDI());

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1COMUNE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1PROVINCIA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1REGIONE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1TITOLODISP());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1MQ());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE1ACCRREG());
                    //SEDE 1 - VALUTAZIONE ALLEGATO F
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2COMUNE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2PROVINCIA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2REGIONE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2TITOLODISP());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2MQ());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE2ACCRREG());
                    //SEDE 2 - VALUTAZIONE ALLEGATO F
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3COMUNE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3PROVINCIA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3REGIONE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3TITOLODISP());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3MQ());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE3ACCRREG());
                    //SEDE 3 - VALUTAZIONE ALLEGATO F
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4COMUNE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4PROVINCIA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4REGIONE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4TITOLODISP());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4MQ());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE4ACCRREG());
                    //SEDE 4 - VALUTAZIONE ALLEGATO F
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5COMUNE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5PROVINCIA());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5REGIONE());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5TITOLODISP());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5MQ());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getSEDE5ACCRREG());
                    //SEDE 5 - VALUTAZIONE ALLEGATO F
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNDOCENTI());

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNOMEDOCENTE1());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCOGNOMEDOCENTE1());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCFDOCENTE1());
                    //DOCENTE 1 DATI ENM DA INSERIRE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNOMEDOCENTE2());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCOGNOMEDOCENTE2());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCFDOCENTE2());
                    //DOCENTE 2 DATI ENM DA INSERIRE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNOMEDOCENTE3());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCOGNOMEDOCENTE3());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCFDOCENTE3());
                    //DOCENTE 3 DATI ENM DA INSERIRE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNOMEDOCENTE4());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCOGNOMEDOCENTE4());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCFDOCENTE4());
                    //DOCENTE 4 DATI ENM DA INSERIRE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getNOMEDOCENTE5());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCOGNOMEDOCENTE5());
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), v1.getCFDOCENTE5());
                    //DOCENTE 5 DATI ENM DA INSERIRE
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");
                    setCell(getCell(rigasecondofoglio, rowind.getAndAdd(1)), "");

                    indice.addAndGet(1);

                });
                int maxfoglio1 = foglio1.getRow(0).getLastCellNum();
                int maxfoglio2 = foglio2.getRow(0).getLastCellNum();
                for (int i = 0; i < Math.max(maxfoglio1, maxfoglio2); i++) {
                    if (i < maxfoglio1) {
                        foglio1.autoSizeColumn(i);
                    }
                    if (i < maxfoglio2) {
                        foglio2.autoSizeColumn(i);
                    }
                }
                String ts = dt1.toString(timestamp);
                sq1 = dt1.toString(patternSql);
                sq2 = dt1.toString(timestampSQL);
                createDir(pathTemp);
                output = createFile(pathTemp + "Domande_consegnate_" + ts + ".xlsx");
                try (FileOutputStream fos = new FileOutputStream(output)) {
                    wb.write(fos);
                }
            }

            db1.insertReportExcel(
                    sq1,
                    Base64.encodeBase64String(FileUtils.readFileToByteArray(output)),
                    sq2);
            db1.closeDB();
            output.delete();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

//    public static void main(String[] args) {
//        Engine en = new Engine(false);
//        en.update_domande_fase1();
//        en.crea_report();
//    }
}
