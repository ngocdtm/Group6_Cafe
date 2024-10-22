import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { UserService } from './user.service';


@Injectable({
  providedIn: 'root'
})
export class CartService {

  url = environment.apiUrl;
  cartItemCountSubject = new BehaviorSubject<number>(0);

  constructor(
    private httpClient: HttpClient,
    private userService: UserService
  ) {
    this.initializeCart();
  }

  private initializeCart() {
    this.userService.isLoggedIn().subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.updateCartCount();
      } else {
        this.cartItemCountSubject.next(0);
      }
    });
  }

  private updateCartCount() {
    this.getCartItemCount().subscribe(
      count => this.cartItemCountSubject.next(count),
      error => this.cartItemCountSubject.next(0)
    );
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${token}`);
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    if (error.status === 401) {
      // Instead of clearing everything, we'll let the UserService handle the logout
      console.log('Authorization error occurred');
    }
    return throwError(() => error);
  }

  getCart(): Observable<any> {
    return this.httpClient.get(`${this.url}/api/v1/cart/get`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }


  addToCart(product: any): Observable<any> {
    const data = {
      productId: product.id,
      quantity: 1
    };
    return this.httpClient.post(`${this.url}/api/v1/cart/add`, data).pipe(
      tap(() => this.updateCartCount()),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Handle unauthorized - maybe redirect to login
          throw new Error('Please login to add items to cart');
        }
        throw error;
      })
    );
  }

  updateCartItem(data: any): Observable<any> {
    return this.httpClient.put(`${this.url}/api/v1/cart/update`, data, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  removeFromCart(cartItemId: number): Observable<any> {
    return this.httpClient.delete(`${this.url}/api/v1/cart/remove/${cartItemId}`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  clearCart(): Observable<any> {
    return this.httpClient.delete(`${this.url}/api/v1/cart/clear`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  getCartItemCount(): Observable<number> {
    return this.httpClient.get<number>(`${this.url}/api/v1/cart/count`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => {
        console.error('Error getting cart count:', error);
        return throwError(() => error);
      })
    );
  }
  
}