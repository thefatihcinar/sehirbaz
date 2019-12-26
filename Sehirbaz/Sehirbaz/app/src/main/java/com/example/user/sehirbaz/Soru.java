package com.example.user.sehirbaz;

/**
 * Created by user on 12/16/2019.
 */

public class Soru {

    private String Resim;
    private String Sehir;

    Soru(String gelenResim, String gelenSehir){
        this.setResim(gelenResim);
        this.setSehir(gelenSehir);
    }

    Soru(){
        this.setResim("");
        this.setSehir("");
    }

    public void setResim(String gelenResim){
        this.Resim = gelenResim;
    }

    public void setSehir(String gelenSehir){
        this.Sehir = gelenSehir;
    }

    public String getResim(){
        return this.Resim;
    }

    public String getSehir(){
        return this.Sehir;
    }

}
