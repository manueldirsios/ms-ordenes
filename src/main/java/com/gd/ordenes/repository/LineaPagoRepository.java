package com.gd.ordenes.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gd.ordenes.entities.LineaPago;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class LineaPagoRepository {

    private final DynamoDbTable<LineaPago> lineaPagoTable;

    public LineaPagoRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.lineaPagoTable = enhancedClient.table("LineaPago", TableSchema.fromBean(LineaPago.class));
    }

    public void save(LineaPago lineaPago) {
        lineaPagoTable.putItem(lineaPago);
    }

    public LineaPago findById(String id) {
        return lineaPagoTable.getItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public List<LineaPago> findByPagoId(long pagoId) {
    	 return lineaPagoTable
    	            .query(QueryEnhancedRequest.builder()
    	                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(pagoId)))
    	                .build())
    	            .items()
    	            .stream()
    	            .toList();
    	 }

    public void deleteById(String id) {
        lineaPagoTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }
}