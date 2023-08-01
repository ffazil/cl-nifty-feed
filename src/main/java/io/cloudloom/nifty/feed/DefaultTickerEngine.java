package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultTickerEngine {

    private final ApplicationEventPublisher publisher;
    private final NiftyProxy niftyProxy;

    @Scheduled(fixedDelay = 30000)
    public void on(){
        Flux<JsonNode> response = niftyProxy.ticker("BANKNIFTY", "OPTIDX", LocalDate.of(2023, 8, 3), Type.PE, Money.of(45500.00, "INR"));
        response.subscribe(jsonNode -> {
            Tick tick = new Tick(jsonNode);
            log.info("Tick: {}", tick);
            publisher.publishEvent(tick);
        });


    }

}
