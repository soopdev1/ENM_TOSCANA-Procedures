/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import static rc.soop.exe.Utils.setEmptyString;

/**
 *
 * @author rcosco
 */
public class ExcelDomande {

    String USERNAME,
            CODICEDOMANDA, DATACONSEGNA, ORACONSEGNA, RAGIONESOCIALE, PIVA, SEDELEGALEINDIRIZZO, SEDELEGALECAP, SEDELEGALECOMUNE, SEDELEGALEPROVINCIA, SEDELEGALEREGIONE, PEC, EMAIL, TELEFONO, NPROTOCOLLO,
            NSEDI,
            SEDE1INDIRIZZO, SEDE1COMUNE, SEDE1PROVINCIA, SEDE1REGIONE, SEDE1TITOLODISP, SEDE1MQ, SEDE1ACCRREG,
            SEDE2INDIRIZZO, SEDE2COMUNE, SEDE2PROVINCIA, SEDE2REGIONE, SEDE2TITOLODISP, SEDE2MQ, SEDE2ACCRREG,
            SEDE3INDIRIZZO, SEDE3COMUNE, SEDE3PROVINCIA, SEDE3REGIONE, SEDE3TITOLODISP, SEDE3MQ, SEDE3ACCRREG,
            SEDE4INDIRIZZO, SEDE4COMUNE, SEDE4PROVINCIA, SEDE4REGIONE, SEDE4TITOLODISP, SEDE4MQ, SEDE4ACCRREG,
            SEDE5INDIRIZZO, SEDE5COMUNE, SEDE5PROVINCIA, SEDE5REGIONE, SEDE5TITOLODISP, SEDE5MQ, SEDE5ACCRREG,
            NDOCENTI,
            NOMEDOCENTE1, COGNOMEDOCENTE1, CFDOCENTE1, FASCIAPROPOSTADOCENTE1,
            NOMEDOCENTE2, COGNOMEDOCENTE2, CFDOCENTE2, FASCIAPROPOSTADOCENTE2,
            NOMEDOCENTE3, COGNOMEDOCENTE3, CFDOCENTE3, FASCIAPROPOSTADOCENTE3,
            NOMEDOCENTE4, COGNOMEDOCENTE4, CFDOCENTE4, FASCIAPROPOSTADOCENTE4,
            NOMEDOCENTE5, COGNOMEDOCENTE5, CFDOCENTE5, FASCIAPROPOSTADOCENTE5;
    
    String STATODOMANDA;
    String CODICEACCREDITAMENTO;
    String TIPOLOGIASOGGETTO;
    
    public ExcelDomande() {
        setEmptyString(this);
    }

    public ExcelDomande(String USERNAME, String CODICEDOMANDA, String DATACONSEGNA, String ORACONSEGNA, String RAGIONESOCIALE, String PIVA, String SEDELEGALEINDIRIZZO, String SEDELEGALECAP, String SEDELEGALECOMUNE, String SEDELEGALEPROVINCIA, String SEDELEGALEREGIONE, String PEC, String EMAIL, String TELEFONO, String NPROTOCOLLO, String NSEDI, String SEDE1INDIRIZZO, String SEDE1COMUNE, String SEDE1PROVINCIA, String SEDE1REGIONE, String SEDE1TITOLODISP, String SEDE1MQ, String SEDE2INDIRIZZO, String SEDE2COMUNE, String SEDE2PROVINCIA, String SEDE2REGIONE, String SEDE2TITOLODISP, String SEDE2MQ, String SEDE3INDIRIZZO, String SEDE3COMUNE, String SEDE3PROVINCIA, String SEDE3REGIONE, String SEDE3TITOLODISP, String SEDE3MQ, String SEDE4INDIRIZZO, String SEDE4COMUNE, String SEDE4PROVINCIA, String SEDE4REGIONE, String SEDE4TITOLODISP, String SEDE4MQ, String SEDE5INDIRIZZO, String SEDE5COMUNE, String SEDE5PROVINCIA, String SEDE5REGIONE, String SEDE5TITOLODISP, String SEDE5MQ, String NDOCENTI, String NOMEDOCENTE1, String COGNOMEDOCENTE1, String CFDOCENTE1, String FASCIAPROPOSTADOCENTE1, String NOMEDOCENTE2, String COGNOMEDOCENTE2, String CFDOCENTE2, String FASCIAPROPOSTADOCENTE2, String NOMEDOCENTE3, String COGNOMEDOCENTE3, String CFDOCENTE3, String FASCIAPROPOSTADOCENTE3, String NOMEDOCENTE4, String COGNOMEDOCENTE4, String CFDOCENTE4, String FASCIAPROPOSTADOCENTE4, String NOMEDOCENTE5, String COGNOMEDOCENTE5, String CFDOCENTE5, String FASCIAPROPOSTADOCENTE5) {
        this.USERNAME = USERNAME;
        this.CODICEDOMANDA = CODICEDOMANDA;
        this.DATACONSEGNA = DATACONSEGNA;
        this.ORACONSEGNA = ORACONSEGNA;
        this.RAGIONESOCIALE = RAGIONESOCIALE;
        this.PIVA = PIVA;
        this.SEDELEGALEINDIRIZZO = SEDELEGALEINDIRIZZO;
        this.SEDELEGALECAP = SEDELEGALECAP;
        this.SEDELEGALECOMUNE = SEDELEGALECOMUNE;
        this.SEDELEGALEPROVINCIA = SEDELEGALEPROVINCIA;
        this.SEDELEGALEREGIONE = SEDELEGALEREGIONE;
        this.PEC = PEC;
        this.EMAIL = EMAIL;
        this.TELEFONO = TELEFONO;
        this.NPROTOCOLLO = NPROTOCOLLO;
        this.NSEDI = NSEDI;
        this.SEDE1INDIRIZZO = SEDE1INDIRIZZO;
        this.SEDE1COMUNE = SEDE1COMUNE;
        this.SEDE1PROVINCIA = SEDE1PROVINCIA;
        this.SEDE1REGIONE = SEDE1REGIONE;
        this.SEDE1TITOLODISP = SEDE1TITOLODISP;
        this.SEDE1MQ = SEDE1MQ;
        this.SEDE2INDIRIZZO = SEDE2INDIRIZZO;
        this.SEDE2COMUNE = SEDE2COMUNE;
        this.SEDE2PROVINCIA = SEDE2PROVINCIA;
        this.SEDE2REGIONE = SEDE2REGIONE;
        this.SEDE2TITOLODISP = SEDE2TITOLODISP;
        this.SEDE2MQ = SEDE2MQ;
        this.SEDE3INDIRIZZO = SEDE3INDIRIZZO;
        this.SEDE3COMUNE = SEDE3COMUNE;
        this.SEDE3PROVINCIA = SEDE3PROVINCIA;
        this.SEDE3REGIONE = SEDE3REGIONE;
        this.SEDE3TITOLODISP = SEDE3TITOLODISP;
        this.SEDE3MQ = SEDE3MQ;
        this.SEDE4INDIRIZZO = SEDE4INDIRIZZO;
        this.SEDE4COMUNE = SEDE4COMUNE;
        this.SEDE4PROVINCIA = SEDE4PROVINCIA;
        this.SEDE4REGIONE = SEDE4REGIONE;
        this.SEDE4TITOLODISP = SEDE4TITOLODISP;
        this.SEDE4MQ = SEDE4MQ;
        this.SEDE5INDIRIZZO = SEDE5INDIRIZZO;
        this.SEDE5COMUNE = SEDE5COMUNE;
        this.SEDE5PROVINCIA = SEDE5PROVINCIA;
        this.SEDE5REGIONE = SEDE5REGIONE;
        this.SEDE5TITOLODISP = SEDE5TITOLODISP;
        this.SEDE5MQ = SEDE5MQ;
        this.NDOCENTI = NDOCENTI;
        this.NOMEDOCENTE1 = NOMEDOCENTE1;
        this.COGNOMEDOCENTE1 = COGNOMEDOCENTE1;
        this.CFDOCENTE1 = CFDOCENTE1;
        this.FASCIAPROPOSTADOCENTE1 = FASCIAPROPOSTADOCENTE1;
        this.NOMEDOCENTE2 = NOMEDOCENTE2;
        this.COGNOMEDOCENTE2 = COGNOMEDOCENTE2;
        this.CFDOCENTE2 = CFDOCENTE2;
        this.FASCIAPROPOSTADOCENTE2 = FASCIAPROPOSTADOCENTE2;
        this.NOMEDOCENTE3 = NOMEDOCENTE3;
        this.COGNOMEDOCENTE3 = COGNOMEDOCENTE3;
        this.CFDOCENTE3 = CFDOCENTE3;
        this.FASCIAPROPOSTADOCENTE3 = FASCIAPROPOSTADOCENTE3;
        this.NOMEDOCENTE4 = NOMEDOCENTE4;
        this.COGNOMEDOCENTE4 = COGNOMEDOCENTE4;
        this.CFDOCENTE4 = CFDOCENTE4;
        this.FASCIAPROPOSTADOCENTE4 = FASCIAPROPOSTADOCENTE4;
        this.NOMEDOCENTE5 = NOMEDOCENTE5;
        this.COGNOMEDOCENTE5 = COGNOMEDOCENTE5;
        this.CFDOCENTE5 = CFDOCENTE5;
        this.FASCIAPROPOSTADOCENTE5 = FASCIAPROPOSTADOCENTE5;
    }

    public String getSEDE1ACCRREG() {
        return SEDE1ACCRREG;
    }

    public void setSEDE1ACCRREG(String SEDE1ACCRREG) {
        this.SEDE1ACCRREG = SEDE1ACCRREG;
    }

    public String getSEDE2ACCRREG() {
        return SEDE2ACCRREG;
    }

    public void setSEDE2ACCRREG(String SEDE2ACCRREG) {
        this.SEDE2ACCRREG = SEDE2ACCRREG;
    }

    public String getSEDE3ACCRREG() {
        return SEDE3ACCRREG;
    }

    public void setSEDE3ACCRREG(String SEDE3ACCRREG) {
        this.SEDE3ACCRREG = SEDE3ACCRREG;
    }

    public String getSEDE4ACCRREG() {
        return SEDE4ACCRREG;
    }

    public void setSEDE4ACCRREG(String SEDE4ACCRREG) {
        this.SEDE4ACCRREG = SEDE4ACCRREG;
    }

    public String getSEDE5ACCRREG() {
        return SEDE5ACCRREG;
    }

    public void setSEDE5ACCRREG(String SEDE5ACCRREG) {
        this.SEDE5ACCRREG = SEDE5ACCRREG;
    }
    
    

    public String getTIPOLOGIASOGGETTO() {
        return TIPOLOGIASOGGETTO;
    }

    public void setTIPOLOGIASOGGETTO(String TIPOLOGIASOGGETTO) {
        this.TIPOLOGIASOGGETTO = TIPOLOGIASOGGETTO;
    }
    
    public String getCODICEACCREDITAMENTO() {
        return CODICEACCREDITAMENTO;
    }

    public void setCODICEACCREDITAMENTO(String CODICEACCREDITAMENTO) {
        this.CODICEACCREDITAMENTO = CODICEACCREDITAMENTO;
    }

    public String getSTATODOMANDA() {
        return STATODOMANDA;
    }

    public void setSTATODOMANDA(String STATODOMANDA) {
        this.STATODOMANDA = STATODOMANDA;
    }
    
    

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getCODICEDOMANDA() {
        return CODICEDOMANDA;
    }

    public void setCODICEDOMANDA(String CODICEDOMANDA) {
        this.CODICEDOMANDA = CODICEDOMANDA;
    }

    public String getDATACONSEGNA() {
        return DATACONSEGNA;
    }

    public void setDATACONSEGNA(String DATACONSEGNA) {
        this.DATACONSEGNA = DATACONSEGNA;
    }

    public String getORACONSEGNA() {
        return ORACONSEGNA;
    }

    public void setORACONSEGNA(String ORACONSEGNA) {
        this.ORACONSEGNA = ORACONSEGNA;
    }

    public String getRAGIONESOCIALE() {
        return RAGIONESOCIALE;
    }

    public void setRAGIONESOCIALE(String RAGIONESOCIALE) {
        this.RAGIONESOCIALE = RAGIONESOCIALE;
    }

    public String getPIVA() {
        return PIVA;
    }

    public void setPIVA(String PIVA) {
        this.PIVA = PIVA;
    }

    public String getSEDELEGALEINDIRIZZO() {
        return SEDELEGALEINDIRIZZO;
    }

    public void setSEDELEGALEINDIRIZZO(String SEDELEGALEINDIRIZZO) {
        this.SEDELEGALEINDIRIZZO = SEDELEGALEINDIRIZZO;
    }

    public String getSEDELEGALECAP() {
        return SEDELEGALECAP;
    }

    public void setSEDELEGALECAP(String SEDELEGALECAP) {
        this.SEDELEGALECAP = SEDELEGALECAP;
    }

    public String getSEDELEGALECOMUNE() {
        return SEDELEGALECOMUNE;
    }

    public void setSEDELEGALECOMUNE(String SEDELEGALECOMUNE) {
        this.SEDELEGALECOMUNE = SEDELEGALECOMUNE;
    }

    public String getSEDELEGALEPROVINCIA() {
        return SEDELEGALEPROVINCIA;
    }

    public void setSEDELEGALEPROVINCIA(String SEDELEGALEPROVINCIA) {
        this.SEDELEGALEPROVINCIA = SEDELEGALEPROVINCIA;
    }

    public String getSEDELEGALEREGIONE() {
        return SEDELEGALEREGIONE;
    }

    public void setSEDELEGALEREGIONE(String SEDELEGALEREGIONE) {
        this.SEDELEGALEREGIONE = SEDELEGALEREGIONE;
    }

    public String getPEC() {
        return PEC;
    }

    public void setPEC(String PEC) {
        this.PEC = PEC;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getTELEFONO() {
        return TELEFONO;
    }

    public void setTELEFONO(String TELEFONO) {
        this.TELEFONO = TELEFONO;
    }

    public String getNPROTOCOLLO() {
        return NPROTOCOLLO;
    }

    public void setNPROTOCOLLO(String NPROTOCOLLO) {
        this.NPROTOCOLLO = NPROTOCOLLO;
    }

    public String getNSEDI() {
        return NSEDI;
    }

    public void setNSEDI(String NSEDI) {
        this.NSEDI = NSEDI;
    }

    public String getSEDE1INDIRIZZO() {
        return SEDE1INDIRIZZO;
    }

    public void setSEDE1INDIRIZZO(String SEDE1INDIRIZZO) {
        this.SEDE1INDIRIZZO = SEDE1INDIRIZZO;
    }

    public String getSEDE1COMUNE() {
        return SEDE1COMUNE;
    }

    public void setSEDE1COMUNE(String SEDE1COMUNE) {
        this.SEDE1COMUNE = SEDE1COMUNE;
    }

    public String getSEDE1PROVINCIA() {
        return SEDE1PROVINCIA;
    }

    public void setSEDE1PROVINCIA(String SEDE1PROVINCIA) {
        this.SEDE1PROVINCIA = SEDE1PROVINCIA;
    }

    public String getSEDE1REGIONE() {
        return SEDE1REGIONE;
    }

    public void setSEDE1REGIONE(String SEDE1REGIONE) {
        this.SEDE1REGIONE = SEDE1REGIONE;
    }

    public String getSEDE1TITOLODISP() {
        return SEDE1TITOLODISP;
    }

    public void setSEDE1TITOLODISP(String SEDE1TITOLODISP) {
        this.SEDE1TITOLODISP = SEDE1TITOLODISP;
    }

    public String getSEDE1MQ() {
        return SEDE1MQ;
    }

    public void setSEDE1MQ(String SEDE1MQ) {
        this.SEDE1MQ = SEDE1MQ;
    }

    public String getSEDE2INDIRIZZO() {
        return SEDE2INDIRIZZO;
    }

    public void setSEDE2INDIRIZZO(String SEDE2INDIRIZZO) {
        this.SEDE2INDIRIZZO = SEDE2INDIRIZZO;
    }

    public String getSEDE2COMUNE() {
        return SEDE2COMUNE;
    }

    public void setSEDE2COMUNE(String SEDE2COMUNE) {
        this.SEDE2COMUNE = SEDE2COMUNE;
    }

    public String getSEDE2PROVINCIA() {
        return SEDE2PROVINCIA;
    }

    public void setSEDE2PROVINCIA(String SEDE2PROVINCIA) {
        this.SEDE2PROVINCIA = SEDE2PROVINCIA;
    }

    public String getSEDE2REGIONE() {
        return SEDE2REGIONE;
    }

    public void setSEDE2REGIONE(String SEDE2REGIONE) {
        this.SEDE2REGIONE = SEDE2REGIONE;
    }

    public String getSEDE2TITOLODISP() {
        return SEDE2TITOLODISP;
    }

    public void setSEDE2TITOLODISP(String SEDE2TITOLODISP) {
        this.SEDE2TITOLODISP = SEDE2TITOLODISP;
    }

    public String getSEDE2MQ() {
        return SEDE2MQ;
    }

    public void setSEDE2MQ(String SEDE2MQ) {
        this.SEDE2MQ = SEDE2MQ;
    }

    public String getSEDE3INDIRIZZO() {
        return SEDE3INDIRIZZO;
    }

    public void setSEDE3INDIRIZZO(String SEDE3INDIRIZZO) {
        this.SEDE3INDIRIZZO = SEDE3INDIRIZZO;
    }

    public String getSEDE3COMUNE() {
        return SEDE3COMUNE;
    }

    public void setSEDE3COMUNE(String SEDE3COMUNE) {
        this.SEDE3COMUNE = SEDE3COMUNE;
    }

    public String getSEDE3PROVINCIA() {
        return SEDE3PROVINCIA;
    }

    public void setSEDE3PROVINCIA(String SEDE3PROVINCIA) {
        this.SEDE3PROVINCIA = SEDE3PROVINCIA;
    }

    public String getSEDE3REGIONE() {
        return SEDE3REGIONE;
    }

    public void setSEDE3REGIONE(String SEDE3REGIONE) {
        this.SEDE3REGIONE = SEDE3REGIONE;
    }

    public String getSEDE3TITOLODISP() {
        return SEDE3TITOLODISP;
    }

    public void setSEDE3TITOLODISP(String SEDE3TITOLODISP) {
        this.SEDE3TITOLODISP = SEDE3TITOLODISP;
    }

    public String getSEDE3MQ() {
        return SEDE3MQ;
    }

    public void setSEDE3MQ(String SEDE3MQ) {
        this.SEDE3MQ = SEDE3MQ;
    }

    public String getSEDE4INDIRIZZO() {
        return SEDE4INDIRIZZO;
    }

    public void setSEDE4INDIRIZZO(String SEDE4INDIRIZZO) {
        this.SEDE4INDIRIZZO = SEDE4INDIRIZZO;
    }

    public String getSEDE4COMUNE() {
        return SEDE4COMUNE;
    }

    public void setSEDE4COMUNE(String SEDE4COMUNE) {
        this.SEDE4COMUNE = SEDE4COMUNE;
    }

    public String getSEDE4PROVINCIA() {
        return SEDE4PROVINCIA;
    }

    public void setSEDE4PROVINCIA(String SEDE4PROVINCIA) {
        this.SEDE4PROVINCIA = SEDE4PROVINCIA;
    }

    public String getSEDE4REGIONE() {
        return SEDE4REGIONE;
    }

    public void setSEDE4REGIONE(String SEDE4REGIONE) {
        this.SEDE4REGIONE = SEDE4REGIONE;
    }

    public String getSEDE4TITOLODISP() {
        return SEDE4TITOLODISP;
    }

    public void setSEDE4TITOLODISP(String SEDE4TITOLODISP) {
        this.SEDE4TITOLODISP = SEDE4TITOLODISP;
    }

    public String getSEDE4MQ() {
        return SEDE4MQ;
    }

    public void setSEDE4MQ(String SEDE4MQ) {
        this.SEDE4MQ = SEDE4MQ;
    }

    public String getSEDE5INDIRIZZO() {
        return SEDE5INDIRIZZO;
    }

    public void setSEDE5INDIRIZZO(String SEDE5INDIRIZZO) {
        this.SEDE5INDIRIZZO = SEDE5INDIRIZZO;
    }

    public String getSEDE5COMUNE() {
        return SEDE5COMUNE;
    }

    public void setSEDE5COMUNE(String SEDE5COMUNE) {
        this.SEDE5COMUNE = SEDE5COMUNE;
    }

    public String getSEDE5PROVINCIA() {
        return SEDE5PROVINCIA;
    }

    public void setSEDE5PROVINCIA(String SEDE5PROVINCIA) {
        this.SEDE5PROVINCIA = SEDE5PROVINCIA;
    }

    public String getSEDE5REGIONE() {
        return SEDE5REGIONE;
    }

    public void setSEDE5REGIONE(String SEDE5REGIONE) {
        this.SEDE5REGIONE = SEDE5REGIONE;
    }

    public String getSEDE5TITOLODISP() {
        return SEDE5TITOLODISP;
    }

    public void setSEDE5TITOLODISP(String SEDE5TITOLODISP) {
        this.SEDE5TITOLODISP = SEDE5TITOLODISP;
    }

    public String getSEDE5MQ() {
        return SEDE5MQ;
    }

    public void setSEDE5MQ(String SEDE5MQ) {
        this.SEDE5MQ = SEDE5MQ;
    }

    public String getNDOCENTI() {
        return NDOCENTI;
    }

    public void setNDOCENTI(String NDOCENTI) {
        this.NDOCENTI = NDOCENTI;
    }

    public String getNOMEDOCENTE1() {
        return NOMEDOCENTE1;
    }

    public void setNOMEDOCENTE1(String NOMEDOCENTE1) {
        this.NOMEDOCENTE1 = NOMEDOCENTE1;
    }

    public String getCOGNOMEDOCENTE1() {
        return COGNOMEDOCENTE1;
    }

    public void setCOGNOMEDOCENTE1(String COGNOMEDOCENTE1) {
        this.COGNOMEDOCENTE1 = COGNOMEDOCENTE1;
    }

    public String getCFDOCENTE1() {
        return CFDOCENTE1;
    }

    public void setCFDOCENTE1(String CFDOCENTE1) {
        this.CFDOCENTE1 = CFDOCENTE1;
    }

    public String getFASCIAPROPOSTADOCENTE1() {
        return FASCIAPROPOSTADOCENTE1;
    }

    public void setFASCIAPROPOSTADOCENTE1(String FASCIAPROPOSTADOCENTE1) {
        this.FASCIAPROPOSTADOCENTE1 = FASCIAPROPOSTADOCENTE1;
    }

    public String getNOMEDOCENTE2() {
        return NOMEDOCENTE2;
    }

    public void setNOMEDOCENTE2(String NOMEDOCENTE2) {
        this.NOMEDOCENTE2 = NOMEDOCENTE2;
    }

    public String getCOGNOMEDOCENTE2() {
        return COGNOMEDOCENTE2;
    }

    public void setCOGNOMEDOCENTE2(String COGNOMEDOCENTE2) {
        this.COGNOMEDOCENTE2 = COGNOMEDOCENTE2;
    }

    public String getCFDOCENTE2() {
        return CFDOCENTE2;
    }

    public void setCFDOCENTE2(String CFDOCENTE2) {
        this.CFDOCENTE2 = CFDOCENTE2;
    }

    public String getFASCIAPROPOSTADOCENTE2() {
        return FASCIAPROPOSTADOCENTE2;
    }

    public void setFASCIAPROPOSTADOCENTE2(String FASCIAPROPOSTADOCENTE2) {
        this.FASCIAPROPOSTADOCENTE2 = FASCIAPROPOSTADOCENTE2;
    }

    public String getNOMEDOCENTE3() {
        return NOMEDOCENTE3;
    }

    public void setNOMEDOCENTE3(String NOMEDOCENTE3) {
        this.NOMEDOCENTE3 = NOMEDOCENTE3;
    }

    public String getCOGNOMEDOCENTE3() {
        return COGNOMEDOCENTE3;
    }

    public void setCOGNOMEDOCENTE3(String COGNOMEDOCENTE3) {
        this.COGNOMEDOCENTE3 = COGNOMEDOCENTE3;
    }

    public String getCFDOCENTE3() {
        return CFDOCENTE3;
    }

    public void setCFDOCENTE3(String CFDOCENTE3) {
        this.CFDOCENTE3 = CFDOCENTE3;
    }

    public String getFASCIAPROPOSTADOCENTE3() {
        return FASCIAPROPOSTADOCENTE3;
    }

    public void setFASCIAPROPOSTADOCENTE3(String FASCIAPROPOSTADOCENTE3) {
        this.FASCIAPROPOSTADOCENTE3 = FASCIAPROPOSTADOCENTE3;
    }

    public String getNOMEDOCENTE4() {
        return NOMEDOCENTE4;
    }

    public void setNOMEDOCENTE4(String NOMEDOCENTE4) {
        this.NOMEDOCENTE4 = NOMEDOCENTE4;
    }

    public String getCOGNOMEDOCENTE4() {
        return COGNOMEDOCENTE4;
    }

    public void setCOGNOMEDOCENTE4(String COGNOMEDOCENTE4) {
        this.COGNOMEDOCENTE4 = COGNOMEDOCENTE4;
    }

    public String getCFDOCENTE4() {
        return CFDOCENTE4;
    }

    public void setCFDOCENTE4(String CFDOCENTE4) {
        this.CFDOCENTE4 = CFDOCENTE4;
    }

    public String getFASCIAPROPOSTADOCENTE4() {
        return FASCIAPROPOSTADOCENTE4;
    }

    public void setFASCIAPROPOSTADOCENTE4(String FASCIAPROPOSTADOCENTE4) {
        this.FASCIAPROPOSTADOCENTE4 = FASCIAPROPOSTADOCENTE4;
    }

    public String getNOMEDOCENTE5() {
        return NOMEDOCENTE5;
    }

    public void setNOMEDOCENTE5(String NOMEDOCENTE5) {
        this.NOMEDOCENTE5 = NOMEDOCENTE5;
    }

    public String getCOGNOMEDOCENTE5() {
        return COGNOMEDOCENTE5;
    }

    public void setCOGNOMEDOCENTE5(String COGNOMEDOCENTE5) {
        this.COGNOMEDOCENTE5 = COGNOMEDOCENTE5;
    }

    public String getCFDOCENTE5() {
        return CFDOCENTE5;
    }

    public void setCFDOCENTE5(String CFDOCENTE5) {
        this.CFDOCENTE5 = CFDOCENTE5;
    }

    public String getFASCIAPROPOSTADOCENTE5() {
        return FASCIAPROPOSTADOCENTE5;
    }

    public void setFASCIAPROPOSTADOCENTE5(String FASCIAPROPOSTADOCENTE5) {
        this.FASCIAPROPOSTADOCENTE5 = FASCIAPROPOSTADOCENTE5;
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

}
