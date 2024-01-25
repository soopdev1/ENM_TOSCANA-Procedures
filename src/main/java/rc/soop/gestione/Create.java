/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.google.common.base.Splitter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.timestampSQL;
import static rc.soop.gestione.Constant.calcoladurata;
import static rc.soop.gestione.Constant.convertHours;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author rcosco
 */
public class Create {

    public static void crearegistri(boolean testing) {
        boolean print = false;
        boolean save = true;

        log.log(Level.INFO, "PRINT: {0}", print);
        log.log(Level.INFO, "SAVE: {0}", save);

        List<Integer> list_id = new ArrayList<>();

        try {
            FaseA FA = new FaseA(testing);
            Db_Gest db0 = new Db_Gest(FA.getHost());

            String sql0 = "SELECT DISTINCT(mp.id_progettoformativo) "
                    + "FROM lezioni_modelli lm, modelli_progetti mp "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto "
                    + "AND lm.tipolez='F' AND lm.giorno = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";

            try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    list_id.add(rs0.getInt(1));
                }
            }
            db0.closeDB();

            FaseB FB = new FaseB(testing);

            list_id.forEach(idpr -> {

                //  FASE A
                try {
                    log.log(Level.INFO, "REPORT FASE A - IDPR {0}", idpr);
                    List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), print, save, false);

                    FA.registro_aula_FaseA(idpr, FA.getHost(), save, calendar1);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
                //  FASE B
                try {
                    log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
                    List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), print, save, false);

                    FB.registro_aula_FaseB(idpr, FA.getHost(), save, calendar2);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }

            });

            List<Integer> list_id_conclusi = new ArrayList<>();

            //COMPLESSIVO
            Db_Gest dbA0 = new Db_Gest(FA.getHost());
            String sqlA0 = "SELECT idprogetti_formativi FROM progetti_formativi WHERE END < CURDATE() AND stato NOT LIKE '%E' "
                    + "AND idprogetti_formativi NOT IN (SELECT idprogetto FROM documenti_progetti WHERE tipo=33)";
            try (Statement st0 = dbA0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sqlA0)) {
                while (rs0.next()) {
                    list_id_conclusi.add(rs0.getInt(1));
                }
            }
            dbA0.closeDB();

            Complessivo c1 = new Complessivo(FA.getHost());
            list_id_conclusi.forEach(idpr -> {
                try {
                    log.log(Level.INFO, "REPORT COMPLESSIVO - IDPR {0}", idpr);
                    List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, c1.getHost(), false, false, false);
                    List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, c1.getHost(), false, false, false);
                    c1.registro_complessivo(idpr, c1.getHost(), ca, cb, save);
                    log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static void gestisciorerendicontabili(LinkedList<Presenti> report, int idpr, String host, Lezione cal) {

        try {
            DateTimeFormatter fmt = forPattern(timestampSQL);
            List<Presenti> allievi = report.stream().filter(pr1 -> !pr1.getRuolo().equalsIgnoreCase("DOCENTE")).collect(Collectors.toList());
//            List<Presenti> docenti = report.stream().filter(pr1 -> pr1.getRuolo().equalsIgnoreCase("DOCENTE")).collect(Collectors.toList());

//            if (docenti.size() == 1) {
            List<Presenti> docenti = report.stream().filter(pr1 -> pr1.getRuolo().equalsIgnoreCase("DOCENTE"))
                    .sorted(Comparator.comparing(Presenti::getOradilogin)).collect(Collectors.toList());

            StringBuilder doc1getOradilogin = new StringBuilder();
            StringBuilder doc1getOradilogout = new StringBuilder();
            for (Presenti doc1 : docenti) {
                doc1getOradilogin.append(doc1.getOradilogin()).append("\n");
                doc1getOradilogout.append(doc1.getOradilogout()).append("\n");
            }

            Presenti docente = new Presenti("", "", "", "", doc1getOradilogin.toString(), doc1getOradilogout.toString(), "", "");

            if (docente != null && !allievi.isEmpty()) {
                List<Interval> accessi_docente = new ArrayList<>();

                List<Interval> accessi_complessivi = new ArrayList<>();
                List<String> login_docente = Splitter.on("\n").splitToList(docente.getOradilogin().trim());
                List<String> logout_docente = Splitter.on("\n").splitToList(docente.getOradilogout().trim());
                for (int x = 0; x < login_docente.size(); x++) {
                    DateTime start1 = fmt.parseDateTime("2021-01-01 " + login_docente.get(x));
                    DateTime end1 = fmt.parseDateTime("2021-01-01 " + logout_docente.get(x));
                    if (end1.isAfter(start1)) {
                        accessi_docente.add(new Interval(start1, end1));
                    }
                }
                allievi.forEach(cnsmr -> {
                    AtomicLong millis_rendicontabili = new AtomicLong(0L);
                    List<Interval> accessi = new ArrayList<>();
                    List<String> login = Splitter.on("\n").splitToList(cnsmr.getOradilogin());
                    List<String> logout = Splitter.on("\n").splitToList(cnsmr.getOradilogout());

                    for (int x = 0; x < login.size(); x++) {
                        DateTime start2 = fmt.parseDateTime("2021-01-01 " + login.get(x));
                        DateTime end2 = fmt.parseDateTime("2021-01-01 " + logout.get(x));
                        if (end2.isAfter(start2)) {
                            accessi.add(new Interval(start2, end2));
                            accessi_complessivi.add(new Interval(start2, end2));
                        }
                    }
                    accessi.forEach(intervallo2 -> {
                        accessi_docente.forEach(intervallo1 -> {
                            if (intervallo2.overlaps(intervallo1)) {
                                millis_rendicontabili.addAndGet(intervallo2.overlap(intervallo1).toDurationMillis());
                            }
                        });

                    });

                    long millischeck = nuova_rendicontazione_ore(millis_rendicontabili.get(), idpr, host);
                    long millischeck1 = nuova_rendicontazione_ore(cnsmr.getMillistotaleore(), idpr, host);

                    long ore = convertHours(cal.getOre());

                    if (millischeck >= ore) {
                        cnsmr.setTotaleorerendicontabili(calcoladurata(ore));
                        cnsmr.setMillistotaleorerendicontabili(ore);
                    } else if (millischeck >= millischeck1) {
                        cnsmr.setTotaleorerendicontabili(calcoladurata(millischeck1));
                        cnsmr.setMillistotaleorerendicontabili(millischeck1);
                    } else {
                        cnsmr.setTotaleorerendicontabili(calcoladurata(millischeck));
                        cnsmr.setMillistotaleorerendicontabili(millischeck);
                    }

                });

                for (Presenti doc1 : docenti) {
                    List<Interval> accessi_docente_original = new ArrayList<>();
                    List<String> login_docente1 = Splitter.on("\n").splitToList(doc1.getOradilogin().trim());
                    List<String> logout_docente1 = Splitter.on("\n").splitToList(doc1.getOradilogout().trim());
                    for (int x = 0; x < login_docente1.size(); x++) {
                        DateTime start1 = fmt.parseDateTime("2021-01-01 " + login_docente1.get(x));
                        DateTime end1 = fmt.parseDateTime("2021-01-01 " + logout_docente1.get(x));
                        if (end1.isAfter(start1)) {
                            accessi_docente_original.add(new Interval(start1, end1));
                        }
                    }
                    AtomicLong millis_rendicontabili_DOCENTE = new AtomicLong(0L);
                    accessi_docente_original.forEach(intervallo1 -> {
                        DateTime start = intervallo1.getStart();
                        while (start.isBefore(intervallo1.getEnd())) {
                            for (int i = 0; i < accessi_complessivi.size(); i++) {
                                Interval ac1 = accessi_complessivi.get(i);
                                if (ac1.getStart().isBefore(start) || ac1.getStart().isEqual(start)) {
                                    if (ac1.getEnd().isAfter(start) || ac1.getEnd().isEqual(start)) {
                                        millis_rendicontabili_DOCENTE.addAndGet(1000);
                                        break;
                                    }
                                }
                            }
                            start = start.plusSeconds(1);
                        }
                    });

                    long millischeck = nuova_rendicontazione_ore(millis_rendicontabili_DOCENTE.get(), idpr, host);
                    long millischeck1 = nuova_rendicontazione_ore(doc1.getMillistotaleore(), idpr, host);

                    String sqloredocente = "SELECT l1.ore FROM lezioni_modelli l, lezione_calendario l1, docenti d1 "
                            + "WHERE l1.id_lezionecalendario=l.id_lezionecalendario AND l.id_modelli_progetto=" + cal.getMpid_modello() + " AND l.giorno='" + cal.getGiorno() + "' "
                            + "AND l.id_docente=d1.iddocenti AND d1.email='" + doc1.getEmail()+ "';";
                    long oredocente = 0;
                    
                    System.out.println(sqloredocente);

                    Db_Gest db0 = new Db_Gest(host);

                    try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sqloredocente)) {
                        while (rs1.next()) {
                            oredocente += convertHours(rs1.getString(1));
                        }
                    }
                    db0.closeDB();

                    if (millischeck >= oredocente) {
                        doc1.setTotaleorerendicontabili(calcoladurata(oredocente));
                        doc1.setMillistotaleorerendicontabili(oredocente);
                    } else if (millischeck >= millischeck1) {
                        doc1.setTotaleorerendicontabili(calcoladurata(millischeck1));
                        doc1.setMillistotaleorerendicontabili(millischeck1);
                    } else {
                        doc1.setTotaleorerendicontabili(calcoladurata(millischeck));
                        doc1.setMillistotaleorerendicontabili(millischeck);
                    }
                    System.out.println("rc.soop.gestione.Create.gestisciorerendicontabili( ) "+doc1.getMillistotaleorerendicontabili());
                }

            }
//            } else {
//                
//                
//                
//                
//            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static long nuova_rendicontazione_ore(long millis, int idpr, String host) {
        try {
            Db_Gest db0 = new Db_Gest(host);
            List<Integer> listpr = db0.elencoidnuovarendicontazione();
            db0.closeDB();
            if (listpr.contains(idpr)) {
                long real = millis / 1800000;
                return real * 1800000;
            }
        } catch (Exception e1) {
            log.severe(estraiEccezione(e1));
        }
        return millis;
    }

    public static void manage(Db_Gest db0, int idpr) {
        try {
            switch (idpr) {
                case 82:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'-- Giorgia','-- GIORGIA GUIDALDI') "
                            + "WHERE f.room LIKE 'FADMCN_" + idpr + "%' AND action LIKE'%-- Giorgia'");
                    break;
                case 123:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'-- Manuela','-- MANUELA CAPUTO') "
                            + "WHERE f.room LIKE 'FADMCN_" + idpr + "%' AND action LIKE'%-- Manuela'");
                    break;
                case 338:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'IANNO&#39;','IANNO\\'') "
                            + "WHERE f.room LIKE 'FADMCNDD_" + idpr + "%' AND action LIKE '%IANNO&#39;'");
                    break;
                case 678:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'CARA&#39;','CARA\\'') "
                            + "WHERE f.room LIKE 'FADMCNDD_" + idpr + "%' AND action LIKE '%CARA&#39;'");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

}
