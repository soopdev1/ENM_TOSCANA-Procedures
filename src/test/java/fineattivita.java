
import java.sql.ResultSet;
import java.sql.Statement;
import static rc.soop.exe.Utils.estraiEccezione;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.Toscana_gestione;
import static rc.soop.gestione.Toscana_gestione.log;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Administrator
 */
public class fineattivita {

    public static void main(String[] args) {
        Toscana_gestione tg = new Toscana_gestione(false);

        Db_Gest db = new Db_Gest(tg.host);

        String sql0 = "SELECT p.idprogetti_formativi FROM progetti_formativi p WHERE p.stato = 'ATB' AND p.`end` < CURDATE()";
        try (Statement st0 = db.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
            while (rs0.next()) {
                String idpr = rs0.getString(1);
                String sql1 = "SELECT a.idallievi,a.gruppo_faseB,a.idprogetti_formativi FROM allievi a "
                        + "WHERE a.id_statopartecipazione = '15' "
                        + "AND a.idprogetti_formativi = " + idpr;
                boolean fineattivita = true;
                try (Statement st1 = db.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                    while (rs1.next()) {
                        String idallievi = rs1.getString(1);
                        String gruppo_fb = rs1.getString(2);
                        String sql2 = "SELECT la.convalidata,p.datalezione FROM presenzelezioniallievi la, presenzelezioni p WHERE la.idallievi=" + idallievi + " AND p.idpresenzelezioni=la.idpresenzelezioni "
                                + "AND p.idlezioneriferimento IN (SELECT m.id_lezionimodelli FROM lezioni_modelli m WHERE m.id_modelli_progetto IN "
                                + "(SELECT mp.id_modello FROM modelli_progetti mp WHERE mp.id_progettoformativo = " + idpr + ") AND m.gruppo_faseB IN (0," + gruppo_fb + "))";

//                    System.out.println(sql2);
                        try (Statement st2 = db.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                            while (rs2.next()) {
                                boolean conv = rs2.getBoolean(1);
                                if (!conv) {
//                                    System.out.println(idallievi + " PR " + idpr + " NON CONVALIDATA () " + rs2.getString(2));
                                    fineattivita = false;
                                    break;
                                }
                            }
                        } catch (Exception ex2) {
                            log.severe(estraiEccezione(ex2));
                        }
                    }
                } catch (Exception ex2) {
                    log.severe(estraiEccezione(ex2));
                }
                
                
                System.out.println(idpr +" fineattivita ---> "+fineattivita);
            }
        } catch (Exception ex1) {
            log.severe(estraiEccezione(ex1));
        }
        db.closeDB();
    }
}
