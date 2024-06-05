/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.google.common.base.Splitter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.patternSql;
import java.sql.ResultSet;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Years;
import rc.soop.exe.Db_Accr;
import rc.soop.exe.UD;
import rc.soop.exe.Utils;
import static rc.soop.exe.Utils.conf;
import static rc.soop.exe.Utils.dtf;
import static rc.soop.exe.Utils.getRow;
import static rc.soop.exe.Utils.getCell;
import static rc.soop.exe.Utils.ricavaGeneredaCF;
import static rc.soop.exe.Utils.sdfITA;
import static rc.soop.exe.Utils.sdfSQL;
import static rc.soop.exe.Utils.setCell;
import static rc.soop.exe.Utils.timestamp;
import static rc.soop.gestione.Constant.checkPDF;
import static rc.soop.gestione.Constant.roundDouble;
import static rc.soop.gestione.Constant.roundDoubleAndFormat;
import static rc.soop.gestione.SendMailJet.sendMail;

public class Toscana_gestione {

    ////////////////////////////////////////////////////////////////////////////
    private static final String startroom = "FADTOSCANA_";
    public String host;
    boolean test;
    public static final Logger log = Utils.createLog("Toscana_GEST_PR");

    ////////////////////////////////////////////////////////////////////////////
    public Toscana_gestione(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_gestione_toscana_prod";
        this.test = test;
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_gestione_toscana";
        }
        log.log(Level.INFO, "HOST: {0}", this.host);
    }

    public List<String> ore_convalidateAllievi() {
        List<String> report = new ArrayList<>();
        Db_Gest db1 = new Db_Gest(this.host);
        String sql1 = "SELECT a.idallievi FROM allievi a WHERE a.id_statopartecipazione IN (15,16,17)";
        try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
            while (rs1.next()) {
                int idallievi = rs1.getInt(1);
                String sql2 = "SELECT r.totaleorerendicontabili,r.fase FROM registro_completo r WHERE r.idutente = ? AND r.ruolo='ALLIEVO'";
                String sql3 = "SELECT a.durataconvalidata FROM presenzelezioniallievi a WHERE a.idallievi = ? AND a.convalidata=1 AND a.durataconvalidata > 0";
                Long presenze = 0L;

                try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                    ps2.setInt(1, idallievi);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            report.add(idallievi + ";" + rs2.getString(2) + ";" + rs2.getLong(1));
                            presenze += rs2.getLong(1);
                        }
                    }
                }

                try (PreparedStatement ps3 = db1.getConnection().prepareStatement(sql3)) {
                    ps3.setInt(1, idallievi);
                    try (ResultSet rs3 = ps3.executeQuery()) {
                        while (rs3.next()) {
                            Long conv = rs3.getString(1) == null ? 0L : rs3.getLong(1);
//                        report.add(idallievi + ";" + StringUtils.substring(rs3.getString(2), 0, 1) + ";" + conv);
                            presenze += conv;
                        }
                    }
                }

                double res = presenze / 3600000.00;

                String upd4 = "UPDATE allievi SET importo = ?, orec_totali = ? WHERE idallievi = ?";

                try (PreparedStatement ps4 = db1.getConnection().prepareStatement(upd4)) {
                    ps4.setString(1, String.valueOf(res));
                    ps4.setString(2, String.valueOf(res));
                    ps4.setInt(3, idallievi);
                    ps4.executeUpdate(upd4);
                }

            }
        } catch (Exception ex1) {
            log.severe(estraiEccezione(ex1));
        }
        db1.closeDB();
        return report;
    }

    ////////////////////////////////////////////////////////////////////////////
    public void verifica_stanze(int idprogetti_formativi) {
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String dataoggi = new DateTime().toString(patternSql);
            String sql1 = "SELECT ud.fase,lm.gruppo_faseB FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo = ? AND pf.idprogetti_formativi=mp.id_progettoformativo AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = ? ORDER BY lm.gruppo_faseB,lm.orario_start";

            try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1)) {
                ps1.setInt(1, idprogetti_formativi);
                ps1.setString(2, dataoggi);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {
                        String fase = rs1.getString("ud.fase");
                        if (fase.endsWith("A")) {
                            String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = ? AND numerocorso = 1";
                            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                                ps2.setInt(1, idprogetti_formativi);
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    String nomestanza = startroom + idprogetti_formativi + "_A1";
                                    if (test) {
                                        nomestanza = "TESTING_" + nomestanza;
                                    }
                                    if (rs2.next()) {//VERIFICO SE ATTIVA
                                        nomestanza = rs2.getString("nomestanza");
                                        String stato = rs2.getString("stato");
                                        if (stato.equals("0")) {
                                            //UPDATE ATTIVA
                                            String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = ?";
                                            try (PreparedStatement ps3 = db1.getConnection().prepareStatement(upd)) {
                                                ps3.setString(1, nomestanza);
                                                ps3.executeUpdate();
                                            }
                                        }
                                    } else { //INSERISCO
                                        String ins = "INSERT INTO fad_multi VALUES (?,?,?,?,?)";
                                        try (PreparedStatement ps3 = db1.getConnection().prepareStatement(ins)) {
                                            ps3.setString(1, nomestanza);
                                            ps3.setInt(2, idprogetti_formativi);
                                            ps3.setString(3, "1");
                                            ps3.setString(4, new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                                            ps3.setString(5, "1");
                                            ps3.execute();
                                        }
                                    }
                                }
                            }
                        } else if (fase.endsWith("B")) {
                            int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");

                            String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = ? AND numerocorso = ?";

                            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                                ps2.setInt(1, idprogetti_formativi);
                                ps2.setInt(2, gruppo_faseB);
                                try (ResultSet rs2 = ps2.executeQuery()) {

                                    String nomestanza = startroom + idprogetti_formativi + "_B" + gruppo_faseB;
                                    if (test) {
                                        nomestanza = "TESTING_" + nomestanza;
                                    }
                                    if (rs2.next()) {//VERIFICO SE ATTIVA
                                        nomestanza = rs2.getString("nomestanza");
                                        String stato = rs2.getString("stato");
                                        if (stato.equals("0")) {
                                            //UPDATE ATTIVA
                                            String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = ?";
                                            try (PreparedStatement ps3 = db1.getConnection().prepareStatement(upd)) {
                                                ps3.setString(1, nomestanza);
                                                ps3.executeUpdate();
                                            }
                                        }
                                    } else { //INSERISCO
                                        String ins = "INSERT INTO fad_multi VALUES (?,?,?,?,?)";
                                        try (PreparedStatement ps3 = db1.getConnection().prepareStatement(ins)) {
                                            ps3.setString(1, nomestanza);
                                            ps3.setInt(2, idprogetti_formativi);
                                            ps3.setInt(3, gruppo_faseB);
                                            ps3.setString(4, new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                                            ps3.setString(5, "1");
                                            ps3.execute();
                                        }
                                    }
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

    ////////////////////////////////////////////////////////////////////////////
    public void fad_allievi(int idprogetti_formativi, boolean manual) {

        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);
            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc,"
                    + " unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi"
                    + " AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY f.nomestanza";

            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc,"
                        + " unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY f.nomestanza";
            }

            try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1)) {
                ps1.setInt(1, idprogetti_formativi);
                ps1.setString(2, dataoggi);
                try (ResultSet rs1 = ps1.executeQuery()) {

                    while (rs1.next()) {

                        String fase = rs1.getString("ud.fase");
                        String nomestanza = rs1.getString("f.nomestanza");
                        String ud = rs1.getString("ud.codice");
                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");

                        String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                                + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                                + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                                + " AND f.nomestanza = ? AND lm.tipolez='F' AND lm.giorno = ? ORDER BY lm.orario_start";
                        StringBuilder orainvitosb = new StringBuilder("");

                        try (PreparedStatement ps1A = db1.getConnection().prepareStatement(sql1A)) {
                            ps1A.setInt(1, idprogetti_formativi);
                            ps1A.setString(2, nomestanza);
                            ps1A.setString(3, dataoggi);
                            try (ResultSet rs1A = ps1A.executeQuery()) {
                                while (rs1A.next()) {
                                    orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                                }
                            }
                        }
                        String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");

                        String sql3;
                        if (fase.endsWith("A")) {
                            sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione "
                                    + "IN ('15','18') AND idprogetti_formativi = ?";
                        } else if (fase.endsWith("B")) {
                            sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione "
                                    + "IN ('15','18') AND idprogetti_formativi = ? AND gruppo_faseB = ?";
                        } else {
                            continue;
                        }
                        try (PreparedStatement ps3 = db1.getConnection().prepareStatement(sql3)) {
                            ps3.setInt(1, idprogetti_formativi);
                            if (!fase.endsWith("A")) {
                                ps3.setInt(2, gruppo_faseB);
                            }
                            try (ResultSet rs3 = ps3.executeQuery()) {
                                while (rs3.next()) {
                                    String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                                    int idsoggetto = rs3.getInt("idallievi");
                                    String email = rs3.getString("email").toLowerCase();
                                    //VERIFICA
                                    String sql4 = "SELECT user FROM fad_access WHERE type='S' AND idprogetti_formativi = ? AND idsoggetto = ? AND data = ? AND ud = ? AND room = ?";
                                    try (PreparedStatement ps4 = db1.getConnection().prepareStatement(sql4)) {
                                        ps4.setInt(1, idprogetti_formativi);
                                        ps4.setInt(2, idsoggetto);
                                        ps4.setString(3, dataoggi);
                                        ps4.setString(4, ud);
                                        ps4.setString(5, nomestanza);
                                        try (ResultSet rs4 = ps4.executeQuery()) {
                                            String user = RandomStringUtils.randomAlphabetic(8);
                                            String psw = RandomStringUtils.randomAlphanumeric(6);
                                            String md5psw = DigestUtils.md5Hex(psw);
                                            if (!rs4.next()) {
                                                String ins = "INSERT INTO fad_access VALUES (?,?,?,?,?,?,?,?)";
                                                try (PreparedStatement ps5 = db1.getConnection().prepareStatement(ins)) {
                                                    ps5.setInt(1, idprogetti_formativi);
                                                    ps5.setInt(2, idsoggetto);
                                                    ps5.setString(3, dataoggi);
                                                    ps5.setString(4, "S");
                                                    ps5.setString(5, nomestanza);
                                                    ps5.setString(6, user);
                                                    ps5.setString(7, md5psw);
                                                    ps5.setString(8, ud);
                                                    ps5.execute();
                                                    try {
                                                        DbSSO dbs = new DbSSO();
                                                        try (PreparedStatement psSSO = dbs.getConnection().prepareStatement(ins)) {
                                                            psSSO.setInt(1, idprogetti_formativi);
                                                            psSSO.setInt(2, idsoggetto);
                                                            psSSO.setString(3, dataoggi);
                                                            psSSO.setString(4, "S");
                                                            psSSO.setString(5, nomestanza);
                                                            psSSO.setString(6, user);
                                                            psSSO.setString(7, md5psw);
                                                            psSSO.setString(8, ud);
                                                            psSSO.execute();
                                                            log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, "TRUE"});
                                                            log.log(Level.INFO, "NUOVE CREDENZIALI DISCENTE ) {0}", nomecognome);
                                                        }
                                                        dbs.closeDB();
                                                    } catch (Exception eSSO) {
                                                        log.severe(estraiEccezione(eSSO));
                                                    }
                                                }
                                            } else {
                                                user = rs4.getString(1);
                                                String upd = "UPDATE fad_access SET psw = ? WHERE idsoggetto = ? AND data = ? AND ud = ? AND type = ?";
                                                try (PreparedStatement ps5 = db1.getConnection().prepareStatement(upd)) {
                                                    ps5.setString(1, md5psw);
                                                    ps5.setInt(2, idsoggetto);
                                                    ps5.setString(3, dataoggi);
                                                    ps5.setString(4, ud);
                                                    ps5.setString(5, "S");
                                                    ps5.executeUpdate();

                                                    try {
                                                        DbSSO dbs = new DbSSO();
                                                        try (PreparedStatement psSSO = dbs.getConnection().prepareStatement(upd)) {
                                                            psSSO.setString(1, md5psw);
                                                            psSSO.setInt(2, idsoggetto);
                                                            psSSO.setString(3, dataoggi);
                                                            psSSO.setString(4, ud);
                                                            psSSO.setString(5, "S");
                                                            boolean es = psSSO.executeUpdate() > 0;
                                                            log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, es});
                                                            log.log(Level.INFO, "RECUPERO CREDENZIALI DISCENTE ) {0}", nomecognome);
                                                        }
                                                        dbs.closeDB();
                                                    } catch (Exception eSSO) {
                                                        log.severe(estraiEccezione(eSSO));
                                                    }
                                                }
                                            }
                                            //INVIO MAIL
                                            String sql5 = "SELECT oggetto,testo FROM email WHERE chiave = 'fad3.0'";
                                            try (Statement st5 = db1.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                                                if (rs5.next()) {
                                                    String emailtesto = rs5.getString(2);
                                                    String emailoggetto = rs5.getString(1);
                                                    String linkweb = db1.getPath("linkfad");
                                                    String linknohttpweb = remove(linkweb, "https://");
                                                    linknohttpweb = remove(linknohttpweb, "http://");
                                                    linknohttpweb = removeEnd(linknohttpweb, "/");

                                                    emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                                                    emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                                    emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                                    emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
                                                    boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                                    if (es) {
                                                        log.log(Level.INFO, "MAIL DISCENTE INVIATA A : {0}", email);
                                                    } else {
                                                        log.log(Level.SEVERE, "MAIL DISCENTE ERROR {0}", email);
                                                    }
                                                }
                                            }

                                        }
                                    }

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
    ////////////////////////////////////////////////////////////////////////////

    public void fad_docenti(int idprogetti_formativi, boolean manual) {
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);

            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY lm.id_docente,f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY lm.id_docente,f.nomestanza";
            }

            try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1)) {
                ps1.setInt(1, idprogetti_formativi);
                ps1.setString(2, dataoggi);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {
                        int id_docente = rs1.getInt("lm.id_docente");
                        String nomestanza = rs1.getString("f.nomestanza");
                        String ud = rs1.getString("ud.codice");

                        String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                                + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                                + " AND mp.id_progettoformativo =? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                                + " AND f.nomestanza = ? AND lm.tipolez='F' AND lm.giorno = ? AND lm.id_docente = ? ORDER BY lm.orario_start";
                        StringBuilder orainvitosb = new StringBuilder("");
                        try (PreparedStatement ps1A = db1.getConnection().prepareStatement(sql1A)) {
                            ps1A.setInt(1, idprogetti_formativi);
                            ps1A.setString(2, nomestanza);
                            ps1A.setString(3, dataoggi);
                            ps1A.setInt(4, id_docente);
                            try (ResultSet rs1A = ps1A.executeQuery()) {
                                while (rs1A.next()) {
                                    orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                                }
                            }
                        }

                        String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
                        String sql4 = "SELECT iddocenti,email,nome,cognome,codicefiscale FROM docenti WHERE iddocenti = ?";
                        try (PreparedStatement ps4 = db1.getConnection().prepareStatement(sql4)) {
                            ps4.setInt(1, id_docente);
                            try (ResultSet rs4 = ps4.executeQuery()) {
                                if (rs4.next()) {
                                    String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                                    int idsoggetto = rs4.getInt("iddocenti");
                                    String email = rs4.getString("email").toLowerCase();

                                    String sql5 = "SELECT user FROM fad_access WHERE type='D' AND idprogetti_formativi = ? AND idsoggetto = ? AND data = ? AND room = ?";

                                    try (PreparedStatement ps5 = db1.getConnection().prepareStatement(sql5)) {
                                        ps5.setInt(1, idprogetti_formativi);
                                        ps5.setInt(2, idsoggetto);
                                        ps5.setString(3, dataoggi);
                                        ps5.setString(4, nomestanza);
                                        try (ResultSet rs5 = ps5.executeQuery()) {
                                            String user = RandomStringUtils.randomAlphabetic(8);
                                            String psw = RandomStringUtils.randomAlphanumeric(6);
                                            String md5psw = DigestUtils.md5Hex(psw);
                                            if (!rs5.next()) {
                                                //CREO CREDENZIALI
                                                String ins = "INSERT INTO fad_access VALUES (?,?,?,?,?,?,?,?)";
                                                try (PreparedStatement ps6 = db1.getConnection().prepareStatement(ins)) {
                                                    ps6.setInt(1, idprogetti_formativi);
                                                    ps6.setInt(2, idsoggetto);
                                                    ps6.setString(3, dataoggi);
                                                    ps6.setString(4, "D");
                                                    ps6.setString(5, nomestanza);
                                                    ps6.setString(6, user);
                                                    ps6.setString(7, md5psw);
                                                    ps6.setString(8, ud);
                                                    ps6.execute();

                                                    try {
                                                        DbSSO dbs = new DbSSO();
                                                        try (PreparedStatement psSSO = dbs.getConnection().prepareStatement(ins)) {
                                                            psSSO.setInt(1, idprogetti_formativi);
                                                            psSSO.setInt(2, idsoggetto);
                                                            psSSO.setString(3, dataoggi);
                                                            psSSO.setString(4, "D");
                                                            psSSO.setString(5, nomestanza);
                                                            psSSO.setString(6, user);
                                                            psSSO.setString(7, md5psw);
                                                            psSSO.setString(8, ud);
                                                            psSSO.execute();
                                                            log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, "TRUE"});
                                                            log.log(Level.INFO, "NUOVE CREDENZIALI DOCENTE ) {0}", nomecognome);
                                                        }
                                                        dbs.closeDB();
                                                    } catch (Exception eSSO) {
                                                        log.severe(estraiEccezione(eSSO));
                                                    }
                                                }
                                            } else { //CREDENZIALI GIA presenti
                                                user = rs5.getString(1);

                                                String upd = "UPDATE fad_access SET psw = ? WHERE idsoggetto = ? AND data = ? AND ud = ? AND type = ?";
                                                try (PreparedStatement ps6 = db1.getConnection().prepareStatement(upd)) {
                                                    ps6.setString(1, md5psw);
                                                    ps6.setInt(2, idsoggetto);
                                                    ps6.setString(3, dataoggi);
                                                    ps6.setString(4, ud);
                                                    ps6.setString(5, "D");
                                                    ps6.executeUpdate();

                                                    try {
                                                        DbSSO dbs = new DbSSO();
                                                        try (PreparedStatement psSSO = dbs.getConnection().prepareStatement(upd)) {
                                                            psSSO.setString(1, md5psw);
                                                            psSSO.setInt(2, idsoggetto);
                                                            psSSO.setString(3, dataoggi);
                                                            psSSO.setString(4, ud);
                                                            psSSO.setString(5, "D");
                                                            boolean es = psSSO.executeUpdate() > 0;
                                                            log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, es});
                                                            log.log(Level.INFO, "RECUPERO CREDENZIALI DOCENTE ) {0}", nomecognome);
                                                        }
                                                        dbs.closeDB();
                                                    } catch (Exception eSSO) {
                                                        log.severe(estraiEccezione(eSSO));
                                                    }
                                                }
                                            }

                                            //INVIO MAIL
                                            String sql6 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0_DOCENTE'";
                                            try (Statement st6 = db1.getConnection().createStatement(); ResultSet rs6 = st6.executeQuery(sql6)) {
                                                if (rs6.next()) {
                                                    String emailtesto = rs6.getString(2);
                                                    String emailoggetto = rs6.getString(1);

                                                    String linkweb = db1.getPath("linkfad");
                                                    String linknohttpweb = remove(linkweb, "https://");
                                                    linknohttpweb = remove(linknohttpweb, "http://");
                                                    linknohttpweb = removeEnd(linknohttpweb, "/");
//
                                                    emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                                                    emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                                    emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                                    emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
//
                                                    boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                                    if (es) {
                                                        log.log(Level.INFO, "MAIL DOCENTE INVIATA A : {0}", email);
                                                    } else {
                                                        log.log(Level.SEVERE, "MAIL DOCENTE ERROR {0}", email);
                                                    }
                                                }
                                            }
                                        }
                                    }
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

    ////////////////////////////////////////////////////////////////////////////
    public void fad_ospiti(int idprogetti_formativi, boolean manual) {
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);

            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                        + " AND lm.tipolez='F' AND lm.giorno = ? GROUP BY f.nomestanza";
            }

            try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1)) {
                ps1.setInt(1, idprogetti_formativi);
                ps1.setString(2, dataoggi);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {

                        String nomestanza = rs1.getString("f.nomestanza");
                        String ud = rs1.getString("ud.codice");

                        String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                                + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                                + " AND mp.id_progettoformativo = ? AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                                + " AND f.nomestanza = ? AND lm.tipolez='F' AND lm.giorno = ? ORDER BY lm.orario_start";

                        StringBuilder orainvitosb = new StringBuilder("");
                        try (PreparedStatement ps1A = db1.getConnection().prepareStatement(sql1A)) {
                            ps1A.setInt(1, idprogetti_formativi);
                            ps1A.setString(2, nomestanza);
                            ps1A.setString(3, dataoggi);
                            try (ResultSet rs1A = ps1A.executeQuery()) {
                                while (rs1A.next()) {
                                    orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                                }
                            }
                        }
                        String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
                        String sql4 = "SELECT id_staff, nome, cognome, email FROM staff_modelli WHERE id_progettoformativo = ?";
                        try (PreparedStatement ps4 = db1.getConnection().prepareStatement(sql4)) {
                            ps4.setInt(1, idprogetti_formativi);
                            try (ResultSet rs4 = ps4.executeQuery()) {
                                while (rs4.next()) {
                                    String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                                    int idsoggetto = rs4.getInt("id_staff");
                                    String email = rs4.getString("email").toLowerCase();
                                    String sql5 = "SELECT user FROM fad_access WHERE type='O' "
                                            + "AND idprogetti_formativi = ? AND idsoggetto = ? AND data = ? AND room = ?";
                                    try (PreparedStatement ps5 = db1.getConnection().prepareStatement(sql5)) {
                                        ps5.setInt(1, idprogetti_formativi);
                                        ps5.setInt(2, idsoggetto);
                                        ps5.setString(3, dataoggi);
                                        ps5.setString(4, nomestanza);
                                        try (ResultSet rs5 = ps5.executeQuery()) {
                                            String user = RandomStringUtils.randomAlphabetic(8);
                                            String psw = RandomStringUtils.randomAlphanumeric(6);
                                            String md5psw = DigestUtils.md5Hex(psw);
                                            if (!rs5.next()) {
                                                //CREO CREDENZIALI                                                
                                                String ins = "INSERT INTO fad_access VALUES (?,?,?,?,?,?,?,?)";
                                                try (PreparedStatement ps6 = db1.getConnection().prepareStatement(ins)) {
                                                    ps6.setInt(1, idprogetti_formativi);
                                                    ps6.setInt(2, idsoggetto);
                                                    ps6.setString(3, dataoggi);
                                                    ps6.setString(4, "O");
                                                    ps6.setString(5, nomestanza);
                                                    ps6.setString(6, user);
                                                    ps6.setString(7, md5psw);
                                                    ps6.setString(8, ud);
                                                    ps6.execute();
                                                }
                                                log.log(Level.INFO, "NUOVE CREDENZIALI OSPITE ) {0}", nomecognome);
                                            } else { //CREDENZIALI GIA presenti
                                                user = rs5.getString(1);
                                                String upd = "UPDATE fad_access SET psw = ? WHERE idsoggetto = ? AND data = ? AND ud = ? AND type = ?";
                                                try (PreparedStatement ps6 = db1.getConnection().prepareStatement(upd)) {
                                                    ps6.setString(1, md5psw);
                                                    ps6.setInt(2, idsoggetto);
                                                    ps6.setString(3, dataoggi);
                                                    ps6.setString(4, ud);
                                                    ps6.setString(5, "O");
                                                    ps6.executeUpdate();
                                                }
                                                log.log(Level.INFO, "RECUPERO CREDENZIALI OSPITE ) {0}", nomecognome);
                                            }
                                            //INVIO MAIL
                                            String sql6 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0'";
                                            try (Statement st6 = db1.getConnection().createStatement(); ResultSet rs6 = st6.executeQuery(sql6)) {
                                                if (rs6.next()) {
                                                    String emailtesto = rs6.getString(2);
                                                    String emailoggetto = rs6.getString(1);

                                                    String linkweb = db1.getPath("linkfad");
                                                    String linknohttpweb = remove(linkweb, "https://");
                                                    linknohttpweb = remove(linknohttpweb, "http://");
                                                    linknohttpweb = removeEnd(linknohttpweb, "/");

                                                    emailtesto = StringUtils.replace(emailtesto, "@nomecognome", "OSPITE " + nomecognome);
                                                    emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                                    emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                                    emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                                    emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                                    emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);

                                                    boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                                    if (es) {
                                                        log.log(Level.INFO, "MAIL OSPITE INVIATA A : {0}", email);
                                                    } else {
                                                        log.log(Level.SEVERE, "MAIL OSPITE ERROR {0}", email);
                                                    }
                                                }
                                            }
                                        }
                                    }
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

    ////////////////////////////////////////////////////////////////////////////
    public void fad_gestione() {
        List<Integer> elenco = new ArrayList<>();
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String sql0 = "SELECT idprogetti_formativi FROM progetti_formativi "
                    + "WHERE CURDATE()>=start AND CURDATE()<=end "
                    + "AND (stato='ATA' OR stato = 'ATB')";
            try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    elenco.add(rs0.getInt("idprogetti_formativi"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db1.closeDB();

        elenco.forEach(pf -> {
            try {
                log.log(Level.WARNING, "VERIFICA STANZE PF -> {0}", pf);
                this.verifica_stanze(pf);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE VERIFICA STANZE PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD ALLIEVI -> {0}", pf);
                this.fad_allievi(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD ALLIEVI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD DOCENTI -> {0}", pf);
                this.fad_docenti(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD DOCENTI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD OSPITI -> {0}", pf);
                this.fad_ospiti(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD OSPITI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
        });
    }

    //REPORT
    private static final String formatdataCell = "#.0";
    private static final String formatdataCellINT = "##";
    private static final String formatdataCellCUR = "€#,##0.00";

    // da completare
    public void report_allievi() {
        try {
//            List<String> presenzeconv = ore_convalidateAllievi();
            String fileing = "/mnt/mcn/yisu_toscana/estrazioni/Report_Allievi_Toscana.xlsx";
            String fileout = "/mnt/mcn/yisu_toscana/estrazioni/Report_Allievi_" + new DateTime().toString(timestamp) + ".xlsx";

            String sql0 = "SELECT * FROM allievi a WHERE a.id_statopartecipazione<>'00' ORDER BY a.cognome";
            Db_Gest db1 = new Db_Gest(this.host);

            try (OutputStream outputStream = new FileOutputStream(new File(fileout)); XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {

                XSSFCellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(wb.createDataFormat().getFormat(formatdataCell));
                XSSFCellStyle cellStyleINT = wb.createCellStyle();
                cellStyleINT.setDataFormat(wb.createDataFormat().getFormat(formatdataCellINT));
                XSSFCellStyle cellStyleCUR = wb.createCellStyle();
                cellStyleCUR.setDataFormat(wb.createDataFormat().getFormat(formatdataCellCUR));

                XSSFSheet sh1 = wb.getSheetAt(0);
                AtomicInteger maxrow = new AtomicInteger(1);
                AtomicInteger indiceriga = new AtomicInteger(1);
                try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                    while (rs0.next()) {
                        int idallievo = rs0.getInt("a.idallievi");

                        String comune_nascita = "";
                        String comune_residenza = "";
                        String comune_domicilio = "";
                        String provincia_residenza = "";
                        String provincia_domicilio = "";
                        String regione_residenza = "";
                        String regione_domicilio = "";

                        String sql1 = "SELECT nome,idcomune,nome_provincia,regione FROM comuni WHERE idcomune IN (?,?,?)";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1)) {
                            ps1.setInt(1, rs0.getInt("a.comune_nascita"));
                            ps1.setInt(2, rs0.getInt("a.comune_residenza"));
                            ps1.setInt(3, rs0.getInt("a.comune_domicilio"));
                            try (ResultSet rs1 = ps1.executeQuery()) {
                                while (rs1.next()) {
                                    if (rs1.getInt(2) == rs0.getInt("a.comune_nascita")) {
                                        comune_nascita = rs1.getString(1).toUpperCase();
                                    }
                                    if (rs1.getInt(2) == rs0.getInt("a.comune_residenza")) {
                                        comune_residenza = rs1.getString(1).toUpperCase();
                                        provincia_residenza = rs1.getString(3).toUpperCase();
                                        regione_residenza = rs1.getString(4).toUpperCase();
                                    }
                                    if (rs1.getInt(2) == rs0.getInt("a.comune_domicilio")) {
                                        comune_domicilio = rs1.getString(1).toUpperCase();
                                        provincia_domicilio = rs1.getString(3).toUpperCase();
                                        regione_domicilio = rs1.getString(4).toUpperCase();
                                    }
                                }
                            }
                        }
                        String statonascita = rs0.getString("a.stato_nascita");
                        if (statonascita.equals("100")) {
                            statonascita = "ITALIA";
                        } else {
                            String sql2 = "SELECT nome FROM nazioni_rc WHERE codicefiscale = ?";
                            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                                ps2.setString(1, statonascita);
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    if (rs2.next()) {
                                        statonascita = rs2.getString(1).toUpperCase();
                                    }
                                }
                            }
                        }

                        String cpi = rs0.getString("a.cpi");
                        String sql3 = "SELECT descrizione,provincia FROM cpi WHERE codice = ?";
                        try (PreparedStatement ps3 = db1.getConnection().prepareStatement(sql3)) {
                            ps3.setString(1, cpi);
                            try (ResultSet rs3 = ps3.executeQuery()) {
                                if (rs3.next()) {
                                    cpi = rs3.getString(1).toUpperCase();
                                }
                            }
                        }

                        String titolo_studio = rs0.getString("a.titolo_studio");
                        String sql4 = "SELECT descrizione FROM titoli_studio WHERE codice = ?";
                        try (PreparedStatement ps4 = db1.getConnection().prepareStatement(sql4)) {
                            ps4.setString(1, titolo_studio);
                            try (ResultSet rs4 = ps4.executeQuery()) {
                                if (rs4.next()) {
                                    titolo_studio = rs4.getString(1).toUpperCase();
                                }
                            }
                        }

                        String vulnerabilita = rs0.getString("a.tos_gruppovulnerabile");
                        String sql5 = "SELECT descrizione FROM gruppovulnerabile WHERE idgruppovulnerabile = ?";
                        try (PreparedStatement ps5 = db1.getConnection().prepareStatement(sql5)) {
                            ps5.setString(1, vulnerabilita);
                            try (ResultSet rs5 = ps5.executeQuery()) {
                                if (rs5.next()) {
                                    vulnerabilita = rs5.getString(1).toUpperCase();
                                }
                            }
                        }

                        String condizione_lavorativa = rs0.getString("a.idcondizione_mercato");
                        String sql6 = "SELECT descrizione FROM condizione_mercato WHERE idcondizione_mercato = ?";
                        try (PreparedStatement ps6 = db1.getConnection().prepareStatement(sql6)) {
                            ps6.setString(1, condizione_lavorativa);
                            try (ResultSet rs6 = ps6.executeQuery()) {
                                if (rs6.next()) {
                                    condizione_lavorativa = rs6.getString(1).toUpperCase();
                                }
                            }
                        }

                        String grado_conoscenza = rs0.getString("a.tos_m0_gradoconoscenza") == null ? "" : rs0.getString("a.tos_m0_gradoconoscenza");
                        grado_conoscenza = switch (grado_conoscenza) {
                            case "1" ->
                                "ALTO";
                            case "2" ->
                                "MEDIO";
                            case "3" ->
                                "SCARSO";
                            case "4" ->
                                "NULLO";
                            default ->
                                "";
                        };

                        String canale_conoscenza = rs0.getString("a.tos_m0_canaleconoscenza") == null ? "" : rs0.getString("a.tos_m0_canaleconoscenza");
                        String sql7 = "SELECT descrizione FROM canale WHERE idcanale = ?";
                        try (PreparedStatement ps7 = db1.getConnection().prepareStatement(sql7)) {
                            ps7.setString(1, canale_conoscenza);
                            try (ResultSet rs7 = ps7.executeQuery()) {
                                if (rs7.next()) {
                                    canale_conoscenza = rs7.getString(1).toUpperCase();
                                }
                            }
                        }
                        String motivazione = rs0.getString("a.tos_m0_motivazione") == null ? "" : rs0.getString("a.tos_m0_motivazione");
                        String sql8 = "SELECT descrizione FROM motivazione WHERE idmotivazione = ?";
                        try (PreparedStatement ps8 = db1.getConnection().prepareStatement(sql8)) {
                            ps8.setString(1, motivazione);
                            try (ResultSet rs8 = ps8.executeQuery()) {
                                if (rs8.next()) {
                                    motivazione = rs8.getString(1).toUpperCase();
                                }
                            }
                        }

                        String utilita = rs0.getString("a.tos_m0_utilita") == null ? "" : rs0.getString("a.tos_m0_utilita");
                        utilita = switch (utilita) {
                            case "1" ->
                                "PER NULLA UTILE";
                            case "2" ->
                                "POCO UTILE";
                            case "3" ->
                                "UTILE";
                            case "4" ->
                                "ABBASTANZA UTILE";
                            case "5" ->
                                "MOLTO UTILE";
                            default ->
                                "";
                        };

                        String aspettative = rs0.getString("a.tos_m0_aspettative") == null ? "" : rs0.getString("a.tos_m0_aspettative");
                        String sql9 = "SELECT descrizione FROM aspettative WHERE idaspettative = ?";
                        try (PreparedStatement ps9 = db1.getConnection().prepareStatement(sql9)) {
                            ps9.setString(1, aspettative);
                            try (ResultSet rs9 = ps9.executeQuery()) {
                                if (rs9.next()) {
                                    aspettative = rs9.getString(1).toUpperCase();
                                }
                            }
                        }

                        String maturazioneidea = rs0.getString("a.tos_m0_maturazione") == null ? "" : rs0.getString("a.tos_m0_maturazione");
                        String sql10 = "SELECT descrizione FROM maturazioneidea WHERE idmaturazioneidea = ?";
                        try (PreparedStatement ps10 = db1.getConnection().prepareStatement(sql10)) {
                            ps10.setString(1, maturazioneidea);
                            try (ResultSet rs10 = ps10.executeQuery()) {
                                if (rs10.next()) {
                                    maturazioneidea = rs10.getString(1).toUpperCase();
                                }
                            }
                        }
                        String motivazioneno = rs0.getString("a.tos_m0_noperche") == null ? "" : rs0.getString("a.tos_m0_noperche");
                        if (motivazioneno.equals("7")) {
                            motivazioneno = rs0.getString("a.tos_m0_noperchealtro") == null ? "" : rs0.getString("a.tos_m0_noperchealtro");
                        } else {
                            String sql11 = "SELECT descrizione FROM motivazioneno WHERE idmotivazioneno = ?";
                            try (PreparedStatement ps11 = db1.getConnection().prepareStatement(sql11)) {
                                ps11.setString(1, motivazioneno);
                                try (ResultSet rs11 = ps11.executeQuery()) {
                                    if (rs11.next()) {
                                        motivazioneno = rs11.getString(1).toUpperCase();
                                    }
                                }
                            }
                        }

                        int idsoggetto_attuatore = rs0.getString("a.idsoggetto_attuatore") == null ? 0 : rs0.getInt("a.idsoggetto_attuatore");
                        String se_scelto = "";
                        String sql12 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = ?";
                        try (PreparedStatement ps12 = db1.getConnection().prepareStatement(sql12)) {
                            ps12.setInt(1, idsoggetto_attuatore);
                            try (ResultSet rs12 = ps12.executeQuery()) {
                                if (rs12.next()) {
                                    se_scelto = rs12.getString(1).toUpperCase();
                                }
                            }
                        }

                        String consapevole = rs0.getString("a.tos_m0_consapevole") == null ? "SI" : (rs0.getInt("a.tos_m0_consapevole") == 1 ? "SI" : "NO");

                        String volonta = rs0.getString("a.tos_m0_volonta") == null ? "NO" : (rs0.getInt("a.tos_m0_volonta") == 1 ? "SI" : "NO");

                        switch (volonta) {
                            case "SI" -> {
                                motivazioneno = "";
                                break;
                            }
                            case "NO" -> {
                                se_scelto = "";
                                consapevole = "";
                                break;
                            }
                            default -> {
                                se_scelto = "";
                                motivazioneno = "";
                                consapevole = "";
                                break;
                            }
                        }

                        String statopartecipazione = rs0.getString("a.id_statopartecipazione");
                        String statopartecipazioneFINALE = "";
                        String sql13 = "SELECT descrizione FROM stato_partecipazione WHERE codice = ?";
                        try (PreparedStatement ps13 = db1.getConnection().prepareStatement(sql13)) {
                            ps13.setString(1, canale_conoscenza);
                            try (ResultSet rs13 = ps13.executeQuery()) {
                                if (rs13.next()) {
                                    if (statopartecipazione.equals("18") || statopartecipazione.equals("19")) {
                                        statopartecipazioneFINALE = rs13.getString(1).toUpperCase();
                                    }
                                    statopartecipazione = rs13.getString(1).toUpperCase();
                                }
                            }
                        }

                        String cip = "";
                        String dataavvio = "";
                        String datachiusura = "";
                        String statoprogetto = "";

                        int idpr = rs0.getString("a.idprogetti_formativi") == null ? 0 : rs0.getInt("a.idprogetti_formativi");

                        if (idpr > 0) {
                            String sql14 = "SELECT p.cip,p.start,p.end,s.descrizione FROM progetti_formativi p,stati_progetto s "
                                    + "WHERE p.stato=s.idstati_progetto AND p.idprogetti_formativi = ?";
                            try (PreparedStatement ps14 = db1.getConnection().prepareStatement(sql14)) {
                                ps14.setInt(1, idpr);
                                try (ResultSet rs14 = ps14.executeQuery()) {
                                    if (rs14.next()) {
                                        if (rs14.getString(1) != null) {
                                            cip = rs14.getString(1).toUpperCase();
                                        }
                                        if (rs14.getDate(2) != null) {
                                            dataavvio = sdfITA.format(rs14.getDate(2));
                                        }
                                        if (rs14.getDate(3) != null) {
                                            if (new DateTime().withMillisOfDay(0).isAfter(new DateTime(rs14.getDate(3).getTime()))) {
                                                datachiusura = sdfITA.format(rs14.getDate(3));
                                            }
                                        }
                                        if (rs14.getString(4) != null) {
                                            statoprogetto = rs14.getString(4).toUpperCase();
                                        }
                                    }
                                }
                            }
                        }

                        double oreUD11 = 0.0;
                        String sql15 = "SELECT SUM(p.orepresenze) FROM presenzeudallievi p WHERE p.ud LIKE '%11%' AND p.idallievi = ?";
                        try (PreparedStatement ps15 = db1.getConnection().prepareStatement(sql15)) {
                            ps15.setInt(1, idallievo);
                            try (ResultSet rs15 = ps15.executeQuery()) {
                                if (rs15.next()) {
                                    oreUD11 = rs15.getDouble(1);
                                }
                            }
                        }

                        String m5_grado_completezza = "";
                        String m5_probabilita = "";
                        String m5_forma_giuridica = "";
                        String m5_ateco = "";
                        String m5_sede = "";
                        String m5_comune_localizzazione = "";
                        String m5_totale_fabbisogno = "";
                        String m5_misura_individuata = "";
                        String m5_misura_si_tipo = "";
                        String m5_misura_no_motivazione = "";

                        String sql16 = "SELECT m.m5_grado_completezza,m.m5_probabilita,m.m5_forma_giuridica,m.m5_ateco,m.m5_sede,m.m5_comune_localizzazione,m.m5_totale_fabbisogno,m.m5_misura_individuata,"
                                + "m.m5_misura_si_tipo,m.m5_misura_no_motivazione FROM maschera_m5 m WHERE m.allievo = ?";

                        try (PreparedStatement ps16 = db1.getConnection().prepareStatement(sql16)) {
                            ps16.setInt(1, idallievo);
                            try (ResultSet rs16 = ps16.executeQuery()) {
                                if (rs16.next()) {
                                    switch (rs16.getString("m.m5_grado_completezza")) {
                                        case "00" ->
                                            m5_grado_completezza = "NON ELABORATO";
                                        case "01" ->
                                            m5_grado_completezza = "INCOMPLETO";
                                        case "02" ->
                                            m5_grado_completezza = "PARIALMENTE COMPLETO";
                                        case "03" ->
                                            m5_grado_completezza = "ABBASTANZA COMPLETO";
                                        case "04" ->
                                            m5_grado_completezza = "COMPLETO";
                                    }
                                    switch (rs16.getString("m.m5_probabilita")) {
                                        case "01" ->
                                            m5_probabilita = "NESSUNA PROBABILITA'";
                                        case "02" ->
                                            m5_probabilita = "IMPROBABILE";
                                        case "03" ->
                                            m5_probabilita = "POCO PROBABILE";
                                        case "04" ->
                                            m5_probabilita = "PROBABILE";
                                        case "05" ->
                                            m5_probabilita = "MOLTO PROBABILE";
                                        case "06" ->
                                            m5_probabilita = "ALTAMENTE PROBABILE";
                                    }

                                    String sql16A = "SELECT * FROM formagiuridica WHERE idformagiuridica = ?";
                                    try (PreparedStatement ps16A = db1.getConnection().prepareStatement(sql16A)) {
                                        ps16A.setString(1, rs16.getString("m.m5_forma_giuridica"));
                                        try (ResultSet rs16A = ps16A.executeQuery()) {
                                            if (rs16A.next()) {
                                                m5_forma_giuridica = rs16A.getString("descrizione").toUpperCase();
                                            }
                                        }
                                    }
                                    String sql16B = "SELECT * FROM ateco WHERE codice = ?";
                                    try (PreparedStatement ps16B = db1.getConnection().prepareStatement(sql16B)) {
                                        ps16B.setString(1, rs16.getString("m.m5_ateco"));
                                        try (ResultSet rs16B = ps16B.executeQuery()) {
                                            if (rs16B.next()) {
                                                m5_ateco = rs16B.getString("descrizione").toUpperCase();
                                            }
                                        }
                                    }

                                    m5_sede = rs16.getBoolean("m.m5_sede") ? "SI" : "NO";
                                    String sql16C = "SELECT nome,idcomune,nome_provincia,regione FROM comuni WHERE idcomune  = ?";
                                    try (PreparedStatement ps16C = db1.getConnection().prepareStatement(sql16C)) {
                                        ps16C.setString(1, rs16.getString("m.m5_comune_localizzazione"));
                                        try (ResultSet rs16C = ps16C.executeQuery()) {
                                            if (rs16C.next()) {
                                                m5_comune_localizzazione = rs16C.getString(1).toUpperCase();
                                            }
                                        }
                                    }

                                    m5_totale_fabbisogno = rs16.getString("m.m5_totale_fabbisogno").trim();

                                    m5_misura_individuata = rs16.getBoolean("m.m5_misura_individuata") ? "SI" : "NO";

                                    if (m5_misura_individuata.equals("SI")) {

                                        switch (rs16.getString("m.m5_misura_si_tipo")) {
                                            case "01" ->
                                                m5_misura_si_tipo = "NAZIONALE";
                                            case "02" ->
                                                m5_misura_si_tipo = "CIRCOSCRIZIONALE";
                                            case "03" ->
                                                m5_misura_si_tipo = "REGIONALE";
                                            case "04" ->
                                                m5_misura_si_tipo = "CREDITO ORDINARIO";
                                            case "05" ->
                                                m5_misura_si_tipo = "AUTOFINANZIAMENTO, FONDI PROPRI";
                                            case "06" ->
                                                m5_misura_si_tipo = "MICROCREDITO TRAMITE ENM";
                                            case "07" ->
                                                m5_misura_si_tipo = "ALTRO";
                                        }
                                    } else {
                                        switch (rs16.getString("m.m5_misura_no_motivazione")) {
                                            case "01" ->
                                                m5_misura_no_motivazione = "Poca convinzione dell'allievo sull'idea imprenditoriale";
                                            case "02" ->
                                                m5_misura_no_motivazione = "Poca convenienza e/o difficile realizzazione dell'idea imprenditoriale";
                                            case "03" ->
                                                m5_misura_no_motivazione = "Indecisione dell'allievo sulla scelta auto-imprenditoriale";
                                            case "04" ->
                                                m5_misura_no_motivazione = "Carenza di motivazione personale";
                                            case "05" ->
                                                m5_misura_no_motivazione = "Difficoltà a reperire finanziamenti";
                                            case "06" ->
                                                m5_misura_no_motivazione = "Difficoltà burocratiche legate a permessi, certificazioni, ecc.";
                                            case "07" ->
                                                m5_misura_no_motivazione = "Mancanza di strumenti finanziari di agevolazione";
                                            case "08" ->
                                                m5_misura_no_motivazione = "Altro";
                                        }
                                    }
                                }
                            }
                        }

                        AtomicInteger indicecolonna = new AtomicInteger(0);
                        XSSFRow row = getRow(sh1, indiceriga.get());
                        indiceriga.addAndGet(1);
                        setCell(getCell(row, 0), String.valueOf(idallievo));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.nome").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.cognome").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.sesso").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.datanascita") == null ? "" : sdfITA.format(rs0.getDate("a.datanascita")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(Years.yearsBetween(new DateTime(rs0.getDate("a.datanascita").getTime()), new DateTime()).getYears()));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), comune_nascita);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), statonascita);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.codicefiscale").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.telefono").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.email").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), regione_residenza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.indirizzoresidenza").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), " ");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), " ");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), comune_residenza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.capresidenza") == null ? "" : rs0.getString("a.capresidenza").trim());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), provincia_residenza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), regione_domicilio);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.indirizzodomicilio") == null ? "" : rs0.getString("a.indirizzodomicilio").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), " ");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), " ");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), comune_domicilio);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.capdomicilio") == null ? "" : rs0.getString("a.capdomicilio").trim());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), provincia_domicilio);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cpi);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.datacpi") == null ? "" : sdfITA.format(rs0.getDate("a.datacpi")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.tos_tipofinanziamento"));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), titolo_studio);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), vulnerabilita);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), condizione_lavorativa);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.tos_dirittoindennita"));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.iscrizionegg") == null ? "" : sdfITA.format(rs0.getDate("a.iscrizionegg")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.tos_m0_datacolloquio") == null ? "" : sdfITA.format(rs0.getDate("a.tos_m0_datacolloquio")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.tos_m0_siglaoperatore") == null ? "" : rs0.getString("a.tos_m0_siglaoperatore"));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.tos_m0_modalitacolloquio") == null ? "" : rs0.getString("a.tos_m0_modalitacolloquio").equals("1") ? "IN PRESENZA" : "TELEFONICO");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), grado_conoscenza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), canale_conoscenza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), motivazione);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), utilita);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), aspettative);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), maturazioneidea);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), volonta);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), motivazioneno);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), se_scelto);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), consapevole);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), " ");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.tos_noteenm") == null ? "" : rs0.getString("a.tos_noteenm").trim().toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), statopartecipazione);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), "SI");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.privacy2"));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("a.privacy3"));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cip);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), dataavvio);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), datachiusura);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), statoprogetto);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyle, rs0.getString("a.orec_totali") == null ? "0.0" : rs0.getString("a.orec_totali").trim(), false, true);

                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.data_inizio_UD11") == null ? "" : sdfITA.format(rs0.getDate("a.data_inizio_UD11")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("a.data_fine_UD11") == null ? "" : sdfITA.format(rs0.getDate("a.data_fine_UD11")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyle, String.valueOf(oreUD11), false, true);

                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyle, rs0.getString("a.orec_fasea") == null ? "0.0" : rs0.getString("a.orec_fasea").trim(), false, true);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyle, rs0.getString("a.orec_faseb") == null ? "0.0" : rs0.getString("a.orec_faseb").trim(), false, true);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyleINT, rs0.getString("a.ud_ok_A") == null ? "0.0" : rs0.getString("a.ud_ok_A").trim(), false, true);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyleINT, rs0.getString("a.ud_ok_B") == null ? "0.0" : rs0.getString("a.ud_ok_B").trim(), false, true);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), statopartecipazioneFINALE);

                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_grado_completezza);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_probabilita);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_forma_giuridica);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_ateco);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_sede);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_comune_localizzazione);
                        if (m5_totale_fabbisogno.equals("")) {

                            setCell(getCell(row, indicecolonna.addAndGet(1)), "");
                        } else {
                            setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyleCUR, m5_totale_fabbisogno, false, true);

                        }
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_misura_individuata);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_misura_si_tipo);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), "");
                        setCell(getCell(row, indicecolonna.addAndGet(1)), m5_misura_no_motivazione);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), "");

                        maxrow.set(indicecolonna.get());

                    }
                }

                for (int i = 0; i < 80; i++) {
                    sh1.autoSizeColumn(i);
                }
                wb.write(outputStream);

            }
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = ? WHERE idestrazione=2";
            try (PreparedStatement ps = db1.getConnection().prepareStatement(upd)) {
                ps.setString(1, fileout);
                ps.executeUpdate();
            }
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void report_docenti_GG1() {
        try {
            String fileing = "/mnt/mcn/yisu_toscana/estrazioni/GG1_Report_Docenti_Toscana.xlsx";
            String fileout = "/mnt/mcn/yisu_toscana/estrazioni/GG1_Report_Docenti_" + new DateTime().toString(timestamp) + ".xlsx";

            Db_Gest db1 = new Db_Gest(this.host);

            String sql0 = "SELECT p.idprogetti_formativi,p.cip,s.ragionesociale,s.piva,p.start,sp.descrizione FROM progetti_formativi p, soggetti_attuatori s, stati_progetto sp "
                    + "WHERE p.stato=sp.idstati_progetto AND s.idsoggetti_attuatori=p.idsoggetti_attuatori AND p.cip IS NOT NULL ORDER BY p.cip";

            try (OutputStream outputStream = new FileOutputStream(new File(fileout)); XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {
//
                XSSFSheet sh1 = wb.getSheetAt(0);
                AtomicInteger indiceriga = new AtomicInteger(1);

                try (Statement st0 = db1.getConnection().createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs0 = st0.executeQuery(sql0)) {
                    while (rs0.next()) {
                        int idpr = rs0.getInt(1);

                        List<Registro_completo> registro = db1.registro_modello6(idpr);

                        String cip = rs0.getString(2).toUpperCase();
                        String ragionesociale = rs0.getString(3).toUpperCase();
                        String piva = rs0.getString(4).toUpperCase();
                        String dataavvio = sdfITA.format(rs0.getDate(5));
                        String stato = rs0.getString(6).toUpperCase();

                        String sql1 = "SELECT d.iddocenti,d.nome,d.cognome,d.codicefiscale,d.comune_di_nascita,d.datanascita,d.email,d.cellulare,d.titolo_di_studio,d.inquadramento,d.tipo_inserimento "
                                + "FROM docenti d, progetti_docenti p WHERE d.iddocenti=p.iddocenti AND p.idprogetti_formativi = " + idpr + " ORDER BY d.cognome";

                        try (Statement st1 = db1.getConnection().createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs1 = st1.executeQuery(sql1)) {
                            int count = 0;
                            try {
                                rs1.last();
                                count = rs1.getRow();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            rs1.beforeFirst();
                            while (rs1.next()) {
                                int ID = rs1.getInt(1);
                                String NOMEDOCENTE = rs1.getString(2).toUpperCase();
                                String COGNOMEDOCENTE = rs1.getString(3).toUpperCase();
                                String CFDOCENTE = rs1.getString(4).toUpperCase();
                                String LUOGONASCITADOCENTE = "";
                                String DATANASCITADOCENTE = sdfITA.format(rs1.getDate(6));
                                String GENEREDOCENTE = ricavaGeneredaCF(CFDOCENTE);
                                String MAILDOCENTE = rs1.getString(7).toLowerCase();
                                String TELCELLULAREDOCENTE = "";
                                String TITOLODISTUDIODOCENTE = "";
                                String INQUADRAMENTODOCENTE = "";

                                String TI = rs1.getString("d.tipo_inserimento");

                                if (TI == null) {
                                    String sql2 = "SELECT c.nome,a.tel,t.descrizione,i.descrizione FROM bando_toscana_mcn b, allegato_b a, comuni_rc c, titolistudio_rc t, inquadramento_rc i "
                                            + "WHERE b.pivacf = ? AND a.username=b.username AND a.cf = ? AND a.comune=c.id AND a.titolistudio=t.id  "
                                            + "AND a.inquadramento=i.codice ";
                                    Db_Accr dbb = new Db_Accr(conf.getString("db.host") + ":3306/enm_toscana_prod");
                                    try (PreparedStatement ps2 = dbb.getConnection().prepareStatement(sql2)) {
                                        ps2.setString(1, piva);
                                        ps2.setString(2, CFDOCENTE);
                                        try (ResultSet rs2 = ps2.executeQuery()) {
                                            if (rs2.next()) {
                                                LUOGONASCITADOCENTE = rs2.getString(1);
                                                TELCELLULAREDOCENTE = rs2.getString(2);
                                                TITOLODISTUDIODOCENTE = rs2.getString(3);
                                                INQUADRAMENTODOCENTE = rs2.getString(4);
                                            }
                                        }
                                    }
                                } else {
                                    LUOGONASCITADOCENTE = rs1.getString(5);
                                    TELCELLULAREDOCENTE = rs1.getString(8);

                                    //TI
                                    String sql2 = "SELECT descrizione FROM titoli_studio WHERE codice = ?";
                                    try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2)) {
                                        ps2.setString(1, rs1.getString(9));
                                        try (ResultSet rs2 = ps2.executeQuery()) {
                                            if (rs2.next()) {
                                                TITOLODISTUDIODOCENTE = rs2.getString(1);
                                            }
                                        }
                                    }

                                    //IN
                                    Db_Accr dbb = new Db_Accr(conf.getString("db.host") + ":3306/enm_toscana_prod");
                                    String sql3 = "SELECT descrizione FROM inquadramento_rc WHERE codice = ?";
                                    try (PreparedStatement ps3 = dbb.getConnection().prepareStatement(sql3)) {
                                        ps3.setString(1, rs1.getString(10));
                                        try (ResultSet rs3 = ps3.executeQuery()) {
                                            if (rs3.next()) {
                                                INQUADRAMENTODOCENTE = rs3.getString(1);
                                            }
                                        }
                                    }
                                    dbb.closeDB();

                                }

                                List<Registro_completo> docente_ore = registro.stream().filter(
                                        r1 -> r1.getRuolo().equals("DOCENTE")
                                        && r1.getIdutente() == ID).collect(Collectors.toList());

                                long ore = 0L;

                                for (Registro_completo rr : docente_ore) {
                                    ore += rr.getTotaleorerendicontabili();
                                }

                                List<Integer> udlist = new LinkedList<>();
                                String sql4 = "SELECT u.descrizione,u.fase FROM lezioni_modelli lm, lezione_calendario lc, unita_didattiche u "
                                        + "WHERE lm.id_lezionecalendario=lc.id_lezionecalendario AND u.codice=lc.codice_ud "
                                        + "AND lm.id_docente = ? AND lm.id_modelli_progetto IN (SELECT m.id_modello "
                                        + "FROM modelli_progetti m WHERE m.id_progettoformativo = ? AND m.modello IN (3,4)) "
                                        + "GROUP BY u.descrizione,u.fase";
                                try (PreparedStatement ps4 = db1.getConnection().prepareStatement(sql4)) {
                                    ps4.setInt(1, ID);
                                    ps4.setInt(2, idpr);
                                    try (ResultSet rs4 = ps4.executeQuery()) {
                                        while (rs4.next()) {
                                            String fase = StringUtils.right(rs4.getString(2), 1);
                                            String UD = rs4.getString(1);
                                            switch (fase) {
                                                case "A" -> {
                                                    switch (UD) {
                                                        case "UD1" -> {
                                                            udlist.add(17);
                                                        }
                                                        case "UD2" -> {
                                                            udlist.add(18);
                                                        }
                                                        case "UD3" -> {
                                                            udlist.add(19);
                                                        }
                                                        case "UD4" -> {
                                                            udlist.add(20);
                                                        }
                                                        case "UD5" -> {
                                                            udlist.add(21);
                                                        }
                                                        case "UD6" -> {
                                                            udlist.add(22);
                                                        }
                                                        case "UD7" -> {
                                                            udlist.add(23);
                                                        }
                                                        case "UD8" -> {
                                                            udlist.add(24);
                                                        }
                                                        case "UD9" -> {
                                                            udlist.add(25);
                                                        }
                                                        case "UD10" -> {
                                                            udlist.add(26);
                                                        }
                                                        case "UD11" -> {
                                                            udlist.add(27);
                                                        }
                                                    }
                                                }
                                                case "B" -> {
                                                    switch (UD) {
                                                        case "UD1" -> {
                                                            udlist.add(28);
                                                        }
                                                        case "UD2" -> {
                                                            udlist.add(29);
                                                        }
                                                        case "UD3" -> {
                                                            udlist.add(30);
                                                        }
                                                        case "UD4" -> {
                                                            udlist.add(31);
                                                        }
                                                        case "UD5" -> {
                                                            udlist.add(32);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                XSSFRow row = getRow(sh1, indiceriga.get());
                                indiceriga.addAndGet(1);

                                AtomicInteger indicecolonna = new AtomicInteger(0);
                                setCell(getCell(row, indicecolonna.get()), cip);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), ragionesociale);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), piva);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), dataavvio);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), stato);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(count));
                                setCell(getCell(row, indicecolonna.addAndGet(1)), NOMEDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), COGNOMEDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), CFDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), LUOGONASCITADOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), DATANASCITADOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), GENEREDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), MAILDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), TELCELLULAREDOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), TITOLODISTUDIODOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), INQUADRAMENTODOCENTE);
                                setCell(getCell(row, indicecolonna.addAndGet(1)), roundDoubleAndFormat(roundDouble(ore, true)));

                                for (Integer in : udlist) {
                                    setCell(getCell(row, in), "X");
                                }
                            }
                        }

                    }
                }
                for (int i = 0; i < 31; i++) {
                    sh1.autoSizeColumn(i);
                }

                wb.write(outputStream);
            }
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);
            String upd = "UPDATE estrazioni SET path = ? WHERE idestrazione=4";
            try (PreparedStatement ps = db1.getConnection().prepareStatement(upd)) {
                ps.setString(1, fileout);
                ps.executeUpdate();
            }
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

    public void report_docenti() {
        try {
            String fileing = "/mnt/mcn/yisu_toscana/estrazioni/Report_Docenti_Toscana.xlsx";
            String fileout = "/mnt/mcn/yisu_toscana/estrazioni/Report_Docenti_" + new DateTime().toString(timestamp) + ".xlsx";

            Db_Gest db1 = new Db_Gest(this.host);

            String sql0 = "SELECT s.ragionesociale,s.piva,s.protocollo,d.nome,d.cognome,d.codicefiscale,d.stato,d.tipo_inserimento,d.datawebinair,d.motivo,d.email FROM docenti d, soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori";

            try (OutputStream outputStream = new FileOutputStream(new File(fileout)); XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {

                XSSFSheet sh1 = wb.getSheetAt(0);
                AtomicInteger indiceriga = new AtomicInteger(1);
                try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                    while (rs0.next()) {

                        String stato = rs0.getString("d.stato") == null ? "" : rs0.getString("d.stato");
                        stato = switch (stato) {
                            case "A" ->
                                "ACCREDITATO";
                            case "DV" ->
                                "DA VALIDARE";
                            case "W" ->
                                "IN ATTESA WEBINAIR";
                            case "R" ->
                                "RIGETTATO";
                            default ->
                                "";
                        };

                        String tipo_inserimento;
                        if (rs0.getString("d.tipo_inserimento") == null || rs0.getString("d.tipo_inserimento").trim().equals("")
                                || rs0.getString("d.tipo_inserimento").trim().equals("-")) {
                            tipo_inserimento = "ACCREDITAMENTO";
                        } else {
                            tipo_inserimento = "GESTIONALE";
                        }

                        AtomicInteger indicecolonna = new AtomicInteger(0);
                        XSSFRow row = getRow(sh1, indiceriga.get());
                        indiceriga.addAndGet(1);
                        setCell(getCell(row, indicecolonna.get()), rs0.getString("s.ragionesociale").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("s.piva").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("s.protocollo").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.nome").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.cognome").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.codicefiscale").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.email").toLowerCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("d.datawebinair") == null ? "" : sdfITA.format(rs0.getDate("d.datawebinair")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), stato);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), tipo_inserimento);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.motivo") == null ? "" : rs0.getString("d.motivo").toUpperCase());

                    }
                }

                for (int i = 0; i < 21; i++) {
                    sh1.autoSizeColumn(i);
                }

                wb.write(outputStream);
            }
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);
//
            String upd = "UPDATE estrazioni SET path = ? WHERE idestrazione=1";
            try (PreparedStatement ps = db1.getConnection().prepareStatement(upd)) {
                ps.setString(1, fileout);
                ps.executeUpdate();
            }
            db1.closeDB();

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void report_pf() {
        try {
            String fileing = "/mnt/mcn/yisu_toscana/estrazioni/Report_ProgettiFormativi_Toscana.xlsx";
            String fileout = "/mnt/mcn/yisu_toscana/estrazioni/Report_ProgettiFormativi_Toscana_" + new DateTime().toString(timestamp) + ".xlsx";
            Db_Gest db1 = new Db_Gest(this.host);

            try (OutputStream outputStream = new FileOutputStream(new File(fileout)); XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {
                //FOGLIO 1
                XSSFSheet sh1 = wb.getSheetAt(0);
                String sql0_foglio1 = "SELECT sa.ragionesociale,sa.piva,sa.idsoggetti_attuatori FROM soggetti_attuatori sa ORDER BY sa.ragionesociale";
                AtomicInteger indiceriga = new AtomicInteger(2);
                try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0_foglio1)) {
                    while (rs0.next()) {
                        int idsa = rs0.getInt("sa.idsoggetti_attuatori");
                        int docenti = 0;
                        String sql1_foglio1 = "SELECT COUNT(iddocenti) FROM docenti d WHERE stato='A' AND d.idsoggetti_attuatori = ?";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1_foglio1)) {
                            ps1.setInt(1, idsa);
                            try (ResultSet rs1 = ps1.executeQuery()) {
                                if (rs1.next()) {
                                    docenti = rs1.getInt(1);
                                }
                            }

                            int DAVALIDARE_p = 0, DAVALIDARE_a = 0;
                            int PROGRAMMATO_p = 0, PROGRAMMATO_a = 0;
                            int DACONFERMARE_p = 0, DACONFERMARE_a = 0;
                            int FASEA_p = 0, FASEA_a = 0;
                            int FASEB_p = 0, FASEB_a = 0;
                            int SOSPESO_p = 0, SOSPESO_a = 0;
                            int RIGETTATO_p = 0, RIGETTATO_a = 0;
                            int FINEATTIVITA_p = 0, FINEATTIVITA_a = 0;
                            int DAVALIDAREMODELLO6_p = 0, DAVALIDAREMODELLO6_a = 0;
                            int INATTESADIMAPPATURA_p = 0, INATTESADIMAPPATURA_a = 0;
                            int INVERIFICA_p = 0, INVERIFICA_a = 0;
                            int ESITOVERIFICACONCLUSO_p = 0, ESITOVERIFICACONCLUSO_a = 0;
                            int ESITOVERIFICAINVIATO_p = 0, ESITOVERIFICAINVIATO_a = 0;
                            int CONCLUSO_p = 0, CONCLUSO_a = 0;
                            String sql2_foglio1 = "SELECT p.idprogetti_formativi,p.stato FROM progetti_formativi p WHERE p.idsoggetti_attuatori = ?";
                            try (PreparedStatement ps2 = db1.getConnection().prepareStatement(sql2_foglio1)) {
                                ps2.setInt(1, idsa);
                                try (ResultSet rs2 = ps2.executeQuery()) {

                                    while (rs2.next()) {
                                        String stato = rs2.getString("p.stato");
                                        int idpr = rs2.getInt("p.idprogetti_formativi");
                                        switch (stato) {
                                            case "DV" -> {
                                                DAVALIDARE_p++;
                                                DAVALIDARE_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "P" -> {
                                                PROGRAMMATO_p++;
                                                PROGRAMMATO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "DC" -> {
                                                DACONFERMARE_p++;
                                                DACONFERMARE_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "ATA" -> {
                                                FASEA_p++;
                                                FASEA_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "ATB" -> {
                                                FASEB_p++;
                                                FASEB_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "SOA", "SOB" -> {
                                                SOSPESO_p++;
                                                SOSPESO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "DCE", "DVE", "ATAE", "DVAE", "ATBE", "DVBE" -> {
                                                RIGETTATO_p++;
                                                RIGETTATO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "F" -> {
                                                FINEATTIVITA_p++;
                                                FINEATTIVITA_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "DVB" -> {
                                                DAVALIDAREMODELLO6_p++;
                                                DAVALIDAREMODELLO6_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "MA" -> {
                                                INATTESADIMAPPATURA_p++;
                                                INATTESADIMAPPATURA_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "IV" -> {
                                                INVERIFICA_p++;
                                                INVERIFICA_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "CK" -> {
                                                ESITOVERIFICACONCLUSO_p++;
                                                ESITOVERIFICACONCLUSO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "EVI" -> {
                                                ESITOVERIFICAINVIATO_p++;
                                                ESITOVERIFICAINVIATO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                            case "CO" -> {
                                                CONCLUSO_p++;
                                                CONCLUSO_a += db1.get_allievi_accreditati(idpr);
                                            }
                                        }
                                    }

                                    XSSFRow row = getRow(sh1, indiceriga.get());
                                    indiceriga.addAndGet(1);

                                    AtomicInteger indicecolonna = new AtomicInteger(0);
                                    setCell(getCell(row, indicecolonna.get()), rs0.getString("sa.ragionesociale").toUpperCase());
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("sa.piva").toUpperCase());

                                    //NUOVI 3 campi
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FINEATTIVITA_p + DAVALIDAREMODELLO6_p + INATTESADIMAPPATURA_p + INVERIFICA_p + ESITOVERIFICACONCLUSO_p + ESITOVERIFICAINVIATO_p + CONCLUSO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FINEATTIVITA_a + DAVALIDAREMODELLO6_a + INATTESADIMAPPATURA_a
                                            + INVERIFICA_a + ESITOVERIFICACONCLUSO_a + ESITOVERIFICAINVIATO_a + CONCLUSO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEA_p + FASEB_p + SOSPESO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEA_a + FASEB_a + SOSPESO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDARE_p + PROGRAMMATO_p + DACONFERMARE_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDARE_a + PROGRAMMATO_a + DACONFERMARE_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(docenti));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDARE_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDARE_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(PROGRAMMATO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(PROGRAMMATO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DACONFERMARE_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DACONFERMARE_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEA_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEA_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEB_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FASEB_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(SOSPESO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(SOSPESO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(RIGETTATO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(RIGETTATO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FINEATTIVITA_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(FINEATTIVITA_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDAREMODELLO6_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(DAVALIDAREMODELLO6_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ESITOVERIFICACONCLUSO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ESITOVERIFICACONCLUSO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ESITOVERIFICAINVIATO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ESITOVERIFICAINVIATO_a));

                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(CONCLUSO_p));
                                    setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(CONCLUSO_a));

                                }
                            }
                        }

                    }
                }

                //FOGLIO 2
                XSSFSheet sh2 = wb.getSheetAt(1);

                String sql0_foglio2 = "SELECT sa.ragionesociale,sa.piva,co.regione,pf.cip,st.descrizione,pf.start,"
                        + "pf.end,pf.idprogetti_formativi,sa.idsoggetti_attuatori,pf.extract "
                        + "FROM progetti_formativi pf, soggetti_attuatori sa, comuni co, stati_progetto st "
                        + "WHERE sa.idsoggetti_attuatori=pf.idsoggetti_attuatori AND co.idcomune=sa.comune "
                        + "AND st.idstati_progetto=pf.stato";

                AtomicInteger indiceriga2 = new AtomicInteger(1);
                try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0_foglio2)) {
                    while (rs0.next()) {
                        String rendicontato = "NO";
                        if (rs0.getInt("pf.extract") != 0) {
                            rendicontato = switch (rs0.getInt("pf.extract")) {
                                case 1 ->
                                    "SI";
                                case 2 ->
                                    "IN ATTESA";
                                default ->
                                    "NO";
                            };
                        }

                        XSSFRow row = getRow(sh2, indiceriga2.get());
                        indiceriga2.addAndGet(1);

                        AtomicInteger indicecolonna = new AtomicInteger(0);
                        setCell(getCell(row, indicecolonna.get()), rs0.getString("sa.ragionesociale").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("sa.piva").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.cip") == null ? "" : rs0.getString("pf.cip").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("st.descrizione").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rendicontato);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.start") == null ? "" : sdfITA.format(rs0.getDate("pf.start")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.end") == null ? "" : sdfITA.format(rs0.getDate("pf.end")));

                        AtomicInteger ALLIEVIISCRITTI = new AtomicInteger(0);
                        AtomicInteger ALLIEVIVALIDATI = new AtomicInteger(0);
                        AtomicInteger ALLIEVIIDONEI = new AtomicInteger(0);
                        int idpr = rs0.getInt("pf.idprogetti_formativi");
                        String sql1_foglio2 = "SELECT a.idallievi,a.id_statopartecipazione FROM allievi a WHERE a.idprogetti_formativi = ?";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(sql1_foglio2)) {
                            ps1.setInt(1, idpr);
                            try (ResultSet rs1 = ps1.executeQuery()) {
                                while (rs1.next()) {
                                    String statoallievo = rs1.getString("a.id_statopartecipazione");
                                    if (statoallievo.equals("15")) {
                                        ALLIEVIVALIDATI.addAndGet(1);
                                    } else if (statoallievo.equals("18")) {
                                        ALLIEVIIDONEI.addAndGet(1);
                                    }
                                    ALLIEVIISCRITTI.addAndGet(1);
                                }
                            }
                        }
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ALLIEVIISCRITTI.get()));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ALLIEVIVALIDATI.get()));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ALLIEVIIDONEI.get()));
                    }
                }

                wb.write(outputStream);
            }

            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = ? WHERE idestrazione=3";
            try (PreparedStatement ps = db1.getConnection().prepareStatement(upd)) {
                ps.setString(1, fileout);
                ps.executeUpdate();
            }
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

    //MODELLO 0
    public void update_modello0() {
        boolean testing = false;
        FaseA FA = new FaseA(testing);

        try {

            String pathdest = "/mnt/mcn/yisu_toscana/SoggettiAttuatori/";
            Db_Gest db1 = new Db_Gest(FA.getHost());

            String sql1 = "SELECT d.iddocumenti_allievi,d.path,d.idallievo FROM documenti_allievi d WHERE d.path LIKE '%/temp/%' AND d.deleted=0 ORDER BY d.idallievo";

            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    Long id_doc = rs1.getLong(1);
                    Long idallievo = rs1.getLong(3);
                    String pathor = rs1.getString(2);
                    String normalized_path = Paths.get(pathor).normalize().toString();
                    File modello0 = new File(normalized_path);
                    if (checkPDF(modello0)) {
                        File modello0_dest = new File(pathdest + modello0.getName().replaceAll("'", ""));
                        try {
                            FileUtils.copyFile(modello0, modello0_dest);
                            if (checkPDF(modello0_dest)) {
                                String update = "UPDATE documenti_allievi SET path = '" + modello0_dest.getPath() + "' WHERE iddocumenti_allievi=" + id_doc;

                                try (Statement st2 = db1.getConnection().createStatement()) {
                                    boolean upd = st2.executeUpdate(update) > 0;
                                    log.log(Level.INFO, "{0} ({1}) {2}", new Object[]{idallievo, upd, update});
                                }

                            } else {
                                log.log(Level.SEVERE, "NOT FOUND) {0}", modello0.getPath());
                            }
                        } catch (Exception ex1) {
                            log.severe(estraiEccezione(ex1));
                        }
                    } else {
                        log.log(Level.SEVERE, "NOT FOUND) {0}", modello0.getPath());
                    }
                }
            }

            db1.closeDB();

        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
    }

    // ATTESTATI
    public void attestati_ok() {
        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());
        String sql0 = "SELECT a.idallievi,a.idprogetti_formativi,a.gruppo_faseB FROM allievi a, progetti_formativi p WHERE a.idprogetti_formativi=p.idprogetti_formativi "
                + "AND a.orec_totali>=70 AND p.stato IN ('F','DVB','IV','CK') AND a.idallievi NOT IN (SELECT d.idallievo FROM documenti_allievi d WHERE d.tipo=22)";

        try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql0)) {
            while (rs.next()) {
                Long idallievo = rs.getLong(1);

                //CAMBIO STATO
                String update = "UPDATE allievi SET id_statopartecipazione='18' WHERE idallievi = " + idallievo;
                try (Statement st2 = db0.getConnection().createStatement()) {
                    st2.executeUpdate(update);
                }

                String gruppo_faseB = rs.getString(3);
                Long idprogetto = rs.getLong(2);

                String sql1 = "SELECT GROUP_CONCAT(lm.tipolez SEPARATOR ';') AS tipomod,u.fase,u.descrizione,lc.codice_ud,lm.tipolez,lm.gruppo_faseB "
                        + " FROM lezioni_modelli lm, modelli_progetti m, lezione_calendario lc, unita_didattiche u "
                        + " WHERE m.id_progettoformativo = ? AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB = ?) "
                        + " AND m.id_modello=lm.id_modelli_progetto "
                        + " AND lc.id_lezionecalendario=lm.id_lezionecalendario "
                        + " AND lc.codice_ud=u.codice GROUP BY u.descrizione,u.fase,lm.gruppo_faseB ORDER BY u.fase,u.ordine";

                Map<String, String> tipoud = new HashMap<>();

                try (PreparedStatement ps1 = db0.getConnection().prepareStatement(sql1)) {
                    ps1.setString(1, String.valueOf(idprogetto));
                    ps1.setString(2, gruppo_faseB);
                    try (ResultSet rs1 = ps1.executeQuery()) {

                        while (rs1.next()) {
//                        String gf = rs1.getString("lm.gruppo_faseB");
                            String fase = rs1.getString("u.fase").endsWith("A") ? "A" : "B";
                            String nomeud = rs1.getString("u.descrizione");
                            String tipomod = rs1.getString("tipomod");
                            String nomefield = fase + "_" + nomeud + "_MOD";
                            String valuefield = "";
                            if (tipomod.contains("P") && tipomod.contains("F")) {
                                valuefield = "MISTA";
                            } else if (tipomod.contains("F")) {
                                valuefield = "FAD";
                            } else if (tipomod.contains("P")) {
                                valuefield = "IN PRESENZA";
                            }
                            tipoud.put(nomefield, valuefield);
                        }
                    }
                }

                File at1 = Pdf.MODELLO7_OK(db0, idallievo, tipoud, new DateTime(), true);
                if (at1 != null) {
                    String ins = "INSERT INTO documenti_allievi (path,idallievo,tipo,deleted) VALUES (?,?,?,?)";
                    try (PreparedStatement ps = db0.getConnection().prepareStatement(ins)) {
                        ps.setString(1, at1.getPath());
                        ps.setLong(2, idallievo);
                        ps.setString(3, "22");
                        ps.setString(4, "1");
                        ps.execute();
                    } catch (Exception ex1) {
                        log.severe(estraiEccezione(ex1));
                    }
                }
            }
        } catch (Exception ex2) {
            log.severe(estraiEccezione(ex2));
        }

        db0.closeDB();
    }

    public void attestati_competenzedigitali() {
        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        String sql0 = "SELECT a.idallievi FROM allievi a WHERE a.codudok_A LIKE '%UD11%' AND a.idallievi NOT IN (SELECT d.idallievo FROM documenti_allievi d WHERE d.tipo=24)";
        try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql0)) {
            while (rs.next()) {

                Long idallievo = rs.getLong(1);

                List<String> tipo = new ArrayList<>();
                List<String> date = new ArrayList<>();
                String sql2_F0 = "SELECT r.data FROM registro_completo r WHERE r.idutente=" + idallievo
                        + " AND r.ruolo='ALLIEVO' AND r.nud LIKE '%A-11%'";

                try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql2_F0)) {
                    while (rs0.next()) {
                        date.add(rs0.getString(1));
                        tipo.add("F");
                    }
                } catch (Exception ex) {
                    log.severe(estraiEccezione(ex));
                }

                String sql2_P = "SELECT p.datalezione FROM presenzelezioni p, presenzelezioniallievi r, lezioni_modelli l , lezione_calendario lc "
                        + "WHERE p.idpresenzelezioni=r.idpresenzelezioni AND l.id_lezionimodelli=p.idlezioneriferimento AND l.id_lezionecalendario=lc.id_lezionecalendario "
                        + "AND r.idallievi=" + idallievo + " AND r.convalidata=1 AND lc.codice_ud LIKE '%A-11%'";

                try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql2_P)) {
                    while (rs1.next()) {
                        date.add(rs1.getString(1));
                        tipo.add("P");
                    }
                } catch (Exception ex1) {
                    log.severe(estraiEccezione(ex1));
                }

                date = date.stream().distinct().sorted().collect(Collectors.toList());
                tipo = tipo.stream().distinct().sorted().collect(Collectors.toList());

                String datainizio = "";
                String datafine = "";

                AtomicInteger index = new AtomicInteger(1);

                for (String d1 : date) {
                    if (index.get() == 1) {
                        datainizio = d1;
                        datafine = d1;
                    } else {
                        try {
                            DateTime dt_datainizio = dtf.parseDateTime(datainizio);
                            DateTime dt_datafine = dtf.parseDateTime(datafine);
                            DateTime dt_verifica = dtf.parseDateTime(d1);
                            if (dt_datainizio.isBefore(dt_verifica)) {
                            } else {
                                datainizio = d1;
                            }
                            if (dt_datafine.isAfter(dt_verifica)) {
                            } else {
                                datafine = d1;
                            }
                        } catch (Exception ex2) {
                            log.severe(estraiEccezione(ex2));
                        }
                    }
                    index.addAndGet(1);
                }

                String upd3 = "UPDATE allievi SET data_inizio_UD11 = ?, data_fine_UD11 = ? WHERE idallievi = ?";
                try (PreparedStatement ps3A = db0.getConnection().prepareStatement(upd3)) {
                    ps3A.setString(1, datainizio);
                    ps3A.setString(2, datafine);
                    ps3A.setString(3, String.valueOf(idallievo));
                    boolean es = ps3A.executeUpdate(upd3) > 0;
                    log.log(Level.WARNING, "{0} )  {1} : {2}", new Object[]{idallievo, upd3, es});
                } catch (Exception ex3) {
                    log.severe(estraiEccezione(ex3));
                }

                String tipo_descr;
                if (tipo.size() > 1) {
                    tipo_descr = "MISTA";
                } else {
                    tipo_descr = tipo.get(0).equals("F") ? "FAD" : "IN PRESENZA";
                }

                File at1 = Pdf.MODELLO7_UD11(db0, idallievo, tipo_descr, new DateTime(), true);

                if (at1 != null) {
                    String ins = "INSERT INTO documenti_allievi (path,idallievo,tipo,deleted) VALUES (?,?,?,?)";
                    try (PreparedStatement ps = db0.getConnection().prepareStatement(ins)) {
                        ps.setString(1, at1.getPath());
                        ps.setLong(2, idallievo);
                        ps.setString(3, "24");
                        ps.setString(4, "1");
                        ps.execute();
                    } catch (Exception ex4) {
                        log.severe(estraiEccezione(ex4));
                    }
                }

            }
        } catch (Exception ex5) {
            log.severe(estraiEccezione(ex5));
        }

        db0.closeDB();
    }

    public void attestati_UD(int iduser) {
        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        String sql0 = "SELECT a.idallievi,a.idprogetti_formativi,a.codudok_A,a.codudok_B,a.gruppo_faseB,a.id_statopartecipazione FROM allievi a WHERE a.orec_totali < 70 "
                + "AND a.idprogetti_formativi IN (SELECT p.idprogetti_formativi FROM progetti_formativi p WHERE p.stato IN ('F','DVB','IV','CK','EVI','CO')) "
                + "AND a.idallievi NOT IN (SELECT d.idallievo FROM documenti_allievi d WHERE d.tipo=23)";
        if (iduser > 0) {
            sql0 = "SELECT a.idallievi,a.idprogetti_formativi,a.codudok_A,a.codudok_B,a.gruppo_faseB,a.id_statopartecipazione FROM allievi a WHERE a.idallievi = ?";
        }

        System.out.println(sql0);
        try (PreparedStatement ps = db0.getConnection().prepareStatement(sql0)) {
            if (iduser > 0) {
                ps.setInt(1, iduser);
            }
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Long idallievo = rs.getLong(1);

                    //CAMBIO STATO
                    String stato_or = rs.getString("a.id_statopartecipazione");
                    if (stato_or.equals("16") || stato_or.equals("17")) {
                        //RITIRATO
                    } else {
                        String update = "UPDATE allievi SET id_statopartecipazione='19' WHERE idallievi = " + idallievo;
                        try (Statement st2 = db0.getConnection().createStatement()) {
                            st2.executeUpdate(update);
                        }
                    }

                    Long idprogetto = rs.getLong(2);

                    String ud_ok_A = rs.getString(3) == null ? "" : rs.getString(3);
                    String ud_ok_B = rs.getString(4) == null ? "" : rs.getString(4);

                    List<String> li_A = ud_ok_A.contains(";") ? Splitter.on(";").splitToList(ud_ok_A) : new ArrayList<>();
                    List<String> li_B = ud_ok_B.contains(";") ? Splitter.on(";").splitToList(ud_ok_B) : new ArrayList<>();

                    try {
                        Collections.sort(li_A);
                    } catch (Exception e) {
                    }
                    try {
                        Collections.sort(li_B);
                    } catch (Exception e) {
                    }

                    String sql1;
                    String gruppo_faseB = rs.getString(5);
                    if (gruppo_faseB == null) {
                        sql1 = "SELECT GROUP_CONCAT(lm.tipolez SEPARATOR ';') AS tipomod,u.fase,u.descrizione,lc.codice_ud,lm.tipolez,lm.gruppo_faseB,u.definizione "
                                + " FROM lezioni_modelli lm, modelli_progetti m, lezione_calendario lc, unita_didattiche u "
                                + " WHERE m.id_progettoformativo = ? AND lm.gruppo_faseB = 0 "
                                + " AND m.id_modello=lm.id_modelli_progetto "
                                + " AND lc.id_lezionecalendario=lm.id_lezionecalendario "
                                + " AND lc.codice_ud=u.codice GROUP BY u.descrizione,u.fase,lm.gruppo_faseB ORDER BY u.fase,u.ordine";
                    } else {
                        sql1 = "SELECT GROUP_CONCAT(lm.tipolez SEPARATOR ';') AS tipomod,u.fase,u.descrizione,lc.codice_ud,lm.tipolez,lm.gruppo_faseB,u.definizione "
                                + " FROM lezioni_modelli lm, modelli_progetti m, lezione_calendario lc, unita_didattiche u "
                                + " WHERE m.id_progettoformativo = ? AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB = ?) "
                                + " AND m.id_modello=lm.id_modelli_progetto "
                                + " AND lc.id_lezionecalendario=lm.id_lezionecalendario "
                                + " AND lc.codice_ud=u.codice GROUP BY u.descrizione,u.fase,lm.gruppo_faseB ORDER BY u.fase,u.ordine";
                    }

                    Map<String, String[]> tipoud = new HashMap<>();

                    try (PreparedStatement ps1 = db0.getConnection().prepareStatement(sql1)) {
                        ps1.setString(1, String.valueOf(idprogetto));
                        if (gruppo_faseB != null) {
                            ps1.setString(2, gruppo_faseB);
                        }
                        try (ResultSet rs1 = ps1.executeQuery()) {
                            while (rs1.next()) {
                                String fase = rs1.getString("u.fase").endsWith("A") ? "A" : "B";
                                String nomeud = rs1.getString("u.descrizione");
                                String definizione = rs1.getString("u.definizione");
                                String tipomod = rs1.getString("tipomod");
                                String nomefield = fase + "_" + nomeud;
                                String valuefield = "";
                                if (tipomod.contains("P") && tipomod.contains("F")) {
                                    valuefield = "MISTA";
                                } else if (tipomod.contains("F")) {
                                    valuefield = "FAD";
                                } else if (tipomod.contains("P")) {
                                    valuefield = "IN PRESENZA";
                                }

                                String[] v1 = {valuefield, definizione};
                                tipoud.put(nomefield, v1);
                            }
                        }
                    }
                    StringBuilder A_UD = new StringBuilder("");
                    StringBuilder B_UD = new StringBuilder("");
                    if (!li_A.isEmpty()) {
                        for (String ud : li_A) {
                            if (!ud.trim().equals("")) {
                                String cod = "A_" + ud;
                                String[] v1 = tipoud.get(cod);
                                if (v1 != null) {
                                    A_UD.append(StringUtils.replace(v1[1], "@modalita", v1[0]));
                                }
                            }
                        }
                    }
                    if (!li_B.isEmpty()) {
                        for (String ud : li_B) {
                            if (!ud.trim().equals("")) {
                                String cod = "B_" + ud;
                                String[] v1 = tipoud.get(cod);
                                if (v1 != null) {
                                    B_UD.append(StringUtils.replace(v1[1], "@modalita", v1[0]));
                                }
                            }
                        }
                    }

                    if (!A_UD.toString().equals("") || !B_UD.toString().equals("")) {
                        File at1 = Pdf.MODELLO7_UD(db0, idallievo, tipoud, li_A, li_B, new DateTime(), true);

                        if (iduser > 0) {
                            log.info(at1.getPath() + " RILASCIATO.");
                        } else {
                            String ins = "INSERT INTO documenti_allievi (path,idallievo,tipo,deleted) VALUES (?,?,?,?)";
                            try (PreparedStatement ps1 = db0.getConnection().prepareStatement(ins)) {
                                ps1.setString(1, at1.getPath());
                                ps1.setLong(2, idallievo);
                                ps1.setString(3, "23");
                                ps1.setString(4, "1");
                                ps1.execute();
                            } catch (Exception ex6) {
                                log.severe(estraiEccezione(ex6));
                            }

                        }
                    } else {
                        System.out.println("a: " + A_UD.toString());
                        System.out.println("b: " + B_UD.toString());
                    }

                }
            }
        } catch (Exception ex7) {
            log.severe(estraiEccezione(ex7));
        }

        db0.closeDB();
    }

    //ORE UD
    public void ore_ud() {
        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        List<Long> idallievi = new ArrayList<>();
        String sql = "SELECT a.idallievi FROM allievi a WHERE a.id_statopartecipazione IN ('15','16','17')";
        try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                idallievi.add(rs.getLong(1));
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }

        //SALVA UD
        String sql0 = "SELECT u.descrizione,u.fase FROM unita_didattiche u GROUP BY u.descrizione,u.fase ORDER BY ordine;";
        try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
            while (rs0.next()) {
                String UD_desc = rs0.getString(1);
                String UD_fase = rs0.getString(2);
                String sql1 = "SELECT u.codice,u.ore FROM unita_didattiche u WHERE u.descrizione = ? AND u.fase = ?";
                Map<String, String> MOD_ore = new HashMap<>();
                List<String> MOD_codici = new ArrayList<>();
                int ore = 0;
                try (PreparedStatement ps1 = db0.getConnection().prepareStatement(sql1)) {
                    ps1.setString(1, UD_desc);
                    ps1.setString(2, UD_fase);
                    try (ResultSet rs1 = ps1.executeQuery()) {
                        while (rs1.next()) {
                            MOD_ore.put(rs1.getString(1), rs1.getString(2));
                            MOD_codici.add(rs1.getString(1));
                            ore += rs1.getInt(2);
                        }
                    }
                }
                if (MOD_codici.size() == 4 && MOD_codici.get(0).startsWith("A")) {
                    String ud11_1 = "A-11A_A-11B";
                    String ud11_2 = "A-11B_A-11C";
                    String ud11_3 = "A-11C_A-11D";
                    MOD_codici.add(ud11_1);
                    MOD_codici.add(ud11_2);
                    MOD_codici.add(ud11_3);
                    MOD_ore.put(ud11_1, "5");
                    MOD_ore.put(ud11_2, "5");
                    MOD_ore.put(ud11_3, "5");
                } else if (MOD_codici.size() == 4 && MOD_codici.get(0).startsWith("B")) {
                    String ud11_1 = "B-1A_B-1B";
                    String ud11_2 = "B-1B_B-1C";
                    String ud11_3 = "B-1C_B-1D";
                    MOD_codici.add(ud11_1);
                    MOD_codici.add(ud11_2);
                    MOD_codici.add(ud11_3);
                    MOD_ore.put(ud11_1, "5");
                    MOD_ore.put(ud11_2, "5");
                    MOD_ore.put(ud11_3, "5");
                }
                String nomemodulo = "";
                for (String m1 : MOD_codici) {
                    nomemodulo += m1 + "_";
                }
                nomemodulo = StringUtils.chop(nomemodulo);
                for (Long idallievo : idallievi) {

                    String sql2_F0 = "SELECT r.totaleorerendicontabili,r.data FROM registro_completo r WHERE r.idutente=" + idallievo
                            + " AND r.ruolo='ALLIEVO' AND r.nud='" + nomemodulo + "'";

                    String datainizio;
                    String datafine;
                    try (Statement st2F0 = db0.getConnection().createStatement(); ResultSet rs2F0 = st2F0.executeQuery(sql2_F0)) {
                        if (rs2F0.next()) {
                            //FAD 2 MODULI IN UNO
                            double res = rs2F0.getLong(1) / 3600000.00;
                            datainizio = sdfSQL.format(rs2F0.getDate(2));
                            datafine = sdfSQL.format(rs2F0.getDate(2));

                            db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(ore)), res)),
                                    datafine, datainizio, nomemodulo.split("-")[0], String.valueOf(res), String.valueOf(ore), nomemodulo, String.valueOf(idallievo));
                            //System.out.println(idallievo+" : "+nomemodulo + " - " + ore + " : " + res + " - " + datainizio + " - " + datafine + " -- " + nomemodulo.split("-")[0]);
                        } else {

                            for (String m1 : MOD_codici) {
                                String sql2_F1 = "SELECT r.totaleorerendicontabili,r.data FROM registro_completo r WHERE r.idutente = ? AND r.ruolo='ALLIEVO' AND r.nud = ?";
                                try (PreparedStatement ps2F1 = db0.getConnection().prepareStatement(sql2_F1)) {
                                    ps2F1.setString(1, String.valueOf(idallievo));
                                    ps2F1.setString(2, m1);
                                    try (ResultSet rs2F1 = ps2F1.executeQuery()) {
                                        if (rs2F1.next()) {
                                            //FAD 2 MODULI SINGOLI
                                            double res = rs2F1.getLong(1) / 3600000.00;
                                            datainizio = sdfSQL.format(rs2F1.getDate(2));
                                            datafine = sdfSQL.format(rs2F1.getDate(2));
                                            db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res)),
                                                    datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
                                        } else {
                                            String sql2_P = "SELECT r.durataconvalidata,p.datalezione FROM presenzelezioni p, presenzelezioniallievi r, lezioni_modelli l , lezione_calendario lc "
                                                    + "WHERE p.idpresenzelezioni=r.idpresenzelezioni AND l.id_lezionimodelli=p.idlezioneriferimento AND l.id_lezionecalendario=lc.id_lezionecalendario "
                                                    + "AND r.idallievi = ? AND r.convalidata=1 AND lc.codice_ud = ? AND r.durataconvalidata > 0";
                                            try (PreparedStatement ps2P = db0.getConnection().prepareStatement(sql2_P)) {
                                                ps2P.setString(1, String.valueOf(idallievo));
                                                ps2P.setString(2, m1);
                                                try (ResultSet rs2P = ps2P.executeQuery()) {
                                                    if (rs2P.next()) {
                                                        //PRESENZA MODULI SINGOLI
                                                        double res = rs2P.getLong(1) / 3600000.00;
                                                        datainizio = sdfSQL.format(rs2P.getDate(2));
                                                        datafine = sdfSQL.format(rs2P.getDate(2));
                                                        db0.insert_UD_presenza((Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res) == 0) ? "1" : "0",
                                                                datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        //UD
        List<UD> elencoud = new ArrayList<>();
        String sql3A = "SELECT descrizione,sum(ore),fase,GROUP_CONCAT(codice SEPARATOR ';') AS moduli FROM unita_didattiche GROUP BY descrizione,fase ORDER BY fase,ordine";
        try (Statement st3A = db0.getConnection().createStatement(); ResultSet rs3A = st3A.executeQuery(sql3A)) {
            while (rs3A.next()) {
                elencoud.add(new UD(rs3A.getString(1), rs3A.getDouble(2), rs3A.getString(3), rs3A.getString(4)));
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        for (Long idallievo : idallievi) {
////            ORE
            String sql3 = "SELECT SUM(p.orepresenze),p.fase FROM presenzeudallievi p WHERE p.idallievi=" + idallievo + " GROUP BY p.fase";
            try (Statement st3 = db0.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                while (rs3.next()) {
                    String field = rs3.getString(2).toLowerCase().equals("a") ? "orec_fasea" : "orec_faseb";
                    String upd3 = "UPDATE allievi SET " + field + " = ? WHERE idallievi = ?";
                    try (PreparedStatement ps3A = db0.getConnection().prepareStatement(upd3)) {
                        ps3A.setString(1, String.valueOf(rs3.getDouble(1)));
                        ps3A.setString(2, String.valueOf(idallievo));
                        boolean es = ps3A.executeUpdate(upd3) > 0;
                        log.log(Level.WARNING, "{0} ) {1} -- {2} : {3}", new Object[]{idallievo, rs3.getString(2), rs3.getDouble(1), es});
                    }
                }
            } catch (Exception ex1) {
                log.severe(estraiEccezione(ex1));
            }
////            UNITA DIDATTICHE
            int ud_ok_A = 0;
            int ud_ok_B = 0;

            String codudok_A = "";
            String codudok_B = "";
            for (UD unit : elencoud) {

                double oreallievo = 0.0;

                String sql4 = "SELECT * FROM presenzeudallievi p WHERE p.idallievi=" + idallievo + " AND (";
                List<String> moduli = Splitter.on(";").splitToList(unit.getModuli());

                for (String m1 : moduli) {
                    sql4 += " ud LIKE '%" + m1 + "%' OR";
                }

                sql4 = StringUtils.substring(sql4, 0, sql4.length() - 2).trim() + ")";
//                    System.out.println(sql4);
                try (Statement st4 = db0.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
                    while (rs4.next()) {
                        oreallievo += rs4.getDouble("orepresenze");
                    }
                } catch (Exception ex2) {
                    log.severe(estraiEccezione(ex2));
                }
                if (unit.getOre() == oreallievo) {
                    if (unit.getFase().endsWith("A")) {
                        ud_ok_A++;
                        codudok_A += unit.getDescrizione() + ";";
                    } else if (unit.getFase().endsWith("B")) {
                        ud_ok_B++;
                        codudok_B += unit.getDescrizione() + ";";
                    }
                }
            }

            String upd4 = "UPDATE allievi SET codudok_A = ?, codudok_B = ?, ud_ok_A = ?, ud_ok_B = ? WHERE idallievi = ?";

            try (PreparedStatement ps4 = db0.getConnection().prepareStatement(upd4)) {
                ps4.setString(1, codudok_A);
                ps4.setString(2, codudok_B);
                ps4.setInt(3, ud_ok_A);
                ps4.setInt(4, ud_ok_B);
                ps4.setLong(5, idallievo);
                boolean es = ps4.executeUpdate() > 0;
                log.log(Level.WARNING, "{0} - {1}", new Object[]{ps4.toString(), es});
            } catch (Exception ex3) {
                log.severe(estraiEccezione(ex3));
            }

            //ASSENZE
            int assenzeOK = 0;
            int assenzeKO = 0;
            String sql5 = "SELECT p.datarealelezione,p.durataconvalidata FROM presenzelezioniallievi p "
                    + "WHERE p.convalidata=1 AND (p.durataconvalidata=0 OR p.durataconvalidata=-1) "
                    + "AND p.idallievi=" + idallievo + " GROUP BY LEFT(p.datarealelezione,10)";

            try (Statement st5 = db0.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                while (rs5.next()) {
                    if (rs5.getInt(2) == 0) {
                        assenzeOK++;
                    } else {
                        assenzeKO++;
                    }
                }
            } catch (Exception ex4) {
                log.severe(estraiEccezione(ex4));
            }

            if (assenzeOK > 0 || assenzeKO > 0) {
                String upd5 = "UPDATE allievi SET assenzeOK = ?, assenzeKO = ? WHERE idallievi = ?";
                try (PreparedStatement ps5 = db0.getConnection().prepareStatement(upd5)) {
                    ps5.setInt(1, assenzeOK);
                    ps5.setInt(2, assenzeKO);
                    ps5.setLong(3, idallievo);
                    boolean es = ps5.executeUpdate() > 0;
                    log.log(Level.WARNING, "{0} - {1}", new Object[]{ps5.toString(), es});
                } catch (Exception ex5) {
                    log.severe(estraiEccezione(ex5));
                }

                if (assenzeKO >= 2) {
                    //CAMBIO STATO
                    log.log(Level.SEVERE, "(ASSENZE KO) {0}", idallievo);
                }
            }

        }

        db0.closeDB();
    }

    public void imposta_fineattivita() {
        Db_Gest db = new Db_Gest(this.host);
        String sql0 = "SELECT p.idprogetti_formativi FROM progetti_formativi p WHERE p.stato = 'ATB' AND p.`end` < CURDATE()";
        try (Statement st0 = db.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
            while (rs0.next()) {
                String idpr = rs0.getString(1);
                String sql1 = "SELECT a.idallievi,a.gruppo_faseB,a.idprogetti_formativi FROM allievi a "
                        + "WHERE a.id_statopartecipazione = '15' "
                        + "AND a.idprogetti_formativi = ?";
                boolean fineattivita = true;
                List<String> idlezioniconvalidate = new ArrayList<>();
                try (PreparedStatement ps1 = db.getConnection().prepareStatement(sql1)) {
                    ps1.setString(1, idpr);
                    try (ResultSet rs1 = ps1.executeQuery()) {
                        while (rs1.next()) {
                            String idallievi = rs1.getString(1);
                            String gruppo_fb = rs1.getString(2);
                            String sql2 = "SELECT la.convalidata,p.datalezione,p.idlezioneriferimento FROM presenzelezioniallievi la, presenzelezioni p "
                                    + "WHERE la.idallievi =? AND p.idpresenzelezioni=la.idpresenzelezioni "
                                    + "AND p.idlezioneriferimento IN (SELECT m.id_lezionimodelli FROM lezioni_modelli m WHERE m.id_modelli_progetto IN "
                                    + "(SELECT mp.id_modello FROM modelli_progetti mp WHERE mp.id_progettoformativo = ?) AND m.gruppo_faseB IN (0,?))"
                                    + " ORDER BY p.datalezione ASC , la.convalidata DESC";
                            try (PreparedStatement ps2 = db.getConnection().prepareStatement(sql2)) {
                                ps2.setString(1, idallievi);
                                ps2.setString(2, idpr);
                                ps2.setString(3, gruppo_fb);
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    while (rs2.next()) {
                                        boolean conv = rs2.getBoolean(1);
                                        if (conv) {
                                            idlezioniconvalidate.add(rs2.getString(3));
                                        } else {
                                            if (!idlezioniconvalidate.contains(rs2.getString(3))) {
                                                log.log(Level.WARNING, "PROGETTO {0} - ALLIEVO {1} : NON ANCORA CONVALIDATA LEZIONE {2}", new Object[]{idpr, idallievi, rs2.getString(2)});
                                                fineattivita = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex2) {
                                log.severe(estraiEccezione(ex2));
                            }
                        }
                    }
                } catch (Exception ex2) {
                    log.severe(estraiEccezione(ex2));
                }
                if (fineattivita) {
                    String upd = "UPDATE progetti_formativi SET stato = 'F' WHERE idprogetti_formativi = ?";
                    try (PreparedStatement ps3 = db.getConnection().prepareStatement(upd)) {
                        ps3.setString(1, idpr);
                        boolean es3 = ps3.executeUpdate(upd) > 0;
                        if (es3) {
                            log.log(Level.INFO, "PROGETTO {0} IMPOSTATO IN FINE ATTIVITA'.", idpr);
                        } else {
                            log.log(Level.SEVERE, "ERRORE NELL''UPDATE DEL PROGETTO {0}", idpr);
                        }
                    }
                } else {
                    log.log(Level.INFO, "PROGETTO {0} ANCORA DA VALIDARE.", idpr);
                }
            }
        } catch (Exception ex1) {
            log.severe(estraiEccezione(ex1));
        }
        db.closeDB();
    }

//    public static void main(String[] args) {
//        new Toscana_gestione(false).report_docenti_GG1();
//    }
}
