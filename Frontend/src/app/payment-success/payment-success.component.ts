import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

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
  isLoading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['orderId'] && params['transactionNo']) {
        this.paymentResult = {
          orderId: params['orderId'],
          transactionNo: params['transactionNo'],
          status: params['status'] || 'success',
          message: 'Thanh toán đã hoàn tất thành công'
        };
        this.isLoading = false;
      } else {
        this.snackBar.open('Không tìm thấy thông tin thanh toán', 'Đóng', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        this.router.navigate(['/menu']);
      }
    });
  }
  
  viewOrder(): void {
    if(this.paymentResult?.orderId) {
      this.router.navigate(['/bill-history'], {
        queryParams: { orderId: this.paymentResult.orderId }
      });
    }
  }
  
  continueShopping(): void {
    this.router.navigate(['/menu']);
  }

  goToHome(): void {
    this.router.navigate(['/home']);
  }
}