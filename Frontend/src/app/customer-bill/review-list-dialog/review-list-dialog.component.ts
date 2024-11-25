import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ReviewService } from 'src/app/services/review.service';
import { ReviewDialogComponent } from '../review-dialog/review-dialog.component';
import { PlatformService } from 'src/app/services/platform.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-review-list-dialog',
  templateUrl: './review-list-dialog.component.html',
  styleUrls: ['./review-list-dialog.component.scss'],
  providers: [DatePipe]
})
export class ReviewListDialogComponent implements OnInit {

  billItems: any[] = [];
  reviewsMap: {[key: number]: any} = {};

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {bill: any},
    private reviewService: ReviewService,
    private dialog: MatDialog,
    private platformService: PlatformService,
    public dialogRef: MatDialogRef<ReviewListDialogComponent>
  ) {
    this.billItems = this.data.bill.billItems;
  }

  ngOnInit() {
    this.loadReviews();
  }

  loadReviews() {
    this.reviewService.getReviewsByBillId(this.data.bill.id)
      .subscribe(reviews => {
        this.reviewsMap = {};
        reviews.forEach((review: any) => {
          this.reviewsMap[review.productId] = review;
        });
      });
  }

  getImageUrl(imagePath: string): string {
    return this.reviewService.getImageUrl(imagePath);
  }
  
  openReviewDialog(item: any) {
    const dialogRef = this.dialog.open(ReviewDialogComponent, {
      width: '500px',
      data: {
        billId: this.data.bill.id,
        productId: item.originalProductId,
        productName: item.productName
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadReviews();
      }
    });
  }
}