//
//import com.google.common.util.concurrent.AtomicDouble;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import static rc.soop.exe.Utils.estraiEccezione;
//import rc.soop.gestione.Db_Gest;
//import rc.soop.gestione.FaseA;
//import rc.soop.gestione.FaseB;
//import rc.soop.gestione.Lezione;
//import static rc.soop.gestione.Toscana_gestione.log;
//
///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
///**
// *
// * @author Administrator
// */
//public class NewClass {
//
//    public static void main(String[] args) {
//        List<Integer> list_id = new ArrayList<>();
//        list_id.add(17);
//        FaseA FA = new FaseA(false);
//        FaseB FB = new FaseB(false);
//        list_id.forEach(idpr -> {
//
//                //  FASE A
//                try {
//                    log.log(Level.INFO, "REPORT FASE A - IDPR {0}", idpr);
//                    List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, true, false);
//
//                    FA.registro_aula_FaseA(idpr, FA.getHost(), false, calendar1);
//                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//                } catch (Exception e1) {
//                    log.severe(estraiEccezione(e1));
//                }
////                //  FASE B
//                try {
//                    log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
//                    List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, true, false);
//
//                    FB.registro_aula_FaseB(idpr, FA.getHost(), false, calendar2);
//                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//                } catch (Exception e1) {
//                    log.severe(estraiEccezione(e1));
//                }
//
//            });
//        
//        
//    }
//
////    public static void main(String[] args) {
////        boolean testing = false;
////
////        FaseA FA = new FaseA(testing);
////        new NewClass().ore_convalidateAllievi(FA);
////    }
//}
