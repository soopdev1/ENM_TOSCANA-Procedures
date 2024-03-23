/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.soop.exe;

/**
 *
 * @author Administrator
 */
public class UD {
    String descrizione;
    double ore;
    String fase, moduli;

    public UD(String descrizione, double ore, String fase, String moduli) {
        this.descrizione = descrizione;
        this.ore = ore;
        this.fase = fase;
        this.moduli = moduli;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public double getOre() {
        return ore;
    }

    public void setOre(double ore) {
        this.ore = ore;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public String getModuli() {
        return moduli;
    }

    public void setModuli(String moduli) {
        this.moduli = moduli;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UD{");
        sb.append("descrizione=").append(descrizione);
        sb.append(", ore=").append(ore);
        sb.append(", fase=").append(fase);
        sb.append(", moduli=").append(moduli);
        sb.append('}');
        return sb.toString();
    }
}
