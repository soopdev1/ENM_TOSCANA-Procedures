/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.exe;

import java.util.logging.Level;
import java.util.logging.Logger;
import rc.soop.accreditamento.Engine;
import rc.soop.arti.Arti;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.gestione.Create.crearegistri;
import static rc.soop.gestione.Create.solocomplessivi;
import rc.soop.gestione.Rendicontazione;
import rc.soop.gestione.Toscana_gestione;

/**
 *
 * @author Administrator
 */
public class MainSelector {

    public static final Logger log = Utils.createLog("Toscana_PR");

    public static void main(String[] args) {

        try {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        } catch (Exception e) {
        }
        boolean testing;
        try {
            testing = args[0].trim().equals("test");
        } catch (Exception e) {
            testing = false;
        }

        int select_action;
        try {
            select_action = Integer.parseInt(args[1].trim());
        } catch (Exception e) {
            select_action = 4;
        }

        Toscana_gestione tg = new Toscana_gestione(testing);
        switch (select_action) {
            case 2 -> {
                log.warning("GESTIONE TOSCANA - FAD");
                try {
                    tg.fad_gestione();
                } catch (Exception e) {
                }
                break;
            }
            case 3 -> {
                log.warning("GESTIONE TOSCANA - ESTRAZIONI");
                log.warning("GENERAZIONE FILE REPORT... INIZIO");
                try {
                    tg.report_docenti();
                } catch (Exception e) {
                }
                try {
                    tg.report_docenti_GG1();
                } catch (Exception e) {
                }
                try {
                    tg.report_allievi();
                } catch (Exception e) {
                }
                try {
                    tg.report_pf();
                } catch (Exception e) {
                }
                log.warning("GENERAZIONE FILE REPORT... FINE");
                break;
            }
            case 4 -> {
                log.warning("GESTIONE TOSCANA - REPORT FAD");
                crearegistri(testing);
                log.warning("GESTIONE TOSCANA - UPDATE ORE CONVALIDATE");
                tg.ore_convalidateAllievi();
                tg.ore_ud();
                break;
            }
            case 5 -> {
                log.log(Level.WARNING, "ACCREDITAMENTO TOSCANA (testing {0})", testing);
                Engine accreditamento = new Engine(testing);
                try {
                    log.info("START ELENCO DOMANDE");
                    accreditamento.elenco_domande_fase1();
                    log.info("FINE ELENCO DOMANDE");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    log.info("START UPDATE DOMANDE");
                    accreditamento.update_domande_fase1();
                    log.info("FINE UPDATE DOMANDE");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    log.info("START AGGIORNA DATA CONVENZIONE");
                    accreditamento.aggiorna_dataconvenzione_fase1();
                    log.info("FINE AGGIORNA DATA CONVENZIONE");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    log.info("START AGGIORNA REPORTISTICA");
                    accreditamento.aggiorna_reportistica();
                    log.info("FINE AGGIORNA REPORTISTICA");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    log.info("START CREA REPORT");
                    accreditamento.crea_report();
                    log.info("FINE CREA REPORT");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                break;
            }
            case 6 -> { //IDOLARTI AGGIORNA STATO ALLIEVI
                try {
                    log.info("START IDOLARTI AGGIORNA STATO ALLIEVI");
                    Arti.cambiostato_allievo(testing);
                    log.info("FINE IDOLARTI AGGIORNA STATO ALLIEVI");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                break;
            }
            case 7 -> { //MAINTENANCE
                try {
                    log.info("START MODELLO 0");
                    tg.update_modello0();
                    log.info("FINE MODELLO 0");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    log.info("START IMPOSTA FINE ATTIVITA'");
                    tg.imposta_fineattivita();
                    log.info("FINE IMPOSTA FINE ATTIVITA'");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
            }
            case 8 -> { //ATTESTATI
                log.info("START ATTESTATI");
                try {
                    tg.attestati_ok();
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    tg.attestati_competenzedigitali();
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                try {
                    tg.attestati_UD(0);
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
            }
            case 9 -> { //SOLO REGISTRI COMPLESSIVI
                solocomplessivi(testing);
            }
            case 10 -> { //RENDICOTNAZIONE
                try {
                    log.info("START RENDICONTAZIONE");
                    Rendicontazione.generaRendicontazione();
                    log.info("FINE RENDICONTAZIONE");
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
            }

            default ->
                log.severe("GESTIONE TOSCANA - NESSUN METODO SELEZIONATO");

        }
    }

}
