import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  url = environment.apiUrl;

  constructor(private httpClient:HttpClient) { }

  add(data:any){
    return this.httpClient.post(`${this.url}/api/v1/category/add`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  update(data:any){
    return this.httpClient.post(`${this.url}/api/v1/category/update`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getCategory(){
    return this.httpClient.get(`${this.url}/api/v1/category/get`)
  }

  getFilteredCategory(){
    return this.httpClient.get(`${this.url}/api/v1/category/get?filterValue=true`)
  }
}
