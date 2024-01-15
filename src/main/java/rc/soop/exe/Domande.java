/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author rcosco
 */
public class Domande {

    String Codicedomanda, Nome, Cognome, CodiceFiscale, RagioneSociale, PartitaIVA, PEC, Dataconsegna, Stato, SedeComune, SedeCap, Cellulare, DataNascita, Email, SedeIndirizzo,NumeroDocumento,ScadenzaDoc,CaricaSoc,Accreditato;
        
    public Domande() {
    }

    public String getAccreditato() {
        return Accreditato;
    }

    public void setAccreditato(String Accreditato) {
        this.Accreditato = Accreditato;
    }
    
    
    
    public String getCaricaSoc() {
        return CaricaSoc;
    }

    public void setCaricaSoc(String CaricaSoc) {
        this.CaricaSoc = CaricaSoc;
    }
    
    public String getScadenzaDoc() {
        return ScadenzaDoc;
    }

    public void setScadenzaDoc(String ScadenzaDoc) {
        this.ScadenzaDoc = ScadenzaDoc;
    }

    public String getNumeroDocumento() {
        return NumeroDocumento;
    }

    public void setNumeroDocumento(String NumeroDocumento) {
        this.NumeroDocumento = NumeroDocumento;
    }

    public String getSedeIndirizzo() {
        return SedeIndirizzo;
    }

    public void setSedeIndirizzo(String SedeIndirizzo) {
        this.SedeIndirizzo = SedeIndirizzo;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getCodicedomanda() {
        return Codicedomanda;
    }

    public void setCodicedomanda(String Codicedomanda) {
        this.Codicedomanda = Codicedomanda;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }

    public String getCognome() {
        return Cognome;
    }

    public void setCognome(String Cognome) {
        this.Cognome = Cognome;
    }

    public String getCodiceFiscale() {
        return CodiceFiscale;
    }

    public void setCodiceFiscale(String CodiceFiscale) {
        this.CodiceFiscale = CodiceFiscale;
    }

    public String getRagioneSociale() {
        return RagioneSociale;
    }

    public void setRagioneSociale(String RagioneSociale) {
        this.RagioneSociale = RagioneSociale;
    }

    public String getPartitaIVA() {
        return PartitaIVA;
    }

    public void setPartitaIVA(String PartitaIVA) {
        this.PartitaIVA = PartitaIVA;
    }

    public String getPEC() {
        return PEC;
    }

    public void setPEC(String PEC) {
        this.PEC = PEC;
    }

    public String getDataconsegna() {
        return Dataconsegna;
    }

    public void setDataconsegna(String Dataconsegna) {
        this.Dataconsegna = Dataconsegna;
    }

    public String getStato() {
        return Stato;
    }

    public void setStato(String Stato) {
        this.Stato = Stato;
    }

    public String getSedeComune() {
        return SedeComune;
    }

    public void setSedeComune(String SedeComune) {
        this.SedeComune = SedeComune;
    }

    public String getSedeCap() {
        return SedeCap;
    }

    public void setSedeCap(String SedeCap) {
        this.SedeCap = SedeCap;
    }

    public String getCellulare() {
        return Cellulare;
    }

    public void setCellulare(String Cellulare) {
        this.Cellulare = Cellulare;
    }

    public String getDataNascita() {
        return DataNascita;
    }

    public void setDataNascita(String DataNascita) {
        this.DataNascita = DataNascita;
    }
    
    
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
