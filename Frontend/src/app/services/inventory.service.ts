import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PlatformService } from './platform.service';

// Enums
export enum TransactionType {
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  IMPORT = 'IMPORT',
  EXPORT = 'EXPORT'
}

// Interfaces
export interface InventoryWrapper {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  minQuantity: number;
  maxQuantity: number;
  lastUpdated: string;
}

export interface InventoryTransactionWrapper {
  id: number;
  productId: number;
  productName: string;
  transactionType: TransactionType;
  quantity: number;
  note: string;
  transactionDate: string;
  createdBy: string;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private url: string;

  constructor(
    private httpClient: HttpClient,
    private platformService: PlatformService
  ) {
    this.url = this.platformService.getApiUrl();
  }
  
  getInventoryStatus(productId: number): Observable<InventoryWrapper> {
    return this.httpClient.get<InventoryWrapper>(
      `${this.url}/api/v1/inventory/status/${productId}`
    );
  }

  getLowStockProducts(): Observable<InventoryWrapper[]> {
    return this.httpClient.get<InventoryWrapper[]>(
      `${this.url}/api/v1/inventory/lowStock`
    );
  }

  addStock(productId: number, quantity: number, note: string): Observable<string> {
    return this.httpClient.post<string>(
      `${this.url}/api/v1/inventory/addStock`,
      null,
      {
        params: {
          productId: productId.toString(),
          quantity: quantity.toString(),
          note
        }
      }
    );
  }

  removeStock(productId: number, quantity: number, note: string): Observable<string> {
    return this.httpClient.post<string>(
      `${this.url}/api/v1/inventory/removeStock`,
      null,
      {
        params: {
          productId: productId.toString(),
          quantity: quantity.toString(),
          note
        }
      }
    );
  }

  getTransactionHistory(productId: number): Observable<InventoryTransactionWrapper[]> {
    return this.httpClient.get<InventoryTransactionWrapper[]>(
      `${this.url}/api/v1/inventory/history/${productId}`
    );
  }

  getAllInventory(): Observable<InventoryWrapper[]> {
    return this.httpClient.get<InventoryWrapper[]>(
      `${this.url}/api/v1/inventory/all`
    );
  }
  
  updateMinMaxStock(productId: number, minQuantity: number, maxQuantity: number): Observable<string> {
    const params = {
      productId: productId.toString(),
      minQuantity: minQuantity?.toString(),
      maxQuantity: maxQuantity?.toString()
    };

    return this.httpClient.put<string>(
      `${this.url}/api/v1/inventory/updateMinMaxStock/${productId}`,
      null,
      { params }
    );
  }
}