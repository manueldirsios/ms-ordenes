package com.gd.ordenes.entities;



import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class Producto {

    private long id;

    private String nombre;

    private String descripcion;

    private Double precio;

    private Double precioFinal;

    private String imagen;

    private String sku;

    private int descuento;

    private boolean promocion;

    private String categoria;

    private int stock;
    
    @DynamoDbPartitionKey
    public long getId() {
        return id;
    }
}