
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.sort;
import java.util.List;
import java.util.logging.Level;
import static rc.soop.exe.Utils.estraiEccezione;
import rc.soop.gestione.Complessivo;
import rc.soop.gestione.FaseA;
import rc.soop.gestione.FaseB;
import rc.soop.gestione.Lezione;
import rc.soop.gestione.Toscana_gestione;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author Administrator
 */
public class Regcomp {

    public static void main(String[] args) {
        boolean testing = false;
        FaseA FA = new FaseA(testing);
        FaseB FB = new FaseB(testing);
        
        Toscana_gestione tg = new Toscana_gestione(testing);
        tg.ore_convalidateAllievi();
                tg.ore_ud();
        
//        List<Integer> list_id_conclusi = new ArrayList<>();
//        list_id_conclusi.add(25);
//        
//        Complessivo c1 = new Complessivo(FA.getHost());
//        list_id_conclusi.forEach(idpr -> {
//            try {
//                log.log(Level.INFO, "REPORT COMPLESSIVO - IDPR {0}", idpr);
//                
//                List<Lezione> pr_a = FA.generaregistrofasea_PR(idpr, c1.getHost(), false, false, false);
//                List<Lezione> pr_b = FB.generaregistrofasea_PR(idpr, c1.getHost(), false, false, false);
//
//                List<Lezione> fad_a = FA.calcolaegeneraregistrofasea(idpr, c1.getHost(), false, true, false);
//                List<Lezione> fad_b = FB.calcolaegeneraregistrofaseb(idpr, c1.getHost(), false, true, false);
//                
//                List<Lezione> ca = new ArrayList<>();
//                ca.addAll(pr_a);
//                ca.addAll(fad_a);                
//                List<Lezione> cb = new ArrayList<>();
//                cb.addAll(pr_b);
//                cb.addAll(fad_b);
////                
//                sort(ca, (emp1, emp2) -> emp1.getGiorno().compareTo(emp2.getGiorno()));
//                sort(cb, (emp1, emp2) -> emp1.getGiorno().compareTo(emp2.getGiorno()));
////                                
//                c1.registro_complessivo(idpr, c1.getHost(), ca, cb, false);
//                
//                log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
//            } catch (Exception e1) {
//                log.severe(estraiEccezione(e1));
//            }
//        });
    }
}
