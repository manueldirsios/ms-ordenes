package com.gd.ordenes.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gd.ordenes.entities.Pago;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class PagoRepository {

    private final DynamoDbTable<Pago> pagoTable;

    public PagoRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.pagoTable = enhancedClient.table("Pago", TableSchema.fromBean(Pago.class));
    }

    public void save(Pago pago) {
        pagoTable.putItem(pago);
    }

    public  Optional<Pago> findById(Long id) {
        return Optional.ofNullable(pagoTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

    public void deleteById(String id) {
        pagoTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public List<Pago> findAll() {
        return pagoTable.scan().items().stream().toList();
    }
    
    public List<Pago> findByReferenciaPago(String referenciaPago) {
        // Ejecutar la consulta en el índice secundario
        SdkIterable<Page<Pago>> results = pagoTable.index("ReferenciaPagoIndex")
                .query(QueryConditional.keyEqualTo(k -> k.partitionValue(referenciaPago)));

        // Procesar las páginas y extraer los elementos
        List<Pago> pagos = new ArrayList<>();
        results.forEach(page -> pagos.addAll(page.items()));

        return pagos;
    }
}