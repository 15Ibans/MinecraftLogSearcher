package me.ibans.minecraftlogsearch;

import java.time.LocalDate;

public class DateRange {

    private LocalDate lowerBound = LocalDate.MIN;
    private LocalDate upperBound = LocalDate.MAX;

    public void setLowerBound(LocalDate lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(LocalDate upperBound) {
        this.upperBound = upperBound;
    }

    public LocalDate getLowerBound() {
        return lowerBound;
    }

    public LocalDate getUpperBound() {
        return upperBound;
    }

    private void fixBounds() {
        if (lowerBound.isAfter(upperBound)) {
            LocalDate after = lowerBound;
            lowerBound = upperBound;
            upperBound = after;
        }
    }

    public boolean isInRange(LocalDate date) {
        fixBounds();
        return date.isAfter(lowerBound) && date.isBefore(upperBound);
    }

}
