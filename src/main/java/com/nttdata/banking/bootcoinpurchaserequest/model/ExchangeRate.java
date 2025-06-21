package com.nttdata.banking.bootcoinpurchaserequest.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate {

    @Id
    private String id;
    private Double saleRate;
    private Double purchaseRate;

}
