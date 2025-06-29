package practice.microservices.APIGateway.filters;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Log method and URI
        log.info("Request: {} {}", request.getMethod(), request.getURI());

        // Buffer the request body
        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);

                    String body = new String(bodyBytes, StandardCharsets.UTF_8);
                    log.info("Request Body: {}", body);

                    // Rebuild the request with cached body
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
        ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody
                            .map(dataBuffer -> {
                                // Read and log the response body content
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer); // release memory
                                String responseBody = new String(content, StandardCharsets.UTF_8);
                                log.info("Response Body: {}", responseBody);

                                // Wrap content again
                                return bufferFactory().wrap(content);
                            }));
                }
                return super.writeWith(body); // fallback
            }
        };
        return decoratedResponse;
    }
    @Override
    public int getOrder() {
        return -1;
    }
}

