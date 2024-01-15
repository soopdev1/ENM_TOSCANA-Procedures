/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 *
 * @author rcosco
 */
public class Presenti {

    String cognome, nome, descrizione, cf, ruolo, email;
    boolean login, logout;
    String date;
    String idfad;
        
    
    String oradilogin,oradilogout,totaleore,totaleorerendicontabili;
    
    long millistotaleore,millistotaleorerendicontabili,orelezione;
    
    public Presenti(String cognome, String nome, String ruolo, String email, String oradilogin, String oradilogout, String totaleore, String totaleorerendicontabili) {
        this.cognome = cognome;
        this.nome = nome;
        this.ruolo = ruolo;
        this.email = email;
        this.oradilogin = oradilogin;
        this.oradilogout = oradilogout;
        this.totaleore = totaleore;
        this.totaleorerendicontabili = totaleorerendicontabili;
        this.millistotaleore = 0L;
        this.millistotaleorerendicontabili = 0L;
        this.orelezione = 0L;
    }
    
    public Presenti(String nome, String cognome, String cf, String email, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.cf = cf;
        this.email = email;
        this.ruolo = ruolo;
        this.login = false;
        this.logout = false;
        this.date = "";
        this.idfad = null;
        this.oradilogin = "";
        this.oradilogout = "";
        this.totaleore = "";
        this.totaleorerendicontabili = "";
        this.millistotaleore = 0L;
        this.millistotaleorerendicontabili = 0L;
        this.orelezione = 0L;
    }

    public String getIdfad() {
        return idfad;
    }

    public void setIdfad(String idfad) {
        this.idfad = idfad;
    }
    
    public long getOrelezione() {
        return orelezione;
    }

    public void setOrelezione(long orelezione) {
        this.orelezione = orelezione;
    }

    public long getMillistotaleore() {
        return millistotaleore;
    }

    public void setMillistotaleore(long millistotaleore) {
        this.millistotaleore = millistotaleore;
    }

    public long getMillistotaleorerendicontabili() {
        return millistotaleorerendicontabili;
    }

    public void setMillistotaleorerendicontabili(long millistotaleorerendicontabili) {
        this.millistotaleorerendicontabili = millistotaleorerendicontabili;
    }
    
    

    public String getOradilogin() {
        return oradilogin;
    }

    public void setOradilogin(String oradilogin) {
        this.oradilogin = oradilogin;
    }

    public String getOradilogout() {
        return oradilogout;
    }

    public void setOradilogout(String oradilogout) {
        this.oradilogout = oradilogout;
    }

    public String getTotaleore() {
        return totaleore;
    }

    public void setTotaleore(String totaleore) {
        this.totaleore = totaleore;
    }

    public String getTotaleorerendicontabili() {
        return totaleorerendicontabili;
    }

    public void setTotaleorerendicontabili(String totaleorerendicontabili) {
        this.totaleorerendicontabili = totaleorerendicontabili;
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

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public boolean isLogout() {
        return logout;
    }

    public void setLogout(boolean logout) {
        this.logout = logout;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
      @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.date);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Presenti other = (Presenti) obj;
        return Objects.equals(this.date, other.date) && Objects.equals(this.login, other.login) && Objects.equals(this.logout, other.logout)&& Objects.equals(this.idfad, other.idfad);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, JSON_STYLE);
    }

    
}
