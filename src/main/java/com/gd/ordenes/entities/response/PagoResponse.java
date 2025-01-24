package com.gd.ordenes.entities.response;

import lombok.Data;

@Data
public class PagoResponse {
 private long idPago;
 private String idPagoStripe;
 private String descripcion;
 private Long monto;
}
