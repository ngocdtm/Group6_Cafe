import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private recentlyViewedUpdateSubject = new Subject<void>();
  recentlyViewedUpdate$ = this.recentlyViewedUpdateSubject.asObservable();

  url = environment.apiUrl;
  imageUrl = environment.imageUrl;

  constructor(private httpClient:HttpClient) { }

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

  deleteImage(productId: number, imageId: number) {
    return this.httpClient.delete(`${this.url}/api/v1/product/deleteImage/${productId}/${imageId}`);
  }

  getProductsByCategory(id:any) {
    return this.httpClient.get(`${this.url}/api/v1/product/getByCategory/${id}`);
  }

  getById(id: any) {
    return this.httpClient.get(`${this.url}/api/v1/product/getById/${id}`);
  }

  searchProducts(keyword: string) {
  return this.httpClient.get(`${this.url}/api/v1/product/search?keyword=${keyword}`);
  }

  getRelatedProducts(productId: number): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/product/related/${productId}`);
  }

  addToRecentlyViewed(productId: number): Observable<any> {
    return new Observable(observer => {
      this.httpClient.post(`${this.url}/api/v1/product/recently-viewed/${productId}`, {}).subscribe({
        next: (response) => {
          this.recentlyViewedUpdateSubject.next();
          observer.next(response);
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
    });
  }

  notifyRecentlyViewedUpdate() {
    this.recentlyViewedUpdateSubject.next();
  }

  getRecentlyViewedProducts(): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/product/recently-viewed`);
  }

}