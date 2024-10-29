import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { UserService, UserWrapper } from 'src/app/services/user.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { MatSelectChange } from '@angular/material/select';
import { UserDetailsDialogComponent } from '../dialog/user-details-dialog/user-details-dialog.component';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';


@Component({
  selector: 'app-manage-user',
  templateUrl: './manage-user.component.html',
  styleUrls: ['./manage-user.component.scss']
})


export class ManageUserComponent implements OnInit {
  displayedColumns: string[] = ['name', 'email', 'phoneNumber', 'role', 'status', 'actions'];
  dataSource: MatTableDataSource<UserWrapper> = new MatTableDataSource<UserWrapper>([]);
  responseMessage: string = '';
  selectedRole: string = 'all';
  roles = [
    { value: 'all', viewValue: 'All Roles' },
    { value: 'ADMIN', viewValue: 'Admin' },
    { value: 'EMPLOYEE', viewValue: 'Employee' },
    { value: 'CUSTOMER', viewValue: 'Customer' }
  ];


  @ViewChild(MatPaginator) paginator!: MatPaginator;


  constructor(
    private ngxService: NgxUiLoaderService,
    private userService: UserService,
    private snackbarService: SnackbarService,
    private dialog: MatDialog
  ) {}


  ngOnInit(): void {
    this.loadUsers();
  }


  ngAfterViewInit() {
    if (this.dataSource) {
      this.dataSource.paginator = this.paginator;
    }
  }


  loadUsers() {
    this.ngxService.start();
    this.userService.getUsers().subscribe({
      next: (users: UserWrapper[]) => {
        console.log('Loaded users:', users);
        this.dataSource = new MatTableDataSource(users);
        this.dataSource.paginator = this.paginator;
       
        // Set up custom filter predicate
        this.dataSource.filterPredicate = (data: UserWrapper, filter: string) => {
          const searchStr = filter.toLowerCase();
          if (this.selectedRole !== 'all' && data.role !== this.selectedRole) {
            return false;
          }
          return data.name.toLowerCase().includes(searchStr) ||
                 data.email.toLowerCase().includes(searchStr) ||
                 data.phoneNumber.includes(searchStr) ||
                 data.role.toLowerCase().includes(searchStr) ||
                 data.status.toLowerCase().includes(searchStr);
        };
       
        this.ngxService.stop();
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.responseMessage = error?.error?.message || GlobalConstants.genericError;
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
        this.ngxService.stop();
      }
    });
  }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }


  onRoleFilterChange(event: MatSelectChange) {
    this.selectedRole = event.value;
    this.dataSource.filterPredicate = (data: UserWrapper, filter: string) => {
      const searchStr = filter.toLowerCase();
      if (this.selectedRole !== 'all' && data.role !== this.selectedRole) {
        return false;
      }
      return data.name.toLowerCase().includes(searchStr) ||
             data.email.toLowerCase().includes(searchStr) ||
             data.phoneNumber.includes(searchStr) ||
             data.role.toLowerCase().includes(searchStr) ||
             data.status.toLowerCase().includes(searchStr);
    };
    this.dataSource.filter = this.selectedRole.toLowerCase();
    if (this.paginator) {
      this.paginator.firstPage();
    }
  }


  suspendUser(id: any) {
    this.ngxService.start();
    const data = {
      status: 'SUSPENDED',
      id: id
    };
    this.updateUserStatus(data);
  }


  activateUser(id: any) {
    this.ngxService.start();
    const data = {
      status: 'ACTIVE',
      id: id
    };
    this.updateUserStatus(data);
  }


  private updateUserStatus(data: any) {
    this.userService.update(data).subscribe({
      next: (response: any) => {
        this.ngxService.stop();
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
        this.loadUsers(); // Refresh the table
      },
      error: (error) => {
        this.ngxService.stop();
        console.error('Error updating user status:', error);
        this.responseMessage = error?.error?.message || GlobalConstants.genericError;
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    });
  }


  openUserDetails(user: UserWrapper) {
    const dialogRef = this.dialog.open(UserDetailsDialogComponent, {
      width: '400px',
      data: user
    });
  }
}

