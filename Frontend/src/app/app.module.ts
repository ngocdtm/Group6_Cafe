import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './shared/material-module';
import { HomeComponent } from './home/home.component';
import { BestSellerComponent } from './best-seller/best-seller.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FlexLayoutModule } from '@angular/flex-layout';
import { SharedModule } from './shared/shared.module';
import { FullComponent } from './layouts/full/full.component';
import { AppHeaderComponent } from './layouts/full/header/header.component';
import { AppSidebarComponent } from './layouts/full/sidebar/sidebar.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { SignupComponent } from './signup/signup.component';
import { NgxUiLoaderConfig, NgxUiLoaderModule, SPINNER } from 'ngx-ui-loader';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { LoginComponent } from './login/login.component';
import { TokenInterceptorInterceptor } from './services/token-interceptor.interceptor';
import { DatePipe } from '@angular/common';
<<<<<<< HEAD
import { ImagePreviewDialogComponent } from './image-preview-dialog/image-preview-dialog.component';
=======
>>>>>>> cf00a40c5600510fb42a44d381423478a8f66271

const ngxUiLoaderConfig: NgxUiLoaderConfig = {
    text:"Loading...",
    textColor: "#FFFFFF",
    textPosition: "center-center",
    bgsColor: "#7b1fa2",
    fgsColor: "#7b1fa2",
    fgsType:SPINNER.squareJellyBox,
    fgsSize:100,
    hasProgressBar: false
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    BestSellerComponent,
    FullComponent,
    AppHeaderComponent,
    AppSidebarComponent,
    SignupComponent,
    ForgotPasswordComponent,
    LoginComponent,
<<<<<<< HEAD
    ImagePreviewDialogComponent,
=======
>>>>>>> cf00a40c5600510fb42a44d381423478a8f66271
   ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    FlexLayoutModule,
    SharedModule,
    HttpClientModule,
    NgxUiLoaderModule.forRoot(ngxUiLoaderConfig)
  ],
<<<<<<< HEAD
<<<<<<< HEAD
  providers: [
    HttpClientModule,
    {
      provide: HTTP_INTERCEPTORS, 
      useClass: TokenInterceptorInterceptor,
      multi:true
    }
  ],
=======
  providers: [DatePipe,HttpClientModule,{provide:HTTP_INTERCEPTORS, useClass:TokenInterceptorInterceptor,multi:true}],
>>>>>>> main
=======
  providers: [DatePipe,HttpClientModule,{provide:HTTP_INTERCEPTORS, useClass:TokenInterceptorInterceptor,multi:true}],
>>>>>>> cf00a40c5600510fb42a44d381423478a8f66271
  bootstrap: [AppComponent]
})
export class AppModule { }
