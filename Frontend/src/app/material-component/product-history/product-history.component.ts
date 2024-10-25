import { Component, OnInit } from '@angular/core';
import { ProductService } from 'src/app/services/product.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-product-history',
  templateUrl: './product-history.component.html',
  styleUrls: ['./product-history.component.scss']
})
export class ProductHistoryComponent implements OnInit {
  historyProducts: any[] = [];

 
  constructor(
    private productService: ProductService,
    private userService: UserService
  ) { }

  ngOnInit() {
    this.userService.isLoggedIn().subscribe((loggedIn: boolean) => {
      console.log('Logged In:', loggedIn);  // Kiểm tra trạng thái đăng nhập
      if (loggedIn) {
        this.userService.getUserId().subscribe((userId: number | null) => {
          console.log('User ID:', userId);  // Kiểm tra ID người dùng
          if (userId) {
            this.loadHistory(userId);
          }
        });
      }
    });
  }
  
  loadHistory(userId: number) {
    this.productService.getProductHistory(userId).subscribe(
      (data: any) => {
        console.log('API Response:', data);  // Kiểm tra dữ liệu trả về từ API
        this.historyProducts = data;
      },
      error => console.error('Error loading history:', error)
    );
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
 
}