import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatComponent } from './chat.component';
import { FormsModule } from '@angular/forms';
@NgModule({
  declarations: [ChatComponent], // Chỉ khai báo ở đây
  imports: [CommonModule,FormsModule],
  exports: [ChatComponent] // Xuất component để dùng ở module khác
})
export class ChatModule { }
