package com.gd.ordenes.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gd.ordenes.entities.Producto;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ProductoRepository {

    private final DynamoDbTable<Producto> productoTable;

    public ProductoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.productoTable = enhancedClient.table("Producto", TableSchema.fromBean(Producto.class));
    }

    public Optional<Producto> findById(long id) {
        return Optional.ofNullable(productoTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

}