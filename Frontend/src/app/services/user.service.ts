import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class UserService {


  url = environment.apiUrl;
  private loggedIn = new BehaviorSubject<boolean>(false);




  constructor(private httpClient: HttpClient) {
    this.checkInitialLoginStatus();
  }


  private checkInitialLoginStatus() {
    const token = localStorage.getItem('token');
    if (token) {
      this.setLoggedIn(true);
    }
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
    })
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
    localStorage.removeItem('token');
    this.setLoggedIn(false);
  }
}

