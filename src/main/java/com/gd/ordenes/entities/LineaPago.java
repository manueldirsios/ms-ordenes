package com.gd.ordenes.entities;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class LineaPago {

	private Long id;
    private Long pagoId;
    private Long productoId;
    private long cantidad;
    private double subtotal;
    private Producto producto;
    @DynamoDbPartitionKey
    public Long getId() {
        return id;
    }

}