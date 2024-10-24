import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { catchError, filter, switchMap, take, finalize } from 'rxjs/operators';
import { UserService } from './user.service';
import { SnackbarService } from './snackbar.service';

@Injectable()
export class TokenInterceptorInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private router: Router,
    private snackbarService: SnackbarService,
    private injector: Injector  // Sử dụng Injector để tránh inject trực tiếp UserService
  ) {}

  private getUserService(): UserService {
    return this.injector.get(UserService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const userService = this.getUserService();
    // Không thêm token cho các request login, signup
    if (this.shouldSkipToken(request)) {
      return next.handle(request);
    }

    // Thêm token vào request
    let authRequest = this.addTokenToRequest(request);
    
    return next.handle(authRequest).pipe(
      catchError((error) => {
        if (error instanceof HttpErrorResponse) {
          switch (error.status) {
            case 403:
              this.snackbarService.openSnackBar('Access forbidden. Please check your permissions.', 'Close');
              return throwError(() => error);
            case 0:
              this.snackbarService.openSnackBar('Unable to connect to server. Please check your connection.', 'Close');
              return throwError(() => error);
            default:
              return throwError(() => error);
          }
        }
        return throwError(() => error);
      })
    );
  }

  private shouldSkipToken(request: HttpRequest<any>): boolean {
    const skipUrls = [
      '/api/v1/user/login',
      '/api/v1/user/signup',
      '/api/v1/user/forgotPassword'
    ];
    return skipUrls.some(url => request.url.includes(url));
  }

  private addTokenToRequest(request: HttpRequest<any>): HttpRequest<any> {
    const token = localStorage.getItem('token');
    if (token) {
      return request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
    }
    return request;
  }
}