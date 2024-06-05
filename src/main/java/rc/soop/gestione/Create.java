/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import com.google.common.base.Splitter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.timestampSQL;
import static rc.soop.gestione.Constant.MAX;
import static rc.soop.gestione.Constant.calcoladurata;
import static rc.soop.gestione.Constant.convertHours;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author rcosco
 */
public class Create {

    public static void solocomplessivi(boolean testing) {

        try {

            FaseA FA = new FaseA(testing);
            FaseB FB = new FaseB(testing);

            List<Integer> list_id_conclusi = new ArrayList<>();

            //COMPLESSIVO
            Db_Gest dbA0 = new Db_Gest(FA.getHost());
            String sqlA0 = "SELECT idprogetti_formativi FROM progetti_formativi WHERE END < CURDATE() AND stato = 'F'"
                    + " AND idprogetti_formativi NOT IN (SELECT idprogetto FROM documenti_progetti WHERE tipo=33)";
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

                    List<Lezione> pr_a = FA.generaregistrofasea_PR(idpr, c1.getHost(), false, false, false);
                    List<Lezione> pr_b = FB.generaregistrofasea_PR(idpr, c1.getHost(), true, false, false);
                    List<Lezione> fad_a = FA.calcolaegeneraregistrofasea(idpr, c1.getHost(), false, false, false);
                    List<Lezione> fad_b = FB.calcolaegeneraregistrofaseb(idpr, c1.getHost(), false, false, false);

                    List<Lezione> ca = new ArrayList<>();
                    if (pr_a != null && !pr_a.isEmpty()) {
                        ca.addAll(pr_a);
                    }
                    if (fad_a != null && !fad_a.isEmpty()) {
                        ca.addAll(fad_a);
                    }

                    List<Lezione> cb = new ArrayList<>();
                    if (pr_b != null && !pr_b.isEmpty()) {
                        cb.addAll(pr_b);
                    }
                    if (fad_b != null && !fad_b.isEmpty()) {
                        cb.addAll(fad_b);
                    }

                    sort(ca, (emp1, emp2) -> emp1.getGiorno().compareTo(emp2.getGiorno()));
                    sort(cb, (emp1, emp2) -> emp1.getGiorno().compareTo(emp2.getGiorno()));

                    c1.registro_complessivo(idpr, c1.getHost(), ca, cb, true);

                    log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
            });

        } catch (Exception ex0) {
            log.severe(estraiEccezione(ex0));
        }

    }

    public static void crearegistri(boolean testing) {
        List<Integer> list_id = new ArrayList<>();

//        list_id.add(13);
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
                    List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, true, false);

                    FA.registro_aula_FaseA(idpr, FA.getHost(), true, calendar1);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
                //  FASE B
                try {
                    log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
                    List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, true, false);
                    FB.registro_aula_FaseB(idpr, FA.getHost(), true, calendar2);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }

            });

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        solocomplessivi(testing);
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

                    long millischeck = nuova_rendicontazione_ore(millis_rendicontabili.get());
                    long millischeck1 = nuova_rendicontazione_ore(cnsmr.getMillistotaleore());

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

                    long millischeck = nuova_rendicontazione_ore(millis_rendicontabili_DOCENTE.get());
                    long millischeck1 = nuova_rendicontazione_ore(doc1.getMillistotaleore());

                    String sqloredocente = "SELECT l1.ore FROM lezioni_modelli l, lezione_calendario l1, docenti d1"
                            + " WHERE l1.id_lezionecalendario=l.id_lezionecalendario AND l.id_modelli_progetto = ? "
                            + " AND l.giorno = ? AND l.id_docente=d1.iddocenti AND d1.email = ?";

                    if (cal.getCodiceud().startsWith("B") && cal.getGruppo() > 0) {
                        sqloredocente = "SELECT l1.ore FROM lezioni_modelli l, lezione_calendario l1, docenti d1"
                                + " WHERE l1.id_lezionecalendario=l.id_lezionecalendario AND l.id_modelli_progetto =? "
                                + " AND l.giorno = ? AND l.id_docente=d1.iddocenti AND d1.email = ? AND l.gruppo_faseB = ?";
                    }

                    long oredocente = 0;

                    Db_Gest db0 = new Db_Gest(host);

                    try (PreparedStatement ps1 = db0.getConnection().prepareStatement(sqloredocente)) {
                        ps1.setInt(1, cal.getMpid_modello());
                        ps1.setString(2, cal.getGiorno());
                        ps1.setString(3, doc1.getEmail());
                        if (cal.getCodiceud().startsWith("B") && cal.getGruppo() > 0) {
                            ps1.setInt(4, cal.getGruppo());
                        }
                        try (ResultSet rs1 = ps1.executeQuery()) {
                            while (rs1.next()) {
                                oredocente += convertHours(rs1.getString(1));
                            }
                        }
                    }
                    db0.closeDB();

                    if (oredocente > MAX) {
                        oredocente = MAX;
                    }

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

//    public static long nuova_rendicontazione_ore(long millis, int idpr, String host) {
//        try {
//            Db_Gest db0 = new Db_Gest(host);
//            List<Integer> listpr = db0.elencoidnuovarendicontazione();
//            db0.closeDB();
//            if (listpr.contains(idpr)) {
//                long real = millis / 1800000;
//                return real * 1800000;
//            }
//        } catch (Exception e1) {
//            log.severe(estraiEccezione(e1));
//        }
//        return millis;
//    }
    public static long nuova_rendicontazione_ore(long millis) { //2024
        try {
            long ora = 3600000L;
            long min_45 = 2700000L;
            long resto = millis % ora;
            int hours = new BigDecimal(TimeUnit.MILLISECONDS.toHours(millis)).intValueExact();
            if (resto >= min_45) {
                hours++;
            }
            return hours * ora;
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        return millis;
    }

}
