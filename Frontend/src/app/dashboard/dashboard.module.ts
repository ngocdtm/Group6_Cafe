import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { DashboardComponent } from './dashboard.component';
import { DashboardRoutes } from './dashboard.routing';
import { MaterialModule } from '../shared/material-module';
import { AdminChatComponent } from '../admin-chat/admin-chat.component';
import { FormsModule } from '@angular/forms';
@NgModule({
  imports: [
    CommonModule,
    MaterialModule,
    FlexLayoutModule,
    RouterModule.forChild(DashboardRoutes),
    CommonModule,
    FormsModule
  ],
  exports: [
    AdminChatComponent
  ],
  declarations: [DashboardComponent,AdminChatComponent]
})
export class DashboardModule { }
