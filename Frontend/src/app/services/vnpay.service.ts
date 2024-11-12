import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface PaymentResponse{
  paymentUrl: string;
  orderId: string;
  status: string;
  message?: string;
}
@Injectable({
  providedIn: 'root'
})
export class VnpayService {
 private url = environment.apiUrl;

  constructor(private http: HttpClient) { }

  createPayment(amount: number, orderId: string): Observable<PaymentResponse> {
    const payload = {
      amount,
      orderId,
      orderInfo: `Payment for order ${orderId}`,
      orderType: 'fashion',
      locale: 'vn'
    };

    return this.http.post<PaymentResponse>(
      `${this.url}/api/v1/vnpay/create-payment`,
      payload,
      {
        headers: new HttpHeaders().set('Content-Type', 'application/json')
      }
    );
  }
}
