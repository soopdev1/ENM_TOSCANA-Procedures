/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Administrator
 */
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import static rc.soop.exe.Utils.getJsonString;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.FaseA;

public class oautharti {

    
    
    
    
    public static void recuperadomicilio() {
        String sql = "SELECT t.azione FROM tracking t WHERE t.azione LIKE 'insertiscrizione%' GROUP BY t.azione;";

        FaseA FA = new FaseA(false);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql)) {
            while (rs1.next()) {
                String azione = rs1.getString("t.azione").replaceAll("insertiscrizione: ", "");

                JsonObject convertedObject = new Gson().fromJson(azione, JsonObject.class);

                String codiceFiscale = getJsonString(convertedObject, "codiceFiscale");
                JsonObject datiIscrizione = convertedObject.getAsJsonObject("datiIscrizione");
//                System.out.println("oautharti.main() "+codiceFiscale);
                JsonObject indirizzoResidenza = datiIscrizione.getAsJsonObject("indirizzoResidenza");
//                String codCatastaleComune = getJsonString(indirizzoResidenza, "codCatastaleComune");
                String via = getJsonString(indirizzoResidenza, "via");
                String cap = getJsonString(indirizzoResidenza, "cap");
                if (!datiIscrizione.get("indirizzoDomicilio").isJsonNull()) {
                    JsonObject indirizzoDomicilio = datiIscrizione.getAsJsonObject("indirizzoDomicilio");
                    String DOM_codCatastaleComune = getJsonString(indirizzoDomicilio, "codCatastaleComune");
                    String DOM_via = getJsonString(indirizzoDomicilio, "via");
                    String DOM_cap = getJsonString(indirizzoDomicilio, "cap");
                    String update = "UPDATE allievi SET comune_domicilio = ?, indirizzodomicilio = ?, capdomicilio = ? WHERE codicefiscale = ? AND comune_domicilio IS NULL";
                    Long idComuneDomicilio = db0.getIdComune(DOM_codCatastaleComune);
                    try (PreparedStatement ps = db0.getConnection().prepareStatement(update)) {
                        ps.setLong(1, idComuneDomicilio);
                        ps.setString(2, DOM_via);
                        ps.setString(3, DOM_cap);
                        ps.setString(4, codiceFiscale);
                        boolean es = ps.executeUpdate() > 1;
                        if (es) {
                            System.out.println(codiceFiscale + " oautharti.main() " + idComuneDomicilio + " - " + via + " - " + cap);
                        } else {
//                            System.out.println(codiceFiscale + " ERRORE " + idComuneDomicilio + " - " + via + " - " + cap);
                        }
                    }
//                    System.out.println(codiceFiscale + " oautharti.main() " + DOM_codCatastaleComune + " - " + DOM_via + " - " + DOM_cap);
                } else {
//                    System.out.println(codiceFiscale + " oautharti.main(NO DOMICILIO) ");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db0.closeDB();
    }
}
