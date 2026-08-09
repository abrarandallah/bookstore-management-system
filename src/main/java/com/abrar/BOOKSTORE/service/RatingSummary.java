package com.abrar.BOOKSTORE.service;

// Plain read-only DTO, not a JPA entity - just a convenient shape for
// ReviewService.summaryForBook()/summariesForAllBooks() to hand back to
// controllers and templates.
public class RatingSummary {

    private final double average;
    private final long count;

    public RatingSummary(double average, long count) {
        this.average = average;
        this.count = count;
    }

    public double getAverage() {
        return average;
    }

    public long getCount() {
        return count;
    }
}