import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '../services/cart.service';
import { HttpClient } from '@angular/common/http';
interface PaymentResult {
  orderId: string;
  transactionNo: string;
  message: string;
  status: string;
}
@Component({
  selector: 'app-payment-success',
  templateUrl: './payment-success.component.html',
  styleUrls: ['./payment-success.component.scss']
})
export class PaymentSuccessComponent implements OnInit {
 paymentResult: PaymentResult | null = null;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    // Lấy thông tin từ query params thay vì gọi API
    this.route.queryParams.subscribe(params => {
      if (params['orderId'] && params['transactionNo']) {
        this.paymentResult = {
          orderId: params['orderId'],
          transactionNo: params['transactionNo'],
          status: params['status'] || 'success',
          message: 'Payment completed successfully'
        };
      } else {
        // Nếu không có params, chuyển về trang chủ
        this.router.navigate(['/']);
      }
    });
  }
  
  viewOrder(){
    if(this.paymentResult?.orderId){
      this.router.navigate(['/order-details', this.paymentResult.orderId]);
    }
  }
  
  continueShopping(){
    this.router.navigate(['/products']);
  }
}
