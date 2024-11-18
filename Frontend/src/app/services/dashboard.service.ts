import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { PlatformService } from './platform.service';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private url: string;

  constructor(
    private httpClient: HttpClient,
    private platformService: PlatformService
  ) {
    this.url = this.platformService.getApiUrl();
  }

  getDetails(){
    return this.httpClient.get(`${this.url}/api/v1/dashboard/details`);
  }
}
