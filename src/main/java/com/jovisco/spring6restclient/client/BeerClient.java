package com.jovisco.spring6restclient.client;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.jovisco.spring6restclient.model.BeerDTO;
import com.jovisco.spring6restclient.model.style;

public interface BeerClient {
    Page<BeerDTO> listBeers();
    Page<BeerDTO> listBeers(String name, style style, Boolean showInventory, Integer pageNumber, Integer pageSize);
    BeerDTO getBeerById(UUID beerId);
    BeerDTO createBeer(BeerDTO newDto);
    BeerDTO updateBeer(BeerDTO beerDto);
    void deleteBeer(UUID beerId);
}
