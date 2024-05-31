package com.jovisco.spring6restclient.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BeerDTO {
    private UUID id;
    private Integer version;
    private String name;
    private style style;
    private String upc;
    private Integer quantityOnHand;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}