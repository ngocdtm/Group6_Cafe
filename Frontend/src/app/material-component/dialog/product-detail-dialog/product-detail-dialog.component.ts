import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CategoryService } from 'src/app/services/category.service';
import { ProductImage, ProductService, RelatedProduct } from 'src/app/services/product.service';
import { ReviewService } from 'src/app/services/review.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-product-detail-dialog',
  templateUrl: './product-detail-dialog.component.html',
  styleUrls: ['./product-detail-dialog.component.scss']
})

export class ProductDetailDialogComponent implements OnInit {

  selectedImageIndex: number = 0;
  categoryName: string = '';
  relatedProducts: RelatedProduct[] = [];
  isLoading: boolean = true;

  // rating & comment
  productRating: any;
  reviews: any[] = [];
  currentPage: number = 0;
  pageSize: number = 5;
  totalReviews: number = 0;
  isLoadingReviews: boolean = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<ProductDetailDialogComponent>,
    private productService: ProductService,
    private reviewService: ReviewService,
    private userService: UserService,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    if (this.data?.id) {
      this.productService.addToRecentlyViewed(this.data.id).subscribe({
        next: () => {
          console.log('Added to recently viewed');
        },
        error: (error) => console.error('Error adding to recently viewed:', error)
      });
    }
    this.loadCategoryName();
    this.loadRelatedProducts();
    this.loadProductRating();
    this.loadReviews();
  }

  loadProductRating() {
    if (this.data?.id) {
      this.reviewService.getProductRating(this.data.id).subscribe(
        rating => this.productRating = rating
      );
    }
  }

  loadReviews() {
    if (this.data?.id) {
      this.isLoadingReviews = true;
      this.reviewService.getProductReviews(this.data.id, this.currentPage, this.pageSize)
        .subscribe(
          (response: any) => {
            // Map reviews to ensure avatar is processed correctly
            const processedReviews = response.map((review: any) => ({
              ...review,
              userAvatar: review.userAvatar || null  // Ensure avatar field exists
            }));
  
            // Append or replace reviews based on current page
            this.reviews = this.currentPage === 0 
              ? processedReviews 
              : [...this.reviews, ...processedReviews];
  
            this.isLoadingReviews = false;
            this.totalReviews = this.reviews.length;
          },
          error => {
            console.error('Error loading reviews:', error);
            this.isLoadingReviews = false;
          }
        );
    }
  }

  loadMoreReviews() {
    this.currentPage++;
    this.loadReviews();
  }

  getReviewImageUrl(imagePath: string): string {
    return this.reviewService.getImageUrl(imagePath);
  }

  getUserAvatarUrl(avatarPath: string): string {
    return avatarPath ? this.userService.getAvatar(avatarPath) : 'assets/default-avatar.png';
  }

  loadCategoryName() {
    this.categoryService.getCategory().subscribe(
      (categories: any) => {
        const category = categories.find((cat: any) => cat.id === this.data.categoryId);
        this.categoryName = category ? category.name : 'Unknown Category';
      },
      (error) => {
        console.error('Error loading category:', error);
        this.categoryName = 'Unknown Category';
      }
    );
  }

  getImageUrl(imagePath: string): string {
    return this.productService.getImageUrl(imagePath);
  }

  selectImage(index: number): void {
    this.selectedImageIndex = index;
  }

  loadRelatedProducts() {
    if (!this.data?.id) {
      console.error('No product ID provided');
      return;
    }
  
    this.isLoading = true;
  
    this.productService.getRelatedProducts(this.data.id).subscribe({
      next: (products: RelatedProduct[]) => {
        // Lấy active images cho mỗi sản phẩm liên quan
        const productsWithImages: Observable<RelatedProduct>[] = products.map((product) =>
          this.productService.getActiveImages(product.id).pipe(
            map((activeImages: ProductImage[]) => ({
              ...product,
              images: activeImages,
            }))
          )
        );
  
        // Đợi tất cả các Observable hoàn thành
        forkJoin(productsWithImages).subscribe({
          next: (completedProducts: RelatedProduct[]) => {
            this.relatedProducts = completedProducts;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading related products images:', error);
            this.isLoading = false;
          },
        });
      },
      error: (error) => {
        console.error('Error loading related products:', error);
        this.relatedProducts = [];
        this.isLoading = false;
      },
    });
  }

  openProduct(product: any): void {
    // console.log('Opening product:', product); // Để debug
    this.dialogRef.close(product);
  }

  close(): void {
    this.dialogRef.close();
  }
}