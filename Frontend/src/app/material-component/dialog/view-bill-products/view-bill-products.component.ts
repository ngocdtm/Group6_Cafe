import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-view-bill-products',
  templateUrl: './view-bill-products.component.html',
  styleUrls: ['./view-bill-products.component.scss']
})
export class ViewBillProductsComponent implements OnInit {

  displayedColumns: string[] = ['name', 'category', 'price', 'quantity', 'total'];
  dataSource: any[] = [];
  data: any;

  constructor(@Inject(MAT_DIALOG_DATA) public dialogData:any,
  public dialogRef: MatDialogRef<ViewBillProductsComponent>) { }

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
