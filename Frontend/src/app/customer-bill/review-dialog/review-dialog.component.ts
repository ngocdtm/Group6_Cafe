import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReviewService } from 'src/app/services/review.service';

@Component({
  selector: 'app-review-dialog',
  templateUrl: './review-dialog.component.html',
  styleUrls: ['./review-dialog.component.scss']
})
export class ReviewDialogComponent implements OnInit {

  reviewForm: FormGroup;
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private reviewService: ReviewService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: {
      billId: number;
      productId: number;
      productName: string;
    },
    public dialogRef: MatDialogRef<ReviewDialogComponent>
  ) {
    this.reviewForm = this.fb.group({
      rating: ['', Validators.required],
      comment: ['']
    });
  }

  ngOnInit(): void {}

  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        if (this.selectedFiles.length >= 5) {
          this.snackBar.open('Maximum 5 images allowed', 'Close', { duration: 3000 });
          break;
        }
        const file = files[i];
        if (file.size > 5 * 1024 * 1024) {
          this.snackBar.open('Each file must be less than 5MB', 'Close', { duration: 3000 });
          continue;
        }
        if (!file.type.match(/image\/(jpeg|png|jpg)/)) {
          this.snackBar.open('Only JPEG, JPG and PNG files are allowed', 'Close', { duration: 3000 });
          continue;
        }
        this.selectedFiles.push(file);
        this.createPreview(file);
      }
    }
  }

  createPreview(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.previewUrls.push(e.target.result);
    };
    reader.readAsDataURL(file);
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
  }

  submitReview(): void {
    if (this.reviewForm.valid) {
      this.isSubmitting = true;
      const formData = new FormData();
      formData.append('billId', this.data.billId.toString());
      formData.append('productId', this.data.productId.toString());
      formData.append('rating', this.reviewForm.get('rating')?.value);
      formData.append('comment', this.reviewForm.get('comment')?.value || '');
      
      this.selectedFiles.forEach(file => {
        formData.append('images', file);
      });

      this.reviewService.createReview(formData).subscribe({
        next: () => {
          this.snackBar.open('Review submitted successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error submitting review:', error);
          this.snackBar.open(
            error.error?.message || 'Failed to submit review', 
            'Close', 
            { duration: 3000 }
          );
          this.isSubmitting = false;
        }
      });
    }
  }
}