import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { UserService } from '../services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { LoginComponent } from '../login/login.component';
import { SnackbarService } from '../services/snackbar.service';
import { LoginPromptComponent } from '../login-prompt/login-prompt.component';


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


  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cartService: CartService,
    private userService: UserService,
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
        this.products = data;
        this.filteredProducts = [...this.products];
      },
      (error) => {
        console.error('Error loading products:', error);
      }
    );
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
      this.filteredProducts = [...this.products];
    } else {
      this.filteredProducts = this.products.filter(product => product.categoryId === categoryId);
    }
  }


  addToCart(product: any) {
    if (this.isLoggedIn) {
      this.cartService.addToCart(product);
      this.snackbarService.openSnackBar("Sản phẩm đã được thêm vào giỏ hàng", "");
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
    return 'assets/default-product-image.png'; // Đường dẫn đến ảnh mặc định
  }


}



