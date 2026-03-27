package io.ssafy.p.j14c103.homerun.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import io.ssafy.p.j14c103.homerun.config.FssApiProperties;
import io.ssafy.p.j14c103.homerun.config.FssRestClientConfig;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class FssLoanClientTest {

    @DisplayName("FSS 대출 상품 API는 RestClient로 호출하고 응답을 파싱한다")
    @Test
    void getCreditLoanProducts() throws Exception {
        AtomicReference<String> requestPath = new AtomicReference<>();
        AtomicReference<String> requestQuery = new AtomicReference<>();
        HttpServer server = createServer();
        server.createContext("/creditLoanProductsSearch.json", exchange -> {
            requestPath.set(exchange.getRequestURI().getPath());
            requestQuery.set(exchange.getRequestURI().getQuery());

            byte[] body = """
                {
                  "result": {
                    "err_cd": "000",
                    "baseList": [
                      {
                        "fin_prdt_cd": "TEST-001",
                        "fin_prdt_nm": "국민신용대출"
                      }
                    ],
                    "optionList": [
                      {
                        "fin_prdt_cd": "TEST-001",
                        "intr_rate_type": "변동금리"
                      }
                    ]
                  }
                }
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            } finally {
                exchange.close();
            }
        });
        server.start();

        try {
            FssLoanResponse response = createClient(server).getCreditLoanProducts();

            assertThat(response.getBaseList()).hasSize(1);
            assertThat(response.getOptionList()).hasSize(1);
            assertThat(requestPath.get()).isEqualTo("/creditLoanProductsSearch.json");
            assertThat(requestQuery.get()).contains("auth=test-auth-key");
            assertThat(requestQuery.get()).contains("topFinGrpNo=020000");
            assertThat(requestQuery.get()).contains("pageNo=1");
        } finally {
            server.stop(0);
        }
    }

    @DisplayName("FSS 대출 상품 API 지연 시 5초 read timeout 안에서 빈 응답으로 복구한다")
    @Test
    void getCreditLoanProductsTimeout() throws Exception {
        HttpServer delayedServer = createServer();
        delayedServer.createContext("/creditLoanProductsSearch.json", exchange -> {
            try {
                Thread.sleep(Duration.ofSeconds(15).toMillis());
                byte[] body = "{\"result\":{\"err_cd\":\"000\",\"baseList\":[],\"optionList\":[]}}"
                    .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(body);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        delayedServer.start();

        try {
            long startedAt = System.nanoTime();
            FssLoanResponse response = createClient(delayedServer).getCreditLoanProducts();
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

            assertThat(response.isEmpty()).isTrue();
            assertThat(elapsed).isGreaterThan(Duration.ofSeconds(4));
            assertThat(elapsed).isLessThan(Duration.ofSeconds(8));
        } finally {
            delayedServer.stop(0);
        }
    }

    private HttpServer createServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(createExecutor());
        return server;
    }

    private ExecutorService createExecutor() {
        return Executors.newCachedThreadPool(task -> {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        });
    }

    private FssLoanClient createClient(final HttpServer server) {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        FssApiProperties properties = FssApiProperties.of(baseUrl, "test-auth-key");
        FssRestClientConfig config = new FssRestClientConfig();
        RestClient restClient = config.fssRestClient(
            RestClient.builder(),
            properties,
            config.fssClientHttpRequestFactory()
        );
        return new FssLoanClient(restClient, properties);
    }
}
