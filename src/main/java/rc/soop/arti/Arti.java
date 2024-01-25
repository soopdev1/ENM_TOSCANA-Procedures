/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.arti;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
import org.dmfs.oauth2.client.grants.ClientCredentialsGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc5545.Duration;
import org.joda.time.DateTime;
import rc.soop.exe.Utils;
import rc.soop.gestione.Db_Gest;
import rc.soop.gestione.FaseA;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author Administrator
 */
public class Arti {

    private static String getToken(Db_Gest db1) {

        try {
            HttpRequestExecutor executor = new HttpUrlConnectionExecutor();

            OAuth2AuthorizationProvider provider = new BasicOAuth2AuthorizationProvider(
                    URI.create(db1.getPath("arti.link.auth")),
                    URI.create(db1.getPath("arti.link.token")),
                    new Duration(1, 0, 3600));
            OAuth2ClientCredentials credentials = new BasicOAuth2ClientCredentials(db1.getPath("arti.client.id"), db1.getPath("arti.client.secret"));

            OAuth2Client client = new BasicOAuth2Client(
                    provider,
                    credentials,
                    new LazyUri(new Precoded("http://localhost")) /* Redirect URL */);

            OAuth2AccessToken token = new ClientCredentialsGrant(client, new BasicScope("")).accessToken(executor);
            return token.accessToken().toString();
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return null;

    }

    public static ResponseStatoIscrizione invia(boolean testing, String codiceAttivita, String codiceFiscale, String statoarti,String dataInizioCorso, String dataFineCorso) {
        try {
            FaseA FA = new FaseA(testing);
            String datenow = new DateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            JsonObject jo = new JsonObject();
            jo.addProperty("codiceAttivita", codiceAttivita);
            jo.addProperty("codiceFiscale", codiceFiscale);
            jo.addProperty("dataEsame", datenow);
            jo.addProperty("dataFineCorso", dataFineCorso);
            jo.addProperty("dataInizioCorso", dataInizioCorso);
            jo.addProperty("dataStato", datenow);
            jo.addProperty("descrizioneStato", statoarti);
            jo.addProperty("stato", statoarti);
            
            log.log(Level.WARNING, "REQUEST: {0}", jo.toString());
            
            Db_Gest db0 = new Db_Gest(FA.getHost());

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jo.toString());
            Request request = new Request.Builder()
                    .url(db0.getPath("arti.link.aggiornastato"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + getToken(db0))
                    .build();
            Response response = client.newCall(request).execute();

            db0.closeDB();
            
            log.log(Level.WARNING, "RESPONSE CODE: {0}", response.code());
            String resp = response.body().string();
            log.log(Level.WARNING, "RESPONSE BODY: {0}", resp);
            return new Gson().fromJson(resp, ResponseStatoIscrizione.class);
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
            return new ResponseStatoIscrizione("fail", estraiEccezione(ex), codiceAttivita, codiceFiscale);
        }
        

    }

}


