import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateMinMaxStockComponent } from './update-min-max-stock.component';

describe('UpdateMinMaxStockComponent', () => {
  let component: UpdateMinMaxStockComponent;
  let fixture: ComponentFixture<UpdateMinMaxStockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UpdateMinMaxStockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateMinMaxStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
