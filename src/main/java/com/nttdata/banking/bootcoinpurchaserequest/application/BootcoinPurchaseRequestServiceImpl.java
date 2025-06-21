package com.nttdata.banking.bootcoinpurchaserequest.application;

import com.nttdata.banking.bootcoinpurchaserequest.dto.BootcoinPurchaseRequestDto;
import com.nttdata.banking.bootcoinpurchaserequest.dto.MovementDto;
import com.nttdata.banking.bootcoinpurchaserequest.exception.ResourceNotFoundException;
import com.nttdata.banking.bootcoinpurchaserequest.infrastructure.*;
import com.nttdata.banking.bootcoinpurchaserequest.model.BootcoinMovement;
import com.nttdata.banking.bootcoinpurchaserequest.model.BootcoinPurchaseRequest;
import com.nttdata.banking.bootcoinpurchaserequest.model.BootcoinsForSale;
import com.nttdata.banking.bootcoinpurchaserequest.model.Client;
import com.nttdata.bootcamp.msbootcoinpurchaserequest.infrastructure.*;
import com.nttdata.bootcamp.msbootcoinpurchaserequest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BootcoinPurchaseRequestServiceImpl implements BootcoinPurchaseRequestService {

    @Autowired
    private BootcoinPurchaseRequestRepository bootcoinPurchaseRequestRepository;
    @Autowired
    private BootcoinsForSaleRepository bootcoinsForSaleRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private MobileWalletRepository mobileWalletRepository;
    @Autowired
    private BootcoinRepository bootcoinRepository;
    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private BootcoinMovementRepository bootcoinMovementRepository;


    @Override
    public Flux<BootcoinPurchaseRequest> findAll() {
        return bootcoinPurchaseRequestRepository.findAll();
    }

    @Override
    public Mono<BootcoinPurchaseRequest> findById(String idBankAccount) {
        return Mono.just(idBankAccount)
                .flatMap(bootcoinPurchaseRequestRepository::findById)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cuenta Bancaria", "idBankAccount", idBankAccount)));
    }

    @Override
    public Mono<BootcoinPurchaseRequest> findByDocumentNumber(String documentNumber) {
        return Mono.just(documentNumber)
                .flatMap(bootcoinPurchaseRequestRepository::findByDocumentNumber)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("BootCoin", "documentNumber", documentNumber)));
    }

    @Override
    public Mono<BootcoinPurchaseRequest> save(BootcoinPurchaseRequestDto bootcoinPurchaseRequestDto) {
        log.info("----save-------bootcoinPurchaseRequestDto : " + bootcoinPurchaseRequestDto.toString());
        return Mono.just(bootcoinPurchaseRequestDto)
                .flatMap(bprd -> clientRepository.findClientByDni(bprd.getDocumentNumber())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cliente", "DocumentNumber", bprd.getDocumentNumber())))
                        .flatMap(cl -> {
                            bprd.setClient(cl);
                            return Mono.just(cl);
                        })
                        .flatMap(cl -> bootcoinsForSaleRepository.findById(bprd.getIdBootcoinsForSale()))
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("bootcoins For Sale", "IdBootcoinsForSale", bprd.getIdBootcoinsForSale())))
                        .flatMap(bcfs -> {
                            bcfs.setBootcoin(null);
                            bcfs.setAmount(null);
                            bprd.setBootcoinsForSale(bcfs);
                            return Mono.just(bcfs);
                        })
                        .flatMap(bcfs -> validateBalance(bcfs, bprd.getAmount()))
                        .flatMap(cl -> bprd.MapperToBootcoinPurchaseRequest("pending"))
                        .flatMap(bpr -> validatesIfUouHaveABankAccount(bpr.getClient())
                                .then(Mono.just(bpr)))
                        .flatMap(bootcoinPurchaseRequestRepository::save)
                );
    }

    public Mono<BootcoinsForSale> validateBalance(BootcoinsForSale bootcoinsForSale, Double amount) {
        if (bootcoinsForSale.getBalance() < amount) {
            return Mono.error(new ResourceNotFoundException("Bootcoin For Sale", "Balance", bootcoinsForSale.getBalance().toString()));
        }
        return Mono.just(bootcoinsForSale);
    }

    public Mono<Boolean> validatesIfUouHaveABankAccount(Client client) {
        log.info("--validatesIfUouHaveABankAccount-------: ");
        return bankAccountRepository.findCantByDocumentNumber(client.getDocumentNumber())
                .flatMap(cnt -> {
                    if (cnt > 0) {
                        return Mono.just(true);
                    } else {
                        return mobileWalletRepository.findByCantDocumentNumber(client.getDocumentNumber())
                                .flatMap(cntmw -> {
                                    if (cntmw > 0) {
                                        return Mono.just(true);
                                    } else {
                                        return Mono.error(new ResourceNotFoundException("Cuenta bancaria", "Cant", cnt.toString()));
                                    }
                                });
                    }
                });
    }

    @Override
    public Mono<BootcoinPurchaseRequest> update(BootcoinPurchaseRequestDto bootcoinPurchaseRequestDto, String idbootcoinPurchaseRequest) {
        log.info("----update-------BootcoinPurchaseRequestDto -- idbootcoinPurchaseRequest: " + bootcoinPurchaseRequestDto.toString() + " -- " + idbootcoinPurchaseRequest);
        return Mono.just(bootcoinPurchaseRequestDto)
                .flatMap(bprd -> clientRepository.findClientByDni(bprd.getDocumentNumber())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cliente", "DocumentNumber", bprd.getDocumentNumber())))
                        .flatMap(cl -> {
                            bprd.setClient(cl);
                            return Mono.just(cl);
                        })
                        .flatMap(cl -> bootcoinsForSaleRepository.findById(bprd.getIdBootcoinsForSale()))
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("bootcoins For Sale", "IdBootcoinsForSale", bprd.getIdBootcoinsForSale())))
                        .flatMap(bcfs -> {
                            String idBootcoin = bcfs.getBootcoin().getIdBootCoin();
                            bcfs.setBootcoin(null);
                            bcfs.getBootcoin().setIdBootCoin(idBootcoin);
                            bcfs.setAmount(null);
                            bprd.setBootcoinsForSale(bcfs);
                            return Mono.just(bcfs);
                        })
                        .flatMap(bcfs -> validateBalance(bcfs, bprd.getAmount()))
                        .flatMap(cl -> bprd.MapperToBootcoinPurchaseRequest("pending"))
                        .flatMap(bpr -> validatesIfUouHaveABankAccount(bpr.getClient())
                                .then(Mono.just(bpr)))
                        .flatMap(bpr -> bootcoinPurchaseRequestRepository.findById(idbootcoinPurchaseRequest)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("bootcoin Purchase Request", "idbootcoinPurchaseRequest", idbootcoinPurchaseRequest)))
                                .flatMap(x -> {
                                    bpr.setIdBootcoinPurchaseRequest(x.getIdBootcoinPurchaseRequest());
                                    return bootcoinPurchaseRequestRepository.save(bpr);
                                })
                        )
                );
    }

    @Override
    public Mono<Void> delete(String idBootcoinPurchaseRequest) {
        return Mono.just(idBootcoinPurchaseRequest)
                .flatMap(b -> bootcoinPurchaseRequestRepository.findById(b))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Bootcoin Purchase Request", "idBootcoinPurchaseRequest", idBootcoinPurchaseRequest)))
                .flatMap(bootcoinPurchaseRequestRepository::delete);
    }

    @Override
    public Mono<BootcoinPurchaseRequest> acceptSale(String idBootcoinPurchaseRequest) {
        log.info("----acceptSale-------idBootcoinPurchaseRequest : " + idBootcoinPurchaseRequest);
        return Mono.just(idBootcoinPurchaseRequest)
                .flatMap(b -> bootcoinPurchaseRequestRepository.findById(b)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("bootcoinPurchaseRequest", "idBootcoinPurchaseRequest", idBootcoinPurchaseRequest))))
                .flatMap(bpr -> bootcoinRepository.findBootcoinByDocumentNumber(bpr.getBootcoinsForSale().getClient().getDocumentNumber())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Bootcoin", "DocumentNumber", bpr.getBootcoinsForSale().getClient().getDocumentNumber())))
                        .flatMap(bc -> {
                            bpr.getBootcoinsForSale().setBootcoin(bc);
                            return Mono.just(bpr);
                        })
                )
                .flatMap(bpr -> validateBalanceBootcoin(bpr))
                .flatMap(bpr -> validateBalanceAccountAndBootcoinForTransfer(bpr))
                .flatMap(bpr -> {
                    //String idBootcoin = bpr.getBootcoinsForSale().getBootcoin().getIdBootCoin();
                    //bpr.getBootcoinsForSale().setBootcoin(null);
                    //bpr.getBootcoinsForSale().getBootcoin().setIdBootCoin(idBootcoin);
                    //bpr.getBootcoinsForSale().setAmount(null);
                    bpr.setState("paid");
                    return Mono.just(bpr);
                })
                .flatMap(bootcoinPurchaseRequestRepository::save);
    }

    public Mono<BootcoinPurchaseRequest> validateBalanceBootcoin(BootcoinPurchaseRequest bootcoinPurchaseRequest) {
        if (bootcoinPurchaseRequest.getAmount() > bootcoinPurchaseRequest.getBootcoinsForSale().getBalance()) {
            return Mono.error(new ResourceNotFoundException("BootcoinsForSale", "Balance", bootcoinPurchaseRequest.getBootcoinsForSale().getBalance().toString()));
        } else {
            return Mono.just(bootcoinPurchaseRequest);
        }
    }

    public Mono<BootcoinPurchaseRequest> validateBalanceAccountAndBootcoinForTransfer(BootcoinPurchaseRequest bootcoinPurchaseRequest) {

        log.info("--validateBalanceAccountAndBootcoinForTransfer---------bootcoinPurchaseRequest : " + bootcoinPurchaseRequest);
        return Mono.just(bootcoinPurchaseRequest)
                .flatMap(bpr -> clientRepository.findClientByDni(bpr.getClient().getDocumentNumber()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cliente", "DocumentNumber", bootcoinPurchaseRequest.getClient().getDocumentNumber())))
                .flatMap(cl -> {
                    if (bootcoinPurchaseRequest.getMethodOfPayment().equals("transfer-out")) {
                        return bankAccountRepository.findByDocumentNumber(bootcoinPurchaseRequest.getClient().getDocumentNumber())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cuenta Comprador", "DocumentNumber", bootcoinPurchaseRequest.getClient().getDocumentNumber())))
                                .doOnNext(a -> log.info("----doOnNext-------a1 : " + a))
                                .flatMap(acc -> exchangeRateRepository.findByCurrencyType(acc.getCurrency())
                                        .flatMap(er -> {
                                            Double total = er.getPurchaseRate() * bootcoinPurchaseRequest.getAmount();
                                            if (total > acc.getBalance()) {
                                                return Mono.error(new ResourceNotFoundException("Account Balance", "Balance", acc.getBalance().toString()));
                                            } else {
                                                return bankAccountRepository.findByDocumentNumber(bootcoinPurchaseRequest.getBootcoinsForSale().getClient().getDocumentNumber())
                                                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cuenta vendedor", "DocumentNumber", bootcoinPurchaseRequest.getBootcoinsForSale().getClient().getDocumentNumber())))
                                                        .doOnNext(a2 -> log.info("----doOnNext-------a2 : " + a2))
                                                        .flatMap(acc2 -> {
                                                            MovementDto movement = MovementDto.builder()
                                                                    .accountNumber(acc.getAccountNumber())
                                                                    .movementType("output-transfer")
                                                                    .amount(total)
                                                                    .currency(acc.getCurrency())
                                                                    .accountNumberForTransfer(acc2.getAccountNumber())
                                                                    .build();
                                                            log.info("-----------movement : " + movement);
                                                            return movementRepository.updateMovement(movement)
                                                                    .flatMap(m -> {

                                                                        log.info("-----------updateMovement : " + movement);
                                                                        BootcoinMovement bootcoinMovement = BootcoinMovement.builder()
                                                                                .documentNumber(bootcoinPurchaseRequest.getClient().getDocumentNumber())
                                                                                .bootcoinMovementType("transfer-out")
                                                                                .amount(bootcoinPurchaseRequest.getAmount())
                                                                                .currency(acc.getCurrency())
                                                                                .documentNumberForTransfer(acc2.getClient().getDocumentNumber())
                                                                                .build();

                                                                        log.info("-----------bootcoinMovement : " + bootcoinMovement);
                                                                        return bootcoinMovementRepository.updateBootcoinMovement(bootcoinMovement);
                                                                    })
                                                                    .doOnNext(a2 -> log.info("----doOnNext-------fin validateBalanceAccountAndBootcoinForTransfer: " + a2))
                                                                    .then(Mono.just(bootcoinPurchaseRequest));
                                                        });
                                            }
                                        })
                                );
                    } else if (bootcoinPurchaseRequest.getMethodOfPayment().equals("mobile-wallet")) {
                        return mobileWalletRepository.findByDocumentNumber(bootcoinPurchaseRequest.getClient().getDocumentNumber())
                                .flatMap(acc -> exchangeRateRepository.findByCurrencyType(acc.getCurrency())
                                        .flatMap(er -> {
                                            Double total = er.getPurchaseRate() * bootcoinPurchaseRequest.getAmount();
                                            if (total > acc.getBalance()) {
                                                return Mono.error(new ResourceNotFoundException("Account Balance", "Balance", acc.getBalance().toString()));
                                            } else {
                                                return bankAccountRepository.findByDocumentNumber(bootcoinPurchaseRequest.getClient().getDocumentNumber())
                                                        .flatMap(acc2 -> {
                                                            MovementDto movement = MovementDto.builder()
                                                                    .cellphone(cl.getCellphone().toString())
                                                                    .movementType("output-transfer")
                                                                    .amount(total)
                                                                    .currency(acc.getCurrency())
                                                                    .accountNumberForTransfer(acc2.getAccountNumber())
                                                                    .build();

                                                            return movementRepository.updateMovement(movement)
                                                                    .flatMap(m -> {
                                                                        BootcoinMovement bootcoinMovement = BootcoinMovement.builder()
                                                                                .cellphone(cl.getCellphone().toString())
                                                                                .bootcoinMovementType("transfer-out")
                                                                                .amount(total)
                                                                                .currency(acc.getCurrency())
                                                                                .documentNumberForTransfer(acc2.getAccountNumber())
                                                                                .build();
                                                                        return bootcoinMovementRepository.updateBootcoinMovement(bootcoinMovement);
                                                                    })
                                                                    .then(Mono.just(bootcoinPurchaseRequest));
                                                        });
                                            }
                                        })
                                );
                    } else {
                        return Mono.error(new ResourceNotFoundException("Bootcoin Purchase Request", "MethodOfPayment", bootcoinPurchaseRequest.getMethodOfPayment()));
                    }
                });
    }

}
