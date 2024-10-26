import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class ProductService {


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


  getById(productId: number, userId?: number): Observable<any> {
    // Thêm userId vào query params như backend yêu cầu
    const params = new HttpParams().set('userId', userId?.toString() || '');
    return this.httpClient.get<any>(`${this.url}/api/v1/product/getById/${productId}`, { params }).pipe(
      catchError(error => {
        console.error('Error fetching product:', error);
        return throwError(error);
      })
    );
  }
  searchProducts(keyword: string) {
  return this.httpClient.get(`${this.url}/api/v1/product/search?keyword=${keyword}`);

}
  getRelatedProducts(productId: number): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/product/related/${productId}`);
  }
  getProductHistory(userId: number): Observable<any> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.httpClient.get(`${this.url}/api/v1/product/history`, { params }).pipe(
      catchError(error => {
        console.error('Error fetching history:', error);
        return throwError(error);
      })
    );
  }
  saveProductView(userId: number, productId: number): Observable<any> {
    // Đảm bảo body request match với backend expectation
    const body = {
      userId: userId,
      productId: productId
    };
    
    return this.httpClient.post(`${this.url}/api/v1/product/history`, body).pipe(
      catchError(error => {
        console.error('Error saving product view:', error);
        return throwError(error);
      })
    );
}

}

