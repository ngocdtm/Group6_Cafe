import { Component, OnInit } from '@angular/core';
import { CartService } from '../services/cart.service';
import { ProductService } from '../services/product.service';
import { SnackbarService } from '../services/snackbar.service';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cartItems: any[] = [];
  totalAmount: number = 0;
  loading: boolean = false;
  isLoggedIn: boolean = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private dialog: MatDialog,
    private cartService: CartService,
    private productService: ProductService,
    private snackbarService: SnackbarService,
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Subscribe to login status changes
    this.subscriptions.push(
      this.userService.isLoggedIn().subscribe(loggedIn => {
        this.isLoggedIn = loggedIn;
        if (loggedIn) {
          this.loadCart();
        } else {
          this.cartItems = [];
          this.totalAmount = 0;
        }
      })
    );
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }  

  loadCart(): void {
    if (!this.isLoggedIn) {
      return;
    }

    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (response: any) => {
        this.cartItems = response.cartItems;
        this.loadProductDetails();
        this.totalAmount = response.totalAmount;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading cart:', error);
        this.loading = false;
        if (error.status === 401) {
          this.handleUnauthorized();
        } else {
          this.snackbarService.openSnackBar('Error loading cart', 'Close');
        }
      }
    });
  }

  private loadProductDetails(): void {
    this.cartItems.forEach(item => {
      this.productService.getById(item.productId).subscribe({
        next: (productData: any) => {
          item.product = productData;
        },
        error: (error) => {
          console.error('Error loading product details:', error);
          if (error.status === 401) {
            this.handleUnauthorized();
          }
        }
      });
    });
  }

  private handleUnauthorized(): void {
    this.userService.logout();
    this.snackbarService.openSnackBar('Session expired. Please login again.', 'Close');
    this.handleLoginAction();
  }

  getImageUrl(item: any): string {
    try {
      if (item?.product?.images?.[0]?.imagePath) {
        return this.productService.getImageUrl(item.product.images[0].imagePath);
      }
      // Return một ảnh placeholder online nếu không có ảnh local
      return 'https://via.placeholder.com/150';
    } catch (error) {
      // Fallback to placeholder
      return 'https://via.placeholder.com/150';
    }
  }

  handleLoginAction(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = "550px";
    const dialogRef = this.dialog.open(LoginComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.isLoggedIn) {
        this.loadCart();
      }
    });
  }

  updateQuantity(cartItemId: number, newQuantity: number): void {
    if (newQuantity < 1) return;

    const data = {
      cartItemId: cartItemId,
      quantity: newQuantity
    };

    this.cartService.updateCartItem(data).subscribe({
      next: () => {
        this.loadCart();
        this.snackbarService.openSnackBar('Cart updated successfully', '');
      },
      error: (error) => {
        console.error('Error updating cart:', error);
        if (error.status === 401) {
          this.handleUnauthorized();
        } else {
          this.snackbarService.openSnackBar('Error updating cart', 'Close');
        }
      }
    });
  }

  removeItem(cartItemId: number): void {
    this.cartService.removeFromCart(cartItemId).subscribe({
      next: () => {
        this.loadCart();
        this.updateCartCount();
        this.snackbarService.openSnackBar('Item removed from cart', '');
      },
      error: (error) => {
        console.error('Error removing item:', error);
        if (error.status === 401) {
          this.handleUnauthorized();
        } else {
          this.snackbarService.openSnackBar('Error removing item', 'Close');
        }
      }
    });
  }

  clearCart(): void {
    this.cartService.clearCart().subscribe({
      next: () => {
        this.loadCart();
        this.updateCartCount();
        this.snackbarService.openSnackBar('Cart cleared', '');
      },
      error: (error) => {
        console.error('Error clearing cart:', error);
        if (error.status === 401) {
          this.handleUnauthorized();
        } else {
          this.snackbarService.openSnackBar('Error clearing cart', 'Close');
        }
      }
    });
  }

  updateCartCount(): void {
    this.cartService.getCartItemCount().subscribe({
      next: (count) => {
        this.cartService.cartItemCountSubject.next(count);
      },
      error: (error) => {
        if (error.status === 401) {
          this.handleUnauthorized();
        }
      }
    });
  }

  proceedToCheckout(): void {
    if (this.cartItems.length > 0) {
      this.router.navigate(['/checkout']);
    } else {
      this.snackbarService.openSnackBar('Your cart is empty', '');
    }
  }


  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(price);
  }
}