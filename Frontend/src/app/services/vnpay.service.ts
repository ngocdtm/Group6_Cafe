import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

// Interface for cart/order item
interface OrderItem {
  id: number;
  quantity: number;
  price: number;
}

// Enhanced PaymentRequest interface
export interface PaymentRequest {
  amount: number;
  orderId: string;
  orderInfo: string; // Will contain JSON string of order items
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

export interface PaymentResponse {
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
  }, cartItems: OrderItem[]): Observable<PaymentResponse> {
    // Convert cart items to JSON string for orderInfo
    const orderInfo = JSON.stringify(cartItems.map(item => ({
      id: item.id,
      quantity: item.quantity,
      price: item.price
    })));

    const payload: PaymentRequest = {
      ...paymentData,
      amount: Math.round(paymentData.amount),
      orderId: paymentData.orderId || this.generateOrderId(),
      orderInfo: orderInfo, // Send cart items as JSON string
      returnUrl: `${window.location.origin}/checkout`
    };

    // Validate payload before sending
    this.validatePayload(payload);

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

  private generateOrderId(): string {
    const timestamp = new Date().getTime();
    const random = Math.floor(Math.random() * 1000);
    return `ORDER_${timestamp}_${random}`;
  }

  private validatePayload(payload: PaymentRequest): void {
    if (payload.amount < 1000) {
      throw new Error('Amount must be at least 1000 VND');
    }

    if (!payload.shippingAddress?.trim()) {
      throw new Error('Shipping address is required');
    }

    if (!payload.customerName?.trim()) {
      throw new Error('Customer name is required');
    }

    if (!payload.customerPhone?.trim()) {
      throw new Error('Customer phone is required');
    }

    if (payload.discount > payload.total) {
      throw new Error('Discount cannot be greater than total amount');
    }

    try {
      // Validate orderInfo is valid JSON
      const parsedOrderInfo = JSON.parse(payload.orderInfo);
      if (!Array.isArray(parsedOrderInfo) || parsedOrderInfo.length === 0) {
        throw new Error('Order must contain at least one item');
      }
    } catch (e) {
      throw new Error('Invalid order information format');
    }
  }
  
}