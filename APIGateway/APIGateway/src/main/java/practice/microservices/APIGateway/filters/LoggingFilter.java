package practice.microservices.APIGateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "logging.filter.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();

        log.info("Request: {} {}", request.getMethod(), request.getURI());

        // For GET/DELETE, skip request body decoration
        if (method == null || method.matches("GET") || method.matches("DELETE")) {
            return chain.filter(exchange.mutate().response(decorateResponse(exchange)).build());
        }

        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);

                    String body = new String(bodyBytes, StandardCharsets.UTF_8);
                    log.info("Request Body: {}", body);

                    ServerHttpRequest mutatedRequest = request.mutate().build();

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(new ServerHttpRequestDecorator(mutatedRequest) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return Flux.just(exchange.getResponse().bufferFactory().wrap(bodyBytes));
                                }
                            })
                            .response(decorateResponse(exchange))
                            .build();

                    return chain.filter(mutatedExchange);
                });
    }

    private ServerHttpResponse decorateResponse(ServerWebExchange exchange) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        return new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    return super.writeWith(
                            Flux.from(body)
                                    .buffer()
                                    .map(dataBuffers -> {
                                        DataBuffer joined = bufferFactory.join(dataBuffers);
                                        byte[] content = new byte[joined.readableByteCount()];
                                        joined.read(content);
                                        DataBufferUtils.release(joined);

                                        String responseBody = new String(content, StandardCharsets.UTF_8);
                                        log.info("Response Body: {}", responseBody);

                                        return bufferFactory.wrap(content);
                                    })
                    );
                }
                return super.writeWith(body);
            }
        };
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
