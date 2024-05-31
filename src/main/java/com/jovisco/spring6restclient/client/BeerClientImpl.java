package com.jovisco.spring6restclient.client;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.jovisco.spring6restclient.model.BeerDTO;
import com.jovisco.spring6restclient.model.BeerDTOPageImpl;
import com.jovisco.spring6restclient.model.style;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeerClientImpl implements BeerClient {

    public static final String BEER_PATH = "/api/v1/beers";
    public static final String BEER_BY_ID_PATH = "/api/v1/beers/{id}";

    private final RestClient.Builder restClientBuilder;

    @Override
    public Page<BeerDTO> listBeers() {
        return listBeers(null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page<BeerDTO> listBeers(String name, style style, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(BEER_PATH);

         if (name != null) {
            uriComponentsBuilder.queryParam("name", name);
        }

        if (style != null) {
            uriComponentsBuilder.queryParam("style", style);
        }

        if (showInventory != null) {
            uriComponentsBuilder.queryParam("showInventory", style);
        }

        if (pageNumber != null) {
            uriComponentsBuilder.queryParam("pageNumber", style);
        }

        if (pageSize != null) {
            uriComponentsBuilder.queryParam("pageSize", style);
        }

        var restClient = restClientBuilder.build();

        return restClient.get()
                .uri(uriComponentsBuilder.toUriString())
                .retrieve()
                .body(BeerDTOPageImpl.class);
    }

    @Override
    public BeerDTO getBeerById(UUID id) {
        var restClient = restClientBuilder.build();

       return restClient
            .get()
            .uri(BEER_BY_ID_PATH, id)
            .retrieve()
            .body(BeerDTO.class);
    }

    @Override
    public BeerDTO createBeer(BeerDTO newDto) {
        var restClient = restClientBuilder.build();
        
        var location = restClient
            .post()
            .uri(BEER_PATH)
            .body(newDto)
            .retrieve()
            .toBodilessEntity()
            .getHeaders()
            .getLocation();

        return restClient
            .get()
            .uri(location.getPath())
            .retrieve()
            .body(BeerDTO.class);

        
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDto) {
        var restClient = restClientBuilder.build();

        restClient
            .put()
            .uri(BEER_BY_ID_PATH,beerDto.getId())
            .body(beerDto)
            .retrieve();

        return getBeerById(beerDto.getId());
    }

    @Override
    public void deleteBeer(UUID id) {
        RestClient restClient = restClientBuilder.build();

        restClient
            .delete()
                .uri(BEER_BY_ID_PATH, id)
                .retrieve();
    }
}