import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
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
    this.checkToken().subscribe({
      next: (response: any) => {
        if (response) {
          this.setLoggedIn(true);
          // If name is not in localStorage, try to get it from the response
          if (!this.userName.value && response.name) {
            this.setUserInfo(response.name, response.role, response.id);
          }
        } else {
          this.logout();
        }
      },
      error: () => {
        this.logout();
      }
    });
  }

  private loadUserInfoFromStorage() {
    const name = localStorage.getItem('userName');
    const role = localStorage.getItem('userRole');
    const id = localStorage.getItem('userId');
    
    if (name) this.userName.next(name);
    if (role) this.userRole.next(role);
    if (id) this.userId.next(Number(id));
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

  login(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/user/login`, data,{
        headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  checkToken() {
    return this.httpClient.get(`${this.url}/api/v1/user/checkToken`);
  }

  changePassword(data:any) {
    return this.httpClient.post(`${this.url}/api/v1/user/changePassword`, data,{
        headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getUsers() {
    return this.httpClient.get(`${this.url}/api/v1/user/get`);
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
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    
    // Reset BehaviorSubjects
    this.loggedIn.next(false);
    this.userName.next('');
    this.userRole.next('');
    this.userId.next(null);
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

