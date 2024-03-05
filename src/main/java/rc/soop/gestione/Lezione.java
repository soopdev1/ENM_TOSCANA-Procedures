/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

/**
 *
 * @author rcosco
 */
public class Lezione {

    int id, gruppo,mpid_modello;
    List<Integer> docente;
    String giorno, start, end, codiceud, ore, nomestanza;
    

    public Lezione(int id, List<Integer> docente, String giorno, String start, String end, String codiceud, String ore, int gruppo, String nomestanza) {
        this.ore = ore;
        this.id = id;
        this.docente = docente;
        this.giorno = giorno;
        this.start = start;
        this.end = end;
        this.codiceud = codiceud;
        this.gruppo = gruppo;
        this.nomestanza = nomestanza;
    }

    public Lezione(int id, int docente, String giorno, String start, String end, String codiceud, String ore, int gruppo, String nomestanza) {
        this.ore = ore;
        this.id = id;
        this.docente = new ArrayList<>();
        this.docente.add(docente);
        this.giorno = giorno;
        this.start = start;
        this.end = end;
        this.codiceud = codiceud;
        this.gruppo = gruppo;
        this.nomestanza = nomestanza;
    }

    public Lezione() {
    }

    public int getMpid_modello() {
        return mpid_modello;
    }

    public void setMpid_modello(int mpid_modello) {
        this.mpid_modello = mpid_modello;
    }

    public String getNomestanza() {
        return nomestanza;
    }

    public void setNomestanza(String nomestanza) {
        this.nomestanza = nomestanza;
    }

    public int getGruppo() {
        return gruppo;
    }

    public void setGruppo(int gruppo) {
        this.gruppo = gruppo;
    }

    public String getOre() {
        return ore;
    }

    public void setOre(String ore) {
        this.ore = ore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getDocente() {
        return docente;
    }

    public void setDocente(List<Integer> docente) {
        this.docente = docente;
    }

    public String getGiorno() {
        return giorno;
    }

    public void setGiorno(String giorno) {
        this.giorno = giorno;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getCodiceud() {
        return codiceud;
    }

    public void setCodiceud(String codiceud) {
        this.codiceud = codiceud;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, JSON_STYLE);
    }

}