import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '../services/cart.service';
import { BillService } from '../services/bill.service';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { ProductService } from '../services/product.service';
import { VnpayService } from '../services/vnpay.service';
import { timeStamp } from 'console';
@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})


export class CheckoutComponent implements OnInit {
  checkoutForm: FormGroup;
  cartItems: any[] = [];
  totalAmount: number = 0;
  loading: boolean = false;

  // New coupon-related properties
  appliedCoupon: any = null;
  discountAmount: number = 0;
  totalAfterDiscount: number = 0;
  applyingCoupon: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private cartService: CartService,
    private billService: BillService,
    private productService: ProductService,
    private userService: UserService,
    private snackbarService: SnackbarService,
    private router: Router,
    private vnpayService: VnpayService,
    private route: ActivatedRoute
  ) {
    this.checkoutForm = this.formBuilder.group({
      customerName: ['', [Validators.required]],
      customerPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      shippingAddress: ['', [Validators.required]],
      paymentMethod: ['', [Validators.required]], // Default to COD
      couponCode: [''] // New form control for coupon
    });
  }

  ngOnInit(): void {
    // Check if user is logged in
    this.route.queryParams.subscribe(params => {
      if (params['vnp_ResponseCode']) {
        this.handleVNPayCallback(params);
      } else {
        // Normal checkout page load
        this.userService.isLoggedIn().subscribe(loggedIn => {
          if (!loggedIn) {
            this.router.navigate(['/login']);
            return;
          }
          this.loadUserInfo();
          this.loadUserProfile();
          this.loadCart();
        });
      }
    });
  }
  private loadUserInfo(): void {
    const userDetails = this.userService.getUserDetails();
    
    if (userDetails) {
      this.checkoutForm.patchValue({
        customerName: userDetails.name || '',
        customerPhone: userDetails.phoneNumber || '',
        shippingAddress: userDetails.address || ''
      });
    }
  }

  private loadUserProfile(): void {
    this.userService.getProfile().subscribe({
      next: (profile: any) => {
        // Only update form fields that are empty
        const currentName = this.checkoutForm.get('customerName')?.value;
        const currentPhone = this.checkoutForm.get('customerPhone')?.value;
        const currentAddress = this.checkoutForm.get('shippingAddress')?.value;
        this.checkoutForm.patchValue({
          customerName: currentName || profile.name || '',
          customerPhone: currentPhone || profile.phoneNumber || '',
          shippingAddress: currentAddress || profile.address || ''
        });
      },
      error: (error) => {
        console.error('Error loading user profile:', error);
      }
    });
  }

  private loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (response: any) => {
        this.cartItems = response.cartItems;
        this.loadProductDetails();
        this.totalAmount = response.totalAmount;
        this.totalAfterDiscount = this.totalAmount; // Initialize total after discount
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading cart:', error);
        this.loading = false;
        this.snackbarService.openSnackBar('Error loading cart items', 'Close');
        this.router.navigate(['/cart']);
      }
    });
  }

  applyCoupon(): void {
    const couponCode = this.checkoutForm.get('couponCode')?.value;
    if (!couponCode) {
      this.snackbarService.openSnackBar('Please enter a coupon code', 'Close');
      return;
    }

    this.applyingCoupon = true;
    this.billService.applyCoupon({
      couponCode: couponCode,
      total: this.totalAmount
    }).subscribe({
      next: (response: any) => {
        this.appliedCoupon = {
          code: response.couponCode,
          discount: response.discount
        };
        this.discountAmount = response.discountAmount;
        this.totalAfterDiscount = response.totalAfterDiscount;
        this.snackbarService.openSnackBar('Coupon applied successfully', 'Close');
      },
      error: (error) => {
        console.error('Error applying coupon:', error);
        this.snackbarService.openSnackBar(
          error.error?.message || 'Error applying coupon',
          'Close'
        );
        this.removeCoupon();
      },
      complete: () => {
        this.applyingCoupon = false;
      }
    });
  }

  // New method to remove applied coupon
  removeCoupon(): void {
    this.appliedCoupon = null;
    this.discountAmount = 0;
    this.totalAfterDiscount = this.totalAmount;
    this.checkoutForm.patchValue({ couponCode: '' });
  }

  private loadProductDetails(): void {
    this.cartItems.forEach(item => {
      this.productService.getById(item.productId).subscribe({
        next: (productData: any) => {
          item.product = productData;
        },
        error: (error) => {
          console.error('Error loading product details:', error);
        }
      });
    });
  }

  getImageUrl(item: any): string {
    try {
      if (item?.product?.images?.[0]?.imagePath) {
        return this.productService.getImageUrl(item.product.images[0].imagePath);
      }
      return 'https://via.placeholder.com/150';
    } catch (error) {
      return 'https://via.placeholder.com/150';
    }
  }

  onSubmit(): void {
    if (this.checkoutForm.valid && this.cartItems.length > 0) {
      this.loading = true;

      // Prepare order data with coupon information
      const orderData = {
        ...this.checkoutForm.value,
        productDetails: JSON.stringify(this.cartItems.map(item => ({
          id: item.productId,
          quantity: item.quantity,
          price: item.product.price
        }))),
        total: this.totalAmount,
        couponCode: this.appliedCoupon?.code || null,
        discount: this.discountAmount,
        totalAfterDiscount: this.totalAfterDiscount,
        paymentMethod: this.checkoutForm.get('paymentMethod')?.value
      };
      if (orderData.paymentMethod === 'VNPAY'){
        //process vnpay payment
        this.processVnpayPayment();
      } else if(orderData.paymentMethod === 'COD') {

      // Process online order
      this.billService.processOnlineOrder(orderData).subscribe({
        next: (response) => {
          this.loading = false;
          this.snackbarService.openSnackBar('Order placed successfully!', 'Close');
          
          // Download bill PDF
          this.downloadBill(response.uuid);
          
          // Clear cart and update cart count immediately before redirecting
        this.cartService.clearCart().subscribe(() => {
          // Cập nhật cart count về 0 ngay lập tức
          this.cartService.cartItemCountSubject.next(0);
          
          // Redirect to order confirmation
          this.router.navigate(['/order-confirmation'], {
            queryParams: { orderId: response.uuid }
          });
        });
      },
        error: (error) => {
          this.loading = false;
          console.error('Error processing order:', error);
          this.snackbarService.openSnackBar('Error placing order', 'Close');
        }
      });
    }else{
      this.loading = false;
      this.snackbarService.openSnackBar('Please select a payment method', 'Close');
    }
   } else {
      this.snackbarService.openSnackBar('Please fill all required fields', 'Close');
    }
  }

  private downloadBill(uuid: string): void {
    this.billService.getPdf({ uuid: uuid }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `bill-${uuid}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading bill:', error);
      }
    });
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(price);
  }

  private handleVNPayCallback(params: any): void {
    this.loading = true;
  
    this.vnpayService.verifyPayment(params).subscribe({
      next: (response) => {
        this.loading = false;
  
        if (response.status === 'success') {
          // Xóa giỏ hàng và chuyển hướng sang trang success
          this.cartService.clearCart().subscribe(() => {
            this.cartService.cartItemCountSubject.next(0);
            // Chuyển hướng trực tiếp đến trang payment-success với params
            this.router.navigate(['/payment-success'], {
              queryParams: {
                orderId: params.vnp_TxnRef,
                transactionNo: params.vnp_TransactionNo,
                status: 'success'
              }
            });
          });
        } else {
          this.snackbarService.openSnackBar(
            'Payment failed: ' + (response.message || 'Unknown error'),
            'Close'
          );
          this.router.navigate(['/cart']);
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Payment verification error:', error);
        this.snackbarService.openSnackBar(
          'Error verifying payment. Please contact support.',
          'Close'
        );
        this.router.navigate(['/cart']);
      }
    });
  }

 private processVnpayPayment(): void {
    if (!this.totalAfterDiscount || this.totalAfterDiscount <= 0) {
      this.snackbarService.openSnackBar('Invalid payment amount', 'Close');
      return;
    }

    this.loading = true;
    const orderUuid = this.generateOrderId();

    this.vnpayService.createPayment({
      amount: this.totalAfterDiscount,
      orderId: orderUuid,
      customerName: this.checkoutForm.get('customerName')?.value,
      customerPhone: this.checkoutForm.get('customerPhone')?.value,
      shippingAddress: this.checkoutForm.get('shippingAddress')?.value,
      total: this.totalAmount,
      discount: this.discountAmount,
      totalAfterDiscount: this.totalAfterDiscount,
      couponCode: this.appliedCoupon?.code,
      bankCode: ''
    }).subscribe({
      next: (response: PaymentResponse) => {
        if (response.paymentUrl) {
          // Redirect tới trang thanh toán VNPAY
          window.location.href = response.paymentUrl;
        } else {
          this.snackbarService.openSnackBar('Payment failed. Please try again.', 'Close');
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Error creating payment:', error);
        this.snackbarService.openSnackBar(
          error.error?.message || 'Error creating payment',
          'Close'
        );
        this.loading = false;
      }
    });
}
  private generateOrderId(): string {
    return 'ORDER-' + new Date().getTime() + '-' + Math.random().toString(36).substr(2,9);
  }
}


export interface PaymentResponse {
  orderId: string;
  paymentUrl?: string; // thêm thuộc tính paymentUrl
  status: string;
  message?: string;
}