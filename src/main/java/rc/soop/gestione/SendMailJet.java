/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

/**
 *
 * @author srotella
 */
import com.mailjet.client.Base64;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author setasrl
 */
public class SendMailJet {

    public static boolean sendMail(String name, String[] to, String[] cc, String txt, String subject, Db_Gest dbb, Logger log) {
        return sendMail(name, to, cc, new String[]{}, txt, subject, null, dbb, log);
    }

    public static boolean sendMail(String name, String[] to, String[] cc, String[] ccn, String txt, String subject, Db_Gest dbb, Logger log) {
        return sendMail(name, to, cc, ccn, txt, subject, null, dbb, log);
    }

    public static boolean sendMail(String name, String[] to, String[] cc, String[] bcc, String txt, String subject, File file, Db_Gest dbb, Logger log) {
        
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String filename = "";
        String content_type = "";
        String b64 = "";
       
        String mailjet_api = dbb.getPath("mailjet_api");
        String mailjet_secret = dbb.getPath("mailjet_secret");
        String mailjet_name = dbb.getPath("mailjet_name");
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
    	OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();        
        
        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjet_api)
                .apiSecretKey(mailjet_secret)
                .okHttpClient(customHttpClient)
                .build();

        client = new MailjetClient(options);

        //client = new MailjetClient(mailjet_api, mailjet_secret, new ClientOptions("v3.1"));
//        client.setDebug(1);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();
        JSONArray ccj = new JSONArray();

        if (to != null) {
            for (String s : to) {
                dest.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            dest.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }

        if (cc != null) {
            for (String s : cc) {
                ccj.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            ccj.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }

        if (bcc != null) {
            for (String s : bcc) {
                ccn.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            ccn.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }

        JSONObject mail = new JSONObject().put(Emailv31.Message.FROM, new JSONObject()
                .put("Email", mailjet_name)
                .put("Name", name))
                .put(Emailv31.Message.TO, dest)
                .put(Emailv31.Message.CC, ccj)
                .put(Emailv31.Message.BCC, ccn)
                .put(Emailv31.Message.SUBJECT, subject)
                .put(Emailv31.Message.HTMLPART, txt);

        if (file != null) {
            try {
                filename = file.getName();
                content_type = Files.probeContentType(file.toPath());
                try (InputStream i = new FileInputStream(file)) {
                    b64 = Base64.encode(IOUtils.toByteArray(i));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                    .put(new JSONObject()
                            .put("ContentType", content_type)
                            .put("Filename", filename)
                            .put("Base64Content", b64)));
        }

        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(mail));

        try {
            response = client.post(request);
            log.log(Level.INFO, "MAIL TO {0} : {1} -- {2}", new Object[]{dest.toList(), response.getStatus(), response.getData()});
            return response.getStatus() == 200;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "MAIL ERROR: {0}", Constant.estraiEccezione(ex));
            return false;
        }
    }

}
