package com.nttdata.banking.bootcoinpurchaserequest.model;

import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class MobileWallet.
 * MobileWallet microservice class MobileWallet.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileWallet {

    @Id
    private String idMobileWallet;
    private Client client;
    private Double balance;
    private String currency;
}
