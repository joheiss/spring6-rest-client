package com.jovisco.spring6restclient.client;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovisco.spring6restclient.config.OAuthClientInterceptor;
import com.jovisco.spring6restclient.config.RestTemplateBuilderConfig;
import com.jovisco.spring6restclient.model.BeerDTO;
import com.jovisco.spring6restclient.model.BeerDTOPageImpl;
import com.jovisco.spring6restclient.model.style;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
public class BeerClientMockTest {

    static final String URL = "http://localhost:8071";
    public static final String BEARER_TEST = "Bearer test";

    BeerClient beerClient;

    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    RestClient.Builder restClientBuilder;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO dto;
    String dtoJson;

    @MockBean
    OAuth2AuthorizedClientManager manager;

    @TestConfiguration
    @Import(RestTemplateBuilderConfig.class)
    public static class TestConfig {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId("springauth")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build());
        }

        @Bean
        OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OAuthClientInterceptor oAuthClientInterceptor(OAuth2AuthorizedClientManager manager, ClientRegistrationRepository clientRegistrationRepository){
            return new OAuthClientInterceptor(manager, clientRegistrationRepository);
        }
    }

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUp() throws JsonProcessingException {

        var clientRegistration = clientRegistrationRepository
                .findByRegistrationId("springauth");

        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "test", Instant.MIN, Instant.MAX);

        when(manager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration,
                "test", token));

        var restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);

        //NOTE: RestClient is built using the mockRestTemplateBuilder.
        beerClient = new BeerClientImpl(RestClient.builder(mockRestTemplateBuilder.build()));
        dto = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(dto);
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        var uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.BEER_PATH)
                .queryParam("name", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(header("Authorization", BEARER_TEST))
                .andExpect(queryParam("name", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        var responsePage = beerClient
                .listBeers("ALE", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testDeleteNotFound() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(dto.getId());
        });

        server.verify();
    }

    @Test
    void testDeleteBeer() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withNoContent());

        beerClient.deleteBeer(dto.getId());

        server.verify();
    }

    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withNoContent());

        mockGetOperation();

        var responseDto = beerClient.updateBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer()  {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.BEER_BY_ID_PATH)
                        .build(dto.getId());

        server.expect(method(HttpMethod.POST))
                        .andExpect(requestTo(URL +
                                BeerClientImpl.BEER_PATH))
                        .andExpect(header("Authorization", BEARER_TEST))
                                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        var responseDto = beerClient.createBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testGetById() {

        mockGetOperation();

        var responseDto = beerClient.getBeerById(dto.getId());
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        var dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    private BeerDTO getBeerDto() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .name("Mango Bobs")
                .style(style.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    private BeerDTOPageImpl<BeerDTO> getPage(){
        return new BeerDTOPageImpl<BeerDTO>(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}