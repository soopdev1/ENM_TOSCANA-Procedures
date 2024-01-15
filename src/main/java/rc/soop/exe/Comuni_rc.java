/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.soop.exe;

import static rc.soop.exe.Utils.cp_toUTF;


/**
 *
 * @author rcosco
 */
public class Comuni_rc {

    int id;
    String codicefiscale, nome, codiceregione, regione, codiceprovincia, provincia;

    public Comuni_rc(int id, String codicefiscale, String nome, String codiceregione, String regione, String codiceprovincia, String provincia) {
        this.id = id;
        this.codicefiscale = codicefiscale;
        this.nome = cp_toUTF(nome);
        this.codiceregione = codiceregione;
        this.regione = cp_toUTF(regione);
        this.codiceprovincia = codiceprovincia;
        this.provincia = cp_toUTF(provincia);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodicefiscale() {
        return codicefiscale;
    }

    public void setCodicefiscale(String codicefiscale) {
        this.codicefiscale = codicefiscale;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodiceregione() {
        return codiceregione;
    }

    public void setCodiceregione(String codiceregione) {
        this.codiceregione = codiceregione;
    }

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    public String getCodiceprovincia() {
        return codiceprovincia;
    }

    public void setCodiceprovincia(String codiceprovincia) {
        this.codiceprovincia = codiceprovincia;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

}
