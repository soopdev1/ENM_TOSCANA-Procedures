/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 *
 * @author rcosco
 */
public class Utenti {

    int id;
    String cognome, nome, descrizione, cf, ruolo, email;

    String gruppofaseB;
    
    public Utenti(int id, String cognome, String nome, String cf, String ruolo, String email, String gruppofaseB) {
        this.id = id;
        this.cognome = cognome;
        this.nome = nome;
        this.descrizione = nome + " " + cognome;
        this.cf = cf;
        this.ruolo = ruolo;
        this.email = email;
        this.gruppofaseB = gruppofaseB;
    }

    
    public Utenti() {
        
    }

    public String getGruppofaseB() {
        return gruppofaseB;
    }

    public void setGruppofaseB(String gruppofaseB) {
        this.gruppofaseB = gruppofaseB;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, JSON_STYLE);
    }
}
