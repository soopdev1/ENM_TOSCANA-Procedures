/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.exe;

import java.util.logging.Level;
import java.util.logging.Logger;
import rc.soop.accreditamento.Engine;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.gestione.Create.crearegistri;
import rc.soop.gestione.Toscana_gestione;

/**
 *
 * @author Administrator
 */
public class MainSelector {

    public static final Logger log = Utils.createLog("Toscana_Accr_PR");

    public static void main(String[] args) {

        try {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        } catch (Exception e) {
        }

        //ARGS[0] TEST
        //////////////////////////////
        //ARGS[1] OPERAZIONE 1 - 3
        //1 - GESTIONE - COMUNICAZIONI
        //2 - GESTIONE - FAD
        //3 - GESTIONE - ESTRAZIONI
        //4 - GESTIONE - REPORT FAD
        //5 - ACCREDITAMENTO
        //6 - REPAIR
        //7 - RENDICONTAZIONE
        //////////////////////////////
        //ARGS[2] NEET - DD
        //////////////////////////////
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
                try {
                    tg.ore_convalidateAllievi();
                } catch (Exception e) {
                }
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
            default -> {
                break;
            }
        }
//                case 0:
//                    log.severe("GESTIONE NEET - NESSUN METODO SELEZIONATO");
//                    break;
//                case 1:
//                    log.warning("GESTIONE NEET - COMUNICAZIONI");
//                    try {
//                        log.warning("MAIL REMIND 1 GIORNO... INIZIO");
//                        ne.mail_remind(1);
//                        log.warning("MAIL REMIND 1 GIORNO... FINE");
//                    } catch (Exception e) {
//                    }
//                    try {
//                        log.warning("MAIL REMIND QUESTIONARI INGRESSO... INIZIO");
//                        ne.mail_questionario_INGRESSO();
//                        log.warning("MAIL REMIND QUESTIONARI INGRESSO... FINE");
//                    } catch (Exception e) {
//                    }
//                    try {
//                        log.warning("MAIL REMIND QUESTIONARI USCITA... INIZIO");
//                        ne.mail_questionario_USCITA();
//                        log.warning("MAIL REMIND QUESTIONARI USCITA... FINE");
//                    } catch (Exception e) {
//                    }
//                    break;
//                case 6:
//                    log.warning("REPAIR NEET");
//                    Repair neetr = new Repair(testing, true);
//                    
//                    try {
//                        neetr.imposta_progetti_finettivita();
//                    } catch (Exception e) {
//                    }
//                    
//                    try {
//                        neetr.impostaritiratounder36oreA();
//                    } catch (Exception e) {
//                        
//                    }
//                    
//                    try {
//                        neetr.copiadocumentidocenti();
//                    } catch (Exception e) {
//                        
//                    }
//                    try {
//                        neetr.crea_pdf_unico_ANPAL(true);
//                    } catch (Exception e) {
//                        
//                    }
//                    break;
//                case 7: 
//                    try {
//                    log.info("START RENDICONTAZIONE NEET");
//                    new Rendicontazione(false, true).generaRendicontazione(true);
//                    log.info("END RENDICONTAZIONE NEET");
//                } catch (Exception e) {
//                }
//                break;
    }

}
