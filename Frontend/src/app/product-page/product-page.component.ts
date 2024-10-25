import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { SnackbarService } from '../services/snackbar.service';
import { LoginPromptComponent } from '../login-prompt/login-prompt.component';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { LoginComponent } from '../login/login.component';


@Component({
  selector: 'app-product-page',
  templateUrl: './product-page.component.html',
  styleUrls: ['./product-page.component.scss']
})
export class ProductPageComponent implements OnInit {
  categories: any[] = [];
  products: any[] = [];
  filteredProducts: any[] = [];
  selectedCategory: string = 'all';
  isLoggedIn: boolean = false;


  minPrice: number = 0;
  maxPrice: number = 1000000;
  currentMinPrice: number = 0;
  currentMaxPrice: number = 1000000;


  constructor(
    private categoryService: CategoryService,
    private productService: ProductService,
    private cartService: CartService,
    private userService: UserService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService
  ) { }


  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
    this.userService.isLoggedIn().subscribe(
      loggedIn => this.isLoggedIn = loggedIn
    );
  }


  loadCategories() {
    this.categoryService.getCategory().subscribe(
      (data: any) => {
        this.categories = data;
        console.log('Categories loaded:', this.categories);
      },
      (error) => {
        console.error('Error loading categories:', error);
      }
    );
  }


  loadProducts() {
    this.productService.getProduct().subscribe(
      (data: any) => {
        this.products = data;
        console.log('Products loaded:', this.products);
        this.updatePriceRange();
        this.filterProducts();
      },
      (error) => {
        console.error('Error loading products:', error);
      }
    );
  }


  updatePriceRange() {
    if (this.products.length > 0) {
      this.minPrice = Math.min(...this.products.map(p => p.price));
      this.maxPrice = Math.max(...this.products.map(p => p.price));
      this.currentMinPrice = this.minPrice;
      this.currentMaxPrice = this.maxPrice;
    }
  }


  filterByCategory(categoryId: string) {
    this.selectedCategory = categoryId;
    this.filterProducts();
  }


  filterProducts() {
    this.filteredProducts = this.products.filter(product => {
      const matchesCategory = this.selectedCategory === 'all' || product.categoryId === this.selectedCategory;
      const matchesPrice = product.price >= this.currentMinPrice && product.price <= this.currentMaxPrice;
      return matchesCategory && matchesPrice;
    });
   
    console.log('Filtered products:', this.filteredProducts);
  }


  onMinPriceChange(event: any) {
    this.currentMinPrice = parseFloat(event.target.value);
    this.filterProducts();
  }


  onMaxPriceChange(event: any) {
    this.currentMaxPrice = parseFloat(event.target.value);
    this.filterProducts();
  }


  addToCart(product: any) {
    if (this.isLoggedIn) {
      this.cartService.addToCart(product).subscribe(
        () => {
          this.snackbarService.openSnackBar("Sản phẩm đã được thêm vào giỏ hàng", "");
        },
        (error) => {
          console.error('Error adding to cart:', error);
          this.snackbarService.openSnackBar("Có lỗi xảy ra khi thêm vào giỏ hàng", "");
        }
      );
    } else {
      this.showLoginPrompt(product);
    }
  }


  showLoginPrompt(product: any) {
    const dialogRef = this.dialog.open(LoginPromptComponent, {
      width: '300px'
    });


    dialogRef.afterClosed().subscribe(result => {
      if (result === 'login') {
        this.openLoginDialog(product);
      }
    });
  }

  openLoginDialog(product: any) {
    const dialogRef = this.dialog.open(LoginComponent, {
      width: '350px'
    });


    dialogRef.afterClosed().subscribe(result => {
      if (result === 'success') {
        this.cartService.addToCart(product);
        this.snackbarService.openSnackBar("Sản phẩm đã được thêm vào giỏ hàng", "");
      }
    });
  }

  getFirstImageUrl(product: any): string {
    if (product.images && product.images.length > 0) {
      return this.productService.getImageUrl(product.images[0].imagePath);
    }
    return 'assets/default-product-image.png';
  }


  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }
 
  onPriceChange() {
    this.filterProducts();
  }

  openProductDetail(product: any) {
    this.dialog.open(ProductDetailDialogComponent, {
      data: product,
      panelClass: 'product-detail-dialog'
    });
  }
}

