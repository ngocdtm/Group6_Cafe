import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ProductImage, ProductService } from '../services/product.service';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

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
        // Lấy active images cho mỗi sản phẩm tìm được
        const productsWithImages = products.map((product: { id: number; }) => 
          this.productService.getActiveImages(product.id).pipe(
            map(activeImages => ({
              ...product,
              images: activeImages
            }))
          )
        );

        // Đợi tất cả các API calls hoàn thành
        if (productsWithImages.length > 0) {
          forkJoin(productsWithImages).subscribe(
            completedProducts => {
              this.searchResults = completedProducts;
              this.isLoading = false;
            },
            error => {
              console.error('Error loading search results images:', error);
              this.searchResults = [];
              this.isLoading = false;
            }
          );
        } else {
          this.searchResults = [];
          this.isLoading = false;
        }
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
    return 'assets/default-product-image.png';
  }

  openProductDetail(product: any) {
    this.dialog.open(ProductDetailDialogComponent, {
      data: product,
      panelClass: 'product-detail-dialog'
    });
  }

}