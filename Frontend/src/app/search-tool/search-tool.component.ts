import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ProductImage, ProductService } from '../services/product.service';
import { ProductDetailDialogComponent } from '../material-component/dialog/product-detail-dialog/product-detail-dialog.component';
import { debounceTime, distinctUntilChanged, map, switchMap } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-search-tool',
  templateUrl: './search-tool.component.html',
  styleUrls: ['./search-tool.component.scss']
})
export class SearchToolComponent implements OnInit {
  isSearchActive = false;
  searchControl = new FormControl('');
  searchResults: any[] = [];
  isLoading = false;

  constructor(
    private productService: ProductService,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit() {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        if (!term || term.length < 2) return of([]);
        this.isLoading = true;
        return this.productService.searchProducts(term).pipe(
          // Sau khi có kết quả search, lấy active images cho mỗi sản phẩm
          switchMap(products => {
            if (products.length === 0) return of([]);
            
            const productsWithImages = products.slice(0, 5).map((product: { id: number; }) =>
              this.productService.getActiveImages(product.id).pipe(
                map(activeImages => ({
                  ...product,
                  images: activeImages
                }))
              )
            );

            return forkJoin(productsWithImages);
          })
        );
      })
    ).subscribe(
      (results: any) => {
        this.searchResults = results.slice(0, 5); // Limit to 5 suggestions
        this.isLoading = false;
      },
      error => {
        console.error('Search error:', error);
        this.isLoading = false;
      }
    );
  }

  toggleSearch() {
    this.isSearchActive = !this.isSearchActive;
    if (!this.isSearchActive) {
      this.clearSearch();
    } else {
      setTimeout(() => {
        const input = document.querySelector('.search-input') as HTMLElement;
        if (input) input.focus();
      }, 100);
    }
  }

  clearSearch() {
    this.searchResults = [];
    this.searchControl.setValue('');
  }

  handleEnter() {
    const searchTerm = this.searchControl.value;
    if (searchTerm) {
      this.router.navigate(['/search'], { 
        queryParams: { q: searchTerm }
      });
      this.isSearchActive = false;
      this.clearSearch();
    }
  }

  getFirstImageUrl(product: any): string {
    if (product.images && product.images.length > 0) {
      return this.productService.getImageUrl(product.images[0].imagePath);
    }
    return 'assets/default-product-image.png';
  }

  openProductDetail(product: any) {
    // Không cần gọi lại API vì đã có active images
    this.dialog.open(ProductDetailDialogComponent, {
      data: product,
      panelClass: 'product-detail-dialog'
    });
  }
  
}