import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface StatisticsTimeFrame {
  timeFrame?: string;
  startDate?: Date;
  endDate?: Date;
}

@Injectable({
  providedIn: 'root'
})

export class StatisticsService {
  private url = environment.apiUrl;

  constructor(private httpClient: HttpClient) { }

  getRevenueStatistics(params: StatisticsTimeFrame): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/revenue`, {
      params: this.buildParams(params)
    });
  }

  getProfitStatistics(params: StatisticsTimeFrame): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/profit`, {
      params: this.buildParams(params)
    });
  }

  getBestSellingProducts(startDate?: Date, endDate?: Date, limit: number = 10): Observable<any> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate.toISOString().split('T')[0]);
    if (endDate) params = params.set('endDate', endDate.toISOString().split('T')[0]);
    params = params.set('limit', limit.toString());

    return this.httpClient.get(`${this.url}/api/v1/statistics/best-selling`, { params });
  }

  getCategoryPerformance(startDate?: Date, endDate?: Date): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/category-performance`, {
      params: this.buildParams({ startDate, endDate })
    });
  }

  getOrderStatistics(params: StatisticsTimeFrame): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/orders`, {
      params: this.buildParams(params)
    });
  }

  getInventoryTurnover(startDate?: Date, endDate?: Date): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/inventory-turnover`, {
      params: this.buildParams({ startDate, endDate })
    });
  }

  getDashboardSummary(): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/statistics/dashboard-summary`);
  }

  private buildParams(params: StatisticsTimeFrame): HttpParams {
    let httpParams = new HttpParams();
    
    if (params.timeFrame) {
      httpParams = httpParams.set('timeFrame', params.timeFrame);
    }
    
    if (params.startDate) {
      httpParams = httpParams.set('startDate', params.startDate.toISOString().split('T')[0]);
    }
    
    if (params.endDate) {
      httpParams = httpParams.set('endDate', params.endDate.toISOString().split('T')[0]);
    }
    
    return httpParams;
  }
}
