package com.imax.app.models;

public class FormaPagoModel {
    public static final String ID_FORMA_PAGO_CREDITO = "002";
    private String idFormaPago;
    private String descripcion;

    public String getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(String idFormaPago) {
        this.idFormaPago = idFormaPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
