//
//import java.io.File;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.Arrays;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.FileUtils;
//import rc.soop.accreditamento.Engine;
//import rc.soop.exe.Db_Accr;
//import rc.soop.gestione.Db_Gest;
//import rc.soop.gestione.FaseA;
//
///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
///**
// *
// * @author Administrator
// */
//public class UPDATEMODELLI {
//
//    public static void main(String[] args) {
//        upload_accr();
//    }
//
//    public static void upload_accr() {
//        try {
//
//            Engine accr = new Engine(false);
//
//            Db_Accr db1 = new Db_Accr(accr.host);
//
//            String dirstart = "C:\\Users\\Administrator\\Desktop\\da caricare\\ACCREDITAMENTO\\";
//            Arrays.asList(
//                    new File(dirstart).listFiles()).forEach(f1 -> {
//                try {
//                    System.out.println("UPDATEMODELLI.upload() " + f1.getName());
//
//                    String param1 = Base64.encodeBase64String(FileUtils.readFileToByteArray(f1));
//
//                    String query = "";
//                    String param2 = "";
//
//                    if (f1.getName().contains("ALLEGATO C1")) {
//                        query = "UPDATE path SET url = ? WHERE id = ?";
//                        param2 = "allegatoc.1.soggettosingolo";
//                    } else if (f1.getName().contains("ALLEGATO C2")) {
//                        query = "UPDATE path SET url = ? WHERE id = ?";
//                        param2 = "allegatoc.2.costituenda";
//                    } else if (f1.getName().contains("ALLEGATO C3")) {
//                        query = "UPDATE path SET url = ? WHERE id = ?";
//                        param2 = "allegatoc.3.costituita";
//                    } else if (f1.getName().contains("ALLEGATO C4")) {
//                        query = "UPDATE path SET url = ? WHERE id = ?";
//                        param2 = "allegatoc.4.rete";
//                    } else if (f1.getName().contains("ALLEGATO C5")) {
//                        query = "UPDATE path SET url = ? WHERE id = ?";
//                        param2 = "allegatoc.5.consorzio";
//                    } else if (f1.getName().contains("ALLEGATO D ")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "DONLD";
//                    } else if (f1.getName().contains("ALLEGATO E ")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "DONLE";
//                    } else if (f1.getName().contains("ALLEGATO F ")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "DONLF";
//                    } else if (f1.getName().contains("ALLEGATO G1")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "MOD1";
//                    } else if (f1.getName().contains("ALLEGATO G2")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "MOD2";
//                    } else if (f1.getName().contains("Allegato_A_")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "DONLA";
//                    } else if (f1.getName().contains("Allegato_B1_")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "ALB1";
//                    } else if (f1.getName().contains("Allegato_B_")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "DONLB";
//                    } else if (f1.getName().contains("Allegato_G_")) {
//                        query = "UPDATE docbandi SET download = ? WHERE codicedoc = ?";
//                        param2 = "CONV";
//                    } else {
//                        //throw new AssertionError();
//                    }
//                    if (!query.isEmpty()) {
//                        try (PreparedStatement ps = db1.getConnection().prepareStatement(query)) {
//                            ps.setString(1, param1);
//                            ps.setString(2, param2);
//                            System.out.println("UPDATEMODELLI.upload_accr() " + (ps.executeUpdate() > 0));
//                        }
//                    } else {
//                        System.out.println("UPDATEMODELLI.upload_accr() NON GESTITO");                        
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//            });
//            db1.closeDB();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void download() {
//        String sql1 = "SELECT t.idtipo_documenti,t.attivo,t.descrizione,t.modello FROM tipo_documenti t WHERE t.modello IS NOT NULL;";
//        String sql2 = "SELECT t.idtipodocumenti_allievi,t.attivo,t.descrizione,t.modello FROM tipo_documenti_allievi t WHERE t.modello IS NOT NULL;";
//
//        FaseA FA = new FaseA(false);
//
//        Db_Gest db0 = new Db_Gest(FA.getHost());
//
//        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//            while (rs1.next()) {
//                FileUtils.writeByteArrayToFile(new File("C:\\Users\\Administrator\\Desktop\\da caricare\\GESTIONALE\\" + rs1.getString(3) + ".pdf"),
//                        Base64.decodeBase64(rs1.getString(4)));
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql2)) {
//            while (rs1.next()) {
//                FileUtils.writeByteArrayToFile(new File("C:\\Users\\Administrator\\Desktop\\da caricare\\GESTIONALE\\" + rs1.getString(3) + ".pdf"),
//                        Base64.decodeBase64(rs1.getString(4)));
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        db0.closeDB();
//    }
//}
