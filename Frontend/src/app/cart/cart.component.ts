import { Component, OnInit } from '@angular/core';
import { CartService } from '../services/cart.service';
import { ProductService } from '../services/product.service';
import { SnackbarService } from '../services/snackbar.service';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import { of, Subscription } from 'rxjs';
import { InventoryService } from '../services/inventory.service';
import { catchError } from 'rxjs/operators';


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
  hasOutOfStockItems: boolean = false;
  private subscriptions: Subscription[] = [];


  constructor(
    private dialog: MatDialog,
    private cartService: CartService,
    private productService: ProductService,
    private inventoryService: InventoryService,
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
        this.totalAmount = response.totalAmount;
        this.hasOutOfStockItems = response.hasOutOfStockItems;
        this.loadProductAndInventoryDetails();
        this.loading = false;
       
        if (this.hasOutOfStockItems) {
          this.snackbarService.openSnackBar(
            'Some items in your cart are out of stock. Please review and remove them.',
            'Close',
          );
        }
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


  private loadProductAndInventoryDetails(): void {
    this.cartItems.forEach(item => {
      this.productService.getById(item.productId).pipe(
        catchError(error => {
          if (error.status === 401) {
            this.handleUnauthorized();
          }
          return of(null);
        })
      ).subscribe((productData: any) => {
        if (productData) {
          item.product = productData;
         
          // Load inventory status
          this.inventoryService.getInventoryStatus(item.productId).pipe(
            catchError(error => {
              console.error('Error loading inventory:', error);
              return of(null);
            })
          ).subscribe(inventoryData => {
            if (inventoryData) {
              item.inventory = {
                quantity: inventoryData.quantity,
                status: productData.status
              };
               // Thêm flag để kiểm tra trạng thái hết hàng
               item.isOutOfStock = inventoryData.quantity === 0;
               // Lưu số lượng có sẵn để kiểm tra khi cập nhật
               item.availableQuantity = inventoryData.quantity;
            }
          });
        }
      });
    });
  }


  private handleUnauthorized(): void {
    this.userService.logout();
    this.snackbarService.openSnackBar('Session expired. Please login again.', 'Close');
    const dialogConfig = {
      width: '400px',
      disableClose: true,
      data: { returnUrl: '/cart' }
    };
    this.dialog.open(LoginComponent, dialogConfig);
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


    const item = this.cartItems.find(i => i.id === cartItemId);
    if (!item || item.isOutOfStock) return; // Prevent updates for out of stock items


    // Check if new quantity exceeds available inventory
    if (item.availableQuantity && newQuantity > item.availableQuantity) {
      this.snackbarService.openSnackBar(
        `Cannot add more than available stock (${item.availableQuantity})`,
        'Close'
      );
      return;
    }


    const data = {
      cartItemId: cartItemId,
      quantity: newQuantity
    };


    this.cartService.updateCartItem(data).subscribe({
      next: () => {
        this.loadCart();
        this.snackbarService.openSnackBar('Cart updated successfully', 'Close');
      },
      error: (error) => {
        console.error('Error updating cart:', error);
        if (error.status === 401) {
          this.handleUnauthorized();
        } else {
          const errorMessage = error.error?.message || 'Error updating cart';
          this.snackbarService.openSnackBar(errorMessage, 'Close');
          this.loadCart();
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


  onQuantityInput(cartItemId: number, event: any): void {
    let inputQuantity = parseInt(event.target.value, 10);
   
    // Kiểm tra số lượng hợp lệ
    if (isNaN(inputQuantity) || inputQuantity < 1) {
      inputQuantity = 1;
    }
 
    const item = this.cartItems.find(i => i.id === cartItemId);
    if (!item) return;
 
    // Giới hạn số lượng tối đa bằng số lượng tồn kho
    if (item.availableQuantity && inputQuantity > item.availableQuantity) {
      inputQuantity = item.availableQuantity;
    }
 
    // Cập nhật số lượng nếu thay đổi
    if (inputQuantity !== item.quantity) {
      this.updateQuantity(cartItemId, inputQuantity);
    }
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
    if (!this.cartItems.length) {
      this.snackbarService.openSnackBar('Your cart is empty', 'Close');
      return;
    }


    // Kiểm tra các sản phẩm out of stock trước khi checkout
    const outOfStockItems = this.cartItems.filter(item => item.isOutOfStock);
    if (outOfStockItems.length > 0) {
      this.snackbarService.openSnackBar(
        'Please remove out of stock items before proceeding to checkout',
        'Close'
      );
      return;
    }


    if (this.isLoggedIn) {
      this.router.navigate(['/checkout']);
    } else {
      this.handleUnauthorized();
    }
  }


  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }


}

