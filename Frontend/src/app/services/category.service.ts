import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformService } from './platform.service';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private url: string;

  constructor(
    private httpClient: HttpClient,
    private platformService: PlatformService
  ) {
    this.url = this.platformService.getApiUrl();
    console.log('API URL:', this.url); // Kiểm tra URL có đúng không
  }

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
    console.log('API URL:', this.url);
    return this.httpClient.get(`${this.url}/api/v1/category/get`)
  }

  getFilteredCategory(){
    return this.httpClient.get(`${this.url}/api/v1/category/get?filterValue=true`)
  }

  delete(id:any) {
    return this.httpClient.post(`${this.url}/api/v1/category/delete/${id}`, {});
  }
}
