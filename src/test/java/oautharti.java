/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Administrator
 */
import java.io.IOException;
import java.net.URI;
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
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.FaseA;

public class oautharti {

    public static void main(String[] args) {

        boolean testing = false;

        FaseA FA = new FaseA(testing);

        Db_Gest db0 = new Db_Gest(FA.getHost());

        String sql_INCORSO = "SELECT a.codicefiscale,p.cip,a.idallievi,p.start,p.end FROM allievi a, progetti_formativi p WHERE a.idprogetti_formativi=p.idprogetti_formativi "
                + "AND  a.id_statopartecipazione='15' AND (a.esito IS NULL OR a.esito NOT IN('AMMESSO_INIZIO_CORSO','AMMESSO_DOPO_INIZIO_CORSO')) ORDER BY a.codicefiscale;";
        String AMMESSO_INIZIO_CORSO = "AMMESSO_INIZIO_CORSO";
        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql_INCORSO)) {
            while (rs1.next()) {
                String CIP = rs1.getString("p.cip");
                String CF = rs1.getString("a.codicefiscale");
                String dataInizioCorso = rs1.getString("p.start") + "T00:00:00.000Z";
                String dataFineCorso = rs1.getString("p.end") + "T00:00:00.000Z";

                ResponseStatoIscrizione resp = Arti.invia(testing, CIP, CF, AMMESSO_INIZIO_CORSO, dataInizioCorso, dataFineCorso);
                if (resp.getStatus().equals("success")) {
                    //UPDATE
                    String update = "UPDATE allievi SET esito='" + AMMESSO_INIZIO_CORSO + "' WHERE idallievi = " + rs1.getString("A.idallievi");
                    try (Statement st2 = db0.getConnection().createStatement()) {
                        boolean ok = st2.executeUpdate(update) > 0;
                        System.out.println(CIP + " () " + CF + ": " + update + " - " + ok);
                    }
                }
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db0.closeDB();

    }
}
