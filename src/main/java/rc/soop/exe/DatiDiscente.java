/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

/**
 *
 * @author rcosco
 */
public class DatiDiscente {


    String username, pivacf, cf, protocollo, decreto, datadecreto;

    public DatiDiscente() {
    }

    public DatiDiscente(String username, String pivacf, String cf, String protocollo, String decreto, String datadecreto) {
        this.username = username;
        this.pivacf = pivacf;
        this.cf = cf;
        this.protocollo = protocollo;
        this.decreto = decreto;
        this.datadecreto = datadecreto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPivacf() {
        return pivacf;
    }

    public void setPivacf(String pivacf) {
        this.pivacf = pivacf;
    }

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }

    public String getProtocollo() {
        return protocollo;
    }

    public void setProtocollo(String protocollo) {
        this.protocollo = protocollo;
    }

    public String getDecreto() {
        return decreto;
    }

    public void setDecreto(String decreto) {
        this.decreto = decreto;
    }

    public String getDatadecreto() {
        return datadecreto;
    }

    public void setDatadecreto(String datadecreto) {
        this.datadecreto = datadecreto;
    }


}
