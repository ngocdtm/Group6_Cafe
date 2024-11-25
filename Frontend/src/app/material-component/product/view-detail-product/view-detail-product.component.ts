import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProductService } from 'src/app/services/product.service';

@Component({
  selector: 'app-view-detail-product',
  templateUrl: './view-detail-product.component.html',
  styleUrls: ['./view-detail-product.component.scss']
})
export class ViewDetailProductComponent implements OnInit {
  displayedColumns: string[] = ['name', 'categoryName', 'originalPrice', 'price', "description", "images"];
  dataSource: any[] = [];
  data: any;
  imageUrls: string[] = [];
  onEditProduct: any;
  onDeleteProduct: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    public dialogRef: MatDialogRef<ViewDetailProductComponent>,
    private productService: ProductService
  ) { }

  ngOnInit() {
    this.data = this.dialogData.data;
    // Process images if they exist
    if (this.data?.images && Array.isArray(this.data.images)) {
      this.imageUrls = this.data.images.map((image: any) => 
        this.productService.getImageUrl(image.imagePath)
      );
    }
    console.log(this.dialogData.data);
  }

  // Helper method to handle image errors
  handleImageError(event: any) {
    event.target.src = 'assets/images/placeholder.png'; // Replace with your placeholder image path
  }
}