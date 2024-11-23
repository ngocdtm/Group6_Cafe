import { TestBed } from '@angular/core/testing';
import { StatisticsService } from './statistic.service';

describe('StatisticService', () => {
  let service: StatisticsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StatisticsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
