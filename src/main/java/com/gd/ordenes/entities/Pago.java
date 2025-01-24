package com.gd.ordenes.entities;

import java.util.List;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;


@Data
@DynamoDbBean
public class Pago {
	private Long id;
	private String fechaPago;
	private String fechaModificacion;
	private String estatusPago;
	private double importeTotal;
	private String referenciaPago;
	private List<LineaPago> lineasPago;
	private long idFactura;
	private String descripcionPago;

	@DynamoDbPartitionKey
	public Long getId() {
		return id;
	}

}
