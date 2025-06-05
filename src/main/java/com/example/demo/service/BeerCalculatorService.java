package com.example.demo.service;

import org.springframework.stereotype.Service;

/**
 * Simple service used for demonstrating automated testing.
 * Calculates the total volume of beer given number of bottles and
 * volume per bottle.
 */
@Service
public class BeerCalculatorService {

    /**
     * Returns the total volume of beer based on bottle count and size.
     *
     * @param bottleCount number of bottles
     * @param volumePerBottle volume of each bottle in liters
     * @return total volume in liters
     */
    public double totalVolume(int bottleCount, double volumePerBottle) {
        if (bottleCount < 0 || volumePerBottle < 0) {
            throw new IllegalArgumentException("Arguments must be non-negative");
        }
        return bottleCount * volumePerBottle;
    }
}
