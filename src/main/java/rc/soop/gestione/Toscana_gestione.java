/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import static rc.soop.exe.Utils.conf;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.patternITA;
import static rc.soop.exe.Utils.patternSql;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.metadata.Database;
import org.joda.time.DateTime;
import org.joda.time.Years;
import rc.soop.exe.Utils;
import static rc.soop.exe.Utils.getRow;
import static rc.soop.exe.Utils.getCell;
import static rc.soop.exe.Utils.sdfITA;
import static rc.soop.exe.Utils.setCell;
import static rc.soop.exe.Utils.timestamp;
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
        String sql1 = "SELECT a.idallievi FROM allievi a WHERE a.id_statopartecipazione='15'";
        try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
            while (rs1.next()) {
                int idallievi = rs1.getInt(1);
                String sql2 = "SELECT r.totaleorerendicontabili,r.fase FROM registro_completo r WHERE r.idutente='" 
                        + idallievi + "' AND r.ruolo='ALLIEVO'";
                String sql3 = "SELECT a.durataconvalidata FROM presenzelezioniallievi a WHERE a.idallievi = '" 
                        + idallievi + "' AND a.convalidata=1";
//                String sql3 = "SELECT p.durataconvalidata,z.codice_ud FROM presenzelezioniallievi p, presenzelezioni l, lezione_calendario z "
//                        + "WHERE p.idallievi = '" + idallievi + "' AND p.convalidata=1 AND l.idpresenzelezioni=p.idpresenzelezioni "
//                        + "AND l.idlezioneriferimento=z.id_lezionecalendario ";

                Long presenze = 0L;

                try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {

                    while (rs2.next()) {
                        report.add(idallievi + ";" + rs2.getString(2) + ";" + rs2.getLong(1));
                        presenze += rs2.getLong(1);
                    }
                }

                try (Statement st3 = db1.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                    while (rs3.next()) {
                        Long conv = rs3.getString(1) == null ? 0L : rs3.getLong(1);
//                        report.add(idallievi + ";" + StringUtils.substring(rs3.getString(2), 0, 1) + ";" + conv);
                        presenze += conv;
                    }
                }

                double res = presenze / 3600000.00;

                String upd4 = "UPDATE allievi SET importo = '" + res + "' WHERE idallievi='" + idallievi + "'";

                try (Statement st4 = db1.getConnection().createStatement()) {
                    st4.executeUpdate(upd4);
                }
                
                System.out.println(sql1);
                System.out.println(sql2);
                System.out.println(sql3);
                System.out.println(upd4);

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
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND pf.idprogetti_formativi=mp.id_progettoformativo AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' ORDER BY lm.gruppo_faseB,lm.orario_start";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    String fase = rs1.getString("ud.fase");
                    if (fase.endsWith("A")) {
                        String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = " + idprogetti_formativi + " AND numerocorso = 1";
                        try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                            String nomestanza = startroom + idprogetti_formativi + "_A1";
                            if (test) {
                                nomestanza = "TESTING_" + nomestanza;
                            }
                            if (rs2.next()) {//VERIFICO SE ATTIVA
                                nomestanza = rs2.getString("nomestanza");
                                String stato = rs2.getString("stato");
                                if (stato.equals("0")) {
                                    //UPDATE ATTIVA
                                    try (Statement st3 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = '" + nomestanza + "'";
                                        st3.executeUpdate(upd);
                                    }
                                }
                            } else { //INSERISCO
                                try (Statement st3 = db1.getConnection().createStatement()) {
                                    String ins = "INSERT INTO fad_multi VALUES ('" + nomestanza + "'," + idprogetti_formativi + ",'1','" + new DateTime().toString("yyyy-MM-dd HH:mm:ss") + "','1')";
                                    st3.executeUpdate(ins);
                                }
                            }
                        }

                    } else if (fase.endsWith("B")) {
                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");

                        String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = " + idprogetti_formativi + " AND numerocorso = " + gruppo_faseB;

                        try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {

                            String nomestanza = startroom + idprogetti_formativi + "_B" + gruppo_faseB;
                            if (test) {
                                nomestanza = "TESTING_" + nomestanza;
                            }
                            if (rs2.next()) {//VERIFICO SE ATTIVA
                                nomestanza = rs2.getString("nomestanza");
                                String stato = rs2.getString("stato");
                                if (stato.equals("0")) {
                                    //UPDATE ATTIVA
                                    try (Statement st3 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = '" + nomestanza + "'";
                                        st3.executeUpdate(upd);
                                    }
                                }
                            } else { //INSERISCO
                                try (Statement st3 = db1.getConnection().createStatement()) {
                                    String ins = "INSERT INTO fad_multi VALUES ('" + nomestanza + "'," + idprogetti_formativi + ",'" + gruppo_faseB + "','" + new DateTime().toString("yyyy-MM-dd HH:mm:ss") + "','1')";
                                    st3.executeUpdate(ins);
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
        DbSSO dbs = new DbSSO();
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);
            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi"
                    + " AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";

            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            }

            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {

                while (rs1.next()) {

                    String fase = rs1.getString("ud.fase");
                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");
                    String sql3;
                    if (fase.endsWith("A")) {
                        sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione='15' AND idprogetti_formativi = " + idprogetti_formativi;
                    } else if (fase.endsWith("B")) {
                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");
                        sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione='15' AND idprogetti_formativi = " + idprogetti_formativi + " AND gruppo_faseB = " + gruppo_faseB;
                    } else {
                        continue;
                    }

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try (Statement st1A = db1.getConnection().createStatement(); ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
                    try (Statement st3 = db1.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                        while (rs3.next()) {
                            String codicefiscale = rs3.getString("codicefiscale").toUpperCase();
                            String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                            int idsoggetto = rs3.getInt("idallievi");
                            String email = rs3.getString("email").toLowerCase();
                            //VERIFICA
                            String sql4 = "SELECT user FROM fad_access WHERE type='S' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND ud ='" + ud + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);
                                if (!rs4.next()) {
                                    try (Statement st5 = db1.getConnection().createStatement()) {
                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','S','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
                                        st5.executeUpdate(ins);

                                        String ins_SSO = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','S','" + nomestanza + "','" + user + "','" + md5psw + "','" + codicefiscale + "')";
                                        log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(ins_SSO)});
                                        log.log(Level.INFO, "NUOVE CREDENZIALI DISCENTE ) {0}", nomecognome);
                                    }
                                } else {
                                    user = rs4.getString(1);
                                    try (Statement st5 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = " + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud + "' AND type = 'S' ";
                                        st5.executeUpdate(upd);
                                        log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(upd)});
                                        log.log(Level.INFO, "RECUPERO CREDENZIALI DISCENTE ) {0}", nomecognome);
                                    }
                                }
                                //INVIO MAIL
                                String sql5 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0'";
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
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
        dbs.closeDB();
    }

    ////////////////////////////////////////////////////////////////////////////
    public void fad_docenti(int idprogetti_formativi, boolean manual) {
        DbSSO dbs = new DbSSO();
        Db_Gest db1 = new Db_Gest(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);

            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY lm.id_docente,f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY lm.id_docente,f.nomestanza";
            }
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {

                    int id_docente = rs1.getInt("lm.id_docente");
                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' AND lm.id_docente = " + id_docente + " ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try (Statement st1A = db1.getConnection().createStatement(); ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");

                    String sql4 = "SELECT iddocenti,email,nome,cognome,codicefiscale FROM docenti WHERE iddocenti = " + id_docente;
                    try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
                        if (rs4.next()) {
                            String codicefiscale = rs4.getString("codicefiscale").toUpperCase();
                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                            int idsoggetto = rs4.getInt("iddocenti");
                            String email = rs4.getString("email").toLowerCase();
                            String sql5 = "SELECT user FROM fad_access WHERE type='D' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try (Statement st5 = db1.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);

                                if (!rs5.next()) {
                                    //CREO CREDENZIALI
                                    try (Statement st6 = db1.getConnection().createStatement()) {
                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','D','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
                                        st6.executeUpdate(ins);
                                        String ins_SSO = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','D','" + nomestanza + "','" + user + "','" + md5psw + "','" + codicefiscale + "')";
                                        log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(ins_SSO)});
                                        log.log(Level.INFO, "NUOVE CREDENZIALI DOCENTE ) {0}", nomecognome);
                                    }
                                } else { //CREDENZIALI GIA presenti
                                    user = rs5.getString(1);
                                    try (Statement st6 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = " + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud
                                                + "' AND type = 'D' ";
                                        st6.executeUpdate(upd);
                                        log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(upd)});
                                        log.log(Level.INFO, "RECUPERO CREDENZIALI DOCENTE ) {0}", nomecognome);
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
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
        dbs.closeDB();
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
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                        + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            }
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {

                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.tipolez='F' AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try (Statement st1A = db1.getConnection().createStatement(); ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");

                    String sql4O = "SELECT id_staff, nome, cognome, email FROM staff_modelli WHERE id_progettoformativo = " + idprogetti_formativi;
                    try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4O)) {
                        while (rs4.next()) {
                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                            int idsoggetto = rs4.getInt("id_staff");
                            String email = rs4.getString("email").toLowerCase();
                            String sql5 = "SELECT user FROM fad_access WHERE type='O' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try (Statement st5 = db1.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);
                                if (!rs5.next()) {
                                    //CREO CREDENZIALI
                                    try (Statement st6 = db1.getConnection().createStatement()) {
                                        String ins = "INSERT INTO fad_access VALUES ("
                                                + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','O','" + nomestanza + "','" + user + "','"
                                                + md5psw + "','" + ud + "')";

                                        st6.executeUpdate(ins);
                                    }
                                    log.log(Level.INFO, "NUOVE CREDENZIALI OSPITE ) {0}", nomecognome);
                                } else { //CREDENZIALI GIA presenti
                                    user = rs5.getString(1);
                                    try (Statement st6 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '"
                                                + md5psw + "' WHERE idsoggetto = "
                                                + idsoggetto + " AND data = '"
                                                + dataoggi + "' AND ud='" + ud + "' AND type = 'O'";
                                        st6.executeUpdate(upd);
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

                        String sql1 = "SELECT nome,idcomune,nome_provincia,regione FROM comuni WHERE idcomune IN ("
                                + rs0.getInt("a.comune_nascita") + ","
                                + rs0.getInt("a.comune_residenza") + ","
                                + rs0.getInt("a.comune_domicilio") + ")";

                        try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
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

                        String statonascita = rs0.getString("a.stato_nascita");
                        if (statonascita.equals("100")) {
                            statonascita = "ITALIA";
                        } else {
                            String sql2 = "SELECT nome FROM nazioni_rc WHERE codicefiscale = '" + statonascita + "'";
                            try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                                if (rs2.next()) {
                                    statonascita = rs2.getString(1).toUpperCase();
                                }
                            }
                        }

                        String cpi = rs0.getString("a.cpi");
                        String sql3 = "SELECT descrizione,provincia FROM cpi WHERE codice = '" + cpi + "'";
                        try (Statement st3 = db1.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                            if (rs3.next()) {
                                cpi = rs3.getString(1).toUpperCase();
                            }
                        }

                        String titolo_studio = rs0.getString("a.titolo_studio");
                        String sql4 = "SELECT descrizione FROM titoli_studio WHERE codice = '" + titolo_studio + "'";
                        try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
                            if (rs4.next()) {
                                titolo_studio = rs4.getString(1).toUpperCase();
                            }
                        }

                        String vulnerabilita = rs0.getString("a.tos_gruppovulnerabile");
                        String sql5 = "SELECT descrizione FROM gruppovulnerabile WHERE idgruppovulnerabile = '" + vulnerabilita + "'";
                        try (Statement st5 = db1.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                            if (rs5.next()) {
                                vulnerabilita = rs5.getString(1).toUpperCase();
                            }
                        }

                        String condizione_lavorativa = rs0.getString("a.idcondizione_mercato");
                        String sql6 = "SELECT descrizione FROM condizione_mercato WHERE idcondizione_mercato = '" + condizione_lavorativa + "'";
                        try (Statement st6 = db1.getConnection().createStatement(); ResultSet rs6 = st6.executeQuery(sql6)) {
                            if (rs6.next()) {
                                condizione_lavorativa = rs6.getString(1).toUpperCase();
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
                        String sql7 = "SELECT descrizione FROM canale WHERE idcanale = '" + canale_conoscenza + "'";
                        try (Statement st7 = db1.getConnection().createStatement(); ResultSet rs7 = st7.executeQuery(sql7)) {
                            if (rs7.next()) {
                                canale_conoscenza = rs7.getString(1).toUpperCase();
                            }
                        }
                        String motivazione = rs0.getString("a.tos_m0_motivazione") == null ? "" : rs0.getString("a.tos_m0_motivazione");
                        String sql8 = "SELECT descrizione FROM motivazione WHERE idmotivazione = '" + motivazione + "'";
                        try (Statement st8 = db1.getConnection().createStatement(); ResultSet rs8 = st8.executeQuery(sql8)) {
                            if (rs8.next()) {
                                motivazione = rs8.getString(1).toUpperCase();
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
                        String sql9 = "SELECT descrizione FROM aspettative WHERE idaspettative = '" + aspettative + "'";
                        try (Statement st9 = db1.getConnection().createStatement(); ResultSet rs9 = st9.executeQuery(sql9)) {
                            if (rs9.next()) {
                                aspettative = rs9.getString(1).toUpperCase();
                            }
                        }

                        String maturazioneidea = rs0.getString("a.tos_m0_maturazione") == null ? "" : rs0.getString("a.tos_m0_maturazione");
                        String sql10 = "SELECT descrizione FROM maturazioneidea WHERE idmaturazioneidea = '" + maturazioneidea + "'";
                        try (Statement st10 = db1.getConnection().createStatement(); ResultSet rs10 = st10.executeQuery(sql10)) {
                            if (rs10.next()) {
                                maturazioneidea = rs10.getString(1).toUpperCase();
                            }
                        }
                        String motivazioneno = rs0.getString("a.tos_m0_noperche") == null ? "" : rs0.getString("a.tos_m0_noperche");

                        if (motivazioneno.equals("7")) {
                            motivazioneno = rs0.getString("a.tos_m0_noperchealtro") == null ? "" : rs0.getString("a.tos_m0_noperchealtro");
                        } else {
                            String sql11 = "SELECT descrizione FROM motivazioneno WHERE idmotivazioneno = '" + motivazioneno + "'";
                            try (Statement st11 = db1.getConnection().createStatement(); ResultSet rs11 = st11.executeQuery(sql11)) {
                                if (rs11.next()) {
                                    motivazioneno = rs11.getString(1).toUpperCase();
                                }
                            }
                        }

                        int idsoggetto_attuatore = rs0.getString("a.idsoggetto_attuatore") == null ? 0 : rs0.getInt("a.idsoggetto_attuatore");
                        String se_scelto = "";
                        String sql12 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = " + idsoggetto_attuatore;
                        try (Statement st12 = db1.getConnection().createStatement(); ResultSet rs12 = st12.executeQuery(sql12)) {
                            if (rs12.next()) {
                                se_scelto = rs12.getString(1).toUpperCase();
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
                        String sql13 = "SELECT descrizione FROM stato_partecipazione WHERE codice = '" + statopartecipazione + "'";
                        try (Statement st13 = db1.getConnection().createStatement(); ResultSet rs13 = st13.executeQuery(sql13)) {
                            if (rs13.next()) {
                                statopartecipazione = rs13.getString(1).toUpperCase();
                            }
                        }

                        String cip = "";
                        String dataavvio = "";
                        String datachiusura = "";

                        int idpr = rs0.getString("a.idprogetti_formativi") == null ? 0 : rs0.getInt("a.idprogetti_formativi");

                        if (idpr > 0) {
                            String sql14 = "SELECT p.cip,p.start,p.end FROM progetti_formativi p WHERE p.idprogetti_formativi=" + idpr;

                            try (Statement st14 = db1.getConnection().createStatement(); ResultSet rs14 = st14.executeQuery(sql14)) {
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
                                }
                            }
                        }

//                        for (String pre1 : presenzeconv) {
//                            
//                        }
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
                        setCell(getCell(row, indicecolonna.addAndGet(1)), cellStyle, rs0.getString("a.importo") == null ? "0.0" : rs0.getString("a.importo").trim(), false, true);

                        maxrow.set(indicecolonna.get());
                    }
                }
                for (int i = 0; i < 56; i++) {
                    sh1.autoSizeColumn(i);
                }
                wb.write(outputStream);

            }
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=2";
            db1.getConnection().createStatement().executeUpdate(upd);
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

            String sql0 = "SELECT s.ragionesociale,s.piva,s.protocollo,d.nome,d.cognome,d.codicefiscale,d.stato,d.tipo_inserimento,d.datawebinair,d.motivo FROM docenti d, soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori";

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
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getDate("d.datawebinair") == null ? "" : sdfITA.format(rs0.getDate("d.datawebinair")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), stato);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), tipo_inserimento);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("d.motivo") == null ? "" : rs0.getString("d.motivo").toUpperCase());

                    }
                }

                for (int i = 0; i < 20; i++) {
                    sh1.autoSizeColumn(i);
                }

                wb.write(outputStream);
            }
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=1";
            db1.getConnection().createStatement().executeUpdate(upd);
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
                        String sql1_foglio1 = "SELECT COUNT(iddocenti) FROM docenti d WHERE stato='A' AND d.idsoggetti_attuatori=" + idsa;
                        try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1_foglio1)) {
                            if (rs1.next()) {
                                docenti = rs1.getInt(1);
                            }
                        }

                        String sql2_foglio1 = "SELECT p.idprogetti_formativi,p.stato FROM progetti_formativi p WHERE p.idsoggetti_attuatori=" + idsa;

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

                        try (ResultSet rs2 = db1.getConnection().createStatement().executeQuery(sql2_foglio1)) {

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
                                case 1 -> "SI";
                                case 2 -> "IN ATTESA";
                                default -> "NO";
                            };
                        }
                        
                        
                        XSSFRow row = getRow(sh2, indiceriga2.get());
                        indiceriga2.addAndGet(1);
                        
                        AtomicInteger indicecolonna = new AtomicInteger(0);
                        setCell(getCell(row, indicecolonna.get()), rs0.getString("sa.ragionesociale").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("sa.piva").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.cip") == null ? "":rs0.getString("pf.cip").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("st.descrizione").toUpperCase());
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rendicontato);
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.start") == null ? "" : sdfITA.format(rs0.getDate("pf.start")));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), rs0.getString("pf.end") == null ? "" : sdfITA.format(rs0.getDate("pf.end")));
                        
                        AtomicInteger ALLIEVIISCRITTI = new AtomicInteger(0);
                        AtomicInteger ALLIEVIVALIDATI = new AtomicInteger(0);
                        int idpr = rs0.getInt("pf.idprogetti_formativi");
                        String sql1_foglio2 = "SELECT a.idallievi,a.id_statopartecipazione FROM allievi a WHERE a.idprogetti_formativi=" + idpr;
                        try (ResultSet rs1 = db1.getConnection().createStatement().executeQuery(sql1_foglio2)) {
                            while (rs1.next()) {
                                String statoallievo = rs1.getString("a.id_statopartecipazione");
                                if (statoallievo.equals("15")) {
                                    ALLIEVIVALIDATI.addAndGet(1);
                                }
                                ALLIEVIISCRITTI.addAndGet(1);
                            }
                        }
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ALLIEVIISCRITTI.get()));
                        setCell(getCell(row, indicecolonna.addAndGet(1)), String.valueOf(ALLIEVIVALIDATI.get()));
                    }
                }

                wb.write(outputStream);
            }

            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);
            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=3";
            db1.getConnection().createStatement().executeUpdate(upd);
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

}
