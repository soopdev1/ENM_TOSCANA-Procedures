/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.arti;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
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

    private static ResponseStatoIscrizione invia(boolean testing, String codiceAttivita, String codiceFiscale,
            String statoarti, String descrizionestatoarti, String dataInizioCorso, String dataFineCorso) {
        try {
            FaseA FA = new FaseA(testing);
            String datenow = new DateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            JsonObject jo = new JsonObject();
            jo.addProperty("codiceAttivita", codiceAttivita == null ? "DA_ASSEGNARE" : codiceAttivita);
            jo.addProperty("codiceFiscale", codiceFiscale);
            jo.addProperty("dataEsame", datenow);

            jo.addProperty("dataFineCorso", dataFineCorso == null ? datenow : dataFineCorso);
            jo.addProperty("dataInizioCorso", dataInizioCorso == null ? datenow : dataInizioCorso);
            jo.addProperty("dataStato", datenow);
            jo.addProperty("descrizioneStato", descrizionestatoarti);
            jo.addProperty("stato", statoarti);

            log.log(Level.WARNING, "REQUEST: {0}", jo.toString());

            Db_Gest db0 = new Db_Gest(FA.getHost());

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jo.toString());

            String tk = getToken(db0);
            Request request = new Request.Builder()
                    .url(encodeURIComponent(db0.getPath("arti.link.aggiornastato")))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + encodeURIComponent(tk))
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

    private static String encodeURIComponent(String unencoded) {
        try {
            String escaped = StringUtils.replace(unencoded, "\\", "\\\\");
            escaped = StringUtils.replace(escaped, "(\\r|\\n)+", " ");
            escaped = StringUtils.replace(escaped, "'", "\\'");
            return escaped;
        } catch (Exception e) {
            System.out.println("Error encoding string: '" + unencoded + "': " + e.getMessage());
            return "";
        }
    }

    public static void cambiostato_allievo(boolean testing) {

        FaseA FA = new FaseA(testing);

        Db_Gest db0 = new Db_Gest(FA.getHost());
//////////        AMMESSI
        String sql_INCORSO = "SELECT a.codicefiscale,p.cip,a.idallievi,p.start,p.end FROM allievi a, progetti_formativi p WHERE a.idprogetti_formativi=p.idprogetti_formativi "
                + "AND  a.id_statopartecipazione='15' AND (a.esito IS NULL OR a.esito NOT IN('AMMESSO_INIZIO_CORSO','AMMESSO_DOPO_INIZIO_CORSO')) ORDER BY a.codicefiscale;";

        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql_INCORSO)) {
            String STATOARTI = "AMMESSO_INIZIO_CORSO";
            while (rs1.next()) {
                String CIP = rs1.getString("p.cip");
                String CF = rs1.getString("a.codicefiscale");
                String dataInizioCorso = rs1.getString("p.start") + "T00:00:00.000Z";
                String dataFineCorso = rs1.getString("p.end") + "T00:00:00.000Z";
                ResponseStatoIscrizione resp = invia(testing, CIP, CF, STATOARTI, STATOARTI, dataInizioCorso, dataFineCorso);
                if (resp.getStatus().equals("success")) {
                    String update = "UPDATE allievi SET esito = ? WHERE idallievi = ?";
                    try (PreparedStatement ps2 = db0.getConnection().prepareStatement(update)) {
                        ps2.setString(1, STATOARTI);
                        ps2.setString(2, rs1.getString("a.idallievi"));
                        boolean ok = ps2.executeUpdate(update) > 0;
                        log.log(Level.INFO, "{0} - {1} ; {2} : {3} = {4}", new Object[]{STATOARTI, CF, CIP, update, ok});
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
////////////        RITIRATI PRIMA AVVIO
        String sql_NOSTART = "SELECT a.codicefiscale,a.esito,a.idallievi FROM allievi a WHERE a.id_statopartecipazione='11' "
                + "AND (a.esito IS NULL OR a.esito NOT IN('AMMESSO_INIZIO_CORSO','AMMESSO_DOPO_INIZIO_CORSO','NON_AMMESSO')) ORDER BY a.codicefiscale";
        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql_NOSTART)) {
            String STATOARTI = "NON_AMMESSO";
            while (rs1.next()) {
                String CF = rs1.getString("a.codicefiscale");
                ResponseStatoIscrizione resp = invia(testing, null, CF, STATOARTI, STATOARTI, null, null);
                if (resp.getStatus().equals("success")) {
                    String update = "UPDATE allievi SET esito = ? WHERE idallievi = ?";
                    try (PreparedStatement ps2 = db0.getConnection().prepareStatement(update)) {
                        ps2.setString(1, STATOARTI);
                        ps2.setString(2, rs1.getString("a.idallievi"));
                        boolean ok = ps2.executeUpdate(update) > 0;
                        log.log(Level.INFO, "{0} - {1} : {2} = {3}", new Object[]{STATOARTI, CF, update, ok});
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }

        //RITIRATI GIUSTIFICATI E NON
        String sql_RITIRATO = "SELECT a.codicefiscale,a.esito,a.idallievi,a.id_statopartecipazione,p.cip,p.start,p.end FROM allievi a, progetti_formativi p WHERE a.idprogetti_formativi=p.idprogetti_formativi AND a.id_statopartecipazione in (16,17) "
                + "AND (a.esito IS NULL OR a.esito NOT IN('RITIRATO')) ORDER BY a.codicefiscale";

        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql_RITIRATO)) {
            while (rs1.next()) {

                String CIP = rs1.getString("p.cip");
                String dataInizioCorso = rs1.getString("p.start") + "T00:00:00.000Z";
                String dataFineCorso = rs1.getString("p.end") + "T00:00:00.000Z";
                String CF = rs1.getString("a.codicefiscale");
                String STATOENM = rs1.getString("a.id_statopartecipazione");
                String STATOARTI = "RITIRATO";
                String DESCRSTATOARTI = STATOENM.equals("16") ? "ALTRO_COMPROVATO_IMPEDIMENTO_OGGETTIVO_E_O_CAUSA_DI_FORZA_MAGGIORE" : "RIFIUTO_NON_GIUSTIFICATO";
                ResponseStatoIscrizione resp = invia(testing, CIP, CF, STATOARTI, DESCRSTATOARTI, dataInizioCorso, dataFineCorso);
                if (resp.getStatus().equals("success")) {
                    String update = "UPDATE allievi SET esito = ? WHERE idallievi = ?";
                    try (PreparedStatement ps2 = db0.getConnection().prepareStatement(update)) {
                        ps2.setString(1, STATOARTI);
                        ps2.setString(2, rs1.getString("a.idallievi"));
                        boolean ok = ps2.executeUpdate(update) > 0;
                        log.log(Level.INFO, "{0} - {1} ; {2} : {3} = {4}", new Object[]{STATOARTI, CF, CIP, update, ok});
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }

        ////    COMPLETATO
        String sql_COMPLETATO = "SELECT a.codicefiscale,p.cip,a.idallievi,p.start,p.end,a.id_statopartecipazione "
                + "FROM allievi a, progetti_formativi p WHERE a.idprogetti_formativi=p.idprogetti_formativi "
                + "AND p.stato IN ('F','DVB','IV','CK','EVI','CO') "
                + "AND a.id_statopartecipazione IN ('18','19') "
                + "AND (a.esito IS NULL OR a.esito NOT IN('IDONEO','TERMINATO_CON_INSUCCESSO')) "
                + "ORDER BY a.codicefiscale";
        try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql_COMPLETATO)) {
            while (rs1.next()) {
                String CIP = rs1.getString("p.cip");
                String dataInizioCorso = rs1.getString("p.start") + "T00:00:00.000Z";
                String dataFineCorso = rs1.getString("p.end") + "T00:00:00.000Z";
                String CF = rs1.getString("a.codicefiscale");
                String STATOENM = rs1.getString("a.id_statopartecipazione");
                String STATOARTI = STATOENM.equals("18") ? "IDONEO" : "TERMINATO_CON_INSUCCESSO";
                ResponseStatoIscrizione resp = invia(testing, CIP, CF, STATOARTI, STATOARTI, dataInizioCorso, dataFineCorso);
                if (resp.getStatus().equals("success")) {
                    String update = "UPDATE allievi SET esito = ? WHERE idallievi = ?";
                    try (PreparedStatement ps2 = db0.getConnection().prepareStatement(update)) {
                        ps2.setString(1, STATOARTI);
                        ps2.setString(2, rs1.getString("a.idallievi"));
                        boolean ok = ps2.executeUpdate(update) > 0;
                        log.log(Level.INFO, "{0} - {1} ; {2} : {3} = {4}", new Object[]{STATOARTI, CF, CIP, update, ok});
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        db0.closeDB();

    }

//    public static void main(String[] args) {
//        cambiostato_allievo(false);
//    }
}
