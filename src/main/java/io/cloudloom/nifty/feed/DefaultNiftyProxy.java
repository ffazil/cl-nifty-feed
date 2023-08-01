package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultNiftyProxy implements NiftyProxy {

    private final WebClient niftyClient;

    @Override
    public Mono<JsonNode> feed(String symbol, String option, LocalDate expiry, Type type, Money strikePrice) {
        Mono<JsonNode> response = niftyClient
                .mutate()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build()
                .get()
                .uri(builder -> builder
                        .path("quote-derivative")
                        .queryParam("symbol", symbol)
                        .queryParam("identifier", option + symbol + expiry.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + type.name() + strikePrice.getNumber())
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class);

        return response;

    }

    @Override
    public Flux<JsonNode> ticker(String symbol, String option, LocalDate expiry, Type type, Money strikePrice) {
        String identifier = option + symbol + expiry.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + type.name() + strikePrice.getNumber().longValueExact();
        log.info("Fetching tick for {}", identifier);
        Flux<JsonNode> response = niftyClient
                .mutate()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build()
                .get()
                .uri(builder -> builder
                        .path("quote-derivative")
                        .queryParam("symbol", symbol)
                        .queryParam("identifier", identifier)
                        .build())
                .retrieve()
                .bodyToFlux(JsonNode.class);

        return response;
    }

}
