package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class BeerCalculatorServiceTest {

    @Autowired
    private BeerCalculatorService service;

    @Test
    void calculatesTotalVolume() {
        double result = service.totalVolume(4, 0.5);
        assertThat(result).isEqualTo(2.0);
    }

    @Test
    void rejectsNegativeArguments() {
        assertThrows(IllegalArgumentException.class, () -> service.totalVolume(-1, 0.5));
    }
}
