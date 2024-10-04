import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  url = environment.apiUrl;

  constructor(private httpClient:HttpClient) { }

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
}
