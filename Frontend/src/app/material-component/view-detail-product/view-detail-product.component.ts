import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-view-detail-product',
  templateUrl: './view-detail-product.component.html',
  styleUrls: ['./view-detail-product.component.scss']
})
export class ViewDetailProductComponent implements OnInit {
  displayedColumns: string[] = ['name', 'categoryName', 'description', 'price'];
  dataSource: any[] = [];
  data: any;
  constructor(@Inject(MAT_DIALOG_DATA) public dialogData:any,
  public dialogRef: MatDialogRef<ViewDetailProductComponent>) { }

  ngOnInit() {
    this.data = this.dialogData.data;
    if (this.data && this.data.productDetails) {
      try {
        this.dataSource = JSON.parse(this.data.productDetails);
      } catch (error) {
        console.error('Error parsing productDetail JSON:', error);
        this.dataSource = [];
      }
    } else {
      console.error('productDetail is undefined or null');
      this.dataSource = [];
    }
    console.log(this.dialogData.data);
  }
}
