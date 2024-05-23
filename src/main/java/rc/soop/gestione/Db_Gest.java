package rc.soop.gestione;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.stripAccents;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import rc.soop.exe.Comuni_rc;
import rc.soop.exe.DatiDiscente;
import rc.soop.exe.ExcelDomande;
import rc.soop.exe.Items;
import rc.soop.exe.Utenti;
import static rc.soop.exe.Utils.conf;
import static rc.soop.exe.Utils.estraiEccezione;
import static rc.soop.exe.Utils.formatStringtoStringDateSQL;
import static rc.soop.exe.Utils.parseIntR;
import static rc.soop.gestione.Toscana_gestione.log;

/**
 *
 * @author raffaele
 */
public class Db_Gest {

    private Connection c = null;

    public Db_Gest(String host, boolean survey) {

        String driver = "com.mysql.cj.jdbc.Driver";
        String user = conf.getString("db.user");
        String password = conf.getString("db.pass");

        if (survey) {
            user = conf.getString("db.user.survey");
            password = conf.getString("db.pass.survey");
        }

        try {
            Class.forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", user);
            p.put("password", password);
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");
            this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (this.c != null) {
                try {
                    this.c.close();
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            this.c = null;
        }
    }

    public Db_Gest(String host) {

        String driver = "com.mysql.cj.jdbc.Driver";

        try {
            Class.forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", conf.getString("db.user"));
            p.put("password", conf.getString("db.pass"));
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");
            this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (this.c != null) {
                try {
                    this.c.close();
                } catch (Exception ex1) {
                }
            }
            this.c = null;
        }
    }

    public void closeDB() {
        try {
            if (this.c != null) {
                this.c.close();
            }
        } catch (SQLException ex) {
        }
    }

    public Connection getConnection() {
        return c;
    }

    public String getPath(String id) {
        String path = "-";
        try {
            String sql = "SELECT url FROM path WHERE id = ?";
            try (PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return path;
    }

    public List<Integer> elencoidnuovarendicontazione() {
        List<Integer> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM nuovarend";
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public boolean insertTracking(String idUser, String azione) {
        try {
            String ins = "INSERT INTO tracking (idUser,azione) VALUES (?,?)";
            try (PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, idUser);
                ps.setString(2, azione);
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public ArrayList<Comuni_rc> query_comuni_rc() {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni_rc";
            try (PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Comuni_rc(rs1.getInt(1), rs1.getString(2),
                            rs1.getString(3),
                            rs1.getString(4), rs1.getString(5), rs1.getString(6), rs1.getString(7)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public String formatStatoDomanda(String statoDomanda) {
        try {
            switch (statoDomanda) {
                case "S" -> {
                    return "NON PROCESSATA";
                }
                case "R" -> {
                    return "RIGETTATA";
                }
                case "A" -> {
                    return "APPROVATA";
                }
                case "A1" -> {
                    return "CONVENZIONE SA";
                }
                case "A2" -> {
                    return "SA ATTIVO";
                }
                case "A3" -> {
                    return "IN ATTESA FIRMA ENM";
                }
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public int countDocumentConvenzioni(String username) {
        int var1 = 0;
        try {
            String query = "select count(*) from docuserconvenzioni where username='" + username + "'";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var1 = rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return var1;
    }

    public String getInvioEmailROMA(String username) {
        String out = "0";
        try {
            String query = "select username,sendmail from docuserconvenzioni where username='" + username + "' and codicedoc='CONV'";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString("sendmail");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public String getConvenzioneROMA(String username) {
        String pathRoma = "";
        try {
            String query = "select path from convenzioniroma where username = '" + username + "' order by timestamp desc limit 1";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pathRoma = rs.getString("path");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathRoma;
    }

    public List<ExcelDomande> listaconsegnate(String table) {
        List<ExcelDomande> out = new LinkedList<>();
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            String sql = "SELECT * FROM " + table + " ORDER BY dataconsegna";
            try (PreparedStatement ps = this.c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    String USERNAME = rs.getString("username");
                    String CODICEDOMANDA = rs.getString("coddomanda");
                    String DATACONSEGNA = formatStringtoStringDateSQL(rs.getString("dataconsegna").split(" ")[0]);
                    String ORACONSEGNA = rs.getString("dataconsegna").split(" ")[1].substring(0, 8);
                    String RAGIONESOCIALE = rs.getString("societa");
                    String PIVA = rs.getString("pivacf");
                    String PEC = rs.getString("pec");
                    String NPROTOCOLLO = rs.getString("protocollo");

                    ExcelDomande ex1 = new ExcelDomande();
                    ex1.setUSERNAME(USERNAME);
                    ex1.setCODICEDOMANDA(CODICEDOMANDA);
                    ex1.setDATACONSEGNA(DATACONSEGNA);
                    ex1.setORACONSEGNA(ORACONSEGNA);
                    ex1.setRAGIONESOCIALE(RAGIONESOCIALE);
                    ex1.setRAGIONESOCIALE(RAGIONESOCIALE);
                    ex1.setPIVA(PIVA);
                    ex1.setPEC(PEC);
                    ex1.setNPROTOCOLLO(NPROTOCOLLO);

                    boolean convenzionedainviare = countDocumentConvenzioni(USERNAME) == 3;
                    boolean convenzioneinviataROMA = getInvioEmailROMA(USERNAME).equals("1");
                    boolean convenzionecaricatacontrofirmata = !getConvenzioneROMA(USERNAME).trim().equals("");

                    if (convenzionedainviare) {
                        if (convenzioneinviataROMA) {
                            if (convenzionecaricatacontrofirmata) {
                                ex1.setSTATODOMANDA(formatStatoDomanda("A2"));
                            } else {
                                ex1.setSTATODOMANDA(formatStatoDomanda("A3"));
                            }
                        } else {
                            ex1.setSTATODOMANDA(formatStatoDomanda("A1"));
                        }
                    } else {
                        ex1.setSTATODOMANDA(formatStatoDomanda(rs.getString("stato_domanda")));
                    }

                    String sql2 = "SELECT * FROM usersvalori WHERE username= '" + USERNAME + "'";
                    try (PreparedStatement ps2 = this.c.prepareStatement(sql2); ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            String campo = rs2.getString("campo");
                            String valore = rs2.getString("valore").toUpperCase().trim();
                            String valore1 = rs2.getString("valore").toUpperCase().trim();

                            switch (campo) {
                                case "sedeindirizzo" ->
                                    ex1.setSEDELEGALEINDIRIZZO(valore.toUpperCase());
                                case "sedecap" ->
                                    ex1.setSEDELEGALECAP(valore.toUpperCase());
                                case "sedecomune" -> {
                                    if (!valore.equals("")) {
                                        Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                        if (c0 != null) {
                                            valore1 = c0.getNome();
                                        }
                                    }
                                    ex1.setSEDELEGALECOMUNE(valore1.toUpperCase());
                                }
                                case "sedeprov" -> {
                                    if (!valore.equals("")) {
                                        Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                        if (c0 != null) {
                                            valore1 = c0.getProvincia();
                                        }
                                    }
                                    ex1.setSEDELEGALEPROVINCIA(valore1.toUpperCase());
                                }
                                case "sederegione" -> {
                                    if (!valore.equals("")) {
                                        Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                        if (c0 != null) {
                                            valore1 = c0.getRegione();
                                        }
                                    }
                                    ex1.setSEDELEGALEREGIONE(valore1.toUpperCase());
                                }
                                case "email" ->
                                    ex1.setEMAIL(valore.toUpperCase());
                                case "cell" ->
                                    ex1.setTELEFONO(valore.toUpperCase());
                                default -> {
                                }
                            }

                        }
                    }

                    HashMap<String, String> allegato_a = getAllegatoA(USERNAME);
                    ex1.setNSEDI(getMapValue(allegato_a, "numaule"));

                    ex1.setSEDE1INDIRIZZO(getMapValue(allegato_a, "indirizzo1"));
                    ex1.setSEDE1COMUNE(getMapValue(allegato_a, "citta1"));
                    ex1.setSEDE1PROVINCIA(getMapValue(allegato_a, "provincia1"));
                    ex1.setSEDE1REGIONE(getMapValue(allegato_a, "regioneaula1"));
                    ex1.setSEDE1TITOLODISP(getMapValue(allegato_a, "titolo1"));
                    ex1.setSEDE1MQ(getMapValue(allegato_a, "estremi1"));

                    ex1.setSEDE2INDIRIZZO(getMapValue(allegato_a, "indirizzo2"));
                    ex1.setSEDE2COMUNE(getMapValue(allegato_a, "citta2"));
                    ex1.setSEDE2PROVINCIA(getMapValue(allegato_a, "provincia2"));
                    ex1.setSEDE2REGIONE(getMapValue(allegato_a, "regioneaula2"));
                    ex1.setSEDE2TITOLODISP(getMapValue(allegato_a, "titolo2"));
                    ex1.setSEDE2MQ(getMapValue(allegato_a, "estremi2"));

                    ex1.setSEDE3INDIRIZZO(getMapValue(allegato_a, "indirizzo3"));
                    ex1.setSEDE3COMUNE(getMapValue(allegato_a, "citta3"));
                    ex1.setSEDE3PROVINCIA(getMapValue(allegato_a, "provincia3"));
                    ex1.setSEDE3REGIONE(getMapValue(allegato_a, "regioneaula3"));
                    ex1.setSEDE3TITOLODISP(getMapValue(allegato_a, "titolo3"));
                    ex1.setSEDE3MQ(getMapValue(allegato_a, "estremi3"));

                    ex1.setSEDE4INDIRIZZO(getMapValue(allegato_a, "indirizzo4"));
                    ex1.setSEDE4COMUNE(getMapValue(allegato_a, "citta4"));
                    ex1.setSEDE4PROVINCIA(getMapValue(allegato_a, "provincia4"));
                    ex1.setSEDE4REGIONE(getMapValue(allegato_a, "regioneaula4"));
                    ex1.setSEDE4TITOLODISP(getMapValue(allegato_a, "titolo4"));
                    ex1.setSEDE4MQ(getMapValue(allegato_a, "estremi4"));

                    ex1.setSEDE5INDIRIZZO(getMapValue(allegato_a, "indirizzo5"));
                    ex1.setSEDE5COMUNE(getMapValue(allegato_a, "citta5"));
                    ex1.setSEDE5PROVINCIA(getMapValue(allegato_a, "provincia5"));
                    ex1.setSEDE5REGIONE(getMapValue(allegato_a, "regioneaula5"));
                    ex1.setSEDE5TITOLODISP(getMapValue(allegato_a, "titolo5"));
                    ex1.setSEDE5MQ(getMapValue(allegato_a, "estremi5"));

                    ex1.setNDOCENTI(getMapValue(allegato_a, "numdocenti"));

                    String sql3 = "select * from allegato_b where username = '" + USERNAME + "' ORDER BY id";

                    try (PreparedStatement ps3 = this.c.prepareStatement(sql3); ResultSet rs3 = ps3.executeQuery()) {
                        while (rs3.next()) {

                            switch (rs3.getInt("id")) {
                                case 1 -> {
                                    ex1.setNOMEDOCENTE1(rs3.getString("nome").toUpperCase());
                                    ex1.setCOGNOMEDOCENTE1(rs3.getString("cognome").toUpperCase());
                                    ex1.setCFDOCENTE1(rs3.getString("cf").toUpperCase());
                                    ex1.setFASCIAPROPOSTADOCENTE1(rs3.getString("fascia").toUpperCase());
                                }
                                case 2 -> {
                                    ex1.setNOMEDOCENTE2(rs3.getString("nome").toUpperCase());
                                    ex1.setCOGNOMEDOCENTE2(rs3.getString("cognome").toUpperCase());
                                    ex1.setCFDOCENTE2(rs3.getString("cf").toUpperCase());
                                    ex1.setFASCIAPROPOSTADOCENTE2(rs3.getString("fascia").toUpperCase());
                                }
                                case 3 -> {
                                    ex1.setNOMEDOCENTE3(rs3.getString("nome").toUpperCase());
                                    ex1.setCOGNOMEDOCENTE3(rs3.getString("cognome").toUpperCase());
                                    ex1.setCFDOCENTE3(rs3.getString("cf").toUpperCase());
                                    ex1.setFASCIAPROPOSTADOCENTE3(rs3.getString("fascia").toUpperCase());
                                }
                                case 4 -> {
                                    ex1.setNOMEDOCENTE4(rs3.getString("nome").toUpperCase());
                                    ex1.setCOGNOMEDOCENTE4(rs3.getString("cognome").toUpperCase());
                                    ex1.setCFDOCENTE4(rs3.getString("cf").toUpperCase());
                                    ex1.setFASCIAPROPOSTADOCENTE4(rs3.getString("fascia").toUpperCase());
                                }
                                case 5 -> {
                                    ex1.setNOMEDOCENTE5(rs3.getString("nome").toUpperCase());
                                    ex1.setCOGNOMEDOCENTE5(rs3.getString("cognome").toUpperCase());
                                    ex1.setCFDOCENTE5(rs3.getString("cf").toUpperCase());
                                    ex1.setFASCIAPROPOSTADOCENTE5(rs3.getString("fascia").toUpperCase());
                                }
                                default -> {
                                }
                            }
                        }

                    }
                    out.add(ex1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String getMapValue(HashMap<String, String> map, String nome) {
        try {
            String valore = map.get(nome);
            if (valore != null) {
                return valore.toUpperCase().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public ArrayList<Items> query_disponibilita_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM disponibilita_rc";
            try (PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public HashMap<String, String> getAllegatoA(String username) {
        HashMap<String, String> map = new HashMap<>();
        String query = "SELECT * FROM allegato_a WHERE username = ?";
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> disponibilita = query_disponibilita_rc();

            try (PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsmd.getColumnName(i);
                        map.put(name, "");
                    }
                    if (rs.next()) {
                        SortedSet<String> indici = new TreeSet<>(map.keySet());
                        indici.forEach(ind -> {
                            try {
                                String valore = rs.getString(ind).toUpperCase().trim();
                                String valore1 = rs.getString(ind).toUpperCase().trim();
                                if (ind.startsWith("citta") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                } else if (ind.startsWith("regione") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                } else if (ind.startsWith("provincia") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getProvincia();
                                    }
                                } else if (ind.startsWith("titolo") && !valore.equals("")) {
                                    Items it1 = disponibilita.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                map.replace(ind, valore1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean insertReportExcel(String data, String base64, String timestamp) {

        if (base64 != null) {
            try {
                String insert = "INSERT INTO excelreport VALUES(?,?,?)";
                try (PreparedStatement ps = this.c.prepareStatement(insert)) {
                    ps.setString(1, data);
                    ps.setString(2, base64);
                    ps.setString(3, timestamp);
                    ps.executeUpdate();
                }
                return true;
            } catch (Exception e) {
                if (e.getMessage().toLowerCase().contains("duplicate")) {
                    try {
                        String insert = "UPDATE excelreport SET content = ?, aggiornamento = ? WHERE giorno = ?";
                        try (PreparedStatement ps1 = this.c.prepareStatement(insert)) {
                            ps1.setString(1, base64);
                            ps1.setString(2, timestamp);
                            ps1.setString(3, data);
                            ps1.executeUpdate();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    return true;
                } else {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    //REPORT
    public List<Utenti> list_Allievi_noAccento(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione IN ('15','18') AND idprogetti_formativi = " + idpr;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            stripAccents(rs.getString("cognome").toUpperCase()).trim(),
                            stripAccents(rs.getString("nome").toUpperCase()).trim(),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Allievi_noAccento(int idpr, int gruppo) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione IN ('15','18') AND idprogetti_formativi = " + idpr + " AND gruppo_faseB = " + gruppo;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            stripAccents(rs.getString("cognome").toUpperCase()).trim(),
                            stripAccents(rs.getString("nome").toUpperCase()).trim(),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Allievi(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione IN ('15','18') AND idprogetti_formativi = " + idpr;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            (rs.getString("cognome").toUpperCase().trim()),
                            (rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Docenti(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT iddocenti,nome,cognome,codicefiscale,email FROM docenti WHERE iddocenti IN "
                    + "(SELECT iddocenti FROM progetti_docenti WHERE idprogetti_formativi = " + idpr + ")";
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("iddocenti"),
                            (rs.getString("cognome").toUpperCase().trim()),
                            (rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "DOCENTE",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Docenti_noAccento(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT iddocenti,nome,cognome,codicefiscale,email FROM docenti WHERE iddocenti IN "
                    + "(SELECT iddocenti FROM progetti_docenti WHERE idprogetti_formativi = " + idpr + ")";
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("iddocenti"),
                            stripAccents(rs.getString("cognome").toUpperCase().trim()),
                            stripAccents(rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "DOCENTE",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public String[] sa_cip(int idpr) {
        try {

            String sql0 = "SELECT cip,idsoggetti_attuatori FROM progetti_formativi WHERE idprogetti_formativi = " + idpr;
            try (Statement st0 = this.c.createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                if (rs0.next()) {
                    String cip = rs0.getString(1);
                    String sql1 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = " + rs0.getInt(2);
                    try (Statement st1 = this.c.createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                        if (rs1.next()) {
                            String[] out = {rs1.getString(1).trim().toUpperCase(), cip, rs0.getString(2)};
                            return out;
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public List<DatiDiscente> getDati() {
        List<DatiDiscente> out = new ArrayList<>();
        try {
            String sql0 = "SELECT username,pivacf,cf,protocollo,decreto,datadecreto FROM bando_toscana_mcn WHERE stato_domanda='A'";
            try (Statement st0 = this.c.createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    out.add(new DatiDiscente(rs0.getString(1), rs0.getString(2), rs0.getString(3), rs0.getString(4), rs0.getString(5), rs0.getString(6)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public int get_allievi_accreditati(int idpr) {
        int out = 0;
        try {
            String sql = "SELECT COUNT(a.idallievi) FROM allievi a WHERE a.id_statopartecipazione IN ('13','14','15','18','19') AND a.idprogetti_formativi=" + idpr;
            try (Statement st1 = this.c.createStatement(); ResultSet rs = st1.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        return out;
    }

    public Long getIdComune(String istat) {
        try {
            String sql = "SELECT a.idcomune FROM comuni a WHERE a.istat = ?";
            try (PreparedStatement ps = this.c.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ps.setString(1, istat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return 0L;
    }

    public boolean insert_UD_presenza(String completa, String datafine, String datainizio, String fase, String orepresenze, String oretotali, String ud, String idallievi) {
        try {

            String s1 = "SELECT * FROM presenzeudallievi p WHERE p.idallievi = ? AND p.ud = ?";
            try (PreparedStatement ps1 = this.c.prepareStatement(s1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ps1.setString(1, idallievi);
                ps1.setString(2, ud);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next()) {
                        String upd = "UPDATE presenzeudallievi SET completa = ?, datafine = ?, datainizio = ?, fase = ? ,orepresenze  = ?, oretotali = ? WHERE ud  = ? AND idallievi = ?";
                        try (PreparedStatement ps = this.c.prepareStatement(upd, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                            ps.setString(1, completa);
                            ps.setString(2, datafine);
                            ps.setString(3, datainizio);
                            ps.setString(4, fase);
                            ps.setString(5, orepresenze);
                            ps.setString(6, oretotali);
                            ps.setString(7, ud);
                            ps.setString(8, idallievi);
                            ps.executeUpdate();
                            return true;
                        }
                    } else {
                        String ins = "INSERT INTO presenzeudallievi (completa,datafine,datainizio,fase,orepresenze,oretotali,ud,idallievi) VALUES (?,?,?,?,?,?,?,?)";
                        try (PreparedStatement ps = this.c.prepareStatement(ins, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                            ps.setString(1, completa);
                            ps.setString(2, datafine);
                            ps.setString(3, datainizio);
                            ps.setString(4, fase);
                            ps.setString(5, orepresenze);
                            ps.setString(6, oretotali);
                            ps.setString(7, ud);
                            ps.setString(8, idallievi);
                            ps.execute();
                            return true;
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public String getTipoDocAllievi(String id) {
        try {
            String sql = "SELECT modello FROM tipo_documenti_allievi WHERE idtipodocumenti_allievi = ?";
            try (PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return null;
    }
    
    
    public List<Registro_completo> registro_modello6(int idpr) {
        List<Registro_completo> registro = new ArrayList<>();
        try {
            //FAD
            String sql = "SELECT * FROM registro_completo WHERE idprogetti_formativi = " + idpr + " GROUP BY ruolo,idutente,data,gruppofaseb ORDER BY data,gruppofaseb";
            System.out.println("rc.so.db.Database.registro_modello6() "+sql);
            try (Statement st = this.c.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {

                    long orerend = rs.getLong(21);
                    Registro_completo rc = new Registro_completo(
                            rs.getInt(1),
                            rs.getInt(2),
                            rs.getInt(3),
                            rs.getString(4),
                            new DateTime(rs.getDate(5).getTime()),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getString(8),
                            rs.getString(9),
                            rs.getLong(10),
                            rs.getString(11),
                            rs.getString(12),
                            rs.getInt(13),
                            rs.getString(14),
                            rs.getString(15),
                            rs.getString(16),
                            rs.getString(17),
                            rs.getString(18),
                            rs.getString(19),
                            rs.getLong(20),
                            orerend,
                            rs.getInt(23));
                    registro.add(rc);
                }
            }

            //PRESENZA
            String sql1 = "SELECT * FROM presenzelezioni p, progetti_formativi f, lezioni_modelli lm, lezione_calendario lc , docenti d "
                    + " WHERE d.iddocenti=p.iddocente AND lc.id_lezionecalendario=lm.id_lezionecalendario AND lm.id_lezionimodelli=p.idlezioneriferimento AND "
                    + " p.idprogetto=f.idprogetti_formativi AND p.idprogetto = " + idpr + " ORDER BY p.datalezione,p.orainizio;";            
            System.out.println("rc.so.db.Database.registro_modello6() "+sql1);          
            try (Statement st1 = this.c.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    String sql2 = "SELECT * FROM presenzelezioniallievi a, allievi l WHERE a.idallievi=l.idallievi AND a.idpresenzelezioni = "
                            + rs1.getInt("p.idpresenzelezioni")
                            + " AND a.convalidata = 1 GROUP BY l.idallievi";

                    try (Statement st2 = this.c.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY); ResultSet rs2 = st2.executeQuery(sql2)) {

                        int numpartecipanti = 0;
                        while (rs2.next()) {
                            numpartecipanti++;
                        }
                        rs2.beforeFirst();

                        long durata = calcolaintervallomillis(rs1.getString("p.orainizio"), rs1.getString("p.orafine"));

                        String fase = rs1.getString("lc.codice_ud").startsWith("A") ? "A" : "B";

                        int gruppofaseb = fase.equals("A") ? 0 : rs1.getInt("lm.gruppo_faseB");

                        Registro_completo docente = new Registro_completo(0,
                                idpr,
                                rs1.getInt("f.idsoggetti_attuatori"),
                                rs1.getString("f.cip"),
                                new DateTime(rs1.getDate("p.datalezione").getTime()),
                                rs1.getString("f.cip") + "_" + fase + "_" + rs1.getString("lc.codice_ud") + "_" + StringUtils.replace(rs1.getString("p.datalezione"), "-", ""),
                                numpartecipanti,
                                rs1.getString("p.orainizio"),
                                rs1.getString("p.orafine"),
                                durata,
                                rs1.getString("lc.codice_ud"),
                                fase,
                                gruppofaseb,
                                "DOCENTE",
                                rs1.getString("d.cognome"),
                                rs1.getString("d.nome"),
                                rs1.getString("d.email"),
                                rs1.getString("p.orainizio"),
                                rs1.getString("p.orafine"),
                                durata,
                                durata,
                                rs1.getInt("d.iddocenti"));
                        registro.add(docente);

                        while (rs2.next()) {

                            Registro_completo rc = new Registro_completo(0,
                                    idpr,
                                    rs1.getInt("f.idsoggetti_attuatori"),
                                    rs1.getString("f.cip"),
                                    new DateTime(rs1.getDate("p.datalezione").getTime()),
                                    rs1.getString("f.cip") + "_" + fase + "_" + rs1.getString("lc.codice_ud") + "_" + StringUtils.replace(rs1.getString("p.datalezione"), "-", ""),
                                    numpartecipanti,
                                    rs1.getString("p.orainizio"),
                                    rs1.getString("p.orafine"),
                                    calcolaintervallomillis(rs1.getString("p.orainizio"), rs1.getString("p.orafine")),
                                    rs1.getString("lc.codice_ud"),
                                    fase,
                                    gruppofaseb,
                                    "ALLIEVO",
                                    rs2.getString("l.cognome"),
                                    rs2.getString("l.nome"),
                                    rs2.getString("l.email"),
                                    rs2.getString("a.orainizio"),
                                    rs2.getString("a.orafine"),
                                    rs2.getLong("a.durata"),
                                    rs2.getLong("a.durataconvalidata"),
                                    rs2.getInt("l.idallievi"));
                            registro.add(rc);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return registro;
    }

    
    private static long calcolaintervallomillis(String orastart, String oraend) {
        try {
            DateTime st_data1 = new DateTime(2000, 1, 1, Integer.parseInt(orastart.split(":")[0]), Integer.parseInt(orastart.split(":")[1]));
            DateTime st_data2 = new DateTime(2000, 1, 1, Integer.parseInt(oraend.split(":")[0]), Integer.parseInt(oraend.split(":")[1]));
            Period p = new Period(st_data1, st_data2, PeriodType.millis());
            return p.getValue(0);
        } catch (Exception e) {
            return 0L;
        }
    }
}
