import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PlatformService } from './platform.service';

export interface Product {
  id: number;
  name: string;
  description: string;
  // Add other product fields as needed
}

export interface RelatedProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice: number | null;
  images: ProductImage[];
}
export interface ProductImage {
  id: number;
  imagePath: string;
  deleted?: string
}

export interface ProductHistory {
  id: number;
  modifiedDate: string;
  modifiedBy: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE' | 'RESTORE' | 'ADD_IMAGES' | 'DELETE_IMAGES' | 'STATUS_CHANGE';
  previousData: string | null;
  newData: string | null;
  details: string | null;
  formattedChanges?: string; // Added for UI display
}

type ProductHistoryAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'RESTORE' | 
                          'ADD_IMAGES' | 'DELETE_IMAGES' | 'STATUS_CHANGE';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private recentlyViewedUpdateSubject = new Subject<void>();
  recentlyViewedUpdate$ = this.recentlyViewedUpdateSubject.asObservable();

  private url: string;

  constructor(
    private httpClient: HttpClient,
    private platformService: PlatformService
  ) {
    this.url = this.platformService.getApiUrl();
  }

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    return `${this.url}/api/v1/product/images/${imagePath}`;
  }
 
  add(formData: FormData) {
    return this.httpClient.post(`${this.url}/api/v1/product/add`, formData);
  }

  update(formData: FormData) {
    return this.httpClient.post(`${this.url}/api/v1/product/update`, formData);
  }

  getProduct() {
    return this.httpClient.get(`${this.url}/api/v1/product/get`);
  }

  updateStatus(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/product/updateStatus`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  delete(id:any) {
    return this.httpClient.post(`${this.url}/api/v1/product/delete/${id}`, {});
  }

  getProductsByCategory(id:any) {
    return this.httpClient.get(`${this.url}/api/v1/product/getByCategory/${id}`);
  }

  getById(id: any) {
    return this.httpClient.get(`${this.url}/api/v1/product/getById/${id}`);
  }

  searchProducts(keyword: string) : Observable<any> {
  return this.httpClient.get(`${this.url}/api/v1/product/search?keyword=${keyword}`);
  }

  getRelatedProducts(productId: number): Observable<RelatedProduct[]> {
    return this.httpClient.get<RelatedProduct[]>(`${this.url}/api/v1/product/related/${productId}`);
  }

  addToRecentlyViewed(productId: number): Observable<any> {
    return new Observable(observer => {
      // Kiểm tra xem user đã đăng nhập chưa
      const token = localStorage.getItem('token');
      if (!token) {
        // Nếu chưa đăng nhập, không gọi API và kết thúc observable
        observer.next(null);
        observer.complete();
        return;
      }

      // Nếu đã đăng nhập, tiếp tục gọi API
      this.httpClient.post(`${this.url}/api/v1/product/recently-viewed/${productId}`, {}).subscribe({
        next: (response) => {
          this.recentlyViewedUpdateSubject.next();
          observer.next(response);
          observer.complete();
        },
        error: (error) => {
          // Bỏ qua lỗi 401 và các lỗi liên quan đến authentication
          if (error.status === 401) {
            observer.next(null);
            observer.complete();
          } else {
            observer.error(error);
          }
        }
      });
    });
  }

  notifyRecentlyViewedUpdate() {
    this.recentlyViewedUpdateSubject.next();
  }

  getRecentlyViewedProducts(): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/product/recently-viewed`);
  }

  restoreProduct(id: number) {
    return this.httpClient.post(`${this.url}/api/v1/product/restore/${id}`, {});
  }

  getActiveImages(productId: number): Observable<ProductImage[]> {
    return this.httpClient.get<ProductImage[]>(`${this.url}/api/v1/product/images/active/${productId}`);
  }

  getDeletedImages(productId: number): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/product/images/deleted/${productId}`);
  }

  restoreImage(imageId: number): Observable<any> {
    return this.httpClient.post(`${this.url}/api/v1/product/image/restore/${imageId}`, {});
  }

  getProductHistory(id: number): Observable<ProductHistory[]> {
    return this.httpClient.get<ProductHistory[]>(`${this.url}/api/v1/product/history/${id}`);
  }

  getActiveProducts() {
    return this.httpClient.get(`${this.url}/api/v1/product/active`);
  }

  getDeletedProducts() {
    return this.httpClient.get(`${this.url}/api/v1/product/deleted`);
  }
}