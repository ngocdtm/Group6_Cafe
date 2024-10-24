import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  url = environment.apiUrl;
  
  private loggedIn = new BehaviorSubject<boolean>(false);
  private userName = new BehaviorSubject<string>('');
  private userRole = new BehaviorSubject<string>('');
  private userId = new BehaviorSubject<number | null>(null);
  private userDetails: any = null;

  constructor(private httpClient: HttpClient) {
    this.checkInitialLoginStatus();
  }

  checkInitialLoginStatus() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.logout();
      return;
    }

    // Load user info from localStorage
    this.loadUserInfoFromStorage();
    
    // Verify token and refresh user info
    this.checkToken().pipe(
      tap((response: any) => {
        if (response) {
          this.setLoggedIn(true);
          if (!this.userName.value && response.name) {
            this.setUserInfo(response.name, response.role, response.id);
          }
          // Store user details
          this.userDetails = response;
        }
      }),
      catchError(() => {
        this.logout();
        return of(null);
      })
    ).subscribe();
  }

  private loadUserInfoFromStorage() {
    const name = localStorage.getItem('userName');
    const role = localStorage.getItem('userRole');
    const id = localStorage.getItem('userId');
    const userDetails = localStorage.getItem('userDetails');
    
    if (name) this.userName.next(name);
    if (role) this.userRole.next(role);
    if (id) this.userId.next(Number(id));
    if (userDetails) this.userDetails = JSON.parse(userDetails);
  }

  singup(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/user/signup`, data,{
        headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  forgotPassword(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/user/forgotPassword`, data,{
        headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  login(data: any) {
    return this.httpClient.post(`${this.url}/api/v1/user/login`, data).pipe(
      tap((response: any) => {
        if (response.token) {
          localStorage.setItem('token', response.token);
          this.setUserInfo(response.name, response.role, response.id);
          localStorage.setItem('userDetails', JSON.stringify(response));
          this.userDetails = response;
          this.setLoggedIn(true);
        }
      })
    );
  }

  getUserDetails(): any {
    return this.userDetails;
  }

  checkToken() {
    return this.httpClient.get(`${this.url}/api/v1/user/checkToken`);
  }

  changePassword(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/user/changePassword`, data,{
        headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getUsers(): Observable<any[]> {
    return this.httpClient.get<any[]>(`${this.url}/api/v1/user/get`).pipe(
      catchError(error => {
        console.error('Error fetching users:', error);
        return [];
      })
    );
  }

  update(data:any){
    return this.httpClient.post(`${this.url}/api/v1/user/update`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
  })
  }

  isLoggedIn() {
    return this.loggedIn.asObservable();
  }

  setLoggedIn(value: boolean) {
    this.loggedIn.next(value);
  }

  logout() {
    localStorage.clear();
    this.loggedIn.next(false);
    this.userName.next('');
    this.userRole.next('');
    this.userId.next(null);
    this.userDetails = null;
  }

  setUserInfo(name: string, role: string, id?: number) {
    localStorage.setItem('userName', name);
    localStorage.setItem('userRole', role);
    if (id) localStorage.setItem('userId', id.toString());
    
    this.userName.next(name);
    this.userRole.next(role);
    if (id) this.userId.next(id);
  }

  getUserId(): Observable<number | null> {
    return this.userId.asObservable();
  }

  getUserName() {
    return this.userName.value;
  }

  getUserRole() {
    return this.userRole.value;
  }

  private loadUserInfo() {
    const name = localStorage.getItem('userName');
    const role = localStorage.getItem('userRole');
    if (name && role) {
      this.userName.next(name);
      this.userRole.next(role);
    }
  }
}

