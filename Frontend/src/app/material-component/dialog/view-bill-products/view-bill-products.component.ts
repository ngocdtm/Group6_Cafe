import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

interface BillItem {
  productName: string;
  productId: number;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-view-bill-products',
  templateUrl: './view-bill-products.component.html',
  styleUrls: ['./view-bill-products.component.scss']
})
export class ViewBillProductsComponent implements OnInit {
  displayedColumns: string[] = ['name', 'price', 'quantity', 'total'];
  dataSource: any[] = [];
  data: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    public dialogRef: MatDialogRef<ViewBillProductsComponent>
  ) {}

  ngOnInit() {
    this.data = this.dialogData.data;
    if (this.data && this.data.billItems) {
      this.dataSource = this.data.billItems.map((item: BillItem) => ({
        name: item.productName,
        price: item.price,
        quantity: item.quantity,
        total: item.price * item.quantity
      }));
      console.log('Processed bill items:', this.dataSource);
    } else {
      console.error('billItems is undefined or null');
      this.dataSource = [];
    }
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }
}