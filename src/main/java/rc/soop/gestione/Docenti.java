/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.gestione;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
/**
 *
 * @author smo
 */
public class Docenti{

    private Long id;
    private String nome;
    private String cognome;
    private String codicefiscale;
    private Date datanascita;
    private String curriculum;
    private String docId;
    private String richiesta_accr;
    private String stato;
    private Date scadenza_doc;
    private String email;
    private Date datawebinair;
    private String pec;
    private String cellulare;
    private String regione_di_residenza;
    private String comune_di_nascita;
    private int titolo_di_studio;
    private int area_prevalente_di_qualificazione;
    private int inquadramento;
    private String motivo;
    private String tipo_inserimento;

    public Docenti(String nome, String cognome, String codicefiscale, Date datanascita) {
        this.nome = nome;
        this.cognome = cognome;
        this.codicefiscale = codicefiscale;
        this.datanascita = datanascita;
    }

    public Docenti(String nome, String cognome, String codicefiscale, Date datanascita, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.codicefiscale = codicefiscale;
        this.datanascita = datanascita;
        this.email = email;
    }

    public Docenti(Long id, String nome, String cognome, String codicefiscale, Date datanascita, String curriculum, String docId, String stato) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.codicefiscale = codicefiscale;
        this.datanascita = datanascita;
        this.curriculum = curriculum;
        this.docId = docId;
        this.stato = stato;
    }

    public Docenti() {
    }

    public String getDescrizionestato() {
        if (null == this.stato) {
            return "";
        } else {
            switch (this.stato) {
                case "A" -> {
                    return "ACCREDITATO";
                }
                case "DV" -> {
                    return "DA VALIDARE";
                }
                case "W" -> {
                    return "IN ATTESA WEBINAIR";
                }
                case "R" -> {
                    return "RIGETTATO";
                }
                default -> {
                }
            }
        }
        return "";
    }

    public Date getDatawebinair() {
        return datawebinair;
    }

    public void setDatawebinair(Date datawebinair) {
        this.datawebinair = datawebinair;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getRichiesta_accr() {
        return richiesta_accr;
    }

    public void setRichiesta_accr(String richiesta_accr) {
        this.richiesta_accr = richiesta_accr;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCodicefiscale() {
        return codicefiscale;
    }

    public void setCodicefiscale(String codicefiscale) {
        this.codicefiscale = codicefiscale;
    }

    public Date getDatanascita() {
        return datanascita;
    }

    public void setDatanascita(Date datanascita) {
        this.datanascita = datanascita;
    }

    public String getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(String curriculum) {
        this.curriculum = curriculum;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Date getScadenza_doc() {
        return scadenza_doc;
    }

    public void setScadenza_doc(Date scadenza_doc) {
        this.scadenza_doc = scadenza_doc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPec() {
        return pec;
    }

    public void setPec(String pec) {
        this.pec = pec;
    }

    public String getCellulare() {
        return cellulare;
    }

    public void setCellulare(String cellulare) {
        this.cellulare = cellulare;
    }

    public String getRegione_di_residenza() {
        return regione_di_residenza;
    }

    public void setRegione_di_residenza(String regione_di_residenza) {
        this.regione_di_residenza = regione_di_residenza;
    }

    public String getComune_di_nascita() {
        return comune_di_nascita;
    }

    public void setComune_di_nascita(String comune_di_nascita) {
        this.comune_di_nascita = comune_di_nascita;
    }

    public int getTitolo_di_studio() {
        return titolo_di_studio;
    }

    public void setTitolo_di_studio(int titolo_di_studio) {
        this.titolo_di_studio = titolo_di_studio;
    }

    public int getArea_prevalente_di_qualificazione() {
        return area_prevalente_di_qualificazione;
    }

    public void setArea_prevalente_di_qualificazione(int area_prevalente_di_qualificazione) {
        this.area_prevalente_di_qualificazione = area_prevalente_di_qualificazione;
    }

    public int getInquadramento() {
        return inquadramento;
    }

    public void setInquadramento(int inquadramento) {
        this.inquadramento = inquadramento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getTipo_inserimento() {
        return tipo_inserimento;
    }

    public void setTipo_inserimento(String tipo_inserimento) {
        this.tipo_inserimento = tipo_inserimento;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final Docenti other = (Docenti) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Docenti{id=").append(id);
        sb.append(", nome=").append(nome);
        sb.append(", cognome=").append(cognome);
        sb.append(", codicefiscale=").append(codicefiscale);
        sb.append(", datanascita=").append(datanascita);
        sb.append(", curriculum=").append(curriculum);
        sb.append(", docId=").append(docId);
        sb.append(", richiesta_accr=").append(richiesta_accr);
        sb.append(", stato=").append(stato);
        sb.append(", scadenza_doc=").append(scadenza_doc);
        sb.append(", email=").append(email);
        sb.append(", datawebinair=").append(datawebinair);
        sb.append(", pec=").append(pec);
        sb.append(", cellulare=").append(cellulare);
        sb.append(", regione_di_residenza=").append(regione_di_residenza);
        sb.append(", comune_di_nascita=").append(comune_di_nascita);
        sb.append(", titolo_di_studio=").append(titolo_di_studio);
        sb.append(", area_prevalente_di_qualificazione=").append(area_prevalente_di_qualificazione);
        sb.append(", inquadramento=").append(inquadramento);
        sb.append(", motivo=").append(motivo);
        sb.append('}');
        return sb.toString();
    }

}
