import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import { SnackbarService } from '../services/snackbar.service';
import { LoginPromptComponent } from '../login-prompt/login-prompt.component';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { InventoryService, InventoryWrapper } from '../services/inventory.service';


@Component({
  selector: 'app-best-seller',
  templateUrl: './best-seller.component.html',
  styleUrls: ['./best-seller.component.scss']
})
export class BestSellerComponent implements OnInit {


  products: any[] = [];
  categories: any[] = [];
  filteredProducts: any[] = [];
  selectedCategory: string = 'all';
  isLoggedIn: boolean = false;
  inventoryMap: { [key: number]: InventoryWrapper } = {};


  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cartService: CartService,
    private userService: UserService,
    private inventoryService: InventoryService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService
  ) { }


  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
    this.userService.isLoggedIn().subscribe(
      loggedIn => this.isLoggedIn = loggedIn
    );
  }


  loadProducts() {
    this.productService.getProduct().subscribe(
      (data: any) => {
        // Lọc sản phẩm có status true ngay khi load
        this.products = data.filter((product: any) => product.status !== "false");
        this.filteredProducts = [...this.products];
        // Load inventory information for each product
        this.loadInventoryInfo();
      },
      (error) => {
        console.error('Error loading products:', error);
      }
    );
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


  loadCategories() {
    this.categoryService.getCategory().subscribe(
      (data: any) => {
        this.categories = data;
      },
      (error) => {
        console.error('Error loading categories:', error);
      }
    );
  }


  filterByCategory(categoryId: string) {
    this.selectedCategory = categoryId;
    if (categoryId === 'all') {
      this.filteredProducts = this.products.filter(product => product.status !== "false");
    } else {
      this.filteredProducts = this.products.filter(product =>
        product.categoryId === categoryId && product.status !== "false"
      );
    }
  }


  isOutOfStock(productId: number): boolean {
    return this.inventoryMap[productId]?.quantity === 0;
  }
 
  getInventoryQuantity(productId: number): number {
    return this.inventoryMap[productId]?.quantity || 0;
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
    return 'assets/default-product-image.png'; // Đường dẫn đến ảnh mặc định
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
}

