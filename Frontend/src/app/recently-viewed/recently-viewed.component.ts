import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { CartService } from '../services/cart.service';
import { SnackbarService } from '../services/snackbar.service';
import { LoginPromptComponent } from '../login-prompt/login-prompt.component';
import { LoginComponent } from '../login/login.component';
import { Subscription } from 'rxjs';

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

  constructor(
    private productService: ProductService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadRecentlyViewedProducts();

    // Subscribe to updates
    const updateSubscription = this.productService.recentlyViewedUpdate$.subscribe(() => {
      this.loadRecentlyViewedProducts();
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
    this.isLoading = true;
    const subscription = this.productService.getRecentlyViewedProducts().subscribe({
      next: (products) => {
        this.recentlyViewedProducts = products;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading recently viewed products:', error);
        this.error = 'Failed to load recently viewed products';
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