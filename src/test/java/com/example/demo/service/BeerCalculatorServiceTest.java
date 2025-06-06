package com.example.demo.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BeerCalculatorServiceTest {

    private final BeerCalculatorService service = new BeerCalculatorService();

    @Test
    void calculatesTotalVolume() {
        assertEquals(3.0, service.totalVolume(6, 0.5), 1e-9);
        assertEquals(0.0, service.totalVolume(0, 1.0), 1e-9);
    }

    @Test
    void rejectsNegativeArguments() {
        assertThrows(IllegalArgumentException.class, () -> service.totalVolume(-1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> service.totalVolume(1, -0.5));
    }
}