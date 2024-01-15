/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.arti;

/**
 *
 * @author Administrator
 */
public class ResponseStatoIscrizione {
    String status,message , codiceAttivita,codiceFiscale;

    public ResponseStatoIscrizione() {
    }

    public ResponseStatoIscrizione(String status, String message, String codiceAttivita, String codiceFiscale) {
        this.status = status;
        this.message = message;
        this.codiceAttivita = codiceAttivita;
        this.codiceFiscale = codiceFiscale;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodiceAttivita() {
        return codiceAttivita;
    }

    public void setCodiceAttivita(String codiceAttivita) {
        this.codiceAttivita = codiceAttivita;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResponseStatoIscrizione{");
        sb.append("status=").append(status);
        sb.append(", message=").append(message);
        sb.append(", codiceAttivita=").append(codiceAttivita);
        sb.append(", codiceFiscale=").append(codiceFiscale);
        sb.append('}');
        return sb.toString();
    }
    
    
    
    
}
