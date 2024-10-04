import { jwtDecode } from 'jwt-decode';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { SnackbarService } from './snackbar.service';
import { Injectable } from '@angular/core';
import { GlobalConstants } from '../shared/global-constants';

@Injectable({
  providedIn: 'root'
})
export class RouteGuardService {

  constructor(public auth: AuthService,
    public router: Router,
    private snackbarService: SnackbarService,
  ) { }

  canActivate(route:ActivatedRouteSnapshot):boolean{
    let expectedRoleArray = route.data;
    expectedRoleArray = expectedRoleArray.expectedRole;

    const token:any = localStorage.getItem('token');

    var tokenPayload:any;
    try{
      tokenPayload = jwtDecode(token);
    }catch(err){
      localStorage.clear();
      this.router.navigate(['/']);
      return false;
    }

    let expectedRole ='';

    for(let i = 0; i < expectedRoleArray.length; i++){
      if(expectedRoleArray[i] == tokenPayload.role){
          expectedRole = tokenPayload.role;
      }
    }

    if(['user', 'admin'].includes(tokenPayload.role)){
      if(this.auth.isAuthenticated() && tokenPayload.role == expectedRole){
        return true;
      }
      this.snackbarService.openSnackBar(GlobalConstants.unauthorized, GlobalConstants.error);
      this.router.navigate(['/cafe/dashboard']);
      return false;
    }else{
      this.router.navigate(['/']);
      localStorage.clear();
      return false;
    }
  }
}
