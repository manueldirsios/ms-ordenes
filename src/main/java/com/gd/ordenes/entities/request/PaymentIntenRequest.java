package com.gd.ordenes.entities.request;

import java.util.List;

import lombok.Data;

@Data
public class PaymentIntenRequest {
	  public enum Currency{
	        USD, EUR,MXN;
	    }
	  	private List<ProductoPagoRequest> listArticulos;
	    private String description;
	    private int amount;
	    private Currency currency;

}
