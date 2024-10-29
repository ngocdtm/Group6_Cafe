import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';


@Component({
  selector: 'app-user-details-dialog',
  templateUrl: './user-details-dialog.component.html',
  styleUrls: ['./user-details-dialog.component.scss']
})
export class UserDetailsDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}



