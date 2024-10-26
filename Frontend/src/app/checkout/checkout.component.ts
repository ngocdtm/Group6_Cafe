import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../services/cart.service';
import { BillService } from '../services/bill.service';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { ProductService } from '../services/product.service';

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
    private router: Router
  ) {
    this.checkoutForm = this.formBuilder.group({
      customerName: ['', [Validators.required]],
      customerPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      shippingAddress: ['', [Validators.required]],
      paymentMethod: ['COD', [Validators.required]], // Default to COD
      couponCode: [''] // New form control for coupon
    });
  }

  ngOnInit(): void {
    // Check if user is logged in
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
        totalAfterDiscount: this.totalAfterDiscount
      };

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
  
}
