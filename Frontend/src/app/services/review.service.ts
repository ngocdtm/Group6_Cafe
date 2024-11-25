import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlatformService } from './platform.service';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private url: string;

  constructor(
    private httpClient: HttpClient,
    private platformService: PlatformService
  ) {
    this.url = this.platformService.getApiUrl();
  }

  createReview(reviewData: FormData): Observable<any> {
    return this.httpClient.post(`${this.url}/api/v1/reviews/create`, reviewData);
  }

  getProductReviews(productId: number, page: number = 0, size: number = 10): Observable<any> {
    return this.httpClient.get(
      `${this.url}/api/v1/reviews/product/${productId}?page=${page}&size=${size}`
    );
  }

  getReviewsByBillId(billId: number): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/reviews/bill/${billId}`);
  }

  isProductReviewed(billId: number, productId: number): Observable<boolean> {
    return this.httpClient.get<boolean>(
      `${this.url}/api/v1/reviews/check/${billId}/${productId}`
    );
  }

  getImageUrl(imagePath: string): string {
    return `${this.url}/api/v1/reviews/images/${imagePath}`;
  }

  getProductRating(productId: number): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/reviews/rating/${productId}`);
  }
}