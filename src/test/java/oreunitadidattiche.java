
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import static rc.soop.exe.Utils.sdfSQL;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.FaseA;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Administrator
 */
public class oreunitadidattiche {

    public static void main(String[] args) {
        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        List<Long> idallievi = new ArrayList<>();
        String sql = "SELECT a.idallievi FROM allievi a WHERE a.id_statopartecipazione='15'";
        try (Statement st = db0.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                idallievi.add(rs.getLong(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String sql0 = "SELECT u.descrizione,u.fase FROM unita_didattiche u GROUP BY u.descrizione,u.fase ORDER BY ordine;";

        try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
            while (rs0.next()) {
                String UD_desc = rs0.getString(1);
                String UD_fase = rs0.getString(2);
                String sql1 = "SELECT u.codice,u.ore FROM unita_didattiche u WHERE u.descrizione = '" + UD_desc + "' AND u.fase = '" + UD_fase + "'";
                Map<String, String> MOD_ore = new HashMap<>();
                List<String> MOD_codici = new ArrayList<>();
                int ore = 0;
                try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                    while (rs1.next()) {
                        MOD_ore.put(rs1.getString(1), rs1.getString(2));
                        MOD_codici.add(rs1.getString(1));
                        ore += rs1.getInt(2);
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

                    String datainizio = "";
                    String datafine = "";
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
                                String sql2_F1 = "SELECT r.totaleorerendicontabili,r.data FROM registro_completo r WHERE r.idutente=" + idallievo
                                        + " AND r.ruolo='ALLIEVO' AND r.nud='" + m1 + "'";
//                            System.out.println(sql2_F1);
                                try (Statement st2F1 = db0.getConnection().createStatement(); ResultSet rs2F1 = st2F1.executeQuery(sql2_F1)) {
                                    if (rs2F1.next()) {
                                        //FAD 2 MODULI SINGOLI
                                        double res = rs2F1.getLong(1) / 3600000.00;
                                        datainizio = sdfSQL.format(rs2F1.getDate(2));
                                        datafine = sdfSQL.format(rs2F1.getDate(2));
                                        db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res)),
                                    datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
//                                        System.out.println(idallievo+" : "+idallievo+" : "+m1 + " - " + MOD_ore.get(m1) + " : " + res + " - " + datainizio + " - " + datafine + " -- " + m1.split("-")[0]);
                                    } else {
                                        String sql2_P = "SELECT r.durataconvalidata,p.datalezione FROM presenzelezioni p, presenzelezioniallievi r, lezione_calendario l "
                                                + "WHERE p.idpresenzelezioni=r.idpresenzelezioni AND l.id_lezionecalendario=p.idlezioneriferimento "
                                                + "AND r.idallievi=" + idallievo + " AND r.convalidata=1 AND l.codice_ud='" + m1 + "'";
                                        try (Statement st2P = db0.getConnection().createStatement(); ResultSet rs2P = st2P.executeQuery(sql2_P)) {
                                            if (rs2P.next()) {
                                                //PRESENZA MODULI SINGOLI
                                                double res = rs2P.getLong(1) / 3600000.00;
                                                datainizio = sdfSQL.format(rs2P.getDate(2));
                                                datafine = sdfSQL.format(rs2P.getDate(2));
                                                db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res)),
                                    datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
//                                                System.out.println(idallievo+" : "+m1 + " - "
//                                                        + MOD_ore.get(m1) + " : " + res + " - " + datainizio + " - " + datafine + " -- " + m1.split("-")[0]);
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
            ex.printStackTrace();
        }

        db0.closeDB();

    }
}
