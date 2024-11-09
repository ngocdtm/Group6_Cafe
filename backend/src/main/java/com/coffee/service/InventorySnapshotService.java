package com.coffee.service;

import java.time.LocalDate;
import java.util.Map;

public interface InventorySnapshotService {
    void createDailySnapshot();

    Map<Integer, Integer> getInventorySnapshotForDate(LocalDate startDate);
}
