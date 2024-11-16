import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface PaymentRequest {
 amount: number;
 orderId: string;
 orderInfo: string;
 bankCode?: string;
 customerName: string;
 customerPhone: string;
 shippingAddress: string;
 total: number;
 discount: number;
 totalAfterDiscount: number;
 couponCode?: string;
 returnUrl: string; 
}
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

  createPayment(paymentData: {
    amount: number;
    orderId: string;
    customerName: string;
    customerPhone: string;
    shippingAddress: string;
    total: number;
    discount: number;
    totalAfterDiscount: number;
    couponCode?: string;
    bankCode: string;
  }): Observable<PaymentResponse> {
    const payload: PaymentRequest = {
      ...paymentData,
      amount: Math.round(paymentData.amount),
      orderId: paymentData.orderId || String(new Date().getTime()),
      orderInfo: `Payment for order ${paymentData.orderId || 'new order'}`,
      // Cập nhật returnUrl để trỏ về trang checkout
      returnUrl: `${window.location.origin}/checkout`
    };

    return this.http.post<PaymentResponse>(
      `${this.url}/api/v1/vnpay/create-payment`,
      payload
    );
  }

  verifyPayment(vnpayParams: any): Observable<any> {
    return this.http.get(`${this.url}/api/v1/vnpay/payment-callback`, {
      params: vnpayParams
    });
  }
}
