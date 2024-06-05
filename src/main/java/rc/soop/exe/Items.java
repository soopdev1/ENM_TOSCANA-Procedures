/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author rcosco
 */
public class Items {
    
    String fase, data, orainizio, orafine, gruppo;
    
    String tipo, filename, content;
    
    public Items(String fase, String data, String orainizio, String orafine, String gruppo) {
        this.fase = fase;
        this.data = data;
        this.orainizio = orainizio;
        this.orafine = orafine;
        this.gruppo = gruppo;
    }

    public Items(String tipo, String filename, String content) {
        this.tipo = tipo;
        this.filename = filename;
        this.content = content;
    }
    
    

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrainizio() {
        return orainizio;
    }

    public void setOrainizio(String orainizio) {
        this.orainizio = orainizio;
    }

    public String getOrafine() {
        return orafine;
    }

    public void setOrafine(String orafine) {
        this.orafine = orafine;
    }

    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
    
    
    
    String cod;
    int codice;
    String descrizione;

    public Items(String cod, String descrizione) {
        this.cod = cod;
        this.descrizione = descrizione;
    }
    
    public Items(int codice, String descrizione) {
        this.codice = codice;
        this.descrizione = descrizione;
    }
    
    
    public static List<Items> formatAction() {
        List<Items> out = new ArrayList<>();
        out.add(new Items("L1", "Login"));
        out.add(new Items("L2", "Logout"));
        out.add(new Items("L3", "Logout"));
        out.add(new Items("L3", "Logout"));
        out.add(new Items("L4", "Logout"));
        out.add(new Items("L5", "Chiusura stanza"));
        out.add(new Items("IN", "Info"));
        return out;
    }
    
    
    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }
    
    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
