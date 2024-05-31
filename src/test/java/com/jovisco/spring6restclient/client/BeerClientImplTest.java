package com.jovisco.spring6restclient.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import com.jovisco.spring6restclient.model.BeerDTO;
import com.jovisco.spring6restclient.model.style;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void testDeleteBeer() {
        var newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .name("Mango Bobs 2")
                .style(style.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();

        var beerDto = beerClient.createBeer(newDto);

        beerClient.deleteBeer(beerDto.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            //should error
            beerClient.getBeerById(beerDto.getId());
        });
    }

    @Test
    void testUpdateBeer() {

        var newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .name("Mango Bobs 2")
                .style(style.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();

        var beerDto = beerClient.createBeer(newDto);

        final String newName = "Mango Bobs 3";
        beerDto.setName(newName);
        var updatedBeer = beerClient.updateBeer(beerDto);

        assertEquals(newName, updatedBeer.getName());
    }

    @Test
    void testCreateBeer() {

        var newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .name("Mango Bobs")
                .style(style.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();

        var savedDto = beerClient.createBeer(newDto);
        assertNotNull(savedDto);
    }

    @Test
    void getBeerById() {

        var beerDTOs = beerClient.listBeers();
        var dto = beerDTOs.getContent().getFirst();

        var byId = beerClient.getBeerById(dto.getId());

        assertNotNull(byId);
    }

    @Test
    void listBeersNoname() {

        beerClient.listBeers(null, null, null, null, null);
    }

    @Test
    void listBeers() {

        beerClient.listBeers("ALE", null, null, null, null);
    }
}