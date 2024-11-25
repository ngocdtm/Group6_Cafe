import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../services/category.service';
import { ProductImage, ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { SnackbarService } from '../services/snackbar.service';
import { LoginPromptComponent } from '../login-prompt/login-prompt.component';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { LoginComponent } from '../login/login.component';
import { InventoryService, InventoryWrapper } from '../services/inventory.service';
import { ReviewService } from '../services/review.service';

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
  inventoryMap: { [key: number]: InventoryWrapper } = {};
  productRatings: { [key: number]: any } = {};

  minPrice: number = 0;
  maxPrice: number = 1000000;
  currentMinPrice: number = 0;
  currentMaxPrice: number = 1000000;

  constructor(
    private categoryService: CategoryService,
    private productService: ProductService,
    private cartService: CartService,
    private reviewService: ReviewService,
    private userService: UserService,
    private inventoryService: InventoryService,
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
        // console.log('Categories loaded:', this.categories);
      },
      (error) => {
        console.error('Error loading categories:', error);
      }
    );
  }

  loadProducts() {
    this.productService.getProduct().subscribe(
      (data: any) => {
        this.products = data.filter((product: any) => 
          product.status !== "false" && product.deleted !== "true"
        );
        
        // Load images and ratings for each product
        this.products.forEach(product => {
          this.productService.getActiveImages(product.id).subscribe(
            (images: ProductImage[]) => {
              product.images = images;
            }
          );
          
          this.reviewService.getProductRating(product.id).subscribe(
            (rating: any) => {
              this.productRatings[product.id] = rating;
            }
          );
        });
        
        this.filteredProducts = [...this.products];
        this.loadInventoryInfo();
      }
    );
  }

  getRating(productId: number): number {
    return this.productRatings[productId]?.averageRating || 0;
  }

  getTotalReviews(productId: number): number {
    return this.productRatings[productId]?.totalReviews || 0;
  }

  loadInventoryInfo() {
    this.filteredProducts.forEach(product => {
      this.inventoryService.getInventoryStatus(product.id).subscribe(
        (inventory: InventoryWrapper) => {
          this.inventoryMap[product.id] = inventory;
        },
        error => {
          console.error(`Error loading inventory for product ${product.id}:`, error);
        }
      );
    });
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
      return matchesCategory && matchesPrice && product.status !== "false";
    });
   
    // console.log('Filtered products:', this.filteredProducts);
    this.loadInventoryInfo(); // Thêm dòng này để cập nhật inventory cho sản phẩm đã lọc
  }

  isOutOfStock(productId: number): boolean {
    return this.inventoryMap[productId]?.quantity === 0;
  }
 
  getInventoryQuantity(productId: number): number {
    return this.inventoryMap[productId]?.quantity || 0;
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
    // Kiểm tra nếu người dùng đã đăng nhập
    if (this.isLoggedIn) {
      // Lấy số lượng tồn kho hiện tại của sản phẩm từ inventoryMap
      const inventoryQuantity = this.getInventoryQuantity(product.id);
     
      // Kiểm tra số lượng sản phẩm hiện có trong giỏ hàng
      this.cartService.getCart().subscribe(cart => {
        const cartItem = cart.cartItems.find((item: any) => item.productId === product.id);
        const currentQuantityInCart = cartItem ? cartItem.quantity : 0;
 
        // Nếu tổng số lượng trong giỏ >= số lượng tồn kho, thông báo không thể thêm
        if (currentQuantityInCart >= inventoryQuantity) {
          this.snackbarService.openSnackBar("Không thể thêm vào giỏ hàng. Sản phẩm đã đạt đến giới hạn tồn kho.", "");
        } else {
          // Gọi API addToCart nếu chưa đạt giới hạn
          this.cartService.addToCart(product).subscribe(
            () => {
              this.snackbarService.openSnackBar("Sản phẩm đã được thêm vào giỏ hàng", "");
            },
            (error) => {
              console.error('Error adding to cart:', error);
              this.snackbarService.openSnackBar("Có lỗi xảy ra khi thêm vào giỏ hàng", "");
            }
          );
        }
      });
    } else {
      // Nếu chưa đăng nhập, hiển thị hộp thoại đăng nhập
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

  openProductDetail(product: any) {
    this.dialog.open(ProductDetailDialogComponent, {
      data: product,
      panelClass: 'product-detail-dialog'
    });
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }
 
  onPriceChange() {
    this.filterProducts();
  }

}