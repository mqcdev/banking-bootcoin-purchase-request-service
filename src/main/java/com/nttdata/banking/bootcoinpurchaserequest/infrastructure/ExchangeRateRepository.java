package com.nttdata.banking.bootcoinpurchaserequest.infrastructure;

import com.nttdata.banking.bootcoinpurchaserequest.config.WebClientConfig;
import com.nttdata.banking.bootcoinpurchaserequest.model.ExchangeRate;
import com.nttdata.banking.bootcoinpurchaserequest.util.Constants;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class ExchangeRateRepository {

    @Value("${local.property.host.ms-exchange-rate}")
    private String propertyHostMsExchangeRate;

    @Autowired
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @CircuitBreaker(name = Constants.EXCHANGERATE_CB, fallbackMethod = "getDefaultfindByCurrencyType")
    public Mono<ExchangeRate> findByCurrencyType(String currencyType) {
        log.info("Inicio----findByCurrencyType-------currencyType: " + currencyType);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsExchangeRate + ":8095")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/exchangerate/currencyType/" + currencyType).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToMono(ExchangeRate.class)
                );
    }

    public Mono<ExchangeRate> getDefaultfindByCurrencyType(String currencyType, Exception e) {
        log.info("Inicio----getDefaultfindByCurrencyType-------currencyType: " + currencyType);
        return Mono.empty();
    }
}
