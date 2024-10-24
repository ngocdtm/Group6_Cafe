import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  url = environment.apiUrl;

  constructor(private httpClient: HttpClient) { }

  // Generate offline bill
  generateOfflineBill(data: any): Observable<any> {
    return this.httpClient.post(`${this.url}/api/v1/bill/generate-offline`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  // Process online order
  processOnlineOrder(data: any): Observable<any> {
    return this.httpClient.post(`${this.url}/api/v1/bill/process-online`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  // Update order status
  updateOrderStatus(id: number, status: string): Observable<any> {
    return this.httpClient.put(`${this.url}/api/v1/bill/${id}/status?status=${status}`, {}, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  // Get PDF
  getPdf(data: any): Observable<Blob> {
    return this.httpClient.post(`${this.url}/api/v1/bill/getPdf`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
      responseType: 'blob'
    });
  }

  // Get all bills
  getBills(): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/bill/getBill`, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  // Delete bill
  deleteBill(id: number): Observable<any> {
    return this.httpClient.post(`${this.url}/api/v1/bill/delete/${id}`, {}, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  applyCoupon(data: any): Observable<any> {
    return this.httpClient.post(this.url + "/api/v1/bill/applyCoupon", data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }  
}