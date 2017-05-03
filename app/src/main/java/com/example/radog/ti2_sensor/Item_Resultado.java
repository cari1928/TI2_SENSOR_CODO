package com.example.radog.ti2_sensor;

/**
 * Created by radog on 01/05/2017.
 */

public class Item_Resultado {

    int repe;
    String efi;
    String dato;

    public Item_Resultado(int repe, String efi, String dato) {
        this.repe = repe;
        this.efi = efi;
        this.dato = dato;
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    public int getRepe() {
        return repe;
    }

    public void setRepe(int repe) {
        this.repe = repe;
    }

    public String getEfi() {
        return efi;
    }

    public void setEfi(String efi) {
        this.efi = efi;
    }
}
