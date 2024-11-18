import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './shared/material-module';
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
import { LoginPromptComponent } from './login-prompt/login-prompt.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { environment } from 'src/environments/environment';
import { ServiceWorkerModule } from '@angular/service-worker';
import { PlatformService } from './services/platform.service';

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
    FullComponent,
    AppHeaderComponent,
    AppSidebarComponent,
    SignupComponent,
    ForgotPasswordComponent,
    LoginComponent,
    LoginPromptComponent
   ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MaterialModule,
    FlexLayoutModule,
    SharedModule,
    HttpClientModule,
    NgxUiLoaderModule.forRoot(ngxUiLoaderConfig),
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      registrationStrategy: 'registerWhenStable:30000'
    })
  ],
  providers: [
    PlatformService,
    DatePipe,
    HttpClientModule,
    {provide:HTTP_INTERCEPTORS, useClass:TokenInterceptorInterceptor,multi:true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }