import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  
  profileForm: FormGroup;
  loading = false;
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  userProfile: any = null;
  allowedFileTypes = ['image/jpeg', 'image/png', 'image/gif'];
  maxFileSize = 10 * 1024 * 1024; // 5MB

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required]],
      address: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.userService.getProfile().subscribe(
      (response) => {
        this.userProfile = response;
        this.profileForm.patchValue({
          name: response.name,
          email: response.email,
          phoneNumber: response.phoneNumber,
          address: response.address
        });
        
        // Load avatar if exists
        if (response.avatar) {
          this.previewUrl = this.userService.getAvatar(response.avatar);
        } else {
          this.previewUrl = 'assets/default-avatar.png';
        }
        this.loading = false;
      },
      (error) => {
        this.showErrorMessage('Error loading profile');
        this.loading = false;
      }
    );
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file size
    if (file.size > this.maxFileSize) {
      this.showErrorMessage('File size should not exceed 5MB');
      return;
    }

    // Validate file type
    if (!this.allowedFileTypes.includes(file.type)) {
      this.showErrorMessage('Only JPG, PNG and GIF files are allowed');
      return;
    }

    this.selectedFile = file;

    // Create preview
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.previewUrl = e.target.result;
    };
    reader.readAsDataURL(file);

    // Automatically upload when file is selected
    this.updateAvatar();
  }

  updateAvatar() {
    if (!this.selectedFile) {
      this.showErrorMessage('Please select an image file');
      return;
    }
  
    this.loading = true;
    const formData = new FormData();
    // Make sure to use 'file' as the key since that's what the backend expects
    formData.append('file', this.selectedFile);
  
    this.userService.updateAvatar(formData).subscribe({
      next: (response: any) => {
        this.showSuccessMessage('Avatar updated successfully');
        if (response.avatar) {
          this.previewUrl = this.userService.getAvatar(response.avatar);
          this.loadProfile();
        }
        this.selectedFile = null;
        this.loading = false;
      },
      error: (error) => {
        if (error.status === 401) {
          this.showErrorMessage('Please login again to update your avatar');
        } else {
          this.showErrorMessage(error.error?.message || 'Error updating avatar');
        }
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (this.profileForm.valid) {
      this.loading = true;
      const formData = {
        ...this.profileForm.value,
        id: this.userProfile.id
      };

      this.userService.updateCustomer(formData).subscribe(
        (response) => {
          this.showSuccessMessage('Profile updated successfully');
          // Update stored user name if it was changed
          if (formData.name !== this.userService.getUserName()) {
            this.userService.setUserInfo(formData.name, this.userService.getUserRole());
          }
          this.loading = false;
        },
        (error) => {
          this.showErrorMessage(error.error?.message || 'Error updating profile');
          this.loading = false;
        }
      );
    }
  }

  private showSuccessMessage(message: string) {
    this.snackBar.open(message, 'Close', { 
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showErrorMessage(message: string) {
    this.snackBar.open(message, 'Close', { 
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }

}