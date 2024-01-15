/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Registro_completo {

    int id, idprogetti_formativi, idsoggetti_attuatori;
    String cip;
    DateTime data;
    String idriunione;
    int numpartecipanti;
    String orainizio, orafine;
    long durata;
    String nud, fase;
    int gruppofaseb;
    String ruolo, cognome, nome, email, orelogin, orelogout;
    long totaleore, totaleorerendicontabili;
    int idutente;

    public Registro_completo(int id, int idprogetti_formativi, int idsoggetti_attuatori, String cip, DateTime data, String idriunione, int numpartecipanti, String orainizio, String orafine, long durata, String nud, String fase, int gruppofaseb, String ruolo, String cognome, String nome, String email, String orelogin, String orelogout, long totaleore, long totaleorerendicontabili,int idutente) {
        this.id = id;
        this.idprogetti_formativi = idprogetti_formativi;
        this.idsoggetti_attuatori = idsoggetti_attuatori;
        this.cip = cip;
        this.data = data;
        this.idriunione = idriunione;
        this.numpartecipanti = numpartecipanti;
        this.orainizio = orainizio;
        this.orafine = orafine;
        this.durata = durata;
        this.nud = nud;
        this.fase = fase;
        this.gruppofaseb = gruppofaseb;
        this.ruolo = ruolo;
        this.cognome = cognome;
        this.nome = nome;
        this.email = email;
        this.orelogin = orelogin;
        this.orelogout = orelogout;
        this.totaleore = totaleore;
        this.totaleorerendicontabili = totaleorerendicontabili;
        this.idutente = idutente;
    }

    public int getIdutente() {
        return idutente;
    }

    public void setIdutente(int idutente) {
        this.idutente = idutente;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdprogetti_formativi() {
        return idprogetti_formativi;
    }

    public void setIdprogetti_formativi(int idprogetti_formativi) {
        this.idprogetti_formativi = idprogetti_formativi;
    }

    public int getIdsoggetti_attuatori() {
        return idsoggetti_attuatori;
    }

    public void setIdsoggetti_attuatori(int idsoggetti_attuatori) {
        this.idsoggetti_attuatori = idsoggetti_attuatori;
    }

    public String getCip() {
        return cip;
    }

    public void setCip(String cip) {
        this.cip = cip;
    }

    public DateTime getData() {
        return data;
    }

    public void setData(DateTime data) {
        this.data = data;
    }

    public String getIdriunione() {
        return idriunione;
    }

    public void setIdriunione(String idriunione) {
        this.idriunione = idriunione;
    }

    public int getNumpartecipanti() {
        return numpartecipanti;
    }

    public void setNumpartecipanti(int numpartecipanti) {
        this.numpartecipanti = numpartecipanti;
    }

    public String getOrainizio() {
        return orainizio;
    }

    public void setORainizio(String rainizio) {
        this.orainizio = rainizio;
    }

    public String getOrafine() {
        return orafine;
    }

    public void setOrafine(String orafine) {
        this.orafine = orafine;
    }

    public long getDurata() {
        return durata;
    }

    public void setDurata(long durata) {
        this.durata = durata;
    }

    public String getNud() {
        return nud;
    }

    public void setNud(String nud) {
        this.nud = nud;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public int getGruppofaseb() {
        return gruppofaseb;
    }

    public void setGruppofaseb(int gruppofaseb) {
        this.gruppofaseb = gruppofaseb;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrelogin() {
        return orelogin;
    }

    public void setOrelogin(String orelogin) {
        this.orelogin = orelogin;
    }

    public String getOrelogout() {
        return orelogout;
    }

    public void setOrelogout(String orelogout) {
        this.orelogout = orelogout;
    }

    public long getTotaleore() {
        return totaleore;
    }

    public void setTotaleore(long totaleore) {
        this.totaleore = totaleore;
    }

    public long getTotaleorerendicontabili() {
        return totaleorerendicontabili;
    }

    public void setTotaleorerendicontabili(long totaleorerendicontabili) {
        this.totaleorerendicontabili = totaleorerendicontabili;
    }
    
    
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, JSON_STYLE);
    }
    
    
}
