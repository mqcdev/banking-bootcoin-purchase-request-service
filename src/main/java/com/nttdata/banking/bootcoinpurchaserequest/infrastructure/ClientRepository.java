package com.nttdata.banking.bootcoinpurchaserequest.infrastructure;

import com.nttdata.banking.bootcoinpurchaserequest.config.WebClientConfig;
import com.nttdata.banking.bootcoinpurchaserequest.model.Client;
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
public class ClientRepository {

    @Value("${local.property.host.ms-client}")
    private String propertyHostMsClient;

    @Autowired
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @CircuitBreaker(name = Constants.CLIENT_CB, fallbackMethod = "getDefaultClientByDni")
    public Mono<Client> findClientByDni(String documentNumber) {
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsClient + ":8080")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/clients/documentNumber/" + documentNumber).retrieve()
                                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                                .bodyToMono(Client.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.just(new Client())))
                );
    }


    public Mono<Client> getDefaultClientByDni(String documentNumber, Exception e) {
        return Mono.empty();
    }

}
