import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { forkJoin, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-recently-viewed',
  templateUrl: './recently-viewed.component.html',
  styleUrls: ['./recently-viewed.component.scss']
})
export class RecentlyViewedComponent implements OnInit {

  recentlyViewedProducts: any[] = [];
  isLoading: boolean = true;
  error: string = '';
  private subscriptions: Subscription[] = [];
  isLoggedIn: boolean = false;

  constructor(
    private productService: ProductService,
    private dialog: MatDialog,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Subscribe to login status first
    const loginSubscription = this.userService.isLoggedIn().subscribe(
      loggedIn => {
        this.isLoggedIn = loggedIn;
        if (loggedIn) {
          this.loadRecentlyViewedProducts();
        } else {
          this.recentlyViewedProducts = []; // Reset products when logged out
          this.isLoading = false; // Stop loading state
        }
      }
    );
    this.subscriptions.push(loginSubscription);
  
    // Subscribe to updates (only when logged in)
    const updateSubscription = this.productService.recentlyViewedUpdate$.subscribe(() => {
      if (this.isLoggedIn) {
        this.loadRecentlyViewedProducts();
      }
    });
    this.subscriptions.push(updateSubscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  getImageUrl(imagePath: string): string {
    return this.productService.getImageUrl(imagePath);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }

  scrollLeft(): void {
    const container = document.querySelector('.products-scroll');
    if (container) {
      container.scrollBy({ left: -200, behavior: 'smooth' });
    }
  }

  scrollRight(): void {
    const container = document.querySelector('.products-scroll');
    if (container) {
      container.scrollBy({ left: 200, behavior: 'smooth' });
    }
  }

  loadRecentlyViewedProducts(): void {
    if (!this.isLoggedIn) {
      this.isLoading = false;
      return;
    }
    
    this.isLoading = true;
    const subscription = this.productService.getRecentlyViewedProducts().subscribe({
      next: (products) => {
        // Load active images for each product
        const productsWithActiveImages = products.map((product: { id: number; }) =>
          this.productService.getActiveImages(product.id).pipe(
            map(activeImages => ({
              ...product,
              images: activeImages
            }))
          )
        );

        // Wait for all active images to be loaded
        forkJoin(productsWithActiveImages).subscribe({
          next: (completedProducts) => {
            this.recentlyViewedProducts = completedProducts;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading product images:', error);
            if (this.isLoggedIn) {
              this.error = 'Failed to load product images';
            }
            this.isLoading = false;
          }
        });
      },
      error: (error) => {
        console.error('Error loading recently viewed products:', error);
        if (this.isLoggedIn) {
          this.error = 'Failed to load recently viewed products';
        }
        this.isLoading = false;
      }
    });
    this.subscriptions.push(subscription);
  }

  openProductDetails(product: any): void {
    const dialogRef = this.dialog.open(ProductDetailDialogComponent, {
      panelClass: 'product-detail-dialog',
      data: product
    });

    const dialogSubscription = dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.openProductDetails(result);
      }
    });
    this.subscriptions.push(dialogSubscription);
  }
}