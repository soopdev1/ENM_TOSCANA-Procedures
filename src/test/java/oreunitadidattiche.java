
import com.google.common.base.Splitter;
import java.sql.PreparedStatement;
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

        //SALVA UD
//        String sql0 = "SELECT u.descrizione,u.fase FROM unita_didattiche u GROUP BY u.descrizione,u.fase ORDER BY ordine;";
//        try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
//            while (rs0.next()) {
//                String UD_desc = rs0.getString(1);
//                String UD_fase = rs0.getString(2);
//                String sql1 = "SELECT u.codice,u.ore FROM unita_didattiche u WHERE u.descrizione = '" + UD_desc + "' AND u.fase = '" + UD_fase + "'";
//                Map<String, String> MOD_ore = new HashMap<>();
//                List<String> MOD_codici = new ArrayList<>();
//                int ore = 0;
//                try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//                    while (rs1.next()) {
//                        MOD_ore.put(rs1.getString(1), rs1.getString(2));
//                        MOD_codici.add(rs1.getString(1));
//                        ore += rs1.getInt(2);
//                    }
//                }
//                if (MOD_codici.size() == 4 && MOD_codici.get(0).startsWith("A")) {
//                    String ud11_1 = "A-11A_A-11B";
//                    String ud11_2 = "A-11B_A-11C";
//                    String ud11_3 = "A-11C_A-11D";
//                    MOD_codici.add(ud11_1);
//                    MOD_codici.add(ud11_2);
//                    MOD_codici.add(ud11_3);
//                    MOD_ore.put(ud11_1, "5");
//                    MOD_ore.put(ud11_2, "5");
//                    MOD_ore.put(ud11_3, "5");
//                } else if (MOD_codici.size() == 4 && MOD_codici.get(0).startsWith("B")) {
//                    String ud11_1 = "B-1A_B-1B";
//                    String ud11_2 = "B-1B_B-1C";
//                    String ud11_3 = "B-1C_B-1D";
//                    MOD_codici.add(ud11_1);
//                    MOD_codici.add(ud11_2);
//                    MOD_codici.add(ud11_3);
//                    MOD_ore.put(ud11_1, "5");
//                    MOD_ore.put(ud11_2, "5");
//                    MOD_ore.put(ud11_3, "5");
//                }
//                String nomemodulo = "";
//                for (String m1 : MOD_codici) {
//                    nomemodulo += m1 + "_";
//                }
//                nomemodulo = StringUtils.chop(nomemodulo);
//
//                for (Long idallievo : idallievi) {
//
//                    String sql2_F0 = "SELECT r.totaleorerendicontabili,r.data FROM registro_completo r WHERE r.idutente=" + idallievo
//                            + " AND r.ruolo='ALLIEVO' AND r.nud='" + nomemodulo + "'";
//
//                    String datainizio = "";
//                    String datafine = "";
//                    try (Statement st2F0 = db0.getConnection().createStatement(); ResultSet rs2F0 = st2F0.executeQuery(sql2_F0)) {
//                        if (rs2F0.next()) {
//                            //FAD 2 MODULI IN UNO
//                            double res = rs2F0.getLong(1) / 3600000.00;
//                            datainizio = sdfSQL.format(rs2F0.getDate(2));
//                            datafine = sdfSQL.format(rs2F0.getDate(2));
//
//                            db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(ore)), res)),
//                                    datafine, datainizio, nomemodulo.split("-")[0], String.valueOf(res), String.valueOf(ore), nomemodulo, String.valueOf(idallievo));
//                            //System.out.println(idallievo+" : "+nomemodulo + " - " + ore + " : " + res + " - " + datainizio + " - " + datafine + " -- " + nomemodulo.split("-")[0]);
//                        } else {
//
//                            for (String m1 : MOD_codici) {
//                                String sql2_F1 = "SELECT r.totaleorerendicontabili,r.data FROM registro_completo r WHERE r.idutente=" + idallievo
//                                        + " AND r.ruolo='ALLIEVO' AND r.nud='" + m1 + "'";
////                            System.out.println(sql2_F1);
//                                try (Statement st2F1 = db0.getConnection().createStatement(); ResultSet rs2F1 = st2F1.executeQuery(sql2_F1)) {
//                                    if (rs2F1.next()) {
//                                        //FAD 2 MODULI SINGOLI
//                                        double res = rs2F1.getLong(1) / 3600000.00;
//                                        datainizio = sdfSQL.format(rs2F1.getDate(2));
//                                        datafine = sdfSQL.format(rs2F1.getDate(2));
//                                        db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res)),
//                                                datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
////                                        System.out.println(idallievo+" : "+idallievo+" : "+m1 + " - " + MOD_ore.get(m1) + " : " + res + " - " + datainizio + " - " + datafine + " -- " + m1.split("-")[0]);
//                                    } else {
//                                        String sql2_P = "SELECT r.durataconvalidata,p.datalezione FROM presenzelezioni p, presenzelezioniallievi r, lezione_calendario l "
//                                                + "WHERE p.idpresenzelezioni=r.idpresenzelezioni AND l.id_lezionecalendario=p.idlezioneriferimento "
//                                                + "AND r.idallievi=" + idallievo + " AND r.convalidata=1 AND l.codice_ud='" + m1 + "'";
//                                        try (Statement st2P = db0.getConnection().createStatement(); ResultSet rs2P = st2P.executeQuery(sql2_P)) {
//                                            if (rs2P.next()) {
//                                                //PRESENZA MODULI SINGOLI
//                                                double res = rs2P.getLong(1) / 3600000.00;
//                                                datainizio = sdfSQL.format(rs2P.getDate(2));
//                                                datafine = sdfSQL.format(rs2P.getDate(2));
//                                                db0.insert_UD_presenza(String.valueOf(Double.compare(Double.parseDouble(String.valueOf(MOD_ore.get(m1))), res)),
//                                                        datafine, datainizio, m1.split("-")[0], String.valueOf(res), String.valueOf(MOD_ore.get(m1)), m1, String.valueOf(idallievo));
////                                                System.out.println(idallievo+" : "+m1 + " - "
////                                                        + MOD_ore.get(m1) + " : " + res + " - " + datainizio + " - " + datafine + " -- " + m1.split("-")[0]);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        //UD
        List<UD> elencoud = new ArrayList<>();
        String sql3A = "SELECT descrizione,sum(ore),fase,GROUP_CONCAT(codice SEPARATOR ';') AS moduli FROM unita_didattiche GROUP BY descrizione,fase ORDER BY fase,ordine";
        try (Statement st3A = db0.getConnection().createStatement(); ResultSet rs3A = st3A.executeQuery(sql3A)) {
            while (rs3A.next()) {
                elencoud.add(new UD(rs3A.getString(1), rs3A.getDouble(2), rs3A.getString(3), rs3A.getString(4)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Long idallievo : idallievi) {

            //ORE
//            String sql3 = "SELECT SUM(p.orepresenze),p.fase FROM presenzeudallievi p WHERE p.idallievi=" + idallievo + " GROUP BY p.fase";
//            try (Statement st3 = db0.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
//                while (rs3.next()) {
//                    String upd3 = "UPDATE allievi SET orec_fase" + rs3.getString(2).toLowerCase() + " = '" + rs3.getDouble(1) + "' WHERE idallievi = " + idallievo;
//                    try (Statement st3A = db0.getConnection().createStatement()) {
//                        boolean es = st3A.executeUpdate(upd3) > 0;
////                        System.out.println(idallievo + " ) " + rs3.getString(2) + " -- " + rs3.getDouble(1) + " : " + es);
//                    }
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            
//            UNITA DIDATTICHE
//            int ud_ok_A = 0;
//            int ud_ok_B = 0;
//
//            String codudok_A = "";
//            String codudok_B = "";
//            for (UD unit : elencoud) {
//
//                double oreallievo = 0.0;
//
//                String sql4 = "SELECT * FROM presenzeudallievi p WHERE p.idallievi=" + idallievo + " AND (";
//                List<String> moduli = Splitter.on(";").splitToList(unit.getModuli());
//
//                for (String m1 : moduli) {
//                    sql4 += " ud LIKE '%" + m1 + "%' OR";
//                }
//
//                sql4 = StringUtils.substring(sql4, 0, sql4.length() - 2).trim() + ")";
////                    System.out.println(sql4);
//                try (Statement st4 = db0.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
//                    while (rs4.next()) {
//                        oreallievo += rs4.getDouble("orepresenze");
////                            System.out.println("oreunitadidattiche.main() "+rs4.getString("ud")+" - "+rs4.getDouble("orepresenze"));
//                    }
//
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                if (unit.getOre() == oreallievo) {
//                    if (unit.getFase().endsWith("A")) {
//                        ud_ok_A++;
//                        codudok_A += unit.getDescrizione() + ";";
//                    } else if (unit.getFase().endsWith("B")) {
//                        ud_ok_B++;
//                        codudok_B += unit.getDescrizione() + ";";
//
//                    }
//
////                        System.out.println(unit.toString() + " () " + idallievo + ": COMPLETA ");
//                } else {
////                        System.out.println(unit.toString() + " () " + idallievo + ": INCOMPLETA " + oreallievo);
//
//                }
//
//            }
//
//            String upd4 = "UPDATE allievi SET codudok_A = ?, codudok_B = ?, ud_ok_A = ?, ud_ok_B = ? WHERE idallievi = ?";
//
//            try (PreparedStatement ps4 = db0.getConnection().prepareStatement(upd4)) {
//                ps4.setString(1, codudok_A);
//                ps4.setString(2, codudok_B);
//                ps4.setInt(3, ud_ok_A);
//                ps4.setInt(4, ud_ok_B);
//                ps4.setLong(5, idallievo);
//                boolean es = ps4.executeUpdate() > 0;
//                System.out.println(ps4.toString() + " - " + es);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }

            //ASSENZE
            int assenzeOK = 0;
            int assenzeKO = 0;
            String sql5 = "SELECT p.datarealelezione,p.assenzagiustificata FROM presenzelezioniallievi p WHERE p.convalidata=1 AND p.durataconvalidata=0 AND p.idallievi=" + idallievo + " GROUP BY LEFT(p.datarealelezione,10)";

            try (Statement st5 = db0.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
                while (rs5.next()) {
                    if (rs5.getBoolean(2)) {
                        assenzeOK++;
                    } else {
                        assenzeKO++;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (assenzeOK > 0 || assenzeKO > 0) {
                String upd5 = "UPDATE allievi SET assenzeOK = ?, assenzeKO = ? WHERE idallievi = ?";
                try (PreparedStatement ps5 = db0.getConnection().prepareStatement(upd5)) {
                    ps5.setInt(1, assenzeOK);
                    ps5.setInt(2, assenzeKO);
                    ps5.setLong(3, idallievo);
                    boolean es = ps5.executeUpdate() > 0;
                    System.out.println(ps5.toString() + " - " + es);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (assenzeKO >= 2) {
                    //CAMBIO STATO
                }
            }

//            }
        }

        db0.closeDB();

    }
}

class UD {

    String descrizione;
    double ore;
    String fase, moduli;

    public UD(String descrizione, double ore, String fase, String moduli) {
        this.descrizione = descrizione;
        this.ore = ore;
        this.fase = fase;
        this.moduli = moduli;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public double getOre() {
        return ore;
    }

    public void setOre(double ore) {
        this.ore = ore;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public String getModuli() {
        return moduli;
    }

    public void setModuli(String moduli) {
        this.moduli = moduli;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UD{");
        sb.append("descrizione=").append(descrizione);
        sb.append(", ore=").append(ore);
        sb.append(", fase=").append(fase);
        sb.append(", moduli=").append(moduli);
        sb.append('}');
        return sb.toString();
    }

}
