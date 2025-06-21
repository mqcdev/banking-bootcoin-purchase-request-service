package com.nttdata.banking.bootcoinpurchaserequest.dto;

import com.nttdata.banking.bootcoinpurchaserequest.model.Bootcoin;
import com.nttdata.banking.bootcoinpurchaserequest.model.BootcoinsForSale;
import com.nttdata.banking.bootcoinpurchaserequest.model.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@ToString
@Builder
public class BootcoinsForSaleDto {

    @Id
    private String idBootcoinsForSale;
    private String idBootcoinPurchaseRequest;
    private String documentNumber;
    private Client client;
    private Double amount;
    private LocalDateTime bootcoinPurchaseRequestDate;
    private Double balance;

    public Mono<BootcoinsForSale> MapperToBootcoinPurchaseRequest(Bootcoin bootcoin, String state) {
        LocalDateTime date = LocalDateTime.now();
        log.info("ini MapperToBootcoinPurchaseRequest-------date: " + date);
        Client client = bootcoin.getClient();
        bootcoin.setClient(null);

        BootcoinsForSale bootcoinsForSale = BootcoinsForSale.builder()
                .idBootcoinsForSale(this.getIdBootcoinsForSale())
                .amount(this.getAmount())
                .balance(this.getBalance())
                .client(client)
                .bootcoin(bootcoin)
                .bootcoinPurchaseRequestDate(date)
                .state(state)
                .build();

        return Mono.just(bootcoinsForSale);
    }
}
