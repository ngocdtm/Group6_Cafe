import { Component, EventEmitter, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

interface ImageItem {
  id: number;
  imagePath: string;
}

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})
export class ProductComponent implements OnInit {

  onAddProduct = new EventEmitter();
  onEditProduct = new EventEmitter();
  productForm:any = FormGroup;
  dialogAction:any = "Add"
  action:any = "Add";
  responseMessage:any;
  categories:any = [];

   // Arrays to manage images
  existingImages: ImageItem[] = [];
  newImages: File[] = [];
  newImagePreviews: { file: File; preview: string }[] = [];
  deletedImageIds: number[] = [];
  activeImages: ImageItem[] = [];
  deletedImages: ImageItem[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData:any,
  private formBuilder: FormBuilder,
  public productService: ProductService,
  private categoryService:CategoryService,
  public dialogRef: MatDialogRef<ProductComponent>,
  private snackbarService: SnackbarService
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
    
    if (this.dialogData.action === 'Edit') {
      this.setupEditMode();
    }
  }

  private initForm(): void {
    this.productForm = this.formBuilder.group({
      name:[null,[Validators.required]],
      categoryId:[null,[Validators.required]],
      description:[null,[Validators.required]],
      price:[null,[Validators.required]],
      originalPrice:[null,[Validators.required]]
    });
  }

  private setupEditMode(): void {
    this.dialogAction = "Edit";
    this.action = "Update";
    this.productForm.patchValue(this.dialogData.data);
    
    // Load both active and deleted images
    if (this.dialogData.data.id) {
      this.loadActiveImages(this.dialogData.data.id);
      this.loadDeletedImages(this.dialogData.data.id);
    }
  }

  private loadActiveImages(productId: number): void {
    this.productService.getActiveImages(productId).subscribe({
      next: (images: ImageItem[]) => {
        this.activeImages = images;
        this.existingImages = images; // For compatibility with existing code
      },
      error: (error: any) => {
        this.handleError(error);
      }
    });
  }

  private loadDeletedImages(productId: number): void {
    this.productService.getDeletedImages(productId).subscribe({
      next: (images: ImageItem[]) => {
        this.deletedImages = images;
      },
      error: (error: any) => {
        this.handleError(error);
      }
    });
  }

  restoreImage(image: ImageItem): void {
    this.productService.restoreImage(image.id).subscribe({
      next: (response: any) => {
        // Move image from deleted to active
        this.deletedImages = this.deletedImages.filter(img => img.id !== image.id);
        this.activeImages.push(image);
        this.existingImages = this.activeImages; // Update existingImages for consistency
        
        this.snackbarService.openSnackBar("Image restored successfully", "success");
      },
      error: (error: any) => {
        this.handleError(error);
      }
    });
  }

  onFileSelected(event: any): void {
    const files = Array.from(event.target.files) as File[];
    
    files.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.newImagePreviews.push({
          file: file,
          preview: e.target.result
        });
        this.newImages.push(file);
      };
      reader.readAsDataURL(file);
    });
  }

  getImageUrl(imagePath: string): string {
    return this.productService.getImageUrl(imagePath);
  }

  removeNewImage(index: number): void {
    this.newImages.splice(index, 1);
    this.newImagePreviews.splice(index, 1);
  }

  removeExistingImage(image: ImageItem): void {
    if (image && image.id) {
      this.deletedImageIds.push(image.id);
      this.activeImages = this.activeImages.filter(img => img.id !== image.id);
      this.existingImages = this.activeImages; // Update existingImages for consistency
      this.deletedImages.push(image); // Add to deleted images immediately for UI feedback
    }
  }
  
  private createFormData(): FormData {
    const formData = new FormData();
    const formValue = this.productForm.value;
    
    // Append basic form fields
    Object.keys(formValue).forEach(key => {
      if (formValue[key] !== null) {
        formData.append(key, formValue[key]);
      }
    });
    
    // Append new images
    this.newImages.forEach((file, index) => {
      formData.append(`files`, file);
    });
    
    return formData;
  }

  handleSubmit(): void {
    const formData = new FormData();
    
    // Append basic form data
    const formValue = this.productForm.value;
    Object.keys(formValue).forEach(key => {
      if (formValue[key] !== null && formValue[key] !== undefined) {
        formData.append(key, formValue[key]);
      }
    });
    
    if (this.dialogAction === 'Edit') {
      formData.append('id', this.dialogData.data.id);
      
      // Safely append deleted image IDs
      if (this.deletedImageIds && this.deletedImageIds.length > 0) {
        this.deletedImageIds.forEach(id => {
          if (id !== null && id !== undefined) {
            formData.append('deletedImageIds', id.toString());
          }
        });
      }
    }
    
    // Append new images
    if (this.newImages && this.newImages.length > 0) {
      this.newImages.forEach(file => {
        formData.append('files', file);
      });
    }

    // Log formData for debugging
    formData.forEach((value, key) => {
      console.log(`${key}:`, value);
    });

    if (this.dialogAction === 'Edit') {
      this.productService.update(formData).subscribe({
        next: (response: any) => {
          this.handleSuccess(response);
        },
        error: (error: any) => {
          this.handleError(error);
        }
      });
    } else {
      this.productService.add(formData).subscribe({
        next: (response: any) => {
          this.handleSuccess(response);
        },
        error: (error: any) => {
          this.handleError(error);
        }
      });
    }
  }

  private handleSuccess(response: any): void {
    this.dialogRef.close();
    if (this.dialogAction === 'Edit') {
      this.onEditProduct.emit();
    } else {
      this.onAddProduct.emit();
    }
    this.snackbarService.openSnackBar(response.message, "success");
  }

  private handleError(error: any): void {
    console.error(error);
    this.responseMessage = error.error?.message || GlobalConstants.genericError;
    this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
  }

  private loadCategories(): void {
    this.categoryService.getCategory().subscribe(
      (response: any) => {
        this.categories = response;
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }

  isFormValid(): boolean {
    return this.productForm.valid && 
           (this.productForm.dirty || 
            this.newImages.length > 0 || 
            this.deletedImageIds.length > 0);
  }
}