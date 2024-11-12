import { DatePipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

// Enums
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum OrderType {
  ONLINE = 'ONLINE',
  IN_STORE = 'IN_STORE'
}

export enum StockLevel {
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  IMPORT = 'IMPORT',
  EXPORT = 'EXPORT'
}

// Interfaces
export interface DateRange {
  startDateTime: string;
  endDateTime: string;
}

export interface ProductStatistics {
  productId: number;
  productName: string;
  totalQuantitySold: number;
  totalRevenue: number;
  averageRating: number;
  stockLevel: StockLevel;
}

export interface CategoryStatistics {
  categoryId: number;
  categoryName: string;
  totalProducts: number;
  totalSales: number;
  totalRevenue: number;
  percentageOfTotalSales: number;
}

export interface CategoryPerformance {
  categoryId: number;
  categoryName: string;
  productCount: number;
  totalQuantitySold: number;
  totalRevenue: number;
}

export interface InventoryTurnover {
  productId: number;
  productName: string;
  beginningInventory: number;
  endingInventory: number;
  soldQuantity: number;
  turnoverRate: number;
  averageInventory: number;
}

export interface DashboardSummary {
  todayOrders: number;
  todayRevenue: number;
  revenueGrowth: number;
  lowStockItems: number;
  bestSellers: ProductStatistics[];
  categoryPerformance: CategoryPerformance[];
  monthlyOrdersTrend: { [key: string]: number };
  monthlyRevenueTrend: { [key: string]: number };
  dateRanges: { [key: string]: DateRange };
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private url = environment.apiUrl;

  constructor(
    private httpClient: HttpClient,
    private datePipe: DatePipe
  ) { }
  
  private getHeaders(): HttpHeaders {
    // Lấy token từ localStorage hoặc service quản lý auth
    const token = localStorage.getItem('auth_token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
  
  getRevenueStatistics(
    timeFrame: string = 'monthly',
    specificDate?: any,
    startDate?: any, 
    endDate?: any
  ): Observable<any> {
    const params = this.buildParams(timeFrame, specificDate, startDate, endDate);
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/revenue`, 
      { params, headers: this.getHeaders() }
    );
  }

  getProfitStatistics(
    timeFrame: string = 'monthly',
    specificDate?: any,
    startDate?: any,
    endDate?: any
  ): Observable<any> {
    const params = this.buildParams(timeFrame, specificDate, startDate, endDate);
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/profit`,
      { params, headers: this.getHeaders() }
    );
  }

  getCategoryPerformance(
    timeFrame: string = 'monthly',
    specificDate?: any,
    startDate?: any,
    endDate?: any
  ): Observable<any> {
    const params = this.buildParams(timeFrame, specificDate, startDate, endDate);
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/category-performance`,
      { params, headers: this.getHeaders() }
    );
  }

  getOrderStatistics(
    timeFrame: string = 'monthly',
    specificDate?: any,
    startDate?: any,
    endDate?: any
  ): Observable<any> {
    const params = this.buildParams(timeFrame, specificDate, startDate, endDate);
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/orders`,
      { params, headers: this.getHeaders() }
    );
  }

  getInventoryTurnover(
    timeFrame: string = 'monthly',
    specificDate?: any,
    startDate?: any,
    endDate?: any
  ): Observable<any> {
    const params = this.buildParams(timeFrame, specificDate, startDate, endDate);
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/inventory-turnover`,
      { params, headers: this.getHeaders() }
    );
  }

  getDashboardSummary(): Observable<any> {
    return this.httpClient.get<any>(
      `${this.url}/api/v1/statistics/dashboard-summary`,
      { headers: this.getHeaders() }
    );
  }

  private formatDate(date: any): string | null {
    if (!date) return null;
    // Chuyển đổi date object thành string format yyyy-MM-dd
    return this.datePipe.transform(date, 'yyyy-MM-dd');
  }

  private buildParams(timeFrame: string, specificDate?: any, startDate?: any, endDate?: any): { [key: string]: string } {
    const params: { [key: string]: string } = {
      timeFrame
    };

    if (specificDate) {
      const formattedSpecificDate = this.formatDate(specificDate);
      if (formattedSpecificDate) {
        params.specificDate = formattedSpecificDate;
      }
    }
    
    if (startDate) {
      const formattedStartDate = this.formatDate(startDate);
      if (formattedStartDate) {
        params.startDate = formattedStartDate;
      }
    }
    
    if (endDate) {
      const formattedEndDate = this.formatDate(endDate);
      if (formattedEndDate) {
        params.endDate = formattedEndDate;
      }
    }

    return params;
  }

}