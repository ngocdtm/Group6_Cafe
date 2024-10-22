import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ProductService } from '../services/product.service';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';

@Component({
  selector: 'app-search-page',
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.scss']
})
export class SearchPageComponent implements OnInit {
  searchResults: any[] = [];
  searchTerm: string = '';
  isLoading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private productService: ProductService
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.searchTerm = params['q'] || '';
      if (this.searchTerm) {
        this.loadSearchResults();
      }
    });
  }

  loadSearchResults() {
    this.isLoading = true;
    this.productService.searchProducts(this.searchTerm).subscribe(
      (products: any) => {
        this.searchResults = products;
        this.isLoading = false;
      },
      error => {
        console.error('Error loading products:', error);
        this.searchResults = [];
        this.isLoading = false;
      }
    );
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
      width: '800px',
      panelClass: 'product-dialog'
    });
  }
}