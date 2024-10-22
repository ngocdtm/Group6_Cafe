import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';




@Component({
  selector: 'app-product-detail-dialog',
  templateUrl: './product-detail-dialog.component.html',
  styleUrls: ['./product-detail-dialog.component.scss']
})
export class ProductDetailDialogComponent implements OnInit {
  selectedImageIndex: number = 0;
  categoryName: string = '';


  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<ProductDetailDialogComponent>,
    private productService: ProductService,
    private categoryService: CategoryService
  ) {}


  ngOnInit() {
    this.loadCategoryName();
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


  close(): void {
    this.dialogRef.close();
  }
}
