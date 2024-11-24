import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewListDialogComponent } from './review-list-dialog.component';

describe('ReviewListDialogComponent', () => {
  let component: ReviewListDialogComponent;
  let fixture: ComponentFixture<ReviewListDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReviewListDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReviewListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
