package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.JsonNode;
import org.javamoney.moneta.Money;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface NiftyProxy {
    Mono<JsonNode> feed(String symbol, String option, LocalDate expiry, Type type, Money strikePrice);

    Flux<JsonNode> ticker(String symbol, String option, LocalDate expiry, Type type, Money strikePrice);
}
