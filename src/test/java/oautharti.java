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
import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant;
import org.dmfs.oauth2.client.grants.ClientCredentialsGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.oauth2.client.tokens.ImplicitGrantAccessToken;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc5545.Duration;
import org.joda.time.DateTime;
import rc.soop.arti.Arti;
import rc.soop.arti.ResponseStatoIscrizione;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.getJsonString;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.FaseA;
import static rc.soop.gestione.Toscana_gestione.log;

public class oautharti {

    public static void main(String[] args) {
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
