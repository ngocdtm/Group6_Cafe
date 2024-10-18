import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';


@Component({
  selector: 'app-login-prompt',
  templateUrl: './login-prompt.component.html',
  styleUrls: ['./login-prompt.component.scss']
})
export class LoginPromptComponent implements OnInit {


  constructor(public dialogRef: MatDialogRef<LoginPromptComponent>) {}


  onNoClick(): void {
    this.dialogRef.close();
  }


  ngOnInit(): void {
  }


}


